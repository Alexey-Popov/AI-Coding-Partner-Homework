import os
from fastapi import APIRouter, HTTPException, status, Depends, Request
from slowapi import Limiter
from slowapi.util import get_remote_address

from ..schemas import (
    RegisterRequest,
    LoginRequest,
    TokenResponse,
    UserResponse
)
from ..dependencies import get_current_user
from ...services.auth_service import AuthService, AuthenticationError, RegistrationError, UserBlockedError
from ...domain.models import User
from ...infrastructure.logging import get_logger

logger = get_logger(__name__)

router = APIRouter(prefix="/auth", tags=["auth"])
auth_service = AuthService()
limiter = Limiter(key_func=get_remote_address)

# Check if we're in testing mode
TESTING = os.environ.get("TESTING", "").lower() == "true"


def rate_limit_unless_testing(limit_string: str):
    """Apply rate limit decorator unless in testing mode."""
    def decorator(func):
        if not TESTING:
            return limiter.limit(limit_string)(func)
        return func
    return decorator


@router.post("/register", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
@rate_limit_unless_testing("3/hour")  # Max 3 registrations per hour per IP
async def register(request: Request, register_req: RegisterRequest):
    """
    Register a new user account.

    - **email**: Unique email address
    - **password**: Password (min 6 characters)
    - **full_name**: Optional full name
    """
    try:
        user = auth_service.register(
            email=register_req.email,
            password=register_req.password,
            full_name=register_req.full_name
        )

        return UserResponse(
            id=user.id,
            email=user.email,
            full_name=user.full_name,
            role=user.role.value,
            status=user.status.value,
            created_at=user.created_at
        )

    except RegistrationError as e:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=str(e)
        )


@router.post("/login", response_model=TokenResponse)
@rate_limit_unless_testing("5/minute")  # Max 5 login attempts per minute per IP
async def login(request: Request, login_req: LoginRequest):
    """
    Authenticate user and receive JWT access token.

    - **email**: User email
    - **password**: User password
    """
    try:
        user, access_token = auth_service.login(
            email=login_req.email,
            password=login_req.password
        )

        return TokenResponse(access_token=access_token)

    except UserBlockedError as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={"code": "USER_BLOCKED", "message": str(e)}
        )

    except AuthenticationError as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=str(e)
        )


@router.get("/me", response_model=UserResponse)
async def get_current_user_info(current_user: User = Depends(get_current_user)):
    """Get current authenticated user information."""
    return UserResponse(
        id=current_user.id,
        email=current_user.email,
        full_name=current_user.full_name,
        role=current_user.role.value,
        status=current_user.status.value,
        created_at=current_user.created_at
    )
