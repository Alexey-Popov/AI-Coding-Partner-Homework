"""Transaction routes for the Banking API."""

from typing import Optional
from fastapi import APIRouter, HTTPException, Query, Response

from ..models import (
    Transaction, TransactionCreate, TransactionType,
    ValidationErrorResponse
)
from ..validators import validate_transaction
from ..services import transaction_service

router = APIRouter(prefix="/transactions", tags=["Transactions"])


@router.post(
    "",
    response_model=Transaction,
    status_code=201,
    responses={400: {"model": ValidationErrorResponse}}
)
async def create_transaction(data: TransactionCreate):
    """
    Create a new transaction.

    - **deposit**: Requires toAccount
    - **withdrawal**: Requires fromAccount
    - **transfer**: Requires both fromAccount and toAccount
    """
    # Validate the transaction
    errors = validate_transaction(data)
    if errors:
        raise HTTPException(
            status_code=400,
            detail={
                "error": "Validation failed",
                "details": [{"field": e.field, "message": e.message} for e in errors]
            }
        )

    return transaction_service.create_transaction(data)


@router.get("", response_model=list[Transaction])
async def get_transactions(
    accountId: Optional[str] = Query(None, description="Filter by account ID"),
    type: Optional[TransactionType] = Query(None, description="Filter by transaction type"),
    from_date: Optional[str] = Query(None, alias="from", description="Filter from date (ISO 8601)"),
    to_date: Optional[str] = Query(None, alias="to", description="Filter to date (ISO 8601)")
):
    """
    Get all transactions with optional filtering.

    - Filter by **accountId**: `?accountId=ACC-12345`
    - Filter by **type**: `?type=transfer`
    - Filter by **date range**: `?from=2024-01-01&to=2024-01-31`
    - Combine multiple filters
    """
    return transaction_service.get_transactions(
        account_id=accountId,
        transaction_type=type,
        from_date=from_date,
        to_date=to_date
    )


@router.get("/export")
async def export_transactions(
    format: str = Query("csv", description="Export format (only csv supported)"),
    accountId: Optional[str] = Query(None, description="Filter by account ID"),
    type: Optional[TransactionType] = Query(None, description="Filter by transaction type"),
    from_date: Optional[str] = Query(None, alias="from", description="Filter from date"),
    to_date: Optional[str] = Query(None, alias="to", description="Filter to date")
):
    """
    Export transactions in CSV format.

    Supports the same filters as GET /transactions.
    """
    if format.lower() != "csv":
        raise HTTPException(
            status_code=400,
            detail={"error": "Invalid format", "message": "Only 'csv' format is supported"}
        )

    csv_content = transaction_service.export_transactions_csv(
        account_id=accountId,
        transaction_type=type,
        from_date=from_date,
        to_date=to_date
    )

    return Response(
        content=csv_content,
        media_type="text/csv",
        headers={"Content-Disposition": "attachment; filename=transactions.csv"}
    )


@router.get("/{transaction_id}", response_model=Transaction)
async def get_transaction(transaction_id: str):
    """Get a specific transaction by ID."""
    transaction = transaction_service.get_transaction_by_id(transaction_id)
    if not transaction:
        raise HTTPException(
            status_code=404,
            detail={"error": "Not found", "message": f"Transaction with ID '{transaction_id}' not found"}
        )
    return transaction
