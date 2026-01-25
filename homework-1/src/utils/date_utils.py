"""Date parsing utilities for transaction filtering"""
from datetime import datetime, time
from typing import Optional, Tuple


def parse_date(date_string: str) -> Tuple[bool, Optional[datetime], str]:
    """
    Parse a date string in ISO format (YYYY-MM-DD).

    Args:
        date_string: Date string to parse

    Returns:
        Tuple of (is_valid, parsed_datetime, error_message)
    """
    if not date_string:
        return True, None, ""

    try:
        # Parse date in YYYY-MM-DD format
        parsed = datetime.strptime(date_string, "%Y-%m-%d")
        return True, parsed, ""
    except ValueError:
        return False, None, f"Invalid date format '{date_string}'. Expected format: YYYY-MM-DD"


def parse_date_start(date_string: str) -> Tuple[bool, Optional[datetime], str]:
    """
    Parse a 'from' date - returns datetime at start of day (00:00:00).

    Args:
        date_string: Date string to parse

    Returns:
        Tuple of (is_valid, parsed_datetime, error_message)
    """
    is_valid, parsed, error = parse_date(date_string)
    if is_valid and parsed:
        # Set to start of day
        parsed = datetime.combine(parsed.date(), time.min)
    return is_valid, parsed, error


def parse_date_end(date_string: str) -> Tuple[bool, Optional[datetime], str]:
    """
    Parse a 'to' date - returns datetime at end of day (23:59:59.999999).

    Args:
        date_string: Date string to parse

    Returns:
        Tuple of (is_valid, parsed_datetime, error_message)
    """
    is_valid, parsed, error = parse_date(date_string)
    if is_valid and parsed:
        # Set to end of day
        parsed = datetime.combine(parsed.date(), time.max)
    return is_valid, parsed, error
