"""Integration tests for API endpoints."""

import pytest
from fastapi.testclient import TestClient
from src.main import app
from src.services import transaction_service


@pytest.fixture(autouse=True)
def reset_storage():
    """Reset in-memory storage before each test."""
    transaction_service.transactions.clear()
    transaction_service.account_balances.clear()
    yield


@pytest.fixture
def client():
    """Create test client."""
    return TestClient(app)


class TestHealthEndpoint:
    """Tests for health check endpoint."""

    def test_health_check(self, client):
        """Health endpoint should return healthy status."""
        response = client.get("/health")
        assert response.status_code == 200
        assert response.json()["status"] == "healthy"


class TestCreateTransaction:
    """Tests for POST /transactions endpoint."""

    def test_create_deposit(self, client):
        """Should create deposit transaction."""
        response = client.post("/transactions", json={
            "toAccount": "ACC-12345",
            "amount": 1000.0,
            "currency": "USD",
            "type": "deposit"
        })
        assert response.status_code == 201
        data = response.json()
        assert data["toAccount"] == "ACC-12345"
        assert data["amount"] == 1000.0
        assert data["type"] == "deposit"
        assert "id" in data

    def test_create_transfer(self, client):
        """Should create transfer transaction."""
        response = client.post("/transactions", json={
            "fromAccount": "ACC-12345",
            "toAccount": "ACC-67890",
            "amount": 150.50,
            "currency": "USD",
            "type": "transfer"
        })
        assert response.status_code == 201
        data = response.json()
        assert data["fromAccount"] == "ACC-12345"
        assert data["toAccount"] == "ACC-67890"

    def test_create_withdrawal(self, client):
        """Should create withdrawal transaction."""
        response = client.post("/transactions", json={
            "fromAccount": "ACC-12345",
            "amount": 100.0,
            "currency": "EUR",
            "type": "withdrawal"
        })
        assert response.status_code == 201

    def test_invalid_account_format(self, client):
        """Should return 400 for invalid account format."""
        response = client.post("/transactions", json={
            "toAccount": "INVALID",
            "amount": 100.0,
            "currency": "USD",
            "type": "deposit"
        })
        assert response.status_code == 400
        assert "Validation failed" in response.json()["detail"]["error"]

    def test_negative_amount(self, client):
        """Should return 400 for negative amount."""
        response = client.post("/transactions", json={
            "toAccount": "ACC-12345",
            "amount": -100.0,
            "currency": "USD",
            "type": "deposit"
        })
        assert response.status_code == 422  # Pydantic validation

    def test_invalid_currency(self, client):
        """Should return 400 for invalid currency."""
        response = client.post("/transactions", json={
            "toAccount": "ACC-12345",
            "amount": 100.0,
            "currency": "INVALID",
            "type": "deposit"
        })
        assert response.status_code == 400

    def test_transfer_same_accounts(self, client):
        """Should return 400 for transfer with same accounts."""
        response = client.post("/transactions", json={
            "fromAccount": "ACC-12345",
            "toAccount": "ACC-12345",
            "amount": 100.0,
            "currency": "USD",
            "type": "transfer"
        })
        assert response.status_code == 400


class TestGetTransactions:
    """Tests for GET /transactions endpoint."""

    def test_get_empty_transactions(self, client):
        """Should return empty list when no transactions."""
        response = client.get("/transactions")
        assert response.status_code == 200
        assert response.json() == []

    def test_get_all_transactions(self, client):
        """Should return all transactions."""
        client.post("/transactions", json={
            "toAccount": "ACC-12345", "amount": 100.0, "currency": "USD", "type": "deposit"
        })
        client.post("/transactions", json={
            "toAccount": "ACC-67890", "amount": 200.0, "currency": "EUR", "type": "deposit"
        })

        response = client.get("/transactions")
        assert response.status_code == 200
        assert len(response.json()) == 2

    def test_filter_by_account(self, client):
        """Should filter by accountId."""
        client.post("/transactions", json={
            "toAccount": "ACC-12345", "amount": 100.0, "currency": "USD", "type": "deposit"
        })
        client.post("/transactions", json={
            "toAccount": "ACC-67890", "amount": 200.0, "currency": "EUR", "type": "deposit"
        })

        response = client.get("/transactions?accountId=ACC-12345")
        assert response.status_code == 200
        assert len(response.json()) == 1

    def test_filter_by_type(self, client):
        """Should filter by type."""
        client.post("/transactions", json={
            "toAccount": "ACC-12345", "amount": 100.0, "currency": "USD", "type": "deposit"
        })
        client.post("/transactions", json={
            "fromAccount": "ACC-12345", "toAccount": "ACC-67890", "amount": 50.0, "currency": "USD", "type": "transfer"
        })

        response = client.get("/transactions?type=deposit")
        assert response.status_code == 200
        assert len(response.json()) == 1
        assert response.json()[0]["type"] == "deposit"


class TestGetTransactionById:
    """Tests for GET /transactions/:id endpoint."""

    def test_get_existing_transaction(self, client):
        """Should return transaction by ID."""
        create_response = client.post("/transactions", json={
            "toAccount": "ACC-12345", "amount": 100.0, "currency": "USD", "type": "deposit"
        })
        transaction_id = create_response.json()["id"]

        response = client.get(f"/transactions/{transaction_id}")
        assert response.status_code == 200
        assert response.json()["id"] == transaction_id

    def test_get_nonexistent_transaction(self, client):
        """Should return 404 for nonexistent ID."""
        response = client.get("/transactions/nonexistent-id")
        assert response.status_code == 404


class TestAccountBalance:
    """Tests for GET /accounts/:accountId/balance endpoint."""

    def test_get_balance_after_deposit(self, client):
        """Should return correct balance after deposit."""
        client.post("/transactions", json={
            "toAccount": "ACC-12345", "amount": 1000.0, "currency": "USD", "type": "deposit"
        })

        response = client.get("/accounts/ACC-12345/balance")
        assert response.status_code == 200
        assert response.json()["balance"] == 1000.0

    def test_get_balance_after_multiple_transactions(self, client):
        """Should return correct balance after multiple transactions."""
        client.post("/transactions", json={
            "toAccount": "ACC-12345", "amount": 1000.0, "currency": "USD", "type": "deposit"
        })
        client.post("/transactions", json={
            "fromAccount": "ACC-12345", "toAccount": "ACC-67890", "amount": 300.0, "currency": "USD", "type": "transfer"
        })

        response = client.get("/accounts/ACC-12345/balance")
        assert response.status_code == 200
        assert response.json()["balance"] == 700.0

    def test_invalid_account_format(self, client):
        """Should return 400 for invalid account format."""
        response = client.get("/accounts/INVALID/balance")
        assert response.status_code == 400


class TestAccountSummary:
    """Tests for GET /accounts/:accountId/summary endpoint."""

    def test_get_summary(self, client):
        """Should return correct account summary."""
        client.post("/transactions", json={
            "toAccount": "ACC-12345", "amount": 1000.0, "currency": "USD", "type": "deposit"
        })
        client.post("/transactions", json={
            "fromAccount": "ACC-12345", "amount": 200.0, "currency": "USD", "type": "withdrawal"
        })

        response = client.get("/accounts/ACC-12345/summary")
        assert response.status_code == 200
        data = response.json()
        assert data["totalDeposits"] == 1000.0
        assert data["totalWithdrawals"] == 200.0
        assert data["numberOfTransactions"] == 2
        assert data["currentBalance"] == 800.0


class TestInterestCalculation:
    """Tests for GET /accounts/:accountId/interest endpoint."""

    def test_calculate_interest(self, client):
        """Should calculate simple interest correctly."""
        client.post("/transactions", json={
            "toAccount": "ACC-12345", "amount": 1000.0, "currency": "USD", "type": "deposit"
        })

        response = client.get("/accounts/ACC-12345/interest?rate=0.05&days=365")
        assert response.status_code == 200
        data = response.json()
        assert data["principal"] == 1000.0
        assert data["interest"] == 50.0  # 1000 * 0.05 * 1
        assert data["totalAfterInterest"] == 1050.0


class TestCSVExport:
    """Tests for GET /transactions/export endpoint."""

    def test_export_csv(self, client):
        """Should export transactions as CSV."""
        client.post("/transactions", json={
            "toAccount": "ACC-12345", "amount": 100.0, "currency": "USD", "type": "deposit"
        })

        response = client.get("/transactions/export?format=csv")
        assert response.status_code == 200
        assert "text/csv" in response.headers["content-type"]
        assert "ACC-12345" in response.text
        assert "deposit" in response.text

    def test_export_invalid_format(self, client):
        """Should return 400 for invalid format."""
        response = client.get("/transactions/export?format=xml")
        assert response.status_code == 400
