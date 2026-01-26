from typing import Optional, List, Any
from datetime import datetime
from decimal import Decimal
import uuid

from ..domain.models import Account
from ..infrastructure.db import DatabaseConnection
from ..infrastructure.logging import get_logger

logger = get_logger(__name__)


class AccountRepository:
    """Repository for account data access."""

    def create(
        self,
        user_id: str,
        card_number: str,
        currency: str,
        initial_balance: Decimal,
        connection: Optional[Any] = None
    ) -> Account:
        """Create a new account."""
        account_id = str(uuid.uuid4())
        now = datetime.utcnow()
        now_str = now.isoformat()

        query = """
            INSERT INTO accounts (id, user_id, card_number, currency, balance, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """
        params = (account_id, user_id, card_number, currency, initial_balance, now_str, now_str)

        DatabaseConnection.execute_update(query, params, connection)

        logger.info(f"Account created: {account_id} for user: {user_id}")

        return Account(
            id=account_id,
            user_id=user_id,
            card_number=card_number,
            currency=currency,
            balance=initial_balance,
            created_at=now,
            updated_at=now
        )

    def find_by_id(self, account_id: str, connection: Optional[Any] = None) -> Optional[Account]:
        """Find account by ID."""
        query = """
            SELECT id, user_id, card_number, currency, balance, created_at, updated_at
            FROM accounts
            WHERE id = ?
        """
        params = (account_id,)

        rows = DatabaseConnection.execute_query(query, params, connection)

        if not rows:
            return None

        return self._row_to_account(rows[0])

    def find_by_card_number(self, card_number: str, connection: Optional[Any] = None) -> Optional[Account]:
        """Find account by card number."""
        query = """
            SELECT id, user_id, card_number, currency, balance, created_at, updated_at
            FROM accounts
            WHERE card_number = ?
        """
        params = (card_number,)

        rows = DatabaseConnection.execute_query(query, params, connection)

        if not rows:
            return None

        return self._row_to_account(rows[0])

    def find_by_user_id(self, user_id: str) -> List[Account]:
        """Find all accounts for a user."""
        query = """
            SELECT id, user_id, card_number, currency, balance, created_at, updated_at
            FROM accounts
            WHERE user_id = ?
            ORDER BY created_at DESC
        """
        params = (user_id,)

        rows = DatabaseConnection.execute_query(query, params)

        return [self._row_to_account(row) for row in rows]

    def card_number_exists(self, card_number: str) -> bool:
        """Check if card number already exists."""
        query = "SELECT COUNT(*) FROM accounts WHERE card_number = ?"
        params = (card_number,)

        rows = DatabaseConnection.execute_query(query, params)
        return rows[0][0] > 0

    def update_balance(
        self,
        account_id: str,
        new_balance: Decimal,
        connection: Optional[Any] = None
    ) -> None:
        """Update account balance."""
        now_str = datetime.utcnow().isoformat()

        query = """
            UPDATE accounts
            SET balance = ?, updated_at = ?
            WHERE id = ?
        """
        params = (new_balance, now_str, account_id)

        DatabaseConnection.execute_update(query, params, connection)

        logger.info(f"Account {account_id} balance updated to {new_balance}")

    def lock_account_for_update(self, account_id: str, connection: Any) -> Optional[Account]:
        """Lock account row for update (SELECT FOR UPDATE)."""
        query = """
            SELECT id, user_id, card_number, currency, balance, created_at, updated_at
            FROM accounts
            WHERE id = ?
            FOR UPDATE
        """
        params = (account_id,)

        rows = DatabaseConnection.execute_query(query, params, connection)

        if not rows:
            return None

        return self._row_to_account(rows[0])

    def _row_to_account(self, row: tuple) -> Account:
        """Convert database row to Account model."""
        return Account(
            id=str(row[0]),
            user_id=str(row[1]),
            card_number=str(row[2]),
            currency=str(row[3]),
            balance=Decimal(str(row[4])),
            created_at=row[5],
            updated_at=row[6]
        )
