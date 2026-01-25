"""Transaction data model for banking API"""
from datetime import datetime
from typing import Literal
from uuid import uuid4


class Transaction:
    """Represents a banking transaction"""

    def __init__(
        self,
        from_account: str,
        to_account: str,
        amount: float,
        currency: str,
        transaction_type: Literal["deposit", "withdrawal", "transfer"],
        timestamp: datetime = None,
        status: Literal["pending", "completed", "failed"] = "completed",
        transaction_id: str = None,
    ):
        self.id = transaction_id or str(uuid4())
        self.fromAccount = from_account
        self.toAccount = to_account
        self.amount = amount
        self.currency = currency
        self.type = transaction_type
        self.timestamp = timestamp or datetime.utcnow()
        self.status = status

    def to_dict(self) -> dict:
        """Convert transaction to dictionary"""
        return {
            "id": self.id,
            "fromAccount": self.fromAccount,
            "toAccount": self.toAccount,
            "amount": self.amount,
            "currency": self.currency,
            "type": self.type,
            "timestamp": self.timestamp.isoformat(),
            "status": self.status,
        }

    @classmethod
    def from_dict(cls, data: dict) -> "Transaction":
        """Create transaction from dictionary"""
        return cls(
            from_account=data.get("fromAccount"),
            to_account=data.get("toAccount"),
            amount=data.get("amount"),
            currency=data.get("currency"),
            transaction_type=data.get("type"),
            timestamp=data.get("timestamp"),
            status=data.get("status", "completed"),
            transaction_id=data.get("id"),
        )
