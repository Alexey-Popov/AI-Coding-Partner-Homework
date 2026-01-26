from fastapi import APIRouter, HTTPException, status, Depends
from typing import Optional

from ..schemas import TransferRequest, TransferResponse, AccountResponse, mask_card_number
from ..dependencies import get_current_user, get_idempotency_key
from ...services.transfer_service import (
    TransferService,
    TransferValidationError,
    InsufficientFundsError
)
from ...domain.models import User
from ...infrastructure.logging import get_logger, user_id_ctx

logger = get_logger(__name__)

router = APIRouter(prefix="/transfers", tags=["transfers"])
transfer_service = TransferService()


@router.post("", response_model=TransferResponse, status_code=status.HTTP_201_CREATED)
async def create_transfer(
    request: TransferRequest,
    current_user: User = Depends(get_current_user),
    idempotency_key: Optional[str] = Depends(get_idempotency_key)
):
    """
    Execute a transfer between accounts with currency conversion.

    - **source_card_number**: Source account card number
    - **target_card_number**: Target account card number
    - **source_currency**: Source currency (3-letter code)
    - **source_amount**: Amount to transfer from source
    - **target_currency**: Target currency (3-letter code)
    - **fx_rate**: Exchange rate (course) for currency conversion
    - **target_amount**: Optional target amount (auto-calculated if not provided)
    - **description**: Optional transaction description

    The transfer is atomic: both debit and credit succeed or both fail.
    Supports idempotency via `Idempotency-Key` header.
    """
    # Set user context for logging
    user_id_ctx.set(current_user.id)

    try:
        transaction, source_account, target_account = transfer_service.execute_transfer(
            source_card_number=request.source_card_number,
            target_card_number=request.target_card_number,
            source_currency=request.source_currency,
            source_amount=request.source_amount,
            target_currency=request.target_currency,
            fx_rate=request.fx_rate,
            user_id=current_user.id,  # Required parameter moved up
            target_amount=request.target_amount,
            description=request.description,
            idempotency_key=idempotency_key
        )

        return TransferResponse(
            transaction_id=transaction.id,
            status=transaction.status.value,
            source_account=AccountResponse(
                id=source_account.id,
                user_id=source_account.user_id,
                card_number=source_account.card_number,
                currency=source_account.currency,
                balance=source_account.balance,
                created_at=source_account.created_at,
                updated_at=source_account.updated_at
            ),
            target_account=AccountResponse(
                id=target_account.id,
                user_id=target_account.user_id,
                card_number=target_account.card_number,
                currency=target_account.currency,
                balance=target_account.balance,
                created_at=target_account.created_at,
                updated_at=target_account.updated_at
            )
        )

    except InsufficientFundsError as e:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=str(e)
        )

    except TransferValidationError as e:
        # Check if it's a duplicate idempotency key
        if "idempotency" in str(e).lower():
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail=str(e)
            )
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
