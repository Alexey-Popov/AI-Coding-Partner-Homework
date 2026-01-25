"""Main FastAPI application for Banking Transactions API"""
from fastapi import FastAPI, status
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from datetime import datetime

# Import routes
from src.routes import transactions as transactions_routes
from src.routes import accounts as accounts_routes

# Import custom exception handler
from src.utils.exceptions import validation_exception_handler

# Initialize FastAPI app
app = FastAPI(
    title="Banking Transactions API",
    description="A simple REST API for banking transactions",
    version="1.0.0",
)

# Register custom validation exception handler (Task 2)
app.add_exception_handler(RequestValidationError, validation_exception_handler)

# Include routers
app.include_router(transactions_routes.router)
app.include_router(accounts_routes.router)


@app.get("/", tags=["health"])
def root():
    """Health check endpoint"""
    return {
        "message": "Banking Transactions API is running",
        "version": "1.0.0",
        "timestamp": datetime.utcnow().isoformat()
    }


@app.get("/health", tags=["health"])
def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "timestamp": datetime.utcnow().isoformat()
    }


# Exception handlers
@app.exception_handler(Exception)
async def general_exception_handler(request, exc):
    """Handle general exceptions"""
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "error": "Internal server error",
            "details": str(exc)
        }
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
