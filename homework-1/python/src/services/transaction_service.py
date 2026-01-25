"""Transaction service for business logic and in-memory storage."""

import uuid
from datetime import datetime
from typing import Optional
from ..models import (
    Transaction, TransactionCreate, TransactionType, TransactionStatus,
    AccountBalance, AccountSummary
)


# In-memory storage
transactions: list[Transaction] = []
account_balances: dict[str, float] = {}


def create_transaction(data: TransactionCreate) -> Transaction:
    """Create a new transaction and update account balances."""
    transaction = Transaction(
        id=str(uuid.uuid4()),
        fromAccount=data.fromAccount,
        toAccount=data.toAccount,
        amount=data.amount,
        currency=data.currency.upper(),
        type=data.type,
        timestamp=datetime.utcnow().isoformat() + "Z",
        status=TransactionStatus.COMPLETED
    )

    transactions.append(transaction)
    _update_account_balances(transaction)

    return transaction


def _update_account_balances(transaction: Transaction) -> None:
    """Update account balances based on transaction type."""
    if transaction.type == TransactionType.DEPOSIT:
        if transaction.toAccount:
            account_balances[transaction.toAccount] = (
                account_balances.get(transaction.toAccount, 0) + transaction.amount
            )

    elif transaction.type == TransactionType.WITHDRAWAL:
        if transaction.fromAccount:
            account_balances[transaction.fromAccount] = (
                account_balances.get(transaction.fromAccount, 0) - transaction.amount
            )

    elif transaction.type == TransactionType.TRANSFER:
        if transaction.fromAccount:
            account_balances[transaction.fromAccount] = (
                account_balances.get(transaction.fromAccount, 0) - transaction.amount
            )
        if transaction.toAccount:
            account_balances[transaction.toAccount] = (
                account_balances.get(transaction.toAccount, 0) + transaction.amount
            )


def get_transactions(
    account_id: Optional[str] = None,
    transaction_type: Optional[TransactionType] = None,
    from_date: Optional[str] = None,
    to_date: Optional[str] = None
) -> list[Transaction]:
    """Get transactions with optional filtering."""
    result = transactions.copy()

    # Filter by account ID
    if account_id:
        result = [
            t for t in result
            if t.fromAccount == account_id or t.toAccount == account_id
        ]

    # Filter by transaction type
    if transaction_type:
        result = [t for t in result if t.type == transaction_type]

    # Filter by date range
    if from_date:
        result = [t for t in result if t.timestamp >= from_date]

    if to_date:
        # Add end of day to include the entire to_date
        to_date_end = to_date + "T23:59:59.999Z" if "T" not in to_date else to_date
        result = [t for t in result if t.timestamp <= to_date_end]

    return result


def get_transaction_by_id(transaction_id: str) -> Optional[Transaction]:
    """Get a specific transaction by ID."""
    for transaction in transactions:
        if transaction.id == transaction_id:
            return transaction
    return None


def get_account_balance(account_id: str) -> AccountBalance:
    """Get the current balance for an account."""
    balance = account_balances.get(account_id, 0)
    return AccountBalance(
        accountId=account_id,
        balance=round(balance, 2),
        currency="USD"
    )


def get_account_summary(account_id: str) -> AccountSummary:
    """Get summary statistics for an account."""
    account_transactions = [
        t for t in transactions
        if t.fromAccount == account_id or t.toAccount == account_id
    ]

    total_deposits = 0.0
    total_withdrawals = 0.0
    most_recent_date: Optional[str] = None

    for t in account_transactions:
        # Track most recent transaction
        if most_recent_date is None or t.timestamp > most_recent_date:
            most_recent_date = t.timestamp

        # Calculate deposits (money coming in)
        if t.type == TransactionType.DEPOSIT and t.toAccount == account_id:
            total_deposits += t.amount
        elif t.type == TransactionType.TRANSFER and t.toAccount == account_id:
            total_deposits += t.amount

        # Calculate withdrawals (money going out)
        if t.type == TransactionType.WITHDRAWAL and t.fromAccount == account_id:
            total_withdrawals += t.amount
        elif t.type == TransactionType.TRANSFER and t.fromAccount == account_id:
            total_withdrawals += t.amount

    return AccountSummary(
        accountId=account_id,
        totalDeposits=round(total_deposits, 2),
        totalWithdrawals=round(total_withdrawals, 2),
        numberOfTransactions=len(account_transactions),
        mostRecentTransactionDate=most_recent_date,
        currentBalance=round(account_balances.get(account_id, 0), 2)
    )


def export_transactions_csv(
    account_id: Optional[str] = None,
    transaction_type: Optional[TransactionType] = None,
    from_date: Optional[str] = None,
    to_date: Optional[str] = None
) -> str:
    """Export transactions as CSV format."""
    filtered = get_transactions(account_id, transaction_type, from_date, to_date)

    # CSV header
    lines = ["id,fromAccount,toAccount,amount,currency,type,timestamp,status"]

    # CSV rows
    for t in filtered:
        from_acc = t.fromAccount or ""
        to_acc = t.toAccount or ""
        lines.append(f"{t.id},{from_acc},{to_acc},{t.amount},{t.currency},{t.type.value},{t.timestamp},{t.status.value}")

    return "\n".join(lines)
