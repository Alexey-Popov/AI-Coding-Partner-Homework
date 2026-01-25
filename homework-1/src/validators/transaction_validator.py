"""Transaction validation functions"""
import re
from decimal import Decimal, InvalidOperation

# ISO 4217 currency codes (common ones)
VALID_CURRENCY_CODES = {
    "USD",  # US Dollar
    "EUR",  # Euro
    "GBP",  # British Pound
    "JPY",  # Japanese Yen
    "CAD",  # Canadian Dollar
    "AUD",  # Australian Dollar
    "CHF",  # Swiss Franc
    "CNY",  # Chinese Yuan
    "INR",  # Indian Rupee
    "MXN",  # Mexican Peso
    "BRL",  # Brazilian Real
    "KRW",  # South Korean Won
    "SGD",  # Singapore Dollar
    "HKD",  # Hong Kong Dollar
    "NOK",  # Norwegian Krone
    "SEK",  # Swedish Krona
    "DKK",  # Danish Krone
    "NZD",  # New Zealand Dollar
    "ZAR",  # South African Rand
    "RUB",  # Russian Ruble
    "TRY",  # Turkish Lira
    "PLN",  # Polish Zloty
    "THB",  # Thai Baht
    "IDR",  # Indonesian Rupiah
    "MYR",  # Malaysian Ringgit
    "PHP",  # Philippine Peso
    "CZK",  # Czech Koruna
    "ILS",  # Israeli Shekel
    "CLP",  # Chilean Peso
    "AED",  # UAE Dirham
    "SAR",  # Saudi Riyal
    "TWD",  # Taiwan Dollar
    "ARS",  # Argentine Peso
    "COP",  # Colombian Peso
    "EGP",  # Egyptian Pound
    "VND",  # Vietnamese Dong
    "PKR",  # Pakistani Rupee
    "NGN",  # Nigerian Naira
    "BDT",  # Bangladeshi Taka
    "UAH",  # Ukrainian Hryvnia
    "PEN",  # Peruvian Sol
    "RON",  # Romanian Leu
    "HUF",  # Hungarian Forint
    "BGN",  # Bulgarian Lev
    "HRK",  # Croatian Kuna
    "ISK",  # Icelandic Krona
    "KWD",  # Kuwaiti Dinar
    "QAR",  # Qatari Riyal
    "OMR",  # Omani Rial
    "BHD",  # Bahraini Dinar
    "JOD",  # Jordanian Dinar
}

# Account format pattern: ACC-XXXXX (X is alphanumeric)
ACCOUNT_PATTERN = re.compile(r"^ACC-[A-Za-z0-9]{5}$")


def validate_amount(amount: float) -> tuple[bool, str]:
    """
    Validate transaction amount.

    Rules:
    - Must be positive
    - Maximum 2 decimal places

    Returns:
        tuple: (is_valid, error_message)
    """
    if amount <= 0:
        return False, "Amount must be a positive number"

    try:
        # Convert to Decimal for precise decimal place checking
        decimal_amount = Decimal(str(amount))
        # Get the number of decimal places
        decimal_places = abs(decimal_amount.as_tuple().exponent)

        if decimal_places > 2:
            return False, "Amount must have maximum 2 decimal places"
    except (InvalidOperation, ValueError):
        return False, "Amount must be a valid number"

    return True, ""


def validate_account_format(account: str) -> tuple[bool, str]:
    """
    Validate account number format.

    Rules:
    - Must follow format ACC-XXXXX (where X is alphanumeric)

    Returns:
        tuple: (is_valid, error_message)
    """
    if not account:
        return False, "Account number is required"

    if not ACCOUNT_PATTERN.match(account):
        return False, "Account must follow format ACC-XXXXX (where X is alphanumeric)"

    return True, ""


def validate_currency_code(currency: str) -> tuple[bool, str]:
    """
    Validate ISO 4217 currency code.

    Rules:
    - Must be a valid ISO 4217 currency code

    Returns:
        tuple: (is_valid, error_message)
    """
    if not currency:
        return False, "Currency code is required"

    # Normalize to uppercase for comparison
    if currency.upper() not in VALID_CURRENCY_CODES:
        return False, f"Invalid currency code. Must be a valid ISO 4217 code (e.g., USD, EUR, GBP)"

    return True, ""
