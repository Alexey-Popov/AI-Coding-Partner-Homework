from fastapi import APIRouter, HTTPException, status, Depends, Query

from ..schemas import (
    CreateAccountRequest,
    AccountResponse,
    AccountListResponse,
    TransactionListResponse,
    TransactionResponse,
    mask_card_number
)
from ..dependencies import get_current_user
from ...services.account_service import AccountService, AccountValidationError
from ...services.transfer_service import TransferService
from ...domain.models import User
from ...infrastructure.logging import get_logger

logger = get_logger(__name__)

router = APIRouter(prefix="/accounts", tags=["accounts"])
account_service = AccountService()
transfer_service = TransferService()


@router.post("", response_model=AccountResponse, status_code=status.HTTP_201_CREATED)
async def create_account(
    request: CreateAccountRequest,
    current_user: User = Depends(get_current_user)
):
    """
    Create a new account/card for the authenticated user.

    - **currency**: 3-letter currency code (e.g., EUR, USD)
    - **initial_balance**: Initial balance (default 0)
    - **card_number**: Optional card number (auto-generated if not provided)
    """
    try:
        account = account_service.create_account(
            user_id=current_user.id,
            currency=request.currency,
            initial_balance=request.initial_balance,
            card_number=request.card_number
        )

        return AccountResponse(
            id=account.id,
            user_id=account.user_id,
            card_number=account.card_number,
            currency=account.currency,
            balance=account.balance,
            created_at=account.created_at,
            updated_at=account.updated_at
        )

    except AccountValidationError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )


@router.get("", response_model=AccountListResponse)
async def get_user_accounts(current_user: User = Depends(get_current_user)):
    """Get all accounts for the authenticated user."""
    accounts = account_service.get_user_accounts(current_user.id)

    return AccountListResponse(
        accounts=[
            AccountResponse(
                id=acc.id,
                user_id=acc.user_id,
                card_number=acc.card_number,
                currency=acc.currency,
                balance=acc.balance,
                created_at=acc.created_at,
                updated_at=acc.updated_at
            )
            for acc in accounts
        ]
    )


@router.get("/{account_id}", response_model=AccountResponse)
async def get_account(
    account_id: str,
    current_user: User = Depends(get_current_user)
):
    """Get account details by ID (owner only)."""
    try:
        account = account_service.get_account_by_id(account_id)

        # Check ownership
        if account.user_id != current_user.id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Access denied"
            )

        return AccountResponse(
            id=account.id,
            user_id=account.user_id,
            card_number=account.card_number,
            currency=account.currency,
            balance=account.balance,
            created_at=account.created_at,
            updated_at=account.updated_at
        )

    except AccountValidationError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(e)
        )


@router.get("/{account_id}/transactions", response_model=TransactionListResponse)
async def get_account_transactions(
    account_id: str,
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
    current_user: User = Depends(get_current_user)
):
    """
    Get transactions for an account with pagination (owner only).

    - **limit**: Number of transactions to return (1-100, default 20)
    - **offset**: Number of transactions to skip (default 0)
    """
    try:
        account = account_service.get_account_by_id(account_id)

        # Check ownership
        if account.user_id != current_user.id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Access denied"
            )

        transactions, total = transfer_service.get_account_transactions(
            account_id=account_id,
            limit=limit,
            offset=offset
        )

        return TransactionListResponse(
            transactions=[
                TransactionResponse(
                    id=txn.id,
                    created_at=txn.created_at,
                    type=txn.type.value,
                    source_account_id=txn.source_account_id,
                    target_account_id=txn.target_account_id,
                    source_amount=txn.source_amount,
                    source_currency=txn.source_currency,
                    target_amount=txn.target_amount,
                    target_currency=txn.target_currency,
                    fx_rate=txn.fx_rate,
                    description=txn.description,
                    status=txn.status.value
                )
                for txn in transactions
            ],
            total=total,
            limit=limit,
            offset=offset
        )

    except AccountValidationError as e:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(e)
        )
