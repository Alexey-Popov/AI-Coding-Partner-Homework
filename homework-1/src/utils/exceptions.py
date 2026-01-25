"""Custom exceptions for the banking API"""


class BankingAPIException(Exception):
    """Base exception for banking API"""
    pass


class ValidationError(BankingAPIException):
    """Validation error"""
    pass


class NotFoundError(BankingAPIException):
    """Resource not found error"""
    pass


class AccountNotFoundError(NotFoundError):
    """Account not found error"""
    pass


class TransactionNotFoundError(NotFoundError):
    """Transaction not found error"""
    pass
