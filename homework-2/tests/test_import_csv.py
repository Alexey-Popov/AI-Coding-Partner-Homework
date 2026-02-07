"""
Test Import CSV - CSV parsing tests (6+ tests)

Tests CSV file import functionality:
- Valid CSV import
- Malformed CSV handling
- Missing fields
- Invalid data in rows
- Partial success scenarios
"""

import io
import pytest
from fastapi.testclient import TestClient

from src.main import app
from src.services.ticket_service import ticket_service


@pytest.fixture
def client():
    return TestClient(app)


@pytest.fixture(autouse=True)
def clear():
    ticket_service.clear_all()
    yield
    ticket_service.clear_all()


VALID_CSV = """customer_id,customer_email,customer_name,subject,description,category,priority,tags,source,browser,device_type
CUST-001,user1@example.com,User One,Cannot access account,I've been locked out of my account after multiple failed login attempts and need help.,account_access,urgent,login;urgent,email,,
CUST-002,user2@example.com,User Two,Feature request for export,Would be great to add ability to export reports to PDF for documentation purposes.,feature_request,low,enhancement,web_form,Chrome,desktop"""

INVALID_CSV = """customer_id,customer_email,customer_name,subject,description,category,priority,tags,source,browser,device_type
,bad-email,,,,invalid_cat,super,,carrier_pigeon,,"""

MIXED_CSV = """customer_id,customer_email,customer_name,subject,description,category,priority,tags,source,browser,device_type
CUST-001,user1@example.com,User One,Valid ticket subject,This is a valid description that should pass all validation checks.,account_access,urgent,login,email,,
,bad-email,,,,invalid_cat,super,,carrier_pigeon,,"""


def _upload_csv(client, content, filename="test.csv"):
    return client.post(
        "/import/csv",
        files={"file": (filename, io.BytesIO(content.encode()), "text/csv")},
    )


class TestCSVImport:
    def test_import_valid_csv(self, client):
        resp = _upload_csv(client, VALID_CSV)
        assert resp.status_code == 200
        data = resp.json()
        assert data["total"] == 2
        assert data["success_count"] == 2
        assert data["error_count"] == 0

    def test_import_csv_creates_tickets(self, client):
        _upload_csv(client, VALID_CSV)
        resp = client.get("/tickets")
        assert resp.json()["total"] == 2

    def test_import_csv_with_invalid_rows(self, client):
        resp = _upload_csv(client, INVALID_CSV)
        assert resp.status_code == 200
        data = resp.json()
        assert data["error_count"] > 0

    def test_import_csv_partial_success(self, client):
        resp = _upload_csv(client, MIXED_CSV)
        assert resp.status_code == 200
        data = resp.json()
        assert data["success_count"] == 1
        assert data["error_count"] == 1
        assert len(data["errors"]) == 1

    def test_import_csv_wrong_extension(self, client):
        resp = _upload_csv(client, VALID_CSV, filename="test.txt")
        assert resp.status_code == 400

    def test_import_empty_csv(self, client):
        resp = _upload_csv(client, "customer_id,customer_email\n")
        assert resp.status_code == 200
        data = resp.json()
        assert data["total"] == 0

    def test_import_csv_error_details(self, client):
        resp = _upload_csv(client, INVALID_CSV)
        data = resp.json()
        assert len(data["errors"]) > 0
        assert "row" in data["errors"][0]
        assert "errors" in data["errors"][0]
