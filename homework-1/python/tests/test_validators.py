"""Tests for validation utilities."""

import pytest
from src.models import TransactionCreate, TransactionType
from src.validators import (
    validate_account_format,
    validate_amount,
    validate_currency,
    validate_transaction
)


class TestValidateAccountFormat:
    """Tests for account format validation."""

    def test_valid_account_format(self):
        """Valid account formats should pass."""
        assert validate_account_format("ACC-12345", "fromAccount") is None
        assert validate_account_format("ACC-ABCDE", "toAccount") is None
        assert validate_account_format("ACC-A1B2C", "fromAccount") is None

    def test_invalid_account_format_lowercase(self):
        """Lowercase letters should fail."""
        error = validate_account_format("ACC-abcde", "fromAccount")
        assert error is not None
        assert error.field == "fromAccount"

    def test_invalid_account_format_wrong_prefix(self):
        """Wrong prefix should fail."""
        error = validate_account_format("XYZ-12345", "toAccount")
        assert error is not None
        assert error.field == "toAccount"

    def test_invalid_account_format_wrong_length(self):
        """Wrong length should fail."""
        error = validate_account_format("ACC-1234", "fromAccount")
        assert error is not None
        error = validate_account_format("ACC-123456", "fromAccount")
        assert error is not None

    def test_invalid_account_format_special_chars(self):
        """Special characters should fail."""
        error = validate_account_format("ACC-12@45", "fromAccount")
        assert error is not None

    def test_none_account_returns_none(self):
        """None account should return None (no error)."""
        assert validate_account_format(None, "fromAccount") is None


class TestValidateAmount:
    """Tests for amount validation."""

    def test_valid_amounts(self):
        """Valid amounts should pass."""
        assert validate_amount(100.0) is None
        assert validate_amount(100.50) is None
        assert validate_amount(0.01) is None
        assert validate_amount(1000000.99) is None

    def test_negative_amount(self):
        """Negative amounts should fail."""
        error = validate_amount(-100.0)
        assert error is not None
        assert error.field == "amount"
        assert "positive" in error.message.lower()

    def test_zero_amount(self):
        """Zero amount should fail."""
        error = validate_amount(0)
        assert error is not None
        assert error.field == "amount"

    def test_too_many_decimals(self):
        """More than 2 decimal places should fail."""
        error = validate_amount(100.123)
        assert error is not None
        assert error.field == "amount"
        assert "decimal" in error.message.lower()


class TestValidateCurrency:
    """Tests for currency validation."""

    def test_valid_currencies(self):
        """Valid ISO 4217 currencies should pass."""
        assert validate_currency("USD") is None
        assert validate_currency("EUR") is None
        assert validate_currency("GBP") is None
        assert validate_currency("JPY") is None

    def test_valid_currencies_lowercase(self):
        """Lowercase valid currencies should pass (case-insensitive)."""
        assert validate_currency("usd") is None
        assert validate_currency("eur") is None

    def test_invalid_currency(self):
        """Invalid currency codes should fail."""
        error = validate_currency("INVALID")
        assert error is not None
        assert error.field == "currency"

    def test_empty_currency(self):
        """Empty currency should fail."""
        error = validate_currency("")
        assert error is not None


class TestValidateTransaction:
    """Tests for full transaction validation."""

    def test_valid_deposit(self):
        """Valid deposit transaction should pass."""
        transaction = TransactionCreate(
            toAccount="ACC-12345",
            amount=100.0,
            currency="USD",
            type=TransactionType.DEPOSIT
        )
        errors = validate_transaction(transaction)
        assert len(errors) == 0

    def test_valid_withdrawal(self):
        """Valid withdrawal transaction should pass."""
        transaction = TransactionCreate(
            fromAccount="ACC-12345",
            amount=50.0,
            currency="EUR",
            type=TransactionType.WITHDRAWAL
        )
        errors = validate_transaction(transaction)
        assert len(errors) == 0

    def test_valid_transfer(self):
        """Valid transfer transaction should pass."""
        transaction = TransactionCreate(
            fromAccount="ACC-12345",
            toAccount="ACC-67890",
            amount=75.50,
            currency="GBP",
            type=TransactionType.TRANSFER
        )
        errors = validate_transaction(transaction)
        assert len(errors) == 0

    def test_deposit_missing_to_account(self):
        """Deposit without toAccount should fail."""
        transaction = TransactionCreate(
            amount=100.0,
            currency="USD",
            type=TransactionType.DEPOSIT
        )
        errors = validate_transaction(transaction)
        assert len(errors) > 0
        assert any(e.field == "toAccount" for e in errors)

    def test_withdrawal_missing_from_account(self):
        """Withdrawal without fromAccount should fail."""
        transaction = TransactionCreate(
            amount=100.0,
            currency="USD",
            type=TransactionType.WITHDRAWAL
        )
        errors = validate_transaction(transaction)
        assert len(errors) > 0
        assert any(e.field == "fromAccount" for e in errors)

    def test_transfer_missing_accounts(self):
        """Transfer without both accounts should fail."""
        transaction = TransactionCreate(
            amount=100.0,
            currency="USD",
            type=TransactionType.TRANSFER
        )
        errors = validate_transaction(transaction)
        assert len(errors) >= 2
        assert any(e.field == "fromAccount" for e in errors)
        assert any(e.field == "toAccount" for e in errors)

    def test_transfer_same_accounts(self):
        """Transfer with same from and to accounts should fail."""
        transaction = TransactionCreate(
            fromAccount="ACC-12345",
            toAccount="ACC-12345",
            amount=100.0,
            currency="USD",
            type=TransactionType.TRANSFER
        )
        errors = validate_transaction(transaction)
        assert len(errors) > 0
        assert any("same" in e.message.lower() or "different" in e.message.lower() for e in errors)

    def test_multiple_validation_errors(self):
        """Multiple validation errors should be returned."""
        # Use positive amount since Pydantic validates amount > 0 before our validators
        transaction = TransactionCreate(
            toAccount="INVALID",
            amount=100.123,  # Too many decimals
            currency="FAKE",
            type=TransactionType.DEPOSIT
        )
        errors = validate_transaction(transaction)
        assert len(errors) >= 2  # Invalid account format and invalid currency
