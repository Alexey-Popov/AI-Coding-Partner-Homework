from .db import DatabaseConnection
from .logging import get_logger

logger = get_logger(__name__)

SCHEMA_SQL = """
-- Users table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    card_number VARCHAR(19) UNIQUE NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_card_number ON accounts(card_number);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(20) NOT NULL,
    source_account_id VARCHAR(36),
    target_account_id VARCHAR(36),
    source_amount DECIMAL(19, 4),
    source_currency VARCHAR(3),
    target_amount DECIMAL(19, 4),
    target_currency VARCHAR(3),
    fx_rate DECIMAL(19, 8),
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (source_account_id) REFERENCES accounts(id),
    FOREIGN KEY (target_account_id) REFERENCES accounts(id)
);

CREATE INDEX IF NOT EXISTS idx_transactions_source_account ON transactions(source_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_target_account ON transactions(target_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);

-- Idempotency keys table
CREATE TABLE IF NOT EXISTS idempotency_keys (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_status INTEGER NOT NULL,
    response_body TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_idempotency_user_key ON idempotency_keys(user_id, idempotency_key);
"""


def _add_rbac_columns() -> None:
    """Add role and status columns to users table if they don't exist."""
    try:
        with DatabaseConnection.transaction() as conn:
            # Check if columns exist by querying information schema
            check_query = """
                SELECT COUNT(*) as cnt
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = 'USERS' AND COLUMN_NAME = 'ROLE'
            """
            result = DatabaseConnection.execute_query(check_query, connection=conn)
            # execute_query returns tuples, not dicts
            column_exists = result[0][0] > 0 if result else False

            if not column_exists:
                logger.info("Adding role and status columns to users table...")

                # Add role column
                DatabaseConnection.execute_update(
                    "ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER'",
                    connection=conn
                )

                # Add status column
                DatabaseConnection.execute_update(
                    "ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'",
                    connection=conn
                )

                # Create indexes
                DatabaseConnection.execute_update(
                    "CREATE INDEX IF NOT EXISTS idx_users_role ON users(role)",
                    connection=conn
                )
                DatabaseConnection.execute_update(
                    "CREATE INDEX IF NOT EXISTS idx_users_status ON users(status)",
                    connection=conn
                )

                # Backfill existing users
                DatabaseConnection.execute_update(
                    "UPDATE users SET role = 'USER', status = 'ACTIVE' WHERE role IS NULL OR status IS NULL",
                    connection=conn
                )

                logger.info("RBAC columns added and backfilled successfully")
            else:
                logger.info("RBAC columns already exist, skipping migration")

    except Exception as e:
        logger.warning(f"RBAC migration warning (may be already applied): {e}")


def run_migrations() -> None:
    """Run database migrations (create tables if they don't exist)."""
    try:
        logger.info("Running database migrations...")

        # Split and execute each statement
        statements = [s.strip() for s in SCHEMA_SQL.split(';') if s.strip()]

        with DatabaseConnection.transaction() as conn:
            for statement in statements:
                if statement:
                    DatabaseConnection.execute_update(statement, connection=conn)

        logger.info("Database schema migrations completed successfully")

        # Run RBAC column migration
        _add_rbac_columns()

        logger.info("All migrations completed successfully")
    except Exception as e:
        logger.error(f"Migration failed: {e}")
        raise
