from pydantic_settings import BaseSettings
from pydantic import field_validator
from typing import Optional


class Settings(BaseSettings):
    """Application configuration."""

    # JWT settings
    JWT_SECRET: str  # No default - must be explicitly set
    JWT_ALG: str = "HS256"
    JWT_TTL: int = 900  # 15 minutes in seconds

    @field_validator('JWT_SECRET')
    @classmethod
    def validate_jwt_secret(cls, v: str) -> str:
        """Validate JWT secret strength."""
        if not v or len(v) < 32:
            raise ValueError("JWT_SECRET must be at least 32 characters for security")

        # Check for dangerous default values
        dangerous_defaults = [
            "your-secret-key-change-in-production",
            "secret",
            "password",
            "changeme",
            "test",
            "dev",
            "development"
        ]
        if v.lower() in dangerous_defaults:
            raise ValueError(
                f"JWT_SECRET cannot use default/common values. "
                f"Please set a strong secret in .env file."
            )

        return v

    # H2 Database settings
    H2_JAR_PATH: str = "./h2.jar"
    DB_URL: str = "jdbc:h2:file:./bank.db;AUTO_SERVER=TRUE;MODE=PostgreSQL"
    DB_USER: str = "sa"
    DB_PASSWORD: str = ""
    DB_DRIVER: str = "org.h2.Driver"

    # CORS
    CORS_ORIGINS: str = "http://localhost:5173,http://localhost:3000"

    # Logging
    LOG_LEVEL: str = "INFO"

    # Admin seeding (optional)
    ADMIN_EMAIL: Optional[str] = None
    ADMIN_PASSWORD: Optional[str] = None

    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()
