from fastapi import APIRouter, HTTPException, status, Depends, Query
from typing import Optional
from datetime import datetime
from pydantic import BaseModel, Field

from ..dependencies import require_admin
from ...services.admin_service import AdminService
from ...domain.models import User, UserStatus
from ...infrastructure.logging import get_logger

logger = get_logger(__name__)

router = APIRouter(prefix="/admin", tags=["admin"])
admin_service = AdminService()


# Response schemas
class UserSummary(BaseModel):
    """User summary for admin view (no password hash)."""
    id: str
    email: str
    full_name: Optional[str]
    role: str
    status: str
    created_at: datetime
    updated_at: datetime


class UserListResponse(BaseModel):
    """Response for user list endpoint."""
    users: list[UserSummary]
    total: int
    limit: int
    offset: int


class TransactionSummary(BaseModel):
    """Transaction summary with user info for admin view."""
    id: str
    created_at: datetime
    type: str
    source_account_id: Optional[str]
    target_account_id: Optional[str]
    source_amount: Optional[float]
    source_currency: Optional[str]
    target_amount: Optional[float]
    target_currency: Optional[str]
    fx_rate: Optional[float]
    description: Optional[str]
    status: str
    source_user_id: Optional[str]
    target_user_id: Optional[str]
    source_card_number: Optional[str]
    target_card_number: Optional[str]


class TransactionListResponse(BaseModel):
    """Response for transaction list endpoint."""
    transactions: list[TransactionSummary]
    total: int
    limit: int
    offset: int


@router.get("/users", response_model=UserListResponse)
async def list_users(
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
    status: Optional[str] = Query(None, description="Filter by status: ACTIVE or BLOCKED"),
    email_contains: Optional[str] = Query(None, description="Filter by email containing string"),
    admin_user: User = Depends(require_admin)
):
    """
    List all users with pagination and filters (admin only).

    - **limit**: Number of users to return (1-100, default 20)
    - **offset**: Number of users to skip (default 0)
    - **status**: Filter by user status (ACTIVE or BLOCKED)
    - **email_contains**: Filter by email containing string (case-insensitive)
    """
    try:
        # Parse status filter if provided
        status_filter = None
        if status:
            try:
                status_filter = UserStatus(status.upper())
            except ValueError:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Invalid status: {status}. Must be ACTIVE or BLOCKED"
                )

        users, total = admin_service.list_users(
            limit=limit,
            offset=offset,
            status=status_filter,
            email_contains=email_contains
        )

        return UserListResponse(
            users=[
                UserSummary(
                    id=user.id,
                    email=user.email,
                    full_name=user.full_name,
                    role=user.role.value,
                    status=user.status.value,
                    created_at=user.created_at,
                    updated_at=user.updated_at
                )
                for user in users
            ],
            total=total,
            limit=limit,
            offset=offset
        )

    except Exception as e:
        logger.error(f"Error listing users: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to list users"
        )


@router.patch("/users/{user_id}/block", response_model=UserSummary)
async def block_user(
    user_id: str,
    admin_user: User = Depends(require_admin)
):
    """
    Block a user account (admin only).

    The blocked user will not be able to login or access any protected endpoints.

    - **user_id**: ID of the user to block
    """
    try:
        # Prevent admin from blocking themselves
        if user_id == admin_user.id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Cannot block your own account"
            )

        user = admin_service.block_user(user_id)

        return UserSummary(
            id=user.id,
            email=user.email,
            full_name=user.full_name,
            role=user.role.value,
            status=user.status.value,
            created_at=user.created_at,
            updated_at=user.updated_at
        )

    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"code": "NOT_FOUND", "message": str(e)}
        )

    except Exception as e:
        logger.error(f"Error blocking user {user_id}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to block user"
        )


@router.patch("/users/{user_id}/unblock", response_model=UserSummary)
async def unblock_user(
    user_id: str,
    admin_user: User = Depends(require_admin)
):
    """
    Unblock a user account (admin only).

    The unblocked user will be able to login and access protected endpoints again.

    - **user_id**: ID of the user to unblock
    """
    try:
        user = admin_service.unblock_user(user_id)

        return UserSummary(
            id=user.id,
            email=user.email,
            full_name=user.full_name,
            role=user.role.value,
            status=user.status.value,
            created_at=user.created_at,
            updated_at=user.updated_at
        )

    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"code": "NOT_FOUND", "message": str(e)}
        )

    except Exception as e:
        logger.error(f"Error unblocking user {user_id}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to unblock user"
        )


@router.get("/transactions", response_model=TransactionListResponse)
async def list_all_transactions(
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
    user_id: Optional[str] = Query(None, description="Filter by user ID"),
    account_id: Optional[str] = Query(None, description="Filter by account ID"),
    type: Optional[str] = Query(None, description="Filter by transaction type"),
    status: Optional[str] = Query(None, description="Filter by transaction status"),
    from_date: Optional[str] = Query(None, description="Filter by date from (ISO format)"),
    to_date: Optional[str] = Query(None, description="Filter by date to (ISO format)"),
    admin_user: User = Depends(require_admin)
):
    """
    List all transactions across all users with filters (admin only).

    - **limit**: Number of transactions to return (1-100, default 20)
    - **offset**: Number of transactions to skip (default 0)
    - **user_id**: Filter by user ID (source or target)
    - **account_id**: Filter by account ID (source or target)
    - **type**: Filter by transaction type (DEBIT, CREDIT, TRANSFER)
    - **status**: Filter by transaction status (PENDING, COMPLETED, FAILED)
    - **from_date**: Filter by transactions after this date (ISO format)
    - **to_date**: Filter by transactions before this date (ISO format)
    """
    try:
        # Parse date filters if provided
        from_date_parsed = None
        to_date_parsed = None

        if from_date:
            try:
                from_date_parsed = datetime.fromisoformat(from_date.replace('Z', '+00:00'))
            except ValueError:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Invalid from_date format. Use ISO format (YYYY-MM-DDTHH:MM:SS)"
                )

        if to_date:
            try:
                to_date_parsed = datetime.fromisoformat(to_date.replace('Z', '+00:00'))
            except ValueError:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Invalid to_date format. Use ISO format (YYYY-MM-DDTHH:MM:SS)"
                )

        transactions, total = admin_service.list_all_transactions(
            limit=limit,
            offset=offset,
            user_id=user_id,
            account_id=account_id,
            transaction_type=type.upper() if type else None,
            transaction_status=status.upper() if status else None,
            from_date=from_date_parsed,
            to_date=to_date_parsed
        )

        return TransactionListResponse(
            transactions=[
                TransactionSummary(**txn)
                for txn in transactions
            ],
            total=total,
            limit=limit,
            offset=offset
        )

    except HTTPException:
        raise

    except Exception as e:
        logger.error(f"Error listing transactions: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to list transactions"
        )
