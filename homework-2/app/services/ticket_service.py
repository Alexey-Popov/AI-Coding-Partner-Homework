"""
Ticket service — business logic layer.

Manages the in-memory ticket store and exposes CRUD + filtering operations
that the route layer calls into.
"""

from app.models.ticket import Ticket
from app.services.classification_service import classify_ticket
from app.utils.validators import validate_ticket_data


# In-memory storage: { ticket_id: Ticket }
_tickets: dict[str, Ticket] = {}


def reset_store() -> None:
    """Clear all tickets. Useful for testing or resetting state."""
    _tickets.clear()


def create_ticket(data: dict, auto_classify: bool = False) -> tuple[dict, list[str]]:
    """
    Validate *data* and persist a new ticket.

    If *auto_classify* is True, the ticket is automatically classified
    after creation and its category/priority are updated accordingly.

    Returns (ticket_dict, errors).  If errors is non-empty the ticket was
    NOT created.
    """
    errors = validate_ticket_data(data, is_update=False)
    if errors:
        return {}, errors

    ticket = Ticket(data)
    _tickets[ticket.id] = ticket

    # Optionally run auto-classification right after creation
    if auto_classify:
        result = classify_ticket(ticket.to_dict())
        ticket.apply_classification(result)

    return ticket.to_dict(), []


def get_ticket(ticket_id: str) -> dict | None:
    """Return a ticket dict by ID, or None if not found."""
    ticket = _tickets.get(ticket_id)
    return ticket.to_dict() if ticket else None


def get_all_tickets(filters: dict | None = None) -> list[dict]:
    """
    Return all tickets, optionally filtered.

    Supported filter keys: status, priority, category, assigned_to,
    customer_email.
    """
    results = list(_tickets.values())

    if filters:
        if "status" in filters:
            results = [t for t in results if t.status == filters["status"]]
        if "priority" in filters:
            results = [t for t in results if t.priority == filters["priority"]]
        if "category" in filters:
            results = [t for t in results if t.category == filters["category"]]
        if "assigned_to" in filters:
            results = [t for t in results if t.assigned_to == filters["assigned_to"]]
        if "customer_email" in filters:
            results = [t for t in results if t.customer_email == filters["customer_email"]]

    return [t.to_dict() for t in results]


def update_ticket(ticket_id: str, data: dict) -> tuple[dict | None, list[str]]:
    """
    Validate *data* and apply a partial update to an existing ticket.

    Returns (updated_ticket_dict | None, errors).
    None means the ticket was not found.
    """
    ticket = _tickets.get(ticket_id)
    if ticket is None:
        return None, ["Ticket not found"]

    errors = validate_ticket_data(data, is_update=True)
    if errors:
        return {}, errors

    ticket.update(data)
    return ticket.to_dict(), []


def auto_classify_ticket(ticket_id: str) -> tuple[dict | None, dict | None]:
    """
    Run auto-classification on an existing ticket.

    Returns (ticket_dict | None, classification_result | None).
    First element is None if the ticket was not found.
    """
    ticket = _tickets.get(ticket_id)
    if ticket is None:
        return None, None

    # Run the classifier against the ticket's current content
    result = classify_ticket(ticket.to_dict())

    # Apply classification results to the ticket
    ticket.apply_classification(result)

    return ticket.to_dict(), result


def delete_ticket(ticket_id: str) -> bool:
    """Delete a ticket by ID. Returns True if deleted, False if not found."""
    if ticket_id in _tickets:
        del _tickets[ticket_id]
        return True
    return False
