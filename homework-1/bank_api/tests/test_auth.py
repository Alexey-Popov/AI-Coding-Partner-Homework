import pytest
from decimal import Decimal


def test_register_success(client):
    """Test successful user registration."""
    response = client.post(
        "/v1/auth/register",
        json={
            "email": "test@example.com",
            "password": "password123",
            "full_name": "Test User"
        }
    )

    assert response.status_code == 201
    data = response.json()
    assert data["email"] == "test@example.com"
    assert data["full_name"] == "Test User"
    assert "id" in data
    assert "created_at" in data


def test_register_duplicate_email(client):
    """Test registration with duplicate email."""
    # Register first user
    client.post(
        "/v1/auth/register",
        json={
            "email": "duplicate@example.com",
            "password": "password123"
        }
    )

    # Try to register again with same email
    response = client.post(
        "/v1/auth/register",
        json={
            "email": "duplicate@example.com",
            "password": "password456"
        }
    )

    assert response.status_code == 409


def test_login_success(client):
    """Test successful login."""
    # Register user
    client.post(
        "/v1/auth/register",
        json={
            "email": "login@example.com",
            "password": "password123"
        }
    )

    # Login
    response = client.post(
        "/v1/auth/login",
        json={
            "email": "login@example.com",
            "password": "password123"
        }
    )

    assert response.status_code == 200
    data = response.json()
    assert "access_token" in data
    assert data["token_type"] == "bearer"


def test_login_invalid_credentials(client):
    """Test login with invalid credentials."""
    response = client.post(
        "/v1/auth/login",
        json={
            "email": "nonexistent@example.com",
            "password": "wrongpassword"
        }
    )

    assert response.status_code == 401


def test_get_current_user(client):
    """Test getting current user info."""
    # Register and login
    client.post(
        "/v1/auth/register",
        json={
            "email": "current@example.com",
            "password": "password123",
            "full_name": "Current User"
        }
    )

    login_response = client.post(
        "/v1/auth/login",
        json={
            "email": "current@example.com",
            "password": "password123"
        }
    )

    token = login_response.json()["access_token"]

    # Get current user
    response = client.get(
        "/v1/auth/me",
        headers={"Authorization": f"Bearer {token}"}
    )

    assert response.status_code == 200
    data = response.json()
    assert data["email"] == "current@example.com"
    assert data["full_name"] == "Current User"


def test_protected_endpoint_without_token(client):
    """Test accessing protected endpoint without token."""
    response = client.get("/v1/accounts")

    assert response.status_code == 403
