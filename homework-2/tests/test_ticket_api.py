"""
Test Ticket API - API endpoint tests (11+ tests)

Tests all CRUD endpoints for tickets:
- Create, Read, Update, Delete operations
- Filtering and statistics
- Error handling and edge cases
"""

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


VALID_TICKET = {
    "customer_id": "CUST-001",
    "customer_email": "test@example.com",
    "customer_name": "Test User",
    "subject": "Test ticket subject",
    "description": "This is a test ticket description that is long enough.",
    "category": "technical_issue",
    "priority": "medium",
    "tags": ["test"],
    "metadata": {"source": "web_form", "browser": "Chrome", "device_type": "desktop"},
}


def _create_ticket(client):
    resp = client.post("/tickets", json=VALID_TICKET)
    assert resp.status_code == 201
    return resp.json()


class TestCreateTicket:
    def test_create_ticket_success(self, client):
        resp = client.post("/tickets", json=VALID_TICKET)
        assert resp.status_code == 201
        data = resp.json()
        assert data["subject"] == VALID_TICKET["subject"]
        assert data["status"] == "new"
        assert "id" in data

    def test_create_ticket_missing_required_field(self, client):
        incomplete = {**VALID_TICKET}
        del incomplete["subject"]
        resp = client.post("/tickets", json=incomplete)
        assert resp.status_code == 422

    def test_create_ticket_invalid_email(self, client):
        bad = {**VALID_TICKET, "customer_email": "not-an-email"}
        resp = client.post("/tickets", json=bad)
        assert resp.status_code == 422

    def test_create_ticket_invalid_category(self, client):
        bad = {**VALID_TICKET, "category": "nonexistent"}
        resp = client.post("/tickets", json=bad)
        assert resp.status_code == 422


class TestGetTicket:
    def test_get_ticket_success(self, client):
        created = _create_ticket(client)
        resp = client.get(f"/tickets/{created['id']}")
        assert resp.status_code == 200
        assert resp.json()["id"] == created["id"]

    def test_get_ticket_not_found(self, client):
        resp = client.get("/tickets/00000000-0000-0000-0000-000000000000")
        assert resp.status_code == 404


class TestListTickets:
    def test_list_tickets_empty(self, client):
        resp = client.get("/tickets")
        assert resp.status_code == 200
        assert resp.json()["total"] == 0

    def test_list_tickets_with_filter(self, client):
        _create_ticket(client)
        resp = client.get("/tickets?category=technical_issue")
        assert resp.status_code == 200
        assert resp.json()["total"] == 1

        resp = client.get("/tickets?category=billing_question")
        assert resp.status_code == 200
        assert resp.json()["total"] == 0


class TestUpdateTicket:
    def test_update_ticket_success(self, client):
        created = _create_ticket(client)
        resp = client.patch(f"/tickets/{created['id']}", json={"subject": "Updated subject"})
        assert resp.status_code == 200
        assert resp.json()["subject"] == "Updated subject"

    def test_update_ticket_not_found(self, client):
        resp = client.patch(
            "/tickets/00000000-0000-0000-0000-000000000000",
            json={"subject": "x"},
        )
        assert resp.status_code == 404


class TestDeleteTicket:
    def test_delete_ticket_success(self, client):
        created = _create_ticket(client)
        resp = client.delete(f"/tickets/{created['id']}")
        assert resp.status_code == 204

        resp = client.get(f"/tickets/{created['id']}")
        assert resp.status_code == 404

    def test_delete_ticket_not_found(self, client):
        resp = client.delete("/tickets/00000000-0000-0000-0000-000000000000")
        assert resp.status_code == 404


class TestStatistics:
    def test_statistics_endpoint(self, client):
        _create_ticket(client)
        resp = client.get("/tickets/stats")
        assert resp.status_code == 200
        data = resp.json()
        assert data["total"] == 1
        assert "by_category" in data
        assert "by_priority" in data
        assert "by_status" in data
