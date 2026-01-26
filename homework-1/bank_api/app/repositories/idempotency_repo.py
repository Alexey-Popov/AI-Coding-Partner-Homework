from typing import Optional, Any
from datetime import datetime
import hashlib
import json

from ..domain.models import IdempotencyKey
from ..infrastructure.db import DatabaseConnection
from ..infrastructure.logging import get_logger

logger = get_logger(__name__)


class IdempotencyRepository:
    """Repository for idempotency key data access."""

    def create(
        self,
        key: str,
        user_id: str,
        endpoint: str,
        request_body: dict,
        response_status: int,
        response_body: dict,
        connection: Optional[Any] = None
    ) -> IdempotencyKey:
        """Create a new idempotency key record."""
        now = datetime.utcnow()
        now_str = now.isoformat()
        request_hash = self._hash_request(request_body)
        response_json = json.dumps(response_body)

        query = """
            INSERT INTO idempotency_keys (idempotency_key, user_id, endpoint, request_hash, response_status, response_body, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """
        params = (key, user_id, endpoint, request_hash, response_status, response_json, now_str)

        DatabaseConnection.execute_update(query, params, connection)

        logger.info(f"Idempotency key created: {key}")

        return IdempotencyKey(
            key=key,
            user_id=user_id,
            endpoint=endpoint,
            request_hash=request_hash,
            response_status=response_status,
            response_body=response_json,
            created_at=now
        )

    def find_by_key(self, key: str, user_id: str) -> Optional[IdempotencyKey]:
        """Find idempotency key by key and user."""
        query = """
            SELECT idempotency_key, user_id, endpoint, request_hash, response_status, response_body, created_at
            FROM idempotency_keys
            WHERE idempotency_key = ? AND user_id = ?
        """
        params = (key, user_id)

        rows = DatabaseConnection.execute_query(query, params)

        if not rows:
            return None

        row = rows[0]
        return IdempotencyKey(
            key=row[0],
            user_id=row[1],
            endpoint=row[2],
            request_hash=row[3],
            response_status=row[4],
            response_body=row[5],
            created_at=row[6]
        )

    def _hash_request(self, request_body: dict) -> str:
        """Create a hash of the request body for comparison."""
        request_json = json.dumps(request_body, sort_keys=True)
        return hashlib.sha256(request_json.encode()).hexdigest()
