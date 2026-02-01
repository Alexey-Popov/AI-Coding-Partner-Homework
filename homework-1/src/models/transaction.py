from pydantic import BaseModel, Field, field_validator
from enum import Enum
from datetime import datetime
from typing import Optional
import re


class TransactionType(str, Enum):
    """Transaction type enumeration"""
    DEPOSIT = "deposit"
    WITHDRAWAL = "withdrawal"
    TRANSFER = "transfer"


class TransactionStatus(str, Enum):
    """Transaction status enumeration"""
    PENDING = "pending"
    COMPLETED = "completed"
    FAILED = "failed"


class Transaction(BaseModel):
    """
    Pydantic model for banking transactions with comprehensive validation.

    Attributes:
        id: Unique transaction identifier (auto-generated)
        fromAccount: Source account (optional for deposits)
        toAccount: Destination account (optional for withdrawals)
        amount: Transaction amount (must be positive with max 2 decimals)
        currency: ISO 4217 currency code (USD, EUR, GBP, JPY, etc.)
        type: Transaction type (deposit, withdrawal, transfer)
        timestamp: Transaction timestamp (auto-generated)
        status: Transaction status (pending, completed, failed)
    """
    id: Optional[str] = None
    fromAccount: Optional[str] = None
    toAccount: Optional[str] = None
    amount: float = Field(..., gt=0, description="Transaction amount (must be positive)")
    currency: str = Field(..., min_length=3, max_length=3, description="ISO 4217 currency code")
    type: TransactionType
    timestamp: Optional[datetime] = None
    status: TransactionStatus = TransactionStatus.PENDING

    @field_validator('amount')
    @classmethod
    def validate_amount(cls, v: float) -> float:
        """
        Validate amount has maximum 2 decimal places and is positive.

        Args:
            v: Amount value to validate

        Returns:
            Validated amount

        Raises:
            ValueError: If amount has more than 2 decimal places
        """
        if v <= 0:
            raise ValueError("Amount must be a positive number")

        # Check for maximum 2 decimal places
        if round(v, 2) != v:
            raise ValueError("Amount must have maximum 2 decimal places")

        return v

    @field_validator('fromAccount', 'toAccount')
    @classmethod
    def validate_account_format(cls, v: Optional[str]) -> Optional[str]:
        """
        Validate account number format (ACC-XXXXX pattern).

        Args:
            v: Account number to validate

        Returns:
            Validated account number

        Raises:
            ValueError: If account format is invalid
        """
        if v is None:
            return v

        pattern = r'^ACC-[A-Z0-9]{5}$'
        if not re.match(pattern, v):
            raise ValueError("Account must match pattern ACC-XXXXX (5 alphanumeric characters)")

        return v

    @field_validator('currency')
    @classmethod
    def validate_currency(cls, v: str) -> str:
        """
        Validate ISO 4217 currency code.

        Args:
            v: Currency code to validate

        Returns:
            Validated currency code (uppercase)

        Raises:
            ValueError: If currency code is not valid ISO 4217
        """
        # Common ISO 4217 currency codes
        valid_currencies = {
            'USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'NZD',
            'CNY', 'INR', 'BRL', 'RUB', 'ZAR', 'KRW', 'SGD', 'HKD',
            'SEK', 'NOK', 'DKK', 'PLN', 'THB', 'MYR', 'IDR', 'PHP',
            'MXN', 'ARS', 'CLP', 'COP', 'PEN', 'TRY', 'SAR', 'AED',
            'ILS', 'EGP', 'NGN', 'KES', 'GHS', 'MAD', 'PKR', 'BDT',
            'VND', 'CZK', 'HUF', 'RON', 'ISK', 'HRK', 'BGN', 'UAH'
        }

        v_upper = v.upper()
        if v_upper not in valid_currencies:
            raise ValueError(f"Currency must be a valid ISO 4217 code. Received: {v}")

        return v_upper

    @field_validator('type')
    @classmethod
    def validate_transaction_type(cls, v: TransactionType) -> TransactionType:
        """
        Validate transaction type is one of the allowed types.

        Args:
            v: Transaction type to validate

        Returns:
            Validated transaction type
        """
        return v

    def model_post_init(self, __context) -> None:
        """
        Post-initialization validation to ensure account requirements.

        Raises:
            ValueError: If transaction type requirements are not met
        """
        if self.type == TransactionType.DEPOSIT and not self.toAccount:
            raise ValueError("Deposit transactions require toAccount")

        if self.type == TransactionType.WITHDRAWAL and not self.fromAccount:
            raise ValueError("Withdrawal transactions require fromAccount")

        if self.type == TransactionType.TRANSFER:
            if not self.fromAccount or not self.toAccount:
                raise ValueError("Transfer transactions require both fromAccount and toAccount")

    class Config:
        """Pydantic configuration"""
        json_schema_extra = {
            "example": {
                "fromAccount": "ACC-12345",
                "toAccount": "ACC-67890",
                "amount": 100.50,
                "currency": "USD",
                "type": "transfer",
                "status": "completed"
            }
        }
