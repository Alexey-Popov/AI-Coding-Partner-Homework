"""
Transaction API Routes.

This module defines all API endpoints for the Banking Transactions API:
- POST /transactions - Create a new transaction
- GET /transactions - Get all transactions with optional filtering
- GET /transactions/{id} - Get a specific transaction by ID
- GET /accounts/{accountId}/balance - Get account balance
- GET /accounts/{accountId}/summary - Get account summary (additional feature)
"""

from fastapi import APIRouter, HTTPException, Query, status
from typing import List, Optional
from models.transaction import Transaction
from services.transaction_service import TransactionService
from utils.helpers import apply_filters

# Create router
router = APIRouter(prefix="/api", tags=["transactions"])

# Initialize service (singleton pattern)
transaction_service = TransactionService()


@router.post(
    "/transactions",
    response_model=Transaction,
    status_code=status.HTTP_201_CREATED,
    summary="Create a new transaction",
    response_description="The created transaction with auto-generated ID and timestamp"
)
async def create_transaction(transaction: Transaction):
    """
    Create a new banking transaction.

    **Request Body:**
    - **fromAccount**: Source account (optional for deposits) - Format: ACC-XXXXX
    - **toAccount**: Destination account (optional for withdrawals) - Format: ACC-XXXXX
    - **amount**: Transaction amount (positive, max 2 decimals)
    - **currency**: ISO 4217 currency code (e.g., USD, EUR, GBP)
    - **type**: Transaction type (deposit, withdrawal, transfer)
    - **status**: Transaction status (pending, completed, failed) - Default: pending

    **Validation Rules:**
    - Amount must be positive with maximum 2 decimal places
    - Account numbers must match pattern ACC-XXXXX (5 alphanumeric characters)
    - Currency must be a valid ISO 4217 code
    - Deposits require toAccount
    - Withdrawals require fromAccount
    - Transfers require both fromAccount and toAccount

    **Returns:**
    - Created transaction with auto-generated ID and timestamp
    """
    try:
        created_transaction = transaction_service.create_transaction(transaction)
        return created_transaction
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )


@router.get(
    "/transactions",
    response_model=List[Transaction],
    summary="Get all transactions",
    response_description="List of transactions (optionally filtered)"
)
async def get_transactions(
    accountId: Optional[str] = Query(None, description="Filter by account ID (e.g., ACC-12345)"),
    type: Optional[str] = Query(None, description="Filter by transaction type (deposit, withdrawal, transfer)"),
    from_date: Optional[str] = Query(None, alias="from", description="Start date in ISO format (e.g., 2024-01-01)"),
    to_date: Optional[str] = Query(None, alias="to", description="End date in ISO format (e.g., 2024-12-31)")
):
    """
    Retrieve all transactions with optional filtering.

    **Query Parameters (all optional):**
    - **accountId**: Filter by account (returns transactions where account is source or destination)
    - **type**: Filter by transaction type (deposit, withdrawal, transfer)
    - **from**: Start date in ISO format (inclusive)
    - **to**: End date in ISO format (inclusive)

    **Examples:**
    - Get all transactions: `/api/transactions`
    - Filter by account: `/api/transactions?accountId=ACC-12345`
    - Filter by type: `/api/transactions?type=deposit`
    - Filter by date range: `/api/transactions?from=2024-01-01&to=2024-12-31`
    - Multiple filters: `/api/transactions?accountId=ACC-12345&type=transfer&from=2024-01-01`

    **Returns:**
    - List of transactions matching the specified filters (empty list if no matches)
    """
    all_transactions = transaction_service.get_all_transactions()

    # Apply filters using helper function
    filtered_transactions = apply_filters(
        transactions=all_transactions,
        account_id=accountId,
        transaction_type=type,
        from_date=from_date,
        to_date=to_date
    )

    return filtered_transactions


@router.get(
    "/transactions/{transaction_id}",
    response_model=Transaction,
    summary="Get transaction by ID",
    response_description="The transaction with the specified ID"
)
async def get_transaction(transaction_id: str):
    """
    Retrieve a specific transaction by its unique ID.

    **Path Parameters:**
    - **transaction_id**: Unique transaction identifier (UUID format)

    **Returns:**
    - Transaction object if found

    **Errors:**
    - 404 Not Found: If no transaction exists with the specified ID
    """
    transaction = transaction_service.get_transaction_by_id(transaction_id)

    if transaction is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Transaction with ID '{transaction_id}' not found"
        )

    return transaction


@router.get(
    "/accounts/{account_id}/balance",
    summary="Get account balance",
    response_description="Current balance for the specified account"
)
async def get_account_balance(
    account_id: str,
    currency: Optional[str] = Query(None, description="Optional currency filter (e.g., USD)")
):
    """
    Calculate the current balance for a specific account.

    **Balance Calculation:**
    - Deposits: Add amount to toAccount
    - Withdrawals: Subtract amount from fromAccount
    - Transfers: Subtract from fromAccount, add to toAccount
    - Only includes COMPLETED transactions

    **Path Parameters:**
    - **account_id**: Account ID to calculate balance for (e.g., ACC-12345)

    **Query Parameters (optional):**
    - **currency**: Filter transactions by currency (e.g., USD)

    **Returns:**
    - JSON object with accountId and balance

    **Example Response:**
    ```json
    {
        "accountId": "ACC-12345",
        "balance": 1250.50,
        "currency": "USD"
    }
    ```
    """
    balance = transaction_service.calculate_account_balance(account_id, currency)

    return {
        "accountId": account_id,
        "balance": balance,
        "currency": currency.upper() if currency else "ALL"
    }


@router.get(
    "/accounts/{account_id}/summary",
    summary="Get account summary",
    response_description="Transaction summary for the specified account"
)
async def get_account_summary(account_id: str):
    """
    Generate a comprehensive summary for a specific account.

    **Summary Includes:**
    - **totalDeposits**: Sum of all deposits to the account (completed only)
    - **totalWithdrawals**: Sum of all withdrawals from the account (completed only)
    - **transactionCount**: Total number of transactions involving this account
    - **mostRecentTransaction**: Timestamp of the most recent transaction

    **Path Parameters:**
    - **account_id**: Account ID to generate summary for (e.g., ACC-12345)

    **Returns:**
    - JSON object with account summary statistics

    **Example Response:**
    ```json
    {
        "accountId": "ACC-12345",
        "totalDeposits": 1500.00,
        "totalWithdrawals": 500.00,
        "transactionCount": 25,
        "mostRecentTransaction": "2024-01-15T10:30:00"
    }
    ```

    **Note:**
    - This is the additional feature (Option A: Transaction Summary) from the homework requirements
    - For transfers, the amount is counted as a deposit for toAccount and withdrawal for fromAccount
    """
    summary = transaction_service.get_account_summary(account_id)
    return summary
