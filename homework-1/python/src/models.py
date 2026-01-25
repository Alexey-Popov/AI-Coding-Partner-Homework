"""Pydantic models for Banking Transactions API."""

from datetime import datetime
from enum import Enum
from typing import Optional
from pydantic import BaseModel, Field


class TransactionType(str, Enum):
    """Valid transaction types."""
    DEPOSIT = "deposit"
    WITHDRAWAL = "withdrawal"
    TRANSFER = "transfer"


class TransactionStatus(str, Enum):
    """Transaction status values."""
    PENDING = "pending"
    COMPLETED = "completed"
    FAILED = "failed"


# Valid ISO 4217 currency codes
VALID_CURRENCIES = {
    "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "INR", "MXN",
    "BRL", "KRW", "SGD", "HKD", "NOK", "SEK", "DKK", "NZD", "ZAR", "RUB"
}


class TransactionCreate(BaseModel):
    """Request model for creating a transaction."""
    fromAccount: Optional[str] = Field(None, description="Source account (required for withdrawal/transfer)")
    toAccount: Optional[str] = Field(None, description="Destination account (required for deposit/transfer)")
    amount: float = Field(..., gt=0, description="Transaction amount (must be positive)")
    currency: str = Field(..., description="ISO 4217 currency code")
    type: TransactionType = Field(..., description="Transaction type")


class Transaction(BaseModel):
    """Complete transaction model."""
    id: str = Field(..., description="Unique transaction ID")
    fromAccount: Optional[str] = Field(None, description="Source account")
    toAccount: Optional[str] = Field(None, description="Destination account")
    amount: float = Field(..., description="Transaction amount")
    currency: str = Field(..., description="Currency code")
    type: TransactionType = Field(..., description="Transaction type")
    timestamp: str = Field(..., description="ISO 8601 timestamp")
    status: TransactionStatus = Field(default=TransactionStatus.COMPLETED, description="Transaction status")


class AccountBalance(BaseModel):
    """Account balance response model."""
    accountId: str
    balance: float
    currency: str = "USD"


class AccountSummary(BaseModel):
    """Account summary response model."""
    accountId: str
    totalDeposits: float
    totalWithdrawals: float
    numberOfTransactions: int
    mostRecentTransactionDate: Optional[str]
    currentBalance: float


class ValidationErrorDetail(BaseModel):
    """Single validation error detail."""
    field: str
    message: str


class ValidationErrorResponse(BaseModel):
    """Validation error response model."""
    error: str = "Validation failed"
    details: list[ValidationErrorDetail]
