import pytest
from fastapi.testclient import TestClient
from decimal import Decimal

from app.main import app

client = TestClient(app)


def setup_user_and_accounts():
    """Helper to create user and two accounts."""
    # Register user
    client.post(
        "/v1/auth/register",
        json={
            "email": "transfer@example.com",
            "password": "password123"
        }
    )

    # Login
    login_response = client.post(
        "/v1/auth/login",
        json={
            "email": "transfer@example.com",
            "password": "password123"
        }
    )
    token = login_response.json()["access_token"]

    # Create source account with EUR
    source_response = client.post(
        "/v1/accounts",
        json={
            "currency": "EUR",
            "initial_balance": "1000.00",
            "card_number": "1111 2222 3333 4444"
        },
        headers={"Authorization": f"Bearer {token}"}
    )
    source_account = source_response.json()

    # Create target account with USD
    target_response = client.post(
        "/v1/accounts",
        json={
            "currency": "USD",
            "initial_balance": "0.00",
            "card_number": "5555 6666 7777 8888"
        },
        headers={"Authorization": f"Bearer {token}"}
    )
    target_account = target_response.json()

    return token, source_account, target_account


def test_transfer_success():
    """Test successful transfer with currency conversion."""
    token, source_account, target_account = setup_user_and_accounts()

    # Execute transfer: 100 EUR -> USD at rate 1.1
    response = client.post(
        "/v1/transfers",
        json={
            "source_card_number": source_account["card_number"],
            "target_card_number": target_account["card_number"],
            "source_currency": "EUR",
            "source_amount": "100.00",
            "target_currency": "USD",
            "fx_rate": "1.1",
            "description": "Test transfer"
        },
        headers={"Authorization": f"Bearer {token}"}
    )

    assert response.status_code == 201
    data = response.json()

    # Check response structure
    assert "transaction_id" in data
    assert data["status"] == "COMPLETED"
    assert "source_account" in data
    assert "target_account" in data

    # Check balances
    assert float(data["source_account"]["balance"]) == 900.0  # 1000 - 100
    assert float(data["target_account"]["balance"]) == 110.0  # 0 + (100 * 1.1)


def test_transfer_insufficient_funds():
    """Test transfer with insufficient funds."""
    token, source_account, target_account = setup_user_and_accounts()

    # Try to transfer more than available
    response = client.post(
        "/v1/transfers",
        json={
            "source_card_number": source_account["card_number"],
            "target_card_number": target_account["card_number"],
            "source_currency": "EUR",
            "source_amount": "2000.00",
            "target_currency": "USD",
            "fx_rate": "1.1"
        },
        headers={"Authorization": f"Bearer {token}"}
    )

    assert response.status_code == 422
    assert "insufficient" in response.json()["detail"].lower()


def test_transfer_invalid_account():
    """Test transfer with invalid account."""
    token, source_account, target_account = setup_user_and_accounts()

    response = client.post(
        "/v1/transfers",
        json={
            "source_card_number": source_account["card_number"],
            "target_card_number": "9999 9999 9999 9999",
            "source_currency": "EUR",
            "source_amount": "100.00",
            "target_currency": "USD",
            "fx_rate": "1.1"
        },
        headers={"Authorization": f"Bearer {token}"}
    )

    assert response.status_code == 400
    assert "not found" in response.json()["detail"].lower()


def test_transfer_same_account():
    """Test transfer to same account."""
    token, source_account, _ = setup_user_and_accounts()

    response = client.post(
        "/v1/transfers",
        json={
            "source_card_number": source_account["card_number"],
            "target_card_number": source_account["card_number"],
            "source_currency": "EUR",
            "source_amount": "100.00",
            "target_currency": "EUR",
            "fx_rate": "1.0"
        },
        headers={"Authorization": f"Bearer {token}"}
    )

    assert response.status_code == 400
    assert "same account" in response.json()["detail"].lower()


def test_get_account_transactions():
    """Test getting account transactions."""
    token, source_account, target_account = setup_user_and_accounts()

    # Execute a transfer
    client.post(
        "/v1/transfers",
        json={
            "source_card_number": source_account["card_number"],
            "target_card_number": target_account["card_number"],
            "source_currency": "EUR",
            "source_amount": "50.00",
            "target_currency": "USD",
            "fx_rate": "1.1"
        },
        headers={"Authorization": f"Bearer {token}"}
    )

    # Get transactions for source account
    response = client.get(
        f"/v1/accounts/{source_account['id']}/transactions",
        headers={"Authorization": f"Bearer {token}"}
    )

    assert response.status_code == 200
    data = response.json()

    assert "transactions" in data
    assert data["total"] >= 1
    assert len(data["transactions"]) >= 1

    # Check transaction details
    txn = data["transactions"][0]
    assert txn["type"] == "TRANSFER"
    assert txn["status"] == "COMPLETED"
    assert txn["source_account_id"] == source_account["id"]
    assert txn["target_account_id"] == target_account["id"]
