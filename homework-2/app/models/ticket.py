"""
Ticket model.

Represents a customer support ticket with all associated fields.
Handles UUID generation, timestamp management, and serialization.
"""

import uuid
from datetime import datetime, timezone


class Ticket:
    """In-memory representation of a support ticket."""

    def __init__(self, data: dict):
        """
        Build a Ticket from a validated data dict.

        Auto-generates id, created_at, and updated_at if not supplied.
        """
        now = datetime.now(timezone.utc).isoformat()

        self.id: str = data.get("id", str(uuid.uuid4()))
        self.customer_id: str = data["customer_id"]
        self.customer_email: str = data["customer_email"]
        self.customer_name: str = data["customer_name"]
        self.subject: str = data["subject"]
        self.description: str = data["description"]
        self.category: str = data["category"]
        self.priority: str = data["priority"]
        self.status: str = data.get("status", "new")
        self.created_at: str = data.get("created_at", now)
        self.updated_at: str = data.get("updated_at", now)
        self.resolved_at: str | None = data.get("resolved_at")
        self.assigned_to: str | None = data.get("assigned_to")
        self.tags: list[str] = data.get("tags", [])
        self.metadata: dict = data.get("metadata", {})

    def to_dict(self) -> dict:
        """Serialize the ticket to a plain dictionary for JSON responses."""
        return {
            "id": self.id,
            "customer_id": self.customer_id,
            "customer_email": self.customer_email,
            "customer_name": self.customer_name,
            "subject": self.subject,
            "description": self.description,
            "category": self.category,
            "priority": self.priority,
            "status": self.status,
            "created_at": self.created_at,
            "updated_at": self.updated_at,
            "resolved_at": self.resolved_at,
            "assigned_to": self.assigned_to,
            "tags": self.tags,
            "metadata": self.metadata,
        }

    def update(self, data: dict) -> None:
        """
        Apply partial updates from *data* to this ticket.

        Only fields present in *data* are modified.
        Automatically refreshes updated_at and sets resolved_at when the
        status transitions to 'resolved' or 'closed'.
        """
        updatable_fields = [
            "customer_id", "customer_email", "customer_name",
            "subject", "description", "category", "priority",
            "status", "assigned_to", "tags", "metadata",
        ]

        for field in updatable_fields:
            if field in data:
                setattr(self, field, data[field])

        # Refresh the update timestamp
        self.updated_at = datetime.now(timezone.utc).isoformat()

        # Auto-set resolved_at when ticket is resolved or closed
        if "status" in data and data["status"] in ("resolved", "closed") and self.resolved_at is None:
            self.resolved_at = self.updated_at
