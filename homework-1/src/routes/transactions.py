"""Transaction routes for the banking API"""
from fastapi import APIRouter, HTTPException, Query, status
from typing import List, Optional, Literal
from src.models.transaction import Transaction
from src.schemas.transaction import TransactionCreate, TransactionResponse
from src.storage.store import TransactionStore
from src.utils.date_utils import parse_date_start, parse_date_end

router = APIRouter(prefix="/transactions", tags=["transactions"])
store = TransactionStore()


@router.post("", response_model=TransactionResponse, status_code=status.HTTP_201_CREATED)
def create_transaction(transaction_data: TransactionCreate) -> dict:
    """
    Create a new transaction

    - **fromAccount**: Source account number
    - **toAccount**: Destination account number
    - **amount**: Transaction amount (must be positive)
    - **currency**: ISO 4217 currency code
    - **type**: Transaction type (deposit, withdrawal, or transfer)
    """
    try:
        # Create transaction object
        transaction = Transaction(
            from_account=transaction_data.fromAccount,
            to_account=transaction_data.toAccount,
            amount=transaction_data.amount,
            currency=transaction_data.currency,
            transaction_type=transaction_data.type,
        )

        # Add to store
        store.add(transaction)

        return transaction.to_dict()
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )


@router.get("", response_model=List[TransactionResponse])
def list_transactions(
    accountId: Optional[str] = Query(
        None,
        description="Filter by account ID (matches fromAccount or toAccount)"
    ),
    type: Optional[Literal["deposit", "withdrawal", "transfer"]] = Query(
        None,
        description="Filter by transaction type"
    ),
    from_date: Optional[str] = Query(
        None,
        alias="from",
        description="Filter transactions from this date (inclusive, format: YYYY-MM-DD)"
    ),
    to_date: Optional[str] = Query(
        None,
        alias="to",
        description="Filter transactions to this date (inclusive, format: YYYY-MM-DD)"
    ),
) -> List[dict]:
    """
    Get all transactions with optional filtering

    - **accountId**: Filter by account (matches fromAccount OR toAccount)
    - **type**: Filter by transaction type (deposit, withdrawal, transfer)
    - **from**: Filter transactions on or after this date (YYYY-MM-DD)
    - **to**: Filter transactions on or before this date (YYYY-MM-DD)

    All filters can be combined. Returns empty array if no matches found.
    """
    # Parse and validate date parameters
    parsed_from = None
    parsed_to = None
    validation_errors = []

    if from_date:
        is_valid, parsed_from, error = parse_date_start(from_date)
        if not is_valid:
            validation_errors.append({"field": "from", "message": error})

    if to_date:
        is_valid, parsed_to, error = parse_date_end(to_date)
        if not is_valid:
            validation_errors.append({"field": "to", "message": error})

    # Return validation errors if any
    if validation_errors:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={
                "error": "Validation failed",
                "details": validation_errors
            }
        )

    # Validate date range (from should not be after to)
    if parsed_from and parsed_to and parsed_from > parsed_to:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={
                "error": "Validation failed",
                "details": [
                    {"field": "from", "message": "'from' date cannot be after 'to' date"}
                ]
            }
        )

    # Apply filters
    transactions = store.filter_transactions(
        account_id=accountId,
        transaction_type=type,
        from_date=parsed_from,
        to_date=parsed_to,
    )

    return [txn.to_dict() for txn in transactions]


@router.get("/{transaction_id}", response_model=TransactionResponse)
def get_transaction(transaction_id: str) -> dict:
    """
    Get a specific transaction by ID

    - **transaction_id**: The ID of the transaction to retrieve
    """
    transaction = store.get_by_id(transaction_id)
    if not transaction:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Transaction with ID '{transaction_id}' not found"
        )
    return transaction.to_dict()
