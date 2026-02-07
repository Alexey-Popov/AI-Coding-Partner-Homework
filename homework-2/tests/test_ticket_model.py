"""
Test Ticket Model - Data validation tests (9+ tests)

Tests Pydantic models and custom validators:
- Required field validation
- String length constraints
- Email format validation
- Enum validation
- Tags and metadata validation
"""

import pytest
from pydantic import ValidationError

from src.models import (
    Ticket, TicketCreate, TicketUpdate, TicketMetadata,
    TicketCategory, TicketPriority, TicketStatus, TicketSource,
)
from src.validators.ticket_validator import (
    validate_email, validate_subject, validate_description,
    validate_category, validate_priority, validate_tags,
    validate_metadata, validate_ticket_data,
)


VALID_METADATA = {"source": "web_form", "browser": "Chrome", "device_type": "desktop"}

VALID_DATA = {
    "customer_id": "CUST-001",
    "customer_email": "test@example.com",
    "customer_name": "Test",
    "subject": "Valid subject",
    "description": "A valid description that is long enough for validation.",
    "category": "technical_issue",
    "priority": "medium",
    "tags": ["test"],
    "metadata": VALID_METADATA,
}


class TestEmailValidation:
    def test_valid_email(self):
        assert validate_email("user@example.com") == (True, "")

    def test_invalid_email(self):
        is_valid, msg = validate_email("not-an-email")
        assert not is_valid

    def test_empty_email(self):
        is_valid, msg = validate_email("")
        assert not is_valid


class TestSubjectValidation:
    def test_valid_subject(self):
        assert validate_subject("Hello") == (True, "")

    def test_empty_subject(self):
        is_valid, _ = validate_subject("")
        assert not is_valid

    def test_too_long_subject(self):
        is_valid, _ = validate_subject("x" * 201)
        assert not is_valid


class TestDescriptionValidation:
    def test_valid_description(self):
        assert validate_description("A" * 50) == (True, "")

    def test_too_short_description(self):
        is_valid, _ = validate_description("Short")
        assert not is_valid

    def test_too_long_description(self):
        is_valid, _ = validate_description("x" * 2001)
        assert not is_valid


class TestCategoryValidation:
    def test_valid_category(self):
        assert validate_category("account_access") == (True, "")

    def test_invalid_category(self):
        is_valid, _ = validate_category("nonexistent")
        assert not is_valid


class TestPriorityValidation:
    def test_valid_priority(self):
        assert validate_priority("urgent") == (True, "")

    def test_invalid_priority(self):
        is_valid, _ = validate_priority("super_high")
        assert not is_valid


class TestTagsValidation:
    def test_valid_tags(self):
        assert validate_tags(["tag1", "tag2"]) == (True, "")

    def test_invalid_tags_not_list(self):
        is_valid, _ = validate_tags("not-a-list")
        assert not is_valid

    def test_empty_tag_string(self):
        is_valid, _ = validate_tags(["valid", ""])
        assert not is_valid


class TestMetadataValidation:
    def test_valid_metadata(self):
        assert validate_metadata(VALID_METADATA) == (True, "")

    def test_missing_source(self):
        is_valid, _ = validate_metadata({"browser": "Chrome"})
        assert not is_valid

    def test_invalid_source(self):
        is_valid, _ = validate_metadata({"source": "carrier_pigeon"})
        assert not is_valid

    def test_invalid_device_type(self):
        is_valid, _ = validate_metadata({"source": "web_form", "device_type": "smartwatch"})
        assert not is_valid


class TestTicketDataValidation:
    def test_valid_ticket_data(self):
        errors = validate_ticket_data(VALID_DATA)
        assert errors == []

    def test_missing_required_fields(self):
        errors = validate_ticket_data({})
        assert len(errors) > 0
        fields = [e["field"] for e in errors]
        assert "subject" in fields
        assert "description" in fields
        assert "customer_email" in fields

    def test_multiple_validation_errors(self):
        bad = {
            **VALID_DATA,
            "customer_email": "bad",
            "subject": "",
            "description": "short",
            "category": "nope",
        }
        errors = validate_ticket_data(bad)
        assert len(errors) >= 3


class TestPydanticModels:
    def test_ticket_create_valid(self):
        meta = TicketMetadata(**VALID_METADATA)
        ticket = TicketCreate(**{**VALID_DATA, "metadata": meta})
        assert ticket.subject == VALID_DATA["subject"]

    def test_ticket_create_invalid_raises(self):
        with pytest.raises(ValidationError):
            TicketCreate(
                customer_id="x",
                customer_email="bad",
                customer_name="x",
                subject="x",
                description="short",
                category="technical_issue",
                metadata=TicketMetadata(source="web_form"),
            )

    def test_ticket_update_partial(self):
        update = TicketUpdate(subject="New subject")
        assert update.subject == "New subject"
        assert update.description is None

    def test_ticket_has_uuid_and_timestamps(self):
        meta = TicketMetadata(**VALID_METADATA)
        ticket = Ticket(**{**VALID_DATA, "metadata": meta})
        assert ticket.id is not None
        assert ticket.created_at is not None
        assert ticket.status == TicketStatus.NEW
