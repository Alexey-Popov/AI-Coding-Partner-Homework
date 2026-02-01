"""
Helper functions for filtering and processing transactions.

This module provides utility functions for:
- Filtering transactions by account ID
- Filtering transactions by type
- Filtering transactions by date range
"""

from typing import List
from datetime import datetime
from models.transaction import Transaction, TransactionType


def filter_by_account(transactions: List[Transaction], account_id: str) -> List[Transaction]:
    """
    Filter transactions by account ID.

    Returns transactions where the account is either the source (fromAccount)
    or destination (toAccount).

    Args:
        transactions: List of Transaction objects to filter
        account_id: Account ID to filter by (e.g., "ACC-12345")

    Returns:
        List of transactions involving the specified account

    Examples:
        >>> transactions = [...]  # List of Transaction objects
        >>> filtered = filter_by_account(transactions, "ACC-12345")
        >>> len(filtered)
        5
    """
    if not account_id:
        return transactions

    return [
        t for t in transactions
        if t.fromAccount == account_id or t.toAccount == account_id
    ]


def filter_by_type(transactions: List[Transaction], transaction_type: str) -> List[Transaction]:
    """
    Filter transactions by transaction type.

    Args:
        transactions: List of Transaction objects to filter
        transaction_type: Type to filter by ("deposit", "withdrawal", "transfer")

    Returns:
        List of transactions matching the specified type

    Examples:
        >>> transactions = [...]  # List of Transaction objects
        >>> deposits = filter_by_type(transactions, "deposit")
        >>> len(deposits)
        3
    """
    if not transaction_type:
        return transactions

    try:
        # Convert string to TransactionType enum
        filter_type = TransactionType(transaction_type.lower())
        return [t for t in transactions if t.type == filter_type]
    except ValueError:
        # Invalid transaction type, return empty list
        return []


def filter_by_date_range(
    transactions: List[Transaction],
    from_date: str = None,
    to_date: str = None
) -> List[Transaction]:
    """
    Filter transactions by date range.

    Args:
        transactions: List of Transaction objects to filter
        from_date: Start date in ISO format (e.g., "2024-01-01" or "2024-01-01T10:00:00")
        to_date: End date in ISO format (e.g., "2024-12-31" or "2024-12-31T23:59:59")

    Returns:
        List of transactions within the specified date range (inclusive)

    Examples:
        >>> transactions = [...]  # List of Transaction objects
        >>> filtered = filter_by_date_range(
        ...     transactions,
        ...     from_date="2024-01-01",
        ...     to_date="2024-12-31"
        ... )
        >>> len(filtered)
        10
    """
    filtered = transactions

    if from_date:
        try:
            from_datetime = datetime.fromisoformat(from_date.replace('Z', '+00:00'))
            filtered = [t for t in filtered if t.timestamp and t.timestamp >= from_datetime]
        except (ValueError, AttributeError):
            # Invalid date format, skip filtering
            pass

    if to_date:
        try:
            to_datetime = datetime.fromisoformat(to_date.replace('Z', '+00:00'))
            filtered = [t for t in filtered if t.timestamp and t.timestamp <= to_datetime]
        except (ValueError, AttributeError):
            # Invalid date format, skip filtering
            pass

    return filtered


def apply_filters(
    transactions: List[Transaction],
    account_id: str = None,
    transaction_type: str = None,
    from_date: str = None,
    to_date: str = None
) -> List[Transaction]:
    """
    Apply multiple filters to transactions.

    Convenience function that applies all filters in sequence:
    1. Filter by account ID
    2. Filter by transaction type
    3. Filter by date range

    Args:
        transactions: List of Transaction objects to filter
        account_id: Optional account ID to filter by
        transaction_type: Optional transaction type to filter by
        from_date: Optional start date in ISO format
        to_date: Optional end date in ISO format

    Returns:
        List of transactions matching all specified filters

    Examples:
        >>> transactions = [...]  # List of Transaction objects
        >>> filtered = apply_filters(
        ...     transactions,
        ...     account_id="ACC-12345",
        ...     transaction_type="deposit",
        ...     from_date="2024-01-01"
        ... )
    """
    result = transactions

    if account_id:
        result = filter_by_account(result, account_id)

    if transaction_type:
        result = filter_by_type(result, transaction_type)

    if from_date or to_date:
        result = filter_by_date_range(result, from_date, to_date)

    return result
