"""
Transaction Service for managing banking transactions in-memory.

This service provides CRUD operations for transactions and business logic
for balance calculations and transaction summaries.
"""

from typing import List, Optional, Dict
from datetime import datetime
import uuid
from models.transaction import Transaction, TransactionType, TransactionStatus


class TransactionService:
    """
    Service class for managing transactions in-memory.

    This class provides methods for:
    - Creating new transactions
    - Retrieving all transactions
    - Getting a specific transaction by ID
    - Calculating account balances
    - Generating account summaries
    """

    def __init__(self):
        """Initialize the transaction service with an empty transaction list."""
        self._transactions: List[Transaction] = []

    def create_transaction(self, transaction: Transaction) -> Transaction:
        """
        Create a new transaction with auto-generated ID and timestamp.

        Args:
            transaction: Transaction object (ID and timestamp will be auto-generated)

        Returns:
            Created transaction with ID and timestamp populated

        Examples:
            >>> service = TransactionService()
            >>> txn = Transaction(
            ...     fromAccount="ACC-12345",
            ...     toAccount="ACC-67890",
            ...     amount=100.50,
            ...     currency="USD",
            ...     type=TransactionType.TRANSFER
            ... )
            >>> created = service.create_transaction(txn)
            >>> created.id
            'a1b2c3d4-...'
        """
        # Generate unique ID
        transaction.id = str(uuid.uuid4())

        # Set timestamp if not provided
        if transaction.timestamp is None:
            transaction.timestamp = datetime.utcnow()

        # Store the transaction
        self._transactions.append(transaction)

        return transaction

    def get_all_transactions(self) -> List[Transaction]:
        """
        Retrieve all transactions.

        Returns:
            List of all Transaction objects

        Examples:
            >>> service = TransactionService()
            >>> all_txns = service.get_all_transactions()
            >>> len(all_txns)
            0
        """
        return self._transactions.copy()

    def get_transaction_by_id(self, transaction_id: str) -> Optional[Transaction]:
        """
        Retrieve a specific transaction by its ID.

        Args:
            transaction_id: Unique transaction identifier

        Returns:
            Transaction object if found, None otherwise

        Examples:
            >>> service = TransactionService()
            >>> txn = service.get_transaction_by_id("abc-123")
            >>> txn is None
            True
        """
        for transaction in self._transactions:
            if transaction.id == transaction_id:
                return transaction
        return None

    def calculate_account_balance(self, account_id: str, currency: str = None) -> float:
        """
        Calculate the current balance for a specific account.

        Balance calculation:
        - Deposits: Add to toAccount
        - Withdrawals: Subtract from fromAccount
        - Transfers: Subtract from fromAccount, add to toAccount

        Args:
            account_id: Account ID to calculate balance for (e.g., "ACC-12345")
            currency: Optional currency filter (if provided, only count transactions in this currency)

        Returns:
            Current balance for the account (can be negative)

        Examples:
            >>> service = TransactionService()
            >>> balance = service.calculate_account_balance("ACC-12345")
            >>> balance
            0.0
        """
        balance = 0.0

        for transaction in self._transactions:
            # Skip if currency filter is specified and doesn't match
            if currency and transaction.currency != currency.upper():
                continue

            # Skip pending or failed transactions
            if transaction.status != TransactionStatus.COMPLETED:
                continue

            # Deposits: Add to toAccount
            if transaction.type == TransactionType.DEPOSIT:
                if transaction.toAccount == account_id:
                    balance += transaction.amount

            # Withdrawals: Subtract from fromAccount
            elif transaction.type == TransactionType.WITHDRAWAL:
                if transaction.fromAccount == account_id:
                    balance -= transaction.amount

            # Transfers: Subtract from fromAccount, add to toAccount
            elif transaction.type == TransactionType.TRANSFER:
                if transaction.fromAccount == account_id:
                    balance -= transaction.amount
                if transaction.toAccount == account_id:
                    balance += transaction.amount

        return round(balance, 2)

    def get_account_summary(self, account_id: str) -> Dict:
        """
        Generate a comprehensive summary for a specific account.

        Summary includes:
        - Total deposits
        - Total withdrawals
        - Transaction count
        - Most recent transaction date

        Args:
            account_id: Account ID to generate summary for

        Returns:
            Dictionary containing summary statistics

        Examples:
            >>> service = TransactionService()
            >>> summary = service.get_account_summary("ACC-12345")
            >>> summary['transactionCount']
            0
        """
        total_deposits = 0.0
        total_withdrawals = 0.0
        transaction_count = 0
        most_recent_date = None

        for transaction in self._transactions:
            # Check if this transaction involves the account
            is_involved = (
                transaction.fromAccount == account_id or
                transaction.toAccount == account_id
            )

            if not is_involved:
                continue

            transaction_count += 1

            # Track most recent transaction date
            if transaction.timestamp:
                if most_recent_date is None or transaction.timestamp > most_recent_date:
                    most_recent_date = transaction.timestamp

            # Only count completed transactions for amounts
            if transaction.status != TransactionStatus.COMPLETED:
                continue

            # Calculate deposits (money coming into the account)
            if transaction.type == TransactionType.DEPOSIT:
                if transaction.toAccount == account_id:
                    total_deposits += transaction.amount

            elif transaction.type == TransactionType.TRANSFER:
                if transaction.toAccount == account_id:
                    total_deposits += transaction.amount

            # Calculate withdrawals (money going out of the account)
            if transaction.type == TransactionType.WITHDRAWAL:
                if transaction.fromAccount == account_id:
                    total_withdrawals += transaction.amount

            elif transaction.type == TransactionType.TRANSFER:
                if transaction.fromAccount == account_id:
                    total_withdrawals += transaction.amount

        return {
            "accountId": account_id,
            "totalDeposits": round(total_deposits, 2),
            "totalWithdrawals": round(total_withdrawals, 2),
            "transactionCount": transaction_count,
            "mostRecentTransaction": most_recent_date.isoformat() if most_recent_date else None
        }

    def clear_transactions(self):
        """
        Clear all transactions (useful for testing).

        Examples:
            >>> service = TransactionService()
            >>> service.clear_transactions()
            >>> len(service.get_all_transactions())
            0
        """
        self._transactions.clear()
