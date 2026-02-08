"""
Validation helpers for ticket fields.

Provides functions to validate individual fields and a composite validator
that checks an entire ticket payload, collecting all errors.
"""

import re

# ---------- Allowed enum values ----------

VALID_CATEGORIES = {
    "account_access", "technical_issue", "billing_question",
    "feature_request", "bug_report", "other",
}

VALID_PRIORITIES = {"urgent", "high", "medium", "low"}

VALID_STATUSES = {"new", "in_progress", "waiting_customer", "resolved", "closed"}

VALID_SOURCES = {"web_form", "email", "api", "chat", "phone"}

VALID_DEVICE_TYPES = {"desktop", "mobile", "tablet"}

# Simple but practical email regex (RFC 5322 simplified)
EMAIL_REGEX = re.compile(r"^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$")


def validate_email(email: str) -> str | None:
    """Return an error message if *email* is not valid, else None."""
    if not email or not EMAIL_REGEX.match(email):
        return "Invalid email format"
    return None


def validate_string_length(value: str, field_name: str, min_len: int, max_len: int) -> str | None:
    """Return an error message if *value* length is outside [min_len, max_len]."""
    if not isinstance(value, str):
        return f"{field_name} must be a string"
    length = len(value.strip())
    if length < min_len or length > max_len:
        return f"{field_name} must be between {min_len} and {max_len} characters"
    return None


def validate_enum(value: str, field_name: str, allowed: set[str]) -> str | None:
    """Return an error message if *value* is not in the *allowed* set."""
    if value not in allowed:
        return f"{field_name} must be one of: {', '.join(sorted(allowed))}"
    return None


def validate_ticket_data(data: dict, is_update: bool = False) -> list[str]:
    """
    Validate a full ticket payload.

    For creation (is_update=False) all required fields must be present.
    For updates, only supplied fields are validated.

    Returns a list of error strings (empty list == valid).
    """
    errors: list[str] = []

    # Fields required on creation
    required_on_create = [
        "customer_id", "customer_email", "customer_name",
        "subject", "description", "category", "priority",
    ]

    if not is_update:
        for field in required_on_create:
            if field not in data or data[field] is None or (isinstance(data[field], str) and not data[field].strip()):
                errors.append(f"{field} is required")

    # --- Field-specific validation (only if the field is present) ---

    if "customer_email" in data and data["customer_email"]:
        err = validate_email(data["customer_email"])
        if err:
            errors.append(err)

    if "subject" in data and data["subject"] is not None:
        err = validate_string_length(data["subject"], "subject", 1, 200)
        if err:
            errors.append(err)

    if "description" in data and data["description"] is not None:
        err = validate_string_length(data["description"], "description", 10, 2000)
        if err:
            errors.append(err)

    if "category" in data and data["category"] is not None:
        err = validate_enum(data["category"], "category", VALID_CATEGORIES)
        if err:
            errors.append(err)

    if "priority" in data and data["priority"] is not None:
        err = validate_enum(data["priority"], "priority", VALID_PRIORITIES)
        if err:
            errors.append(err)

    if "status" in data and data["status"] is not None:
        err = validate_enum(data["status"], "status", VALID_STATUSES)
        if err:
            errors.append(err)

    # Metadata sub-fields
    metadata = data.get("metadata")
    if metadata and isinstance(metadata, dict):
        if "source" in metadata and metadata["source"] is not None:
            err = validate_enum(metadata["source"], "metadata.source", VALID_SOURCES)
            if err:
                errors.append(err)
        if "device_type" in metadata and metadata["device_type"] is not None:
            err = validate_enum(metadata["device_type"], "metadata.device_type", VALID_DEVICE_TYPES)
            if err:
                errors.append(err)

    # Tags must be a list of strings if provided
    if "tags" in data and data["tags"] is not None:
        if not isinstance(data["tags"], list):
            errors.append("tags must be an array")
        elif not all(isinstance(t, str) for t in data["tags"]):
            errors.append("Each tag must be a string")

    return errors
