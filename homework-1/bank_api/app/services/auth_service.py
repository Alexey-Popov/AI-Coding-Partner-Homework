from typing import Optional, Tuple
from ..domain.models import User, UserStatus
from ..repositories.user_repo import UserRepository
from ..infrastructure.security import hash_password, verify_password, create_access_token
from ..infrastructure.logging import get_logger

logger = get_logger(__name__)


def _obfuscate_email(email: str) -> str:
    """Obfuscate email for logging: user@example.com -> u***@e***.com"""
    try:
        local, domain = email.split('@')
        domain_parts = domain.split('.')
        if len(domain_parts) >= 2:
            return f"{local[0]}***@{domain_parts[0][0]}***.{domain_parts[-1]}"
        return f"{local[0]}***@***"
    except:
        return "***@***"


class AuthenticationError(Exception):
    """Raised when authentication fails."""
    pass


class UserBlockedError(Exception):
    """Raised when user account is blocked."""
    pass


class RegistrationError(Exception):
    """Raised when registration fails."""
    pass


class AuthService:
    """Service for authentication operations."""

    def __init__(self):
        self.user_repo = UserRepository()

    def register(self, email: str, password: str, full_name: Optional[str] = None) -> User:
        """
        Register a new user.

        Args:
            email: User email
            password: Plain text password
            full_name: Optional full name

        Returns:
            Created user

        Raises:
            RegistrationError: If email already exists
        """
        # Check if email already exists
        if self.user_repo.email_exists(email):
            logger.warning(f"Registration failed: email already exists: {_obfuscate_email(email)}")
            raise RegistrationError("Email already registered")

        # Hash password
        password_hash = hash_password(password)

        # Create user
        user = self.user_repo.create(email, password_hash, full_name)

        logger.info(f"User registered successfully: {user.id}")

        return user

    def login(self, email: str, password: str) -> Tuple[User, str]:
        """
        Authenticate user and generate access token.

        Args:
            email: User email
            password: Plain text password

        Returns:
            Tuple of (User, access_token)

        Raises:
            AuthenticationError: If credentials are invalid
        """
        # Find user by email
        user = self.user_repo.find_by_email(email)

        if not user:
            logger.warning(f"Login failed: user not found: {_obfuscate_email(email)}")
            raise AuthenticationError("Invalid credentials")

        # Verify password
        if not verify_password(password, user.password_hash):
            logger.warning(f"Login failed: invalid password: {_obfuscate_email(email)}")
            raise AuthenticationError("Invalid credentials")

        # Check if user is blocked
        if user.status == UserStatus.BLOCKED:
            logger.warning(f"Login blocked: user account is blocked: {_obfuscate_email(email)}")
            raise UserBlockedError("Account is blocked. Please contact support.")

        # Generate access token with role and status
        token_data = {
            "sub": user.id,
            "email": user.email,
            "role": user.role.value,
            "status": user.status.value
        }
        access_token = create_access_token(token_data)

        logger.info(f"User logged in successfully: {user.id}")

        return user, access_token

    def get_user_by_id(self, user_id: str) -> Optional[User]:
        """Get user by ID."""
        return self.user_repo.find_by_id(user_id)
