"""Pydantic schemas for request/response validation"""
from datetime import datetime
from typing import Literal
from pydantic import BaseModel, Field


class TransactionCreate(BaseModel):
    """Schema for creating a new transaction"""
    fromAccount: str = Field(..., description="Source account number")
    toAccount: str = Field(..., description="Destination account number")
    amount: float = Field(..., gt=0, description="Transaction amount (must be positive)")
    currency: str = Field(..., description="ISO 4217 currency code")
    type: Literal["deposit", "withdrawal", "transfer"] = Field(..., description="Transaction type")


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


class ErrorResponse(BaseModel):
    """Schema for error responses"""
    error: str
    details: str = None
