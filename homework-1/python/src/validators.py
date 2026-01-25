"""Validation utilities for Banking Transactions API."""

import re
from typing import Optional
from .models import TransactionCreate, TransactionType, VALID_CURRENCIES, ValidationErrorDetail


# Account format: ACC-XXXXX where X is alphanumeric (uppercase)
ACCOUNT_PATTERN = re.compile(r'^ACC-[A-Z0-9]{5}$')


def validate_account_format(account: Optional[str], field_name: str) -> Optional[ValidationErrorDetail]:
    """Validate account number format."""
    if account is None:
        return None
    if not ACCOUNT_PATTERN.match(account):
        return ValidationErrorDetail(
            field=field_name,
            message=f"Account must follow format ACC-XXXXX (5 alphanumeric characters). Got: {account}"
        )
    return None


def validate_amount(amount: float) -> Optional[ValidationErrorDetail]:
    """Validate transaction amount."""
    if amount <= 0:
        return ValidationErrorDetail(
            field="amount",
            message="Amount must be a positive number"
        )

    # Check for maximum 2 decimal places
    decimal_str = str(amount)
    if '.' in decimal_str:
        decimal_places = len(decimal_str.split('.')[1])
        if decimal_places > 2:
            return ValidationErrorDetail(
                field="amount",
                message="Amount must have maximum 2 decimal places"
            )
    return None


def validate_currency(currency: str) -> Optional[ValidationErrorDetail]:
    """Validate ISO 4217 currency code."""
    if currency.upper() not in VALID_CURRENCIES:
        return ValidationErrorDetail(
            field="currency",
            message=f"Invalid currency code. Valid codes: {', '.join(sorted(VALID_CURRENCIES))}"
        )
    return None


def validate_transaction(transaction: TransactionCreate) -> list[ValidationErrorDetail]:
    """
    Validate a transaction request.

    Returns a list of validation errors (empty if valid).
    """
    errors: list[ValidationErrorDetail] = []

    # Validate amount
    amount_error = validate_amount(transaction.amount)
    if amount_error:
        errors.append(amount_error)

    # Validate currency
    currency_error = validate_currency(transaction.currency)
    if currency_error:
        errors.append(currency_error)

    # Validate based on transaction type
    if transaction.type == TransactionType.DEPOSIT:
        # Deposit requires toAccount
        if not transaction.toAccount:
            errors.append(ValidationErrorDetail(
                field="toAccount",
                message="toAccount is required for deposit transactions"
            ))
        else:
            account_error = validate_account_format(transaction.toAccount, "toAccount")
            if account_error:
                errors.append(account_error)

    elif transaction.type == TransactionType.WITHDRAWAL:
        # Withdrawal requires fromAccount
        if not transaction.fromAccount:
            errors.append(ValidationErrorDetail(
                field="fromAccount",
                message="fromAccount is required for withdrawal transactions"
            ))
        else:
            account_error = validate_account_format(transaction.fromAccount, "fromAccount")
            if account_error:
                errors.append(account_error)

    elif transaction.type == TransactionType.TRANSFER:
        # Transfer requires both accounts
        if not transaction.fromAccount:
            errors.append(ValidationErrorDetail(
                field="fromAccount",
                message="fromAccount is required for transfer transactions"
            ))
        else:
            account_error = validate_account_format(transaction.fromAccount, "fromAccount")
            if account_error:
                errors.append(account_error)

        if not transaction.toAccount:
            errors.append(ValidationErrorDetail(
                field="toAccount",
                message="toAccount is required for transfer transactions"
            ))
        else:
            account_error = validate_account_format(transaction.toAccount, "toAccount")
            if account_error:
                errors.append(account_error)

        # Check that from and to accounts are different
        if transaction.fromAccount and transaction.toAccount:
            if transaction.fromAccount == transaction.toAccount:
                errors.append(ValidationErrorDetail(
                    field="toAccount",
                    message="fromAccount and toAccount must be different for transfers"
                ))

    return errors
