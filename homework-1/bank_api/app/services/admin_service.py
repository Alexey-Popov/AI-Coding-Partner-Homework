from typing import List, Optional, Tuple
from datetime import datetime

from ..domain.models import User, Transaction, UserStatus
from ..repositories.user_repo import UserRepository
from ..repositories.account_repo import AccountRepository
from ..infrastructure.db import DatabaseConnection
from ..infrastructure.logging import get_logger

logger = get_logger(__name__)


class AdminService:
    """Service for admin operations."""

    def __init__(self):
        self.user_repo = UserRepository()
        self.account_repo = AccountRepository()

    def list_users(
        self,
        limit: int = 20,
        offset: int = 0,
        status: Optional[UserStatus] = None,
        email_contains: Optional[str] = None
    ) -> Tuple[List[User], int]:
        """
        List users with pagination and filters.

        Args:
            limit: Number of users to return
            offset: Number of users to skip
            status: Filter by user status
            email_contains: Filter by email containing string

        Returns:
            Tuple of (users, total_count)
        """
        return self.user_repo.list_users(
            limit=limit,
            offset=offset,
            status=status,
            email_contains=email_contains
        )

    def block_user(self, user_id: str) -> User:
        """
        Block a user account.

        Args:
            user_id: ID of user to block

        Returns:
            Updated user

        Raises:
            ValueError: If user not found
        """
        user = self.user_repo.update_status(user_id, UserStatus.BLOCKED)

        if not user:
            raise ValueError(f"User not found: {user_id}")

        logger.info(f"Admin blocked user: {user_id}")
        return user

    def unblock_user(self, user_id: str) -> User:
        """
        Unblock a user account.

        Args:
            user_id: ID of user to unblock

        Returns:
            Updated user

        Raises:
            ValueError: If user not found
        """
        user = self.user_repo.update_status(user_id, UserStatus.ACTIVE)

        if not user:
            raise ValueError(f"User not found: {user_id}")

        logger.info(f"Admin unblocked user: {user_id}")
        return user

    def list_all_transactions(
        self,
        limit: int = 20,
        offset: int = 0,
        user_id: Optional[str] = None,
        account_id: Optional[str] = None,
        transaction_type: Optional[str] = None,
        transaction_status: Optional[str] = None,
        from_date: Optional[datetime] = None,
        to_date: Optional[datetime] = None
    ) -> Tuple[List[dict], int]:
        """
        List all transactions across all users with filters.

        Args:
            limit: Number of transactions to return
            offset: Number of transactions to skip
            user_id: Filter by user ID
            account_id: Filter by account ID
            transaction_type: Filter by transaction type
            transaction_status: Filter by transaction status
            from_date: Filter by transactions after this date
            to_date: Filter by transactions before this date

        Returns:
            Tuple of (transaction dicts with user info, total_count)
        """
        # Build query with filters
        conditions = []
        params = []

        if account_id:
            conditions.append("(t.source_account_id = ? OR t.target_account_id = ?)")
            params.extend([account_id, account_id])

        if user_id:
            conditions.append("(sa.user_id = ? OR ta.user_id = ?)")
            params.extend([user_id, user_id])

        if transaction_type:
            conditions.append("t.type = ?")
            params.append(transaction_type)

        if transaction_status:
            conditions.append("t.status = ?")
            params.append(transaction_status)

        if from_date:
            conditions.append("t.created_at >= ?")
            params.append(from_date.isoformat())

        if to_date:
            conditions.append("t.created_at <= ?")
            params.append(to_date.isoformat())

        where_clause = f"WHERE {' AND '.join(conditions)}" if conditions else ""

        # Get total count
        count_query = f"""
            SELECT COUNT(DISTINCT t.id)
            FROM transactions t
            LEFT JOIN accounts sa ON t.source_account_id = sa.id
            LEFT JOIN accounts ta ON t.target_account_id = ta.id
            {where_clause}
        """
        count_rows = DatabaseConnection.execute_query(count_query, tuple(params))
        total = count_rows[0][0]

        # Get paginated results with user information
        query = f"""
            SELECT
                t.id,
                t.created_at,
                t.type,
                t.source_account_id,
                t.target_account_id,
                t.source_amount,
                t.source_currency,
                t.target_amount,
                t.target_currency,
                t.fx_rate,
                t.description,
                t.status,
                sa.user_id as source_user_id,
                ta.user_id as target_user_id,
                sa.card_number as source_card_number,
                ta.card_number as target_card_number
            FROM transactions t
            LEFT JOIN accounts sa ON t.source_account_id = sa.id
            LEFT JOIN accounts ta ON t.target_account_id = ta.id
            {where_clause}
            ORDER BY t.created_at DESC, t.id DESC
            LIMIT ? OFFSET ?
        """
        params.extend([limit, offset])

        rows = DatabaseConnection.execute_query(query, tuple(params))

        transactions = [
            {
                "id": str(row[0]),
                "created_at": row[1],
                "type": str(row[2]),
                "source_account_id": str(row[3]) if row[3] else None,
                "target_account_id": str(row[4]) if row[4] else None,
                "source_amount": float(row[5]) if row[5] else None,
                "source_currency": str(row[6]) if row[6] else None,
                "target_amount": float(row[7]) if row[7] else None,
                "target_currency": str(row[8]) if row[8] else None,
                "fx_rate": float(row[9]) if row[9] else None,
                "description": str(row[10]) if row[10] else None,
                "status": str(row[11]),
                "source_user_id": str(row[12]) if row[12] else None,
                "target_user_id": str(row[13]) if row[13] else None,
                "source_card_number": str(row[14]) if row[14] else None,
                "target_card_number": str(row[15]) if row[15] else None,
            }
            for row in rows
        ]

        return transactions, total
