from pydantic import BaseModel, EmailStr, Field, validator
from typing import Optional, List
from datetime import datetime
from decimal import Decimal


def mask_card_number(card_number: str) -> str:
    """Mask all but last 4 digits of card number for PCI compliance."""
    clean = card_number.replace(' ', '').replace('-', '')
    if len(clean) < 4:
        return 'XXXX'
    return f"XXXX XXXX XXXX {clean[-4:]}"


# Auth schemas
class RegisterRequest(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=6)
    full_name: Optional[str] = None


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"


class UserResponse(BaseModel):
    id: str
    email: str
    full_name: Optional[str]
    role: str
    status: str
    created_at: datetime


# Account schemas
class CreateAccountRequest(BaseModel):
    currency: str = Field(..., min_length=3, max_length=3)
    initial_balance: Decimal = Field(default=Decimal('0'), ge=0)
    card_number: Optional[str] = None

    @validator('currency')
    def currency_uppercase(cls, v):
        return v.upper()


class AccountResponse(BaseModel):
    id: str
    user_id: str
    card_number: str  # Full card number needed for transfers
    currency: str
    balance: Decimal
    created_at: datetime
    updated_at: datetime

    class Config:
        json_encoders = {
            Decimal: lambda v: str(v)
        }


class AccountListResponse(BaseModel):
    accounts: List[AccountResponse]


# Transaction schemas
class TransactionResponse(BaseModel):
    id: str
    created_at: datetime
    type: str
    source_account_id: Optional[str]
    target_account_id: Optional[str]
    source_amount: Optional[Decimal]
    source_currency: Optional[str]
    target_amount: Optional[Decimal]
    target_currency: Optional[str]
    fx_rate: Optional[Decimal]
    description: Optional[str]
    status: str

    class Config:
        json_encoders = {
            Decimal: lambda v: str(v)
        }


class TransactionListResponse(BaseModel):
    transactions: List[TransactionResponse]
    total: int
    limit: int
    offset: int


# Transfer schemas
class TransferRequest(BaseModel):
    source_card_number: str = Field(..., description="Source account card number")
    target_card_number: str = Field(..., description="Target account card number")
    source_currency: str = Field(..., min_length=3, max_length=3)
    source_amount: Decimal = Field(..., gt=0)
    target_currency: str = Field(..., min_length=3, max_length=3)
    fx_rate: Decimal = Field(..., gt=0, description="Exchange rate (course)")
    target_amount: Optional[Decimal] = Field(None, gt=0)
    description: Optional[str] = Field(None, max_length=500)

    @validator('source_currency', 'target_currency')
    def currency_uppercase(cls, v):
        return v.upper()


class TransferResponse(BaseModel):
    transaction_id: str
    status: str
    source_account: AccountResponse
    target_account: AccountResponse


# Error schemas
class ErrorDetail(BaseModel):
    code: str
    message: str
    details: Optional[dict] = None


class ErrorResponse(BaseModel):
    error: ErrorDetail
    request_id: Optional[str] = None


# Health schema
class HealthResponse(BaseModel):
    status: str
    database: str
    timestamp: datetime
