"""Pydantic schemas for request/response validation"""
from datetime import datetime
from decimal import Decimal
from typing import Literal, List, Optional
from pydantic import BaseModel, Field, field_validator

from src.validators.transaction_validator import (
    validate_amount,
    validate_account_format,
    validate_currency_code,
    VALID_CURRENCY_CODES,
)


class TransactionCreate(BaseModel):
    """Schema for creating a new transaction"""
    fromAccount: str = Field(..., description="Source account number (format: ACC-XXXXX)")
    toAccount: str = Field(..., description="Destination account number (format: ACC-XXXXX)")
    amount: float = Field(..., gt=0, description="Transaction amount (positive, max 2 decimal places)")
    currency: str = Field(..., description="ISO 4217 currency code (e.g., USD, EUR, GBP)")
    type: Literal["deposit", "withdrawal", "transfer"] = Field(..., description="Transaction type")

    @field_validator("amount")
    @classmethod
    def validate_amount_field(cls, value: float) -> float:
        """Validate amount has maximum 2 decimal places"""
        is_valid, error_msg = validate_amount(value)
        if not is_valid:
            raise ValueError(error_msg)
        return value

    @field_validator("fromAccount")
    @classmethod
    def validate_from_account(cls, value: str) -> str:
        """Validate fromAccount format"""
        is_valid, error_msg = validate_account_format(value)
        if not is_valid:
            raise ValueError(error_msg)
        return value

    @field_validator("toAccount")
    @classmethod
    def validate_to_account(cls, value: str) -> str:
        """Validate toAccount format"""
        is_valid, error_msg = validate_account_format(value)
        if not is_valid:
            raise ValueError(error_msg)
        return value

    @field_validator("currency")
    @classmethod
    def validate_currency_field(cls, value: str) -> str:
        """Validate currency is a valid ISO 4217 code"""
        is_valid, error_msg = validate_currency_code(value)
        if not is_valid:
            raise ValueError(error_msg)
        # Normalize to uppercase
        return value.upper()


class TransactionResponse(BaseModel):
    """Schema for transaction response"""
    id: str = Field(..., description="Transaction ID")
    fromAccount: str
    toAccount: str
    amount: float
    currency: str
    type: Literal["deposit", "withdrawal", "transfer"]
    timestamp: str = Field(..., description="ISO 8601 datetime")
    status: Literal["pending", "completed", "failed"]

    class Config:
        from_attributes = True


class BalanceResponse(BaseModel):
    """Schema for account balance response"""
    accountId: str
    balance: float


class ValidationErrorDetail(BaseModel):
    """Schema for individual validation error detail"""
    field: str
    message: str


class ValidationErrorResponse(BaseModel):
    """Schema for validation error responses (Task 2 format)"""
    error: str = "Validation failed"
    details: List[ValidationErrorDetail]


class ErrorResponse(BaseModel):
    """Schema for general error responses"""
    error: str
    details: Optional[str] = None
