"""
Banking Transactions API - Python/FastAPI Implementation

A REST API for managing banking transactions with support for:
- Creating and retrieving transactions
- Account balance and summary endpoints
- Transaction filtering and CSV export
- Simple interest calculation
"""

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from .routes.transactions import router as transactions_router
from .routes.accounts import router as accounts_router

# Create FastAPI app
app = FastAPI(
    title="Banking Transactions API",
    description="A simple REST API for banking transactions built with FastAPI",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Include routers
app.include_router(transactions_router)
app.include_router(accounts_router)


@app.get("/", tags=["Health"])
async def root():
    """Root endpoint with API information."""
    return {
        "name": "Banking Transactions API",
        "version": "1.0.0",
        "framework": "FastAPI",
        "docs": "/docs",
        "endpoints": {
            "transactions": "/transactions",
            "accounts": "/accounts/{accountId}/balance",
            "health": "/health"
        }
    }


@app.get("/health", tags=["Health"])
async def health_check():
    """Health check endpoint."""
    return {"status": "healthy", "service": "banking-transactions-api"}


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    """Global exception handler for unhandled errors."""
    return JSONResponse(
        status_code=500,
        content={
            "error": "Internal server error",
            "message": str(exc)
        }
    )


# Entry point for running with uvicorn directly
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("src.main:app", host="0.0.0.0", port=3000, reload=True)
