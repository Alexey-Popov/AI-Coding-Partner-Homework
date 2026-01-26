import pytest
from decimal import Decimal

from app.domain.models import UserRole, UserStatus


def test_regular_user_cannot_access_admin_endpoints(client):
    """Test that regular users cannot access admin endpoints."""
    # Register and login as regular user
    client.post(
        "/v1/auth/register",
        json={
            "email": "user@example.com",
            "password": "password123"
        }
    )

    login_response = client.post(
        "/v1/auth/login",
        json={
            "email": "user@example.com",
            "password": "password123"
        }
    )

    token = login_response.json()["access_token"]

    # Try to access admin endpoint
    response = client.get(
        "/v1/admin/users",
        headers={"Authorization": f"Bearer {token}"}
    )

    assert response.status_code == 403
    assert "FORBIDDEN_ROLE" in str(response.json())


def test_admin_can_list_users(client):
    """Test that admin can list users."""
    # Create admin user directly via repository
    from app.repositories.user_repo import UserRepository
    from app.infrastructure.security import hash_password

    user_repo = UserRepository()
    admin_user = user_repo.create(
        email="admin@example.com",
        password_hash=hash_password("admin123"),
        full_name="Admin User",
        role=UserRole.ADMIN,
        status=UserStatus.ACTIVE
    )

    # Login as admin
    login_response = client.post(
        "/v1/auth/login",
        json={
            "email": "admin@example.com",
            "password": "admin123"
        }
    )

    token = login_response.json()["access_token"]

    # List users
    response = client.get(
        "/v1/admin/users",
        headers={"Authorization": f"Bearer {token}"}
    )

    assert response.status_code == 200
    data = response.json()
    assert "users" in data
    assert "total" in data
    assert data["total"] >= 1


def test_admin_can_block_user(client):
    """Test that admin can block a user."""
    # Create admin and regular user
    from app.repositories.user_repo import UserRepository
    from app.infrastructure.security import hash_password

    user_repo = UserRepository()

    admin_user = user_repo.create(
        email="admin@example.com",
        password_hash=hash_password("admin123"),
        role=UserRole.ADMIN,
        status=UserStatus.ACTIVE
    )

    regular_user = user_repo.create(
        email="user@example.com",
        password_hash=hash_password("password123"),
        role=UserRole.USER,
        status=UserStatus.ACTIVE
    )

    # Login as admin
    login_response = client.post(
        "/v1/auth/login",
        json={
            "email": "admin@example.com",
            "password": "admin123"
        }
    )

    admin_token = login_response.json()["access_token"]

    # Block the user
    response = client.patch(
        f"/v1/admin/users/{regular_user.id}/block",
        headers={"Authorization": f"Bearer {admin_token}"}
    )

    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "BLOCKED"


def test_blocked_user_cannot_login(client):
    """Test that blocked users cannot login."""
    # Create a user
    from app.repositories.user_repo import UserRepository
    from app.infrastructure.security import hash_password

    user_repo = UserRepository()
    user = user_repo.create(
        email="user@example.com",
        password_hash=hash_password("password123"),
        role=UserRole.USER,
        status=UserStatus.BLOCKED
    )

    # Try to login
    response = client.post(
        "/v1/auth/login",
        json={
            "email": "user@example.com",
            "password": "password123"
        }
    )

    assert response.status_code == 403
    assert "USER_BLOCKED" in str(response.json())


def test_blocked_user_cannot_access_protected_endpoints(client):
    """Test that blocked users cannot access protected endpoints."""
    # Create and login user first
    from app.repositories.user_repo import UserRepository
    from app.infrastructure.security import hash_password

    user_repo = UserRepository()
    user = user_repo.create(
        email="user@example.com",
        password_hash=hash_password("password123"),
        role=UserRole.USER,
        status=UserStatus.ACTIVE
    )

    # Login to get token
    login_response = client.post(
        "/v1/auth/login",
        json={
            "email": "user@example.com",
            "password": "password123"
        }
    )
    token = login_response.json()["access_token"]

    # Now block the user
    user_repo.update_status(user.id, UserStatus.BLOCKED)

    # Try to access protected endpoint with token
    response = client.get(
        "/v1/accounts",
        headers={"Authorization": f"Bearer {token}"}
    )

    assert response.status_code == 403
    assert "USER_BLOCKED" in str(response.json())


def test_admin_can_unblock_user(client):
    """Test that admin can unblock a user."""
    # Create admin and blocked user
    from app.repositories.user_repo import UserRepository
    from app.infrastructure.security import hash_password

    user_repo = UserRepository()

    admin_user = user_repo.create(
        email="admin@example.com",
        password_hash=hash_password("admin123"),
        role=UserRole.ADMIN,
        status=UserStatus.ACTIVE
    )

    blocked_user = user_repo.create(
        email="blocked@example.com",
        password_hash=hash_password("password123"),
        role=UserRole.USER,
        status=UserStatus.BLOCKED
    )

    # Login as admin
    login_response = client.post(
        "/v1/auth/login",
        json={
            "email": "admin@example.com",
            "password": "admin123"
        }
    )

    admin_token = login_response.json()["access_token"]

    # Unblock the user
    response = client.patch(
        f"/v1/admin/users/{blocked_user.id}/unblock",
        headers={"Authorization": f"Bearer {admin_token}"}
    )

    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ACTIVE"

    # Verify user can now login
    login_response = client.post(
        "/v1/auth/login",
        json={
            "email": "blocked@example.com",
            "password": "password123"
        }
    )

    assert login_response.status_code == 200


def test_admin_can_list_all_transactions(client):
    """Test that admin can list all transactions across all users."""
    # Create admin user
    from app.repositories.user_repo import UserRepository
    from app.infrastructure.security import hash_password

    user_repo = UserRepository()

    admin_user = user_repo.create(
        email="admin@example.com",
        password_hash=hash_password("admin123"),
        role=UserRole.ADMIN,
        status=UserStatus.ACTIVE
    )

    # Login as admin
    login_response = client.post(
        "/v1/auth/login",
        json={
            "email": "admin@example.com",
            "password": "admin123"
        }
    )

    admin_token = login_response.json()["access_token"]

    # List all transactions
    response = client.get(
        "/v1/admin/transactions",
        headers={"Authorization": f"Bearer {admin_token}"}
    )

    assert response.status_code == 200
    data = response.json()
    assert "transactions" in data
    assert "total" in data


def test_admin_cannot_block_themselves(client):
    """Test that admin cannot block their own account."""
    # Create admin user
    from app.repositories.user_repo import UserRepository
    from app.infrastructure.security import hash_password

    user_repo = UserRepository()

    admin_user = user_repo.create(
        email="admin@example.com",
        password_hash=hash_password("admin123"),
        role=UserRole.ADMIN,
        status=UserStatus.ACTIVE
    )

    # Login as admin
    login_response = client.post(
        "/v1/auth/login",
        json={
            "email": "admin@example.com",
            "password": "admin123"
        }
    )

    admin_token = login_response.json()["access_token"]

    # Try to block themselves
    response = client.patch(
        f"/v1/admin/users/{admin_user.id}/block",
        headers={"Authorization": f"Bearer {admin_token}"}
    )

    assert response.status_code == 400
    assert "Cannot block your own account" in response.json()["detail"]


def test_jwt_includes_role_and_status(client):
    """Test that JWT token includes role and status claims."""
    # Register user
    client.post(
        "/v1/auth/register",
        json={
            "email": "user@example.com",
            "password": "password123"
        }
    )

    # Login
    login_response = client.post(
        "/v1/auth/login",
        json={
            "email": "user@example.com",
            "password": "password123"
        }
    )

    token = login_response.json()["access_token"]

    # Decode token
    from jose import jwt
    from app.infrastructure.settings import settings

    payload = jwt.decode(token, settings.JWT_SECRET, algorithms=['HS256'])

    assert "role" in payload
    assert "status" in payload
    assert payload["role"] == "USER"
    assert payload["status"] == "ACTIVE"
