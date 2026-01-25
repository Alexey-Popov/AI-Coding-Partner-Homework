"""Transaction routes for the banking API"""
from fastapi import APIRouter, HTTPException, status
from typing import List
from src.models.transaction import Transaction
from src.schemas.transaction import TransactionCreate, TransactionResponse
from src.storage.store import TransactionStore

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
def list_transactions() -> List[dict]:
    """
    Get all transactions

    Returns a list of all transactions in the system.
    """
    transactions = store.get_all()
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
