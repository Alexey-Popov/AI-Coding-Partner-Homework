"""
Test Categorization - Classification tests (10+ tests)

Tests auto-classification service:
- Category detection by keywords
- Priority detection by keywords
- Confidence scoring
- Disambiguation
- API endpoint integration
"""

import pytest
from fastapi.testclient import TestClient

from src.main import app
from src.models import (
    Ticket, TicketCreate, TicketMetadata,
    TicketCategory, TicketPriority,
)
from src.services.classification_service import ClassificationService
from src.services.ticket_service import ticket_service


@pytest.fixture
def client():
    return TestClient(app)


@pytest.fixture(autouse=True)
def clear():
    ticket_service.clear_all()
    yield
    ticket_service.clear_all()


@pytest.fixture
def classifier():
    return ClassificationService()


def _make_ticket(subject, description, category="other", priority="medium"):
    meta = TicketMetadata(source="api")
    return Ticket(
        customer_id="CUST-001",
        customer_email="test@example.com",
        customer_name="Test",
        subject=subject,
        description=description,
        category=category,
        priority=priority,
        metadata=meta,
    )


def _create_via_api(client, subject, description):
    data = {
        "customer_id": "CUST-001",
        "customer_email": "test@example.com",
        "customer_name": "Test",
        "subject": subject,
        "description": description,
        "category": "other",
        "priority": "medium",
        "tags": [],
        "metadata": {"source": "api"},
    }
    resp = client.post("/tickets", json=data)
    return resp.json()


class TestCategoryClassification:
    def test_account_access_keywords(self, classifier):
        ticket = _make_ticket("Can't login", "I forgot my password and cannot sign in to my account")
        result = classifier.classify_ticket(ticket)
        assert result.suggested_category == TicketCategory.ACCOUNT_ACCESS

    def test_billing_keywords(self, classifier):
        ticket = _make_ticket("Invoice issue", "I need a refund for an incorrect charge on my payment")
        result = classifier.classify_ticket(ticket)
        assert result.suggested_category == TicketCategory.BILLING_QUESTION

    def test_feature_request_keywords(self, classifier):
        ticket = _make_ticket("New feature suggestion", "It would be nice if you could add a dark mode enhancement")
        result = classifier.classify_ticket(ticket)
        assert result.suggested_category == TicketCategory.FEATURE_REQUEST

    def test_technical_issue_keywords(self, classifier):
        ticket = _make_ticket("App error", "The application keeps crashing and shows a timeout error message")
        result = classifier.classify_ticket(ticket)
        assert result.suggested_category == TicketCategory.TECHNICAL_ISSUE

    def test_bug_report_with_reproduction(self, classifier):
        ticket = _make_ticket(
            "Bug found",
            "There is an unexpected behavior when I reproduce the steps to reproduce this regression bug",
        )
        result = classifier.classify_ticket(ticket)
        assert result.suggested_category == TicketCategory.BUG_REPORT

    def test_no_keywords_returns_other(self, classifier):
        ticket = _make_ticket("Hello", "I just wanted to say hello and thank you for your service today")
        result = classifier.classify_ticket(ticket)
        assert result.suggested_category == TicketCategory.OTHER


class TestPriorityClassification:
    def test_urgent_keywords(self, classifier):
        ticket = _make_ticket("Critical issue", "Production is down and we can't access the system, this is a security breach")
        result = classifier.classify_ticket(ticket)
        assert result.suggested_priority == TicketPriority.URGENT

    def test_low_priority_keywords(self, classifier):
        ticket = _make_ticket("Minor cosmetic issue", "This is a minor suggestion, whenever you get a chance eventually")
        result = classifier.classify_ticket(ticket)
        assert result.suggested_priority == TicketPriority.LOW

    def test_default_medium_priority(self, classifier):
        ticket = _make_ticket("General question", "I have a question about how the system works for my needs")
        result = classifier.classify_ticket(ticket)
        assert result.suggested_priority == TicketPriority.MEDIUM


class TestConfidenceScoring:
    def test_confidence_range(self, classifier):
        ticket = _make_ticket("Login issue", "Cannot log in to my account after password reset attempt")
        result = classifier.classify_ticket(ticket)
        assert 0.0 <= result.confidence <= 1.0

    def test_no_match_low_confidence(self, classifier):
        ticket = _make_ticket("Hello", "Just saying hello and checking in on things today nicely")
        result = classifier.classify_ticket(ticket)
        assert result.confidence <= 0.5

    def test_keywords_found_populated(self, classifier):
        ticket = _make_ticket("Payment refund", "I need a refund for the invoice charge on my billing account")
        result = classifier.classify_ticket(ticket)
        assert len(result.keywords_found) > 0

    def test_reasoning_populated(self, classifier):
        ticket = _make_ticket("Error", "The application crashed with an error and stopped working completely")
        result = classifier.classify_ticket(ticket)
        assert len(result.reasoning) > 0


class TestClassificationAPI:
    def test_classify_endpoint(self, client):
        created = _create_via_api(client, "Can't login", "I forgot my password and my account is locked out")
        resp = client.post(f"/tickets/{created['id']}/classify")
        assert resp.status_code == 200
        data = resp.json()
        assert "suggested_category" in data
        assert "confidence" in data

    def test_classify_not_found(self, client):
        resp = client.post("/tickets/00000000-0000-0000-0000-000000000000/classify")
        assert resp.status_code == 404

    def test_apply_classification(self, client):
        created = _create_via_api(client, "Payment refund needed", "I need a refund for the invoice charge on my billing")
        resp = client.post(f"/tickets/{created['id']}/apply-classification")
        assert resp.status_code == 200
        data = resp.json()
        assert data["suggested_category"] == "billing_question"

    def test_classify_all(self, client):
        _create_via_api(client, "Login issue", "Cannot log in to my account after password reset")
        _create_via_api(client, "Payment issue", "Need a refund for incorrect invoice billing charge")
        resp = client.post("/tickets/classify-all")
        assert resp.status_code == 200
        assert len(resp.json()) == 2
