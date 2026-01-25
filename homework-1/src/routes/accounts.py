"""Account routes for balance-related operations"""
from fastapi import APIRouter, HTTPException, Request, status
from src.schemas.transaction import BalanceResponse
from src.storage.store import TransactionStore
from src.utils.rate_limiter import limiter, RATE_LIMIT

router = APIRouter(prefix="/accounts", tags=["accounts"])
store = TransactionStore()


@router.get("/{account_id}/balance", response_model=BalanceResponse)
@limiter.limit(RATE_LIMIT)
def get_account_balance(request: Request, account_id: str) -> dict:
    """
    Get account balance from completed transactions

    - **account_id**: The account ID to get balance for

    Rate limited to 100 requests per minute per IP.
    """
    if not store.has_transactions_for_account(account_id):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Account '{account_id}' not found",
        )

    balance = store.calculate_balance(account_id)
    return {"accountId": account_id, "balance": balance}
