"""Custom exceptions and exception handlers for the banking API"""
from fastapi import Request, status
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError


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


async def validation_exception_handler(request: Request, exc: RequestValidationError) -> JSONResponse:
    """
    Custom exception handler for validation errors.

    Transforms Pydantic validation errors into the Task 2 required format:
    {
        "error": "Validation failed",
        "details": [
            {"field": "amount", "message": "Amount must be a positive number"},
            {"field": "currency", "message": "Invalid currency code"}
        ]
    }
    """
    details = []

    for error in exc.errors():
        # Extract field name from location tuple
        # Location is typically ('body', 'fieldName') for request body fields
        loc = error.get("loc", [])
        field_name = loc[-1] if loc else "unknown"

        # Skip 'body' from field name if it's there
        if field_name == "body":
            field_name = loc[-2] if len(loc) > 1 else "unknown"

        # Get the error message
        msg = error.get("msg", "Invalid value")

        # Clean up Pydantic v2 message format
        # Pydantic v2 prefixes with "Value error, " for custom validators
        if msg.startswith("Value error, "):
            msg = msg[13:]  # Remove "Value error, " prefix

        details.append({
            "field": str(field_name),
            "message": msg
        })

    return JSONResponse(
        status_code=status.HTTP_400_BAD_REQUEST,
        content={
            "error": "Validation failed",
            "details": details
        }
    )
