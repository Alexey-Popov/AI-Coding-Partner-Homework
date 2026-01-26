from typing import Optional, List, Any
from datetime import datetime
from decimal import Decimal
import uuid

from ..domain.models import Transaction, TransactionType, TransactionStatus
from ..infrastructure.db import DatabaseConnection
from ..infrastructure.logging import get_logger

logger = get_logger(__name__)


class TransactionRepository:
    """Repository for transaction data access."""

    def create(
        self,
        type: TransactionType,
        source_account_id: Optional[str],
        target_account_id: Optional[str],
        source_amount: Optional[Decimal],
        source_currency: Optional[str],
        target_amount: Optional[Decimal],
        target_currency: Optional[str],
        fx_rate: Optional[Decimal],
        description: Optional[str],
        status: TransactionStatus,
        connection: Optional[Any] = None
    ) -> Transaction:
        """Create a new transaction."""
        transaction_id = str(uuid.uuid4())
        now = datetime.utcnow()
        now_str = now.isoformat()

        query = """
            INSERT INTO transactions (
                id, created_at, type, source_account_id, target_account_id,
                source_amount, source_currency, target_amount, target_currency,
                fx_rate, description, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        params = (
            transaction_id, now_str, type.value, source_account_id, target_account_id,
            source_amount, source_currency, target_amount, target_currency,
            fx_rate, description, status.value
        )

        DatabaseConnection.execute_update(query, params, connection)

        logger.info(f"Transaction created: {transaction_id} type: {type.value}")

        return Transaction(
            id=transaction_id,
            created_at=now,
            type=type,
            source_account_id=source_account_id,
            target_account_id=target_account_id,
            source_amount=source_amount,
            source_currency=source_currency,
            target_amount=target_amount,
            target_currency=target_currency,
            fx_rate=fx_rate,
            description=description,
            status=status
        )

    def find_by_account_id(
        self,
        account_id: str,
        limit: int = 20,
        offset: int = 0
    ) -> List[Transaction]:
        """Find transactions for an account with pagination."""
        query = """
            SELECT id, created_at, type, source_account_id, target_account_id,
                   source_amount, source_currency, target_amount, target_currency,
                   fx_rate, description, status
            FROM transactions
            WHERE source_account_id = ? OR target_account_id = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
        """
        params = (account_id, account_id, limit, offset)

        rows = DatabaseConnection.execute_query(query, params)

        return [self._row_to_transaction(row) for row in rows]

    def count_by_account_id(self, account_id: str) -> int:
        """Count transactions for an account."""
        query = """
            SELECT COUNT(*)
            FROM transactions
            WHERE source_account_id = ? OR target_account_id = ?
        """
        params = (account_id, account_id)

        rows = DatabaseConnection.execute_query(query, params)
        return rows[0][0]

    def find_by_id(self, transaction_id: str) -> Optional[Transaction]:
        """Find transaction by ID."""
        query = """
            SELECT id, created_at, type, source_account_id, target_account_id,
                   source_amount, source_currency, target_amount, target_currency,
                   fx_rate, description, status
            FROM transactions
            WHERE id = ?
        """
        params = (transaction_id,)

        rows = DatabaseConnection.execute_query(query, params)

        if not rows:
            return None

        return self._row_to_transaction(rows[0])

    def update_status(
        self,
        transaction_id: str,
        status: TransactionStatus,
        connection: Optional[Any] = None
    ) -> None:
        """Update transaction status."""
        query = "UPDATE transactions SET status = ? WHERE id = ?"
        params = (status.value, transaction_id)

        DatabaseConnection.execute_update(query, params, connection)

        logger.info(f"Transaction {transaction_id} status updated to {status.value}")

    def _row_to_transaction(self, row: tuple) -> Transaction:
        """Convert database row to Transaction model."""
        return Transaction(
            id=str(row[0]),
            created_at=row[1],
            type=TransactionType(str(row[2])),
            source_account_id=str(row[3]) if row[3] else None,
            target_account_id=str(row[4]) if row[4] else None,
            source_amount=Decimal(str(row[5])) if row[5] is not None else None,
            source_currency=str(row[6]) if row[6] else None,
            target_amount=Decimal(str(row[7])) if row[7] is not None else None,
            target_currency=str(row[8]) if row[8] else None,
            fx_rate=Decimal(str(row[9])) if row[9] is not None else None,
            description=str(row[10]) if row[10] else None,
            status=TransactionStatus(str(row[11]))
        )
