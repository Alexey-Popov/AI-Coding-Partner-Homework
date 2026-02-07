"""
FastAPI Application Entry Point

Customer Support Ticket System
- REST API for ticket management
- Multi-format bulk import (CSV, JSON, XML)
- Auto-categorization based on content analysis
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from .routes import tickets, import_routes, classification_routes

app = FastAPI(
    title="Customer Support Ticket System",
    description="REST API for managing customer support tickets with bulk import and auto-classification",
    version="1.0.0",
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
async def root():
    """Health check endpoint"""
    return {"status": "healthy", "service": "Customer Support Ticket System"}


@app.get("/health")
async def health_check():
    """Detailed health check"""
    return {
        "status": "healthy",
        "version": "1.0.0",
        "features": {
            "crud": True,
            "import_csv": True,
            "import_json": True,
            "import_xml": True,
            "auto_classification": True,
        }
    }


# Include routers
app.include_router(tickets.router, prefix="/tickets", tags=["Tickets"])
app.include_router(import_routes.router, prefix="/import", tags=["Import"])
app.include_router(classification_routes.router, prefix="/tickets", tags=["Classification"])
