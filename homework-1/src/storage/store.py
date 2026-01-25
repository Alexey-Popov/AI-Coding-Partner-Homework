"""In-memory transaction storage"""
from typing import List, Optional, Literal
from datetime import datetime
from src.models.transaction import Transaction


class TransactionStore:
    """In-memory storage for transactions"""

    _instance = None

    def __new__(cls):
        """Singleton pattern"""
        if cls._instance is None:
            cls._instance = super(TransactionStore, cls).__new__(cls)
            cls._instance._transactions: List[Transaction] = []
        return cls._instance

    def add(self, transaction: Transaction) -> Transaction:
        """Add a transaction to storage"""
        self._transactions.append(transaction)
        return transaction

    def get_by_id(self, transaction_id: str) -> Optional[Transaction]:
        """Get a transaction by ID"""
        for txn in self._transactions:
            if txn.id == transaction_id:
                return txn
        return None

    def get_all(self) -> List[Transaction]:
        """Get all transactions"""
        return self._transactions.copy()

    def filter_transactions(
        self,
        account_id: Optional[str] = None,
        transaction_type: Optional[Literal["deposit", "withdrawal", "transfer"]] = None,
        from_date: Optional[datetime] = None,
        to_date: Optional[datetime] = None,
    ) -> List[Transaction]:
        """
        Filter transactions based on provided criteria.

        Args:
            account_id: Filter by account (matches fromAccount OR toAccount)
            transaction_type: Filter by transaction type
            from_date: Filter transactions on or after this date
            to_date: Filter transactions on or before this date

        Returns:
            List of transactions matching all provided criteria
        """
        results = self._transactions.copy()

        # Filter by account (matches either fromAccount or toAccount)
        if account_id:
            results = [
                txn for txn in results
                if txn.fromAccount == account_id or txn.toAccount == account_id
            ]

        # Filter by transaction type
        if transaction_type:
            results = [
                txn for txn in results
                if txn.type == transaction_type
            ]

        # Filter by from_date (inclusive)
        if from_date:
            results = [
                txn for txn in results
                if txn.timestamp >= from_date
            ]

        # Filter by to_date (inclusive)
        if to_date:
            results = [
                txn for txn in results
                if txn.timestamp <= to_date
            ]

        return results

    def find_by_account(self, account_id: str) -> List[Transaction]:
        """Find all transactions for a specific account"""
        return [
            txn for txn in self._transactions
            if txn.fromAccount == account_id or txn.toAccount == account_id
        ]

    def calculate_balance(self, account_id: str) -> float:
        """Calculate account balance from completed transactions"""
        balance = 0.0
        for txn in self._transactions:
            if txn.status != "completed":
                continue

            if txn.type == "deposit" and txn.toAccount == account_id:
                balance += txn.amount
            elif txn.type == "withdrawal" and txn.fromAccount == account_id:
                balance -= txn.amount
            elif txn.type == "transfer":
                if txn.toAccount == account_id:
                    balance += txn.amount
                elif txn.fromAccount == account_id:
                    balance -= txn.amount

        return round(balance, 2)

    def has_transactions_for_account(self, account_id: str) -> bool:
        """Check if account has any transactions"""
        return any(
            txn.fromAccount == account_id or txn.toAccount == account_id
            for txn in self._transactions
        )
