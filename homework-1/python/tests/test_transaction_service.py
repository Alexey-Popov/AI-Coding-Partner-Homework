"""Tests for transaction service business logic."""

import pytest
from src.models import TransactionCreate, TransactionType, TransactionStatus
from src.services import transaction_service


@pytest.fixture(autouse=True)
def reset_storage():
    """Reset in-memory storage before each test."""
    transaction_service.transactions.clear()
    transaction_service.account_balances.clear()
    yield


class TestCreateTransaction:
    """Tests for transaction creation."""

    def test_create_deposit(self):
        """Creating a deposit should return a valid transaction."""
        data = TransactionCreate(
            toAccount="ACC-12345",
            amount=1000.0,
            currency="USD",
            type=TransactionType.DEPOSIT
        )
        transaction = transaction_service.create_transaction(data)

        assert transaction.id is not None
        assert transaction.toAccount == "ACC-12345"
        assert transaction.amount == 1000.0
        assert transaction.currency == "USD"
        assert transaction.type == TransactionType.DEPOSIT
        assert transaction.status == TransactionStatus.COMPLETED
        assert transaction.timestamp is not None

    def test_create_withdrawal(self):
        """Creating a withdrawal should return a valid transaction."""
        data = TransactionCreate(
            fromAccount="ACC-12345",
            amount=500.0,
            currency="EUR",
            type=TransactionType.WITHDRAWAL
        )
        transaction = transaction_service.create_transaction(data)

        assert transaction.id is not None
        assert transaction.fromAccount == "ACC-12345"
        assert transaction.amount == 500.0
        assert transaction.type == TransactionType.WITHDRAWAL

    def test_create_transfer(self):
        """Creating a transfer should return a valid transaction."""
        data = TransactionCreate(
            fromAccount="ACC-12345",
            toAccount="ACC-67890",
            amount=250.50,
            currency="GBP",
            type=TransactionType.TRANSFER
        )
        transaction = transaction_service.create_transaction(data)

        assert transaction.id is not None
        assert transaction.fromAccount == "ACC-12345"
        assert transaction.toAccount == "ACC-67890"
        assert transaction.amount == 250.50
        assert transaction.type == TransactionType.TRANSFER

    def test_transaction_stored_in_memory(self):
        """Created transaction should be stored in memory."""
        data = TransactionCreate(
            toAccount="ACC-12345",
            amount=100.0,
            currency="USD",
            type=TransactionType.DEPOSIT
        )
        transaction = transaction_service.create_transaction(data)

        assert len(transaction_service.transactions) == 1
        assert transaction_service.transactions[0].id == transaction.id


class TestAccountBalances:
    """Tests for account balance tracking."""

    def test_deposit_increases_balance(self):
        """Deposit should increase account balance."""
        data = TransactionCreate(
            toAccount="ACC-12345",
            amount=1000.0,
            currency="USD",
            type=TransactionType.DEPOSIT
        )
        transaction_service.create_transaction(data)

        balance = transaction_service.get_account_balance("ACC-12345")
        assert balance.balance == 1000.0

    def test_withdrawal_decreases_balance(self):
        """Withdrawal should decrease account balance."""
        # First deposit
        transaction_service.create_transaction(TransactionCreate(
            toAccount="ACC-12345",
            amount=1000.0,
            currency="USD",
            type=TransactionType.DEPOSIT
        ))
        # Then withdraw
        transaction_service.create_transaction(TransactionCreate(
            fromAccount="ACC-12345",
            amount=300.0,
            currency="USD",
            type=TransactionType.WITHDRAWAL
        ))

        balance = transaction_service.get_account_balance("ACC-12345")
        assert balance.balance == 700.0

    def test_transfer_updates_both_accounts(self):
        """Transfer should update both account balances."""
        # Deposit to source account
        transaction_service.create_transaction(TransactionCreate(
            toAccount="ACC-12345",
            amount=1000.0,
            currency="USD",
            type=TransactionType.DEPOSIT
        ))
        # Transfer to destination
        transaction_service.create_transaction(TransactionCreate(
            fromAccount="ACC-12345",
            toAccount="ACC-67890",
            amount=400.0,
            currency="USD",
            type=TransactionType.TRANSFER
        ))

        balance_from = transaction_service.get_account_balance("ACC-12345")
        balance_to = transaction_service.get_account_balance("ACC-67890")

        assert balance_from.balance == 600.0
        assert balance_to.balance == 400.0

    def test_new_account_has_zero_balance(self):
        """New account should have zero balance."""
        balance = transaction_service.get_account_balance("ACC-NEW01")
        assert balance.balance == 0.0


class TestGetTransactions:
    """Tests for transaction retrieval and filtering."""

    def setup_method(self):
        """Set up test transactions."""
        transactions_data = [
            TransactionCreate(toAccount="ACC-12345", amount=1000.0, currency="USD", type=TransactionType.DEPOSIT),
            TransactionCreate(fromAccount="ACC-12345", toAccount="ACC-67890", amount=200.0, currency="USD", type=TransactionType.TRANSFER),
            TransactionCreate(fromAccount="ACC-12345", amount=100.0, currency="EUR", type=TransactionType.WITHDRAWAL),
        ]
        for data in transactions_data:
            transaction_service.create_transaction(data)

    def test_get_all_transactions(self):
        """Should return all transactions."""
        transactions = transaction_service.get_transactions()
        assert len(transactions) == 3

    def test_filter_by_account_id(self):
        """Should filter transactions by account ID."""
        transactions = transaction_service.get_transactions(account_id="ACC-12345")
        assert len(transactions) == 3  # All involve ACC-12345

        transactions = transaction_service.get_transactions(account_id="ACC-67890")
        assert len(transactions) == 1  # Only the transfer

    def test_filter_by_type(self):
        """Should filter transactions by type."""
        transactions = transaction_service.get_transactions(transaction_type=TransactionType.DEPOSIT)
        assert len(transactions) == 1

        transactions = transaction_service.get_transactions(transaction_type=TransactionType.TRANSFER)
        assert len(transactions) == 1

        transactions = transaction_service.get_transactions(transaction_type=TransactionType.WITHDRAWAL)
        assert len(transactions) == 1

    def test_filter_combined(self):
        """Should support combined filters."""
        transactions = transaction_service.get_transactions(
            account_id="ACC-12345",
            transaction_type=TransactionType.DEPOSIT
        )
        assert len(transactions) == 1


class TestGetTransactionById:
    """Tests for getting transaction by ID."""

    def test_get_existing_transaction(self):
        """Should return transaction by ID."""
        data = TransactionCreate(
            toAccount="ACC-12345",
            amount=100.0,
            currency="USD",
            type=TransactionType.DEPOSIT
        )
        created = transaction_service.create_transaction(data)

        found = transaction_service.get_transaction_by_id(created.id)
        assert found is not None
        assert found.id == created.id

    def test_get_nonexistent_transaction(self):
        """Should return None for nonexistent ID."""
        found = transaction_service.get_transaction_by_id("nonexistent-id")
        assert found is None


class TestAccountSummary:
    """Tests for account summary."""

    def test_account_summary(self):
        """Should return correct account summary."""
        # Create some transactions
        transaction_service.create_transaction(TransactionCreate(
            toAccount="ACC-12345", amount=1000.0, currency="USD", type=TransactionType.DEPOSIT
        ))
        transaction_service.create_transaction(TransactionCreate(
            toAccount="ACC-12345", amount=500.0, currency="USD", type=TransactionType.DEPOSIT
        ))
        transaction_service.create_transaction(TransactionCreate(
            fromAccount="ACC-12345", amount=200.0, currency="USD", type=TransactionType.WITHDRAWAL
        ))

        summary = transaction_service.get_account_summary("ACC-12345")

        assert summary.accountId == "ACC-12345"
        assert summary.totalDeposits == 1500.0
        assert summary.totalWithdrawals == 200.0
        assert summary.numberOfTransactions == 3
        assert summary.currentBalance == 1300.0
        assert summary.mostRecentTransactionDate is not None

    def test_empty_account_summary(self):
        """Should return zeros for account with no transactions."""
        summary = transaction_service.get_account_summary("ACC-EMPTY")

        assert summary.totalDeposits == 0.0
        assert summary.totalWithdrawals == 0.0
        assert summary.numberOfTransactions == 0
        assert summary.currentBalance == 0.0


class TestExportCSV:
    """Tests for CSV export."""

    def test_export_csv(self):
        """Should export transactions as CSV."""
        transaction_service.create_transaction(TransactionCreate(
            toAccount="ACC-12345", amount=100.0, currency="USD", type=TransactionType.DEPOSIT
        ))
        transaction_service.create_transaction(TransactionCreate(
            fromAccount="ACC-12345", toAccount="ACC-67890", amount=50.0, currency="USD", type=TransactionType.TRANSFER
        ))

        csv = transaction_service.export_transactions_csv()

        assert "id,fromAccount,toAccount,amount,currency,type,timestamp,status" in csv
        assert "ACC-12345" in csv
        assert "ACC-67890" in csv
        assert "deposit" in csv
        assert "transfer" in csv

    def test_export_csv_with_filters(self):
        """Should export filtered transactions as CSV."""
        transaction_service.create_transaction(TransactionCreate(
            toAccount="ACC-12345", amount=100.0, currency="USD", type=TransactionType.DEPOSIT
        ))
        transaction_service.create_transaction(TransactionCreate(
            fromAccount="ACC-12345", toAccount="ACC-67890", amount=50.0, currency="USD", type=TransactionType.TRANSFER
        ))

        csv = transaction_service.export_transactions_csv(transaction_type=TransactionType.DEPOSIT)

        assert "deposit" in csv
        assert "transfer" not in csv
