from typing import Optional, List, Tuple
from datetime import datetime
from decimal import Decimal
import uuid

from ..domain.models import User, UserRole, UserStatus
from ..infrastructure.db import DatabaseConnection
from ..infrastructure.logging import get_logger

logger = get_logger(__name__)


class UserRepository:
    """Repository for user data access."""

    def create(
        self,
        email: str,
        password_hash: str,
        full_name: Optional[str] = None,
        role: UserRole = UserRole.USER,
        status: UserStatus = UserStatus.ACTIVE
    ) -> User:
        """Create a new user."""
        user_id = str(uuid.uuid4())
        now = datetime.utcnow()
        now_str = now.isoformat()

        query = """
            INSERT INTO users (id, email, password_hash, full_name, role, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """
        params = (user_id, email.lower(), password_hash, full_name, role.value, status.value, now_str, now_str)

        DatabaseConnection.execute_update(query, params)

        logger.info(f"User created: {user_id} with role {role.value}")

        return User(
            id=user_id,
            email=email.lower(),
            password_hash=password_hash,
            full_name=full_name,
            role=role,
            status=status,
            created_at=now,
            updated_at=now
        )

    def find_by_email(self, email: str) -> Optional[User]:
        """Find user by email."""
        query = """
            SELECT id, email, password_hash, full_name, role, status, created_at, updated_at
            FROM users
            WHERE email = ?
        """
        params = (email.lower(),)

        rows = DatabaseConnection.execute_query(query, params)

        if not rows:
            return None

        row = rows[0]
        return User(
            id=str(row[0]),
            email=str(row[1]),
            password_hash=str(row[2]),
            full_name=str(row[3]) if row[3] else None,
            role=UserRole(str(row[4])),
            status=UserStatus(str(row[5])),
            created_at=row[6],
            updated_at=row[7]
        )

    def find_by_id(self, user_id: str) -> Optional[User]:
        """Find user by ID."""
        query = """
            SELECT id, email, password_hash, full_name, role, status, created_at, updated_at
            FROM users
            WHERE id = ?
        """
        params = (user_id,)

        rows = DatabaseConnection.execute_query(query, params)

        if not rows:
            return None

        row = rows[0]
        return User(
            id=str(row[0]),
            email=str(row[1]),
            password_hash=str(row[2]),
            full_name=str(row[3]) if row[3] else None,
            role=UserRole(str(row[4])),
            status=UserStatus(str(row[5])),
            created_at=row[6],
            updated_at=row[7]
        )

    def email_exists(self, email: str) -> bool:
        """Check if email already exists."""
        query = "SELECT COUNT(*) FROM users WHERE email = ?"
        params = (email.lower(),)

        rows = DatabaseConnection.execute_query(query, params)
        return rows[0][0] > 0

    def list_users(
        self,
        limit: int = 20,
        offset: int = 0,
        status: Optional[UserStatus] = None,
        email_contains: Optional[str] = None
    ) -> Tuple[List[User], int]:
        """List users with pagination and filters. Returns (users, total_count)."""
        conditions = []
        params = []

        if status:
            conditions.append("status = ?")
            params.append(status.value)

        if email_contains:
            conditions.append("LOWER(email) LIKE ?")
            params.append(f"%{email_contains.lower()}%")

        where_clause = f"WHERE {' AND '.join(conditions)}" if conditions else ""

        # Get total count
        count_query = f"SELECT COUNT(*) FROM users {where_clause}"
        count_rows = DatabaseConnection.execute_query(count_query, tuple(params))
        total = count_rows[0][0]

        # Get paginated results
        query = f"""
            SELECT id, email, password_hash, full_name, role, status, created_at, updated_at
            FROM users
            {where_clause}
            ORDER BY created_at DESC, id DESC
            LIMIT ? OFFSET ?
        """
        params.extend([limit, offset])

        rows = DatabaseConnection.execute_query(query, tuple(params))

        users = [
            User(
                id=str(row[0]),
                email=str(row[1]),
                password_hash=str(row[2]),
                full_name=str(row[3]) if row[3] else None,
                role=UserRole(str(row[4])),
                status=UserStatus(str(row[5])),
                created_at=row[6],
                updated_at=row[7]
            )
            for row in rows
        ]

        return users, total

    def update_status(self, user_id: str, status: UserStatus) -> Optional[User]:
        """Update user status."""
        now = datetime.utcnow().isoformat()

        query = """
            UPDATE users
            SET status = ?, updated_at = ?
            WHERE id = ?
        """
        params = (status.value, now, user_id)

        updated = DatabaseConnection.execute_update(query, params)

        if updated == 0:
            return None

        logger.info(f"User {user_id} status updated to {status.value}")

        return self.find_by_id(user_id)
