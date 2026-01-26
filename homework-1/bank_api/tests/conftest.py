import pytest
import os
import sys
import glob

# Add app directory to path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))


@pytest.fixture(scope="session", autouse=True)
def setup_test_environment_session():
    """Setup test environment once per test session."""
    # Set a test JWT secret
    os.environ["JWT_SECRET"] = "test-secret-key-for-testing-at-least-32-characters-long"

    # Disable rate limiting for tests
    os.environ["TESTING"] = "true"

    yield

    # Cleanup
    if "TESTING" in os.environ:
        del os.environ["TESTING"]


@pytest.fixture(scope="function", autouse=True)
def clean_test_database():
    """Clean test database before each test."""
    import time
    import random

    # Use a file-based H2 database with a unique name for each test
    # This ensures complete isolation between tests
    random_id = f"{int(time.time())}_{random.randint(1000, 9999)}"
    db_path = f"./test_bank_{random_id}"
    os.environ["DB_URL"] = f"jdbc:h2:file:{db_path}"

    yield

    # Cleanup database files after test
    for db_file in glob.glob(f"{db_path}*"):
        try:
            os.remove(db_file)
        except OSError:
            pass


@pytest.fixture(scope="function")
def client():
    """Provide a fresh TestClient for each test with isolated database."""
    from fastapi.testclient import TestClient

    # Import app after environment variables are set
    from app.main import app

    # Create a fresh client for this test
    with TestClient(app) as test_client:
        yield test_client
