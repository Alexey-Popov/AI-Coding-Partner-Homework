"""
Test Import JSON - JSON parsing tests (5+ tests)

Tests JSON file import functionality:
- Valid JSON array import
- Invalid JSON format
- Non-array JSON
- Partial success
- Error details
"""

import io
import json
import pytest
from fastapi.testclient import TestClient

from src.main import app
from src.services.ticket_service import ticket_service


@pytest.fixture
def client():
    return TestClient(app)


@pytest.fixture(autouse=True)
def clear():
    ticket_service.clear_all()
    yield
    ticket_service.clear_all()


VALID_TICKETS = [
    {
        "customer_id": "CUST-001",
        "customer_email": "user1@example.com",
        "customer_name": "User One",
        "subject": "Application crashes on startup",
        "description": "The desktop application crashes immediately when I try to open it after the latest update.",
        "category": "technical_issue",
        "priority": "high",
        "tags": ["crash"],
        "metadata": {"source": "api", "browser": None, "device_type": "desktop"},
    },
    {
        "customer_id": "CUST-002",
        "customer_email": "user2@example.com",
        "customer_name": "User Two",
        "subject": "Need invoice for taxes",
        "description": "Please send me an official invoice for my recent purchases for quarterly tax filing.",
        "category": "billing_question",
        "priority": "medium",
        "tags": ["invoice"],
        "metadata": {"source": "email", "browser": None, "device_type": None},
    },
]


def _upload_json(client, data, filename="test.json"):
    content = json.dumps(data) if not isinstance(data, str) else data
    return client.post(
        "/import/json",
        files={"file": (filename, io.BytesIO(content.encode()), "application/json")},
    )


class TestJSONImport:
    def test_import_valid_json(self, client):
        resp = _upload_json(client, VALID_TICKETS)
        assert resp.status_code == 200
        data = resp.json()
        assert data["total"] == 2
        assert data["success_count"] == 2

    def test_import_json_creates_tickets(self, client):
        _upload_json(client, VALID_TICKETS)
        resp = client.get("/tickets")
        assert resp.json()["total"] == 2

    def test_import_non_array_json(self, client):
        resp = _upload_json(client, {"not": "an array"})
        data = resp.json()
        assert data["success_count"] == 0
        assert len(data["errors"]) > 0

    def test_import_invalid_json_syntax(self, client):
        resp = _upload_json(client, "{bad json[")
        data = resp.json()
        assert data["success_count"] == 0

    def test_import_json_wrong_extension(self, client):
        resp = _upload_json(client, VALID_TICKETS, filename="test.txt")
        assert resp.status_code == 400

    def test_import_json_with_invalid_ticket(self, client):
        mixed = VALID_TICKETS + [{"customer_id": "", "customer_email": "bad"}]
        resp = _upload_json(client, mixed)
        data = resp.json()
        assert data["success_count"] == 2
        assert data["error_count"] >= 1
