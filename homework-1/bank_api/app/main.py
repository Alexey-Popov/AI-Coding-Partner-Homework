import os
import uuid
from datetime import datetime
from pathlib import Path
from fastapi import FastAPI, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
from contextlib import asynccontextmanager
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded

from .infrastructure.settings import settings
from .infrastructure.logging import setup_logging, get_logger, request_id_ctx
from .infrastructure.migrations import run_migrations
from .infrastructure.db import DatabaseConnection
from .api.v1 import auth, accounts, transfers, admin
from .api.schemas import HealthResponse, ErrorResponse, ErrorDetail
from .repositories.user_repo import UserRepository
from .domain.models import UserRole, UserStatus
from .infrastructure.security import hash_password


def seed_admin_user():
    """Seed initial admin user if ADMIN_EMAIL and ADMIN_PASSWORD env vars are set."""
    logger = get_logger(__name__)

    admin_email = settings.ADMIN_EMAIL
    admin_password = settings.ADMIN_PASSWORD

    if not admin_email or not admin_password:
        logger.info("No admin seeding: ADMIN_EMAIL or ADMIN_PASSWORD not set")
        return

    try:
        user_repo = UserRepository()

        # Check if admin already exists
        existing_admin = user_repo.find_by_email(admin_email)
        if existing_admin:
            logger.info(f"Admin user already exists: {admin_email}")
            return

        # Create admin user
        password_hash = hash_password(admin_password)
        admin_user = user_repo.create(
            email=admin_email,
            password_hash=password_hash,
            full_name="Admin User",
            role=UserRole.ADMIN,
            status=UserStatus.ACTIVE
        )

        logger.info(f"Admin user created successfully: {admin_user.id} ({admin_email})")

    except Exception as e:
        logger.error(f"Failed to seed admin user: {e}")
        # Don't raise - allow app to start even if admin seeding fails


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan events."""
    # Startup
    logger = get_logger(__name__)
    logger.info("Starting application...")

    # Setup logging
    setup_logging(settings.LOG_LEVEL)

    # Initialize database and run migrations
    try:
        DatabaseConnection.initialize()
        run_migrations()
        logger.info("Database initialized and migrations completed")

        # Seed admin user if env vars are set
        seed_admin_user()

    except Exception as e:
        logger.error(f"Failed to initialize database: {e}")
        raise

    yield

    # Shutdown
    logger.info("Shutting down application...")


app = FastAPI(
    title="Banking Transactions API",
    description="Production-ready Banking Transactions API with clean architecture",
    version="1.0.0",
    lifespan=lifespan
)

# Rate limiting setup
limiter = Limiter(key_func=get_remote_address)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# CORS middleware
origins = settings.CORS_ORIGINS.split(",")
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Request ID middleware
@app.middleware("http")
async def add_request_id(request: Request, call_next):
    """Add request ID to all requests."""
    request_id = request.headers.get("X-Request-ID") or str(uuid.uuid4())
    request_id_ctx.set(request_id)

    response = await call_next(request)
    response.headers["X-Request-ID"] = request_id

    return response


# Security headers middleware
@app.middleware("http")
async def add_security_headers(request: Request, call_next):
    """Add security headers to all responses."""
    response = await call_next(request)

    # Skip CSP for Swagger UI endpoints
    if request.url.path not in ["/docs", "/openapi.json", "/redoc"]:
        # Content Security Policy
        response.headers["Content-Security-Policy"] = (
            "default-src 'self'; "
            "script-src 'self'; "
            "style-src 'self' 'unsafe-inline'; "
            "img-src 'self' data:; "
            "font-src 'self'; "
            "connect-src 'self'; "
            "frame-ancestors 'none';"
        )

    # Security headers
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["X-XSS-Protection"] = "1; mode=block"
    response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"

    return response


# Exception handlers
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    """Global exception handler."""
    logger = get_logger(__name__)
    logger.error(f"Unhandled exception: {exc}", exc_info=True)

    error_response = ErrorResponse(
        error=ErrorDetail(
            code="INTERNAL_SERVER_ERROR",
            message="An unexpected error occurred"
        ),
        request_id=request_id_ctx.get('')
    )

    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content=error_response.dict()
    )


# Include routers
app.include_router(auth.router, prefix="/v1")
app.include_router(accounts.router, prefix="/v1")
app.include_router(transfers.router, prefix="/v1")
app.include_router(admin.router, prefix="/v1")


@app.get("/health", response_model=HealthResponse, tags=["health"])
async def health_check():
    """
    Health check endpoint.

    Verifies API and database connectivity.
    """
    db_status = "healthy" if DatabaseConnection.check_health() else "unhealthy"

    return HealthResponse(
        status="healthy" if db_status == "healthy" else "degraded",
        database=db_status,
        timestamp=datetime.utcnow()
    )


# Mount static files for frontend (if built UI exists)
ui_dist_path = Path(__file__).parent.parent / "ui" / "dist"
if ui_dist_path.exists() and ui_dist_path.is_dir():
    logger = get_logger(__name__)
    logger.info(f"Mounting UI static files from {ui_dist_path}")
    
    # Mount static assets (JS, CSS, images, etc.)
    app.mount("/assets", StaticFiles(directory=str(ui_dist_path / "assets")), name="assets")
    
    # Serve index.html for all non-API routes (SPA routing)
    from fastapi.responses import FileResponse
    
    @app.get("/{full_path:path}")
    async def serve_spa(full_path: str = ""):
        """Serve the React SPA for all routes not caught by API endpoints."""
        # Don't serve SPA for API routes, health, docs
        if full_path.startswith(("v1/", "health", "docs", "openapi.json", "redoc")):
            return JSONResponse(
                status_code=404,
                content={"detail": "Not found"}
            )
        
        # Serve index.html for all other routes (let React Router handle routing)
        index_file = ui_dist_path / "index.html"
        if index_file.exists():
            return FileResponse(index_file)
        
        return JSONResponse(
            status_code=404,
            content={"detail": "Frontend not found"}
        )
else:
    logger = get_logger(__name__)
    logger.warning("UI dist directory not found - frontend will not be served")
    
    # Fallback root endpoint when no UI is available
    @app.get("/", tags=["root"])
    async def root():
        """Root endpoint."""
        return {
            "message": "Banking Transactions API",
            "version": "1.0.0",
            "docs": "/docs"
        }
