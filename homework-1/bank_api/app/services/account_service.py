from typing import List
from decimal import Decimal
import secrets

from ..domain.models import Account
from ..domain.money import quantize_money, validate_positive_amount
from ..repositories.account_repo import AccountRepository
from ..infrastructure.logging import get_logger

logger = get_logger(__name__)


class AccountValidationError(Exception):
    """Raised when account validation fails."""
    pass


class AccountService:
    """Service for account operations."""

    def __init__(self):
        self.account_repo = AccountRepository()

    def create_account(
        self,
        user_id: str,
        currency: str,
        initial_balance: Decimal,
        card_number: str = None
    ) -> Account:
        """
        Create a new account.

        Args:
            user_id: Owner user ID
            currency: Currency code (e.g., EUR, USD)
            initial_balance: Initial balance
            card_number: Optional card number (generated if not provided)

        Returns:
            Created account

        Raises:
            AccountValidationError: If validation fails
        """
        # Validate currency
        if not self._validate_currency(currency):
            raise AccountValidationError("Invalid currency code")

        # Validate initial balance
        initial_balance = quantize_money(initial_balance)
        if initial_balance < Decimal('0'):
            raise AccountValidationError("Initial balance cannot be negative")

        # Generate or validate card number
        if not card_number:
            card_number = self._generate_card_number()
        else:
            if not self._validate_card_number(card_number):
                raise AccountValidationError("Invalid card number format")

        # Check if card number already exists
        if self.account_repo.card_number_exists(card_number):
            raise AccountValidationError("Card number already exists")

        # Create account
        account = self.account_repo.create(
            user_id=user_id,
            card_number=card_number,
            currency=currency,
            initial_balance=initial_balance
        )

        logger.info(f"Account created: {account.id} for user: {user_id}")

        return account

    def get_user_accounts(self, user_id: str) -> List[Account]:
        """Get all accounts for a user."""
        return self.account_repo.find_by_user_id(user_id)

    def get_account_by_id(self, account_id: str) -> Account:
        """
        Get account by ID.

        Raises:
            AccountValidationError: If account not found
        """
        account = self.account_repo.find_by_id(account_id)
        if not account:
            raise AccountValidationError("Account not found")
        return account

    def get_account_by_card_number(self, card_number: str) -> Account:
        """
        Get account by card number.

        Raises:
            AccountValidationError: If account not found
        """
        account = self.account_repo.find_by_card_number(card_number)
        if not account:
            raise AccountValidationError("Account not found")
        return account

    def _validate_currency(self, currency: str) -> bool:
        """Validate currency code format."""
        return len(currency) == 3 and currency.isupper() and currency.isalpha()

    def _validate_card_number(self, card_number: str) -> bool:
        """Validate card number format (simple check)."""
        # Remove spaces and dashes
        clean_number = card_number.replace(" ", "").replace("-", "")
        # Check if it's 16 digits
        return len(clean_number) == 16 and clean_number.isdigit()

    def _generate_card_number(self) -> str:
        """Generate a cryptographically secure random 16-digit card number."""
        # Use secrets module for unpredictable random generation
        number = ''.join([str(secrets.randbelow(10)) for _ in range(16)])
        # Format as XXXX XXXX XXXX XXXX
        return f"{number[0:4]} {number[4:8]} {number[8:12]} {number[12:16]}"
