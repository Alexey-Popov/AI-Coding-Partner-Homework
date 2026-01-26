import jaydebeapi
import jpype
from typing import Optional, Any, List, Tuple
from contextlib import contextmanager
from .settings import settings
from .logging import get_logger

logger = get_logger(__name__)


class DatabaseConnection:
    """Manages H2 database connections via JDBC."""

    _connection: Optional[Any] = None
    _jvm_started: bool = False

    @classmethod
    def initialize(cls) -> None:
        """Initialize JVM and start H2 database connection."""
        if not cls._jvm_started:
            try:
                # Start JVM if not already started
                if not jpype.isJVMStarted():
                    jpype.startJVM(
                        jpype.getDefaultJVMPath(),
                        f"-Djava.class.path={settings.H2_JAR_PATH}",
                        "-Xmx512m"
                    )
                cls._jvm_started = True
                logger.info("JVM started successfully")
            except Exception as e:
                logger.error(f"Failed to start JVM: {e}")
                raise

    @classmethod
    def get_connection(cls) -> Any:
        """Get or create a database connection."""
        cls.initialize()

        try:
            connection = jaydebeapi.connect(
                settings.DB_DRIVER,
                settings.DB_URL,
                [settings.DB_USER, settings.DB_PASSWORD],
                settings.H2_JAR_PATH
            )
            logger.info("Database connection established")
            return connection
        except Exception as e:
            logger.error(f"Failed to connect to database: {e}")
            raise

    @classmethod
    @contextmanager
    def get_cursor(cls, connection: Optional[Any] = None):
        """Context manager for database cursor."""
        own_connection = connection is None
        conn = connection or cls.get_connection()
        cursor = conn.cursor()

        try:
            yield cursor
            if own_connection:
                conn.commit()
        except Exception as e:
            if own_connection:
                conn.rollback()
            raise
        finally:
            cursor.close()
            if own_connection:
                conn.close()

    @classmethod
    @contextmanager
    def transaction(cls):
        """Context manager for database transactions."""
        conn = cls.get_connection()
        conn.jconn.setAutoCommit(False)

        try:
            yield conn
            conn.commit()
        except Exception as e:
            conn.rollback()
            logger.error(f"Transaction rolled back due to: {e}")
            raise
        finally:
            conn.close()

    @classmethod
    def execute_query(cls, query: str, params: Optional[Tuple] = None, connection: Optional[Any] = None) -> List[Tuple]:
        """Execute a SELECT query and return results."""
        with cls.get_cursor(connection) as cursor:
            if params:
                cursor.execute(query, params)
            else:
                cursor.execute(query)
            return cursor.fetchall()

    @classmethod
    def execute_update(cls, query: str, params: Optional[Tuple] = None, connection: Optional[Any] = None) -> int:
        """Execute an INSERT/UPDATE/DELETE query and return affected rows."""
        with cls.get_cursor(connection) as cursor:
            if params:
                cursor.execute(query, params)
            else:
                cursor.execute(query)
            return cursor.rowcount

    @classmethod
    def check_health(cls) -> bool:
        """Check database connectivity."""
        try:
            cls.execute_query("SELECT 1")
            return True
        except Exception as e:
            logger.error(f"Health check failed: {e}")
            return False
