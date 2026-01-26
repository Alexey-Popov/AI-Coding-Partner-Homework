from typing import Optional, Tuple, List
from decimal import Decimal
from datetime import datetime

from ..domain.models import Transaction, TransactionType, TransactionStatus, Account
from ..domain.money import (
    quantize_money,
    validate_positive_amount,
    validate_sufficient_funds,
    calculate_conversion
)
from ..repositories.account_repo import AccountRepository
from ..repositories.transaction_repo import TransactionRepository
from ..repositories.idempotency_repo import IdempotencyRepository
from ..infrastructure.db import DatabaseConnection
from ..infrastructure.logging import get_logger

logger = get_logger(__name__)


class TransferValidationError(Exception):
    """Raised when transfer validation fails."""
    pass


class InsufficientFundsError(Exception):
    """Raised when source account has insufficient funds."""
    pass


class TransferService:
    """Service for transfer operations."""

    def __init__(self):
        self.account_repo = AccountRepository()
        self.transaction_repo = TransactionRepository()
        self.idempotency_repo = IdempotencyRepository()

    def execute_transfer(
        self,
        source_card_number: str,
        target_card_number: str,
        source_currency: str,
        source_amount: Decimal,
        target_currency: str,
        fx_rate: Decimal,
        user_id: str,  # REQUIRED - must come before optional params
        target_amount: Optional[Decimal] = None,
        description: Optional[str] = None,
        idempotency_key: Optional[str] = None
    ) -> Tuple[Transaction, Account, Account]:
        """
        Execute a transfer between accounts with currency conversion.

        Args:
            source_card_number: Source account card number
            target_card_number: Target account card number
            source_currency: Source currency
            source_amount: Amount to debit from source
            target_currency: Target currency
            fx_rate: Exchange rate
            target_amount: Optional target amount (calculated if not provided)
            description: Optional description
            user_id: User ID (for idempotency check)
            idempotency_key: Optional idempotency key

        Returns:
            Tuple of (Transaction, updated source Account, updated target Account)

        Raises:
            TransferValidationError: If validation fails
            InsufficientFundsError: If insufficient funds
        """
        # Check idempotency
        if idempotency_key and user_id:
            existing = self.idempotency_repo.find_by_key(idempotency_key, user_id)
            if existing:
                logger.warning(f"Idempotent request replay detected: {idempotency_key}")
                raise TransferValidationError("Duplicate idempotency key")

        # Validate inputs
        source_amount = quantize_money(source_amount)
        fx_rate = quantize_money(fx_rate, 8)

        if not validate_positive_amount(source_amount):
            raise TransferValidationError("Source amount must be positive")

        if not validate_positive_amount(fx_rate):
            raise TransferValidationError("Exchange rate must be positive")

        # Calculate target amount if not provided
        if target_amount is None:
            target_amount = calculate_conversion(source_amount, fx_rate)
        else:
            target_amount = quantize_money(target_amount)

        # Execute transfer in a transaction
        with DatabaseConnection.transaction() as conn:
            # Lock and fetch source account
            source_account = self.account_repo.find_by_card_number(source_card_number, conn)
            if not source_account:
                raise TransferValidationError("Source account not found")

            # CRITICAL: Validate ownership - user must own the source account
            if source_account.user_id != user_id:
                logger.warning(
                    f"Unauthorized transfer attempt: user {user_id} tried to transfer "
                    f"from account {source_account.id} owned by {source_account.user_id}"
                )
                raise TransferValidationError("Unauthorized: you do not own the source account")

            # Lock source account for update
            source_account = self.account_repo.lock_account_for_update(source_account.id, conn)

            # Validate source currency
            if source_account.currency != source_currency:
                raise TransferValidationError(
                    f"Source currency mismatch: expected {source_account.currency}, got {source_currency}"
                )

            # Lock and fetch target account
            target_account = self.account_repo.find_by_card_number(target_card_number, conn)
            if not target_account:
                raise TransferValidationError("Target account not found")

            # Lock target account for update
            target_account = self.account_repo.lock_account_for_update(target_account.id, conn)

            # Validate target currency
            if target_account.currency != target_currency:
                raise TransferValidationError(
                    f"Target currency mismatch: expected {target_account.currency}, got {target_currency}"
                )

            # Check if same account
            if source_account.id == target_account.id:
                raise TransferValidationError("Cannot transfer to the same account")

            # Check sufficient funds
            if not validate_sufficient_funds(source_account.balance, source_amount):
                raise InsufficientFundsError(
                    f"Insufficient funds: available {source_account.balance}, required {source_amount}"
                )

            # Create transaction record
            transaction = self.transaction_repo.create(
                type=TransactionType.TRANSFER,
                source_account_id=source_account.id,
                target_account_id=target_account.id,
                source_amount=source_amount,
                source_currency=source_currency,
                target_amount=target_amount,
                target_currency=target_currency,
                fx_rate=fx_rate,
                description=description,
                status=TransactionStatus.PENDING,
                connection=conn
            )

            # Update balances
            new_source_balance = source_account.balance - source_amount
            new_target_balance = target_account.balance + target_amount

            self.account_repo.update_balance(source_account.id, new_source_balance, conn)
            self.account_repo.update_balance(target_account.id, new_target_balance, conn)

            # Update transaction status to completed
            self.transaction_repo.update_status(
                transaction.id,
                TransactionStatus.COMPLETED,
                conn
            )

            # Update local objects
            source_account.balance = new_source_balance
            target_account.balance = new_target_balance
            transaction.status = TransactionStatus.COMPLETED

            # Store idempotency key in SAME transaction to prevent double-spend
            if idempotency_key and user_id:
                self.idempotency_repo.create(
                    key=idempotency_key,
                    user_id=user_id,
                    endpoint="/v1/transfers",
                    request_body={
                        "source_card_number": source_card_number,
                        "target_card_number": target_card_number,
                        "source_amount": str(source_amount),
                        "source_currency": source_currency,
                        "target_currency": target_currency,
                        "fx_rate": str(fx_rate)
                    },
                    response_status=201,
                    response_body={
                        "transaction_id": transaction.id,
                        "status": transaction.status.value
                    },
                    connection=conn
                )

            logger.info(
                f"Transfer completed: {transaction.id} "
                f"from {source_account.id} to {target_account.id} "
                f"amount: {source_amount} {source_currency} -> {target_amount} {target_currency}"
            )

        return transaction, source_account, target_account

    def get_account_transactions(
        self,
        account_id: str,
        limit: int = 20,
        offset: int = 0
    ) -> Tuple[List[Transaction], int]:
        """
        Get transactions for an account with pagination.

        Returns:
            Tuple of (transactions list, total count)
        """
        transactions = self.transaction_repo.find_by_account_id(account_id, limit, offset)
        total = self.transaction_repo.count_by_account_id(account_id)

        return transactions, total
