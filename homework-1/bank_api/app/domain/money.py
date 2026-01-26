from decimal import Decimal, ROUND_HALF_UP
from typing import Tuple


def quantize_money(amount: Decimal, decimal_places: int = 4) -> Decimal:
    """
    Quantize a decimal amount to specified decimal places.

    Args:
        amount: The amount to quantize
        decimal_places: Number of decimal places (default 4)

    Returns:
        Quantized decimal
    """
    quantizer = Decimal('0.1') ** decimal_places
    return amount.quantize(quantizer, rounding=ROUND_HALF_UP)


def calculate_conversion(
    source_amount: Decimal,
    fx_rate: Decimal,
    reverse: bool = False
) -> Decimal:
    """
    Calculate currency conversion.

    Args:
        source_amount: Source amount
        fx_rate: Exchange rate
        reverse: If True, divide instead of multiply

    Returns:
        Converted amount
    """
    if reverse:
        result = source_amount / fx_rate
    else:
        result = source_amount * fx_rate

    return quantize_money(result)


def validate_positive_amount(amount: Decimal) -> bool:
    """Validate that amount is positive."""
    return amount > Decimal('0')


def validate_sufficient_funds(balance: Decimal, amount: Decimal) -> bool:
    """Validate that balance covers the amount."""
    return balance >= amount
