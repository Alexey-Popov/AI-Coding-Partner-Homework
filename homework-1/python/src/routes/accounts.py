"""Account routes for the Banking API."""

import re
from fastapi import APIRouter, HTTPException, Query
from typing import Optional

from ..models import AccountBalance, AccountSummary
from ..services import transaction_service

router = APIRouter(prefix="/accounts", tags=["Accounts"])

ACCOUNT_PATTERN = re.compile(r'^ACC-[A-Z0-9]{5}$')


def validate_account_id(account_id: str) -> None:
    """Validate account ID format."""
    if not ACCOUNT_PATTERN.match(account_id):
        raise HTTPException(
            status_code=400,
            detail={
                "error": "Validation failed",
                "details": [{
                    "field": "accountId",
                    "message": f"Account must follow format ACC-XXXXX (5 alphanumeric characters). Got: {account_id}"
                }]
            }
        )


@router.get("/{account_id}/balance", response_model=AccountBalance)
async def get_account_balance(account_id: str):
    """
    Get the current balance for an account.

    The balance is calculated based on all transactions involving this account.
    """
    validate_account_id(account_id)
    return transaction_service.get_account_balance(account_id)


@router.get("/{account_id}/summary", response_model=AccountSummary)
async def get_account_summary(account_id: str):
    """
    Get summary statistics for an account.

    Returns:
    - Total deposits
    - Total withdrawals
    - Number of transactions
    - Most recent transaction date
    - Current balance
    """
    validate_account_id(account_id)
    return transaction_service.get_account_summary(account_id)


@router.get("/{account_id}/interest")
async def calculate_interest(
    account_id: str,
    rate: float = Query(..., gt=0, description="Annual interest rate (e.g., 0.05 for 5%)"),
    days: int = Query(..., gt=0, description="Number of days for interest calculation")
):
    """
    Calculate simple interest on the current account balance.

    Formula: Interest = Principal * Rate * (Days / 365)
    """
    validate_account_id(account_id)

    balance_info = transaction_service.get_account_balance(account_id)
    principal = balance_info.balance

    # Simple interest calculation
    interest = principal * rate * (days / 365)

    return {
        "accountId": account_id,
        "principal": round(principal, 2),
        "rate": rate,
        "days": days,
        "interest": round(interest, 2),
        "totalAfterInterest": round(principal + interest, 2)
    }
