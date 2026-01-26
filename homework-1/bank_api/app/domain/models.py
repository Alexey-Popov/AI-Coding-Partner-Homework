from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal
from typing import Optional
from enum import Enum


class TransactionType(str, Enum):
    """Transaction type enumeration."""
    DEBIT = "DEBIT"
    CREDIT = "CREDIT"
    TRANSFER = "TRANSFER"


class TransactionStatus(str, Enum):
    """Transaction status enumeration."""
    PENDING = "PENDING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"


class UserRole(str, Enum):
    """User role enumeration."""
    USER = "USER"
    ADMIN = "ADMIN"


class UserStatus(str, Enum):
    """User status enumeration."""
    ACTIVE = "ACTIVE"
    BLOCKED = "BLOCKED"


@dataclass
class User:
    """User domain model."""
    id: str
    email: str
    password_hash: str
    full_name: Optional[str]
    role: UserRole
    status: UserStatus
    created_at: datetime
    updated_at: datetime


@dataclass
class Account:
    """Account domain model."""
    id: str
    user_id: str
    card_number: str
    currency: str
    balance: Decimal
    created_at: datetime
    updated_at: datetime


@dataclass
class Transaction:
    """Transaction domain model."""
    id: str
    created_at: datetime
    type: TransactionType
    source_account_id: Optional[str]
    target_account_id: Optional[str]
    source_amount: Optional[Decimal]
    source_currency: Optional[str]
    target_amount: Optional[Decimal]
    target_currency: Optional[str]
    fx_rate: Optional[Decimal]
    description: Optional[str]
    status: TransactionStatus


@dataclass
class IdempotencyKey:
    """Idempotency key domain model."""
    key: str
    user_id: str
    endpoint: str
    request_hash: str
    response_status: int
    response_body: str
    created_at: datetime
