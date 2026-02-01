"""
Banking Transactions API - Main Application.

A RESTful API for managing banking transactions with features:
- Transaction creation (deposits, withdrawals, transfers)
- Transaction retrieval with filtering
- Balance calculation
- Account summary generation
- Comprehensive validation (amount, account format, currency codes)

Built with FastAPI and Pydantic for automatic validation and documentation.
"""

from fastapi import FastAPI, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from pydantic import ValidationError
from typing import List, Dict
import uvicorn

from routes.transactions import router as transactions_router

# Create FastAPI application
app = FastAPI(
    title="Banking Transactions API",
    description="""
    A comprehensive RESTful API for managing banking transactions.

    ## Features

    * **Transaction Management**: Create and retrieve transactions (deposits, withdrawals, transfers)
    * **Advanced Filtering**: Filter transactions by account, type, and date range
    * **Balance Calculation**: Calculate current account balance
    * **Account Summary**: Generate comprehensive account statistics
    * **Automatic Validation**: Built-in validation for amounts, accounts, and currency codes

    ## Implementation Details

    - **Framework**: FastAPI with Python 3.8+
    - **Validation**: Pydantic models with custom validators
    - **Storage**: In-memory storage (suitable for demo/testing)
    - **Documentation**: Automatic OpenAPI (Swagger) documentation

    ## Validation Rules

    - **Amount**: Must be positive with maximum 2 decimal places
    - **Account Format**: Must match pattern ACC-XXXXX (5 alphanumeric characters)
    - **Currency**: Must be valid ISO 4217 currency code (USD, EUR, GBP, etc.)
    - **Transaction Types**: deposit, withdrawal, transfer

    ## Getting Started

    1. Visit `/docs` for interactive API documentation (Swagger UI)
    2. Visit `/redoc` for alternative documentation (ReDoc)
    3. Use the API endpoints to create and manage transactions

    ## AI Tools Used

    This project was developed with assistance from:
    - **GitHub Copilot**: Code completion and suggestions
    - **Claude Code**: Architecture design and implementation planning
    """,
    version="1.0.0",
    contact={
        "name": "Banking API Support",
        "email": "support@example.com",
    },
    license_info={
        "name": "MIT",
    },
)

# Configure CORS (Cross-Origin Resource Sharing)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify actual origins
    allow_credentials=True,
    allow_methods=["*"],  # Allow all HTTP methods
    allow_headers=["*"],  # Allow all headers
)


# Exception Handlers

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """
    Custom handler for Pydantic validation errors.

    Returns a structured JSON response with detailed error information.
    """
    errors = []
    for error in exc.errors():
        field = ".".join(str(loc) for loc in error["loc"] if loc != "body")
        errors.append({
            "field": field,
            "message": error["msg"],
            "type": error["type"]
        })

    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "error": "Validation failed",
            "details": errors
        }
    )


@app.exception_handler(ValidationError)
async def pydantic_validation_exception_handler(request: Request, exc: ValidationError):
    """
    Custom handler for Pydantic model validation errors.

    Returns a structured JSON response with detailed error information.
    """
    errors = []
    for error in exc.errors():
        field = ".".join(str(loc) for loc in error["loc"])
        errors.append({
            "field": field,
            "message": error["msg"],
            "type": error["type"]
        })

    return JSONResponse(
        status_code=status.HTTP_400_BAD_REQUEST,
        content={
            "error": "Validation failed",
            "details": errors
        }
    )


@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    """
    Global exception handler for unexpected errors.

    Returns a generic error message to avoid exposing internal details.
    """
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "error": "Internal server error",
            "message": str(exc)
        }
    )


# Root Endpoints

@app.get(
    "/",
    tags=["root"],
    summary="API Information",
    response_description="Basic API information and available endpoints"
)
async def root() -> Dict:
    """
    Get API information and available endpoints.

    Returns:
        JSON object with API metadata and links to documentation
    """
    return {
        "name": "Banking Transactions API",
        "version": "1.0.0",
        "description": "RESTful API for managing banking transactions",
        "documentation": {
            "swagger": "/docs",
            "redoc": "/redoc",
            "openapi": "/openapi.json"
        },
        "endpoints": {
            "transactions": "/api/transactions",
            "balance": "/api/accounts/{accountId}/balance",
            "summary": "/api/accounts/{accountId}/summary"
        },
        "features": [
            "Transaction creation and retrieval",
            "Advanced filtering (account, type, date range)",
            "Balance calculation",
            "Account summary generation",
            "Comprehensive validation"
        ],
        "status": "operational"
    }


@app.get(
    "/health",
    tags=["root"],
    summary="Health Check",
    response_description="API health status"
)
async def health_check() -> Dict:
    """
    Health check endpoint for monitoring and load balancers.

    Returns:
        JSON object with health status
    """
    return {
        "status": "healthy",
        "service": "banking-transactions-api",
        "version": "1.0.0"
    }


# Include routers
app.include_router(transactions_router)


# Run application (for development)
if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,  # Auto-reload on code changes
        log_level="info"
    )
