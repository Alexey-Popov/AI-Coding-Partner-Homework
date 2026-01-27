"""
Validation utilities for transaction data.

This module provides standalone validation functions for:
- Amount validation (positive, max 2 decimals)
- Account format validation (ACC-XXXXX pattern)
- ISO 4217 currency code validation
"""

import re
from typing import Tuple


# ISO 4217 Currency Codes
VALID_CURRENCIES = {
    'USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'NZD',
    'CNY', 'INR', 'BRL', 'RUB', 'ZAR', 'KRW', 'SGD', 'HKD',
    'SEK', 'NOK', 'DKK', 'PLN', 'THB', 'MYR', 'IDR', 'PHP',
    'MXN', 'ARS', 'CLP', 'COP', 'PEN', 'TRY', 'SAR', 'AED',
    'ILS', 'EGP', 'NGN', 'KES', 'GHS', 'MAD', 'PKR', 'BDT',
    'VND', 'CZK', 'HUF', 'RON', 'ISK', 'HRK', 'BGN', 'UAH'
}


def validate_amount(amount: float) -> Tuple[bool, str]:
    """
    Validate transaction amount.

    Rules:
    - Must be positive (> 0)
    - Maximum 2 decimal places

    Args:
        amount: Amount to validate

    Returns:
        Tuple of (is_valid, error_message)
        - is_valid: True if validation passes, False otherwise
        - error_message: Empty string if valid, error description if invalid

    Examples:
        >>> validate_amount(100.50)
        (True, '')
        >>> validate_amount(-50.00)
        (False, 'Amount must be a positive number')
        >>> validate_amount(100.123)
        (False, 'Amount must have maximum 2 decimal places')
    """
    if amount <= 0:
        return False, "Amount must be a positive number"

    # Check for maximum 2 decimal places
    if round(amount, 2) != amount:
        return False, "Amount must have maximum 2 decimal places"

    return True, ""


def validate_account_format(account: str) -> Tuple[bool, str]:
    """
    Validate account number format.

    Rules:
    - Must match pattern: ACC-XXXXX
    - Where X is an uppercase letter (A-Z) or digit (0-9)
    - Example: ACC-12345, ACC-ABCDE, ACC-AB123

    Args:
        account: Account number to validate

    Returns:
        Tuple of (is_valid, error_message)
        - is_valid: True if validation passes, False otherwise
        - error_message: Empty string if valid, error description if invalid

    Examples:
        >>> validate_account_format("ACC-12345")
        (True, '')
        >>> validate_account_format("ACC-ABCDE")
        (True, '')
        >>> validate_account_format("invalid")
        (False, 'Account must match pattern ACC-XXXXX (5 alphanumeric characters)')
    """
    pattern = r'^ACC-[A-Z0-9]{5}$'

    if not re.match(pattern, account):
        return False, "Account must match pattern ACC-XXXXX (5 alphanumeric characters)"

    return True, ""


def validate_currency_code(currency: str) -> Tuple[bool, str]:
    """
    Validate ISO 4217 currency code.

    Rules:
    - Must be a valid 3-letter ISO 4217 currency code
    - Case-insensitive (will be converted to uppercase)
    - Examples: USD, EUR, GBP, JPY, CHF, etc.

    Args:
        currency: Currency code to validate

    Returns:
        Tuple of (is_valid, error_message)
        - is_valid: True if validation passes, False otherwise
        - error_message: Empty string if valid, error description if invalid

    Examples:
        >>> validate_currency_code("USD")
        (True, '')
        >>> validate_currency_code("eur")
        (True, '')
        >>> validate_currency_code("XYZ")
        (False, 'Currency must be a valid ISO 4217 code')
    """
    if len(currency) != 3:
        return False, "Currency code must be exactly 3 characters"

    currency_upper = currency.upper()

    if currency_upper not in VALID_CURRENCIES:
        return False, f"Currency must be a valid ISO 4217 code. Received: {currency}"

    return True, ""


def is_valid_iso_currency(currency: str) -> bool:
    """
    Check if currency code is a valid ISO 4217 code.

    Args:
        currency: Currency code to check

    Returns:
        True if valid, False otherwise

    Examples:
        >>> is_valid_iso_currency("USD")
        True
        >>> is_valid_iso_currency("XYZ")
        False
    """
    return currency.upper() in VALID_CURRENCIES
