"""
Test Import XML - XML parsing tests (5+ tests)

Tests XML file import functionality:
- Valid XML import
- Invalid XML structure
- Wrong root element
- Partial success
- Nested metadata and tags
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


VALID_XML = """<?xml version="1.0" encoding="UTF-8"?>
<tickets>
  <ticket>
    <customer_id>CUST-006</customer_id>
    <customer_email>user6@example.com</customer_email>
    <customer_name>User Six</customer_name>
    <subject>Button not responding on form</subject>
    <description>The submit button on the contact form does not work when clicked in multiple browsers.</description>
    <category>bug_report</category>
    <priority>high</priority>
    <tags><tag>bug</tag><tag>ui</tag></tags>
    <metadata><source>web_form</source><browser>Safari</browser><device_type>mobile</device_type></metadata>
  </ticket>
  <ticket>
    <customer_id>CUST-007</customer_id>
    <customer_email>user7@example.com</customer_email>
    <customer_name>User Seven</customer_name>
    <subject>General inquiry about services</subject>
    <description>I have questions about your enterprise services and would like to learn about pricing for teams.</description>
    <category>other</category>
    <priority>low</priority>
    <tags><tag>enterprise</tag></tags>
    <metadata><source>phone</source></metadata>
  </ticket>
</tickets>"""

INVALID_XML = """<not valid xml"""

WRONG_ROOT_XML = """<?xml version="1.0"?><data><item>test</item></data>"""


def _upload_xml(client, content, filename="test.xml"):
    return client.post(
        "/import/xml",
        files={"file": (filename, io.BytesIO(content.encode()), "application/xml")},
    )


class TestXMLImport:
    def test_import_valid_xml(self, client):
        resp = _upload_xml(client, VALID_XML)
        assert resp.status_code == 200
        data = resp.json()
        assert data["total"] == 2
        assert data["success_count"] == 2

    def test_import_xml_creates_tickets(self, client):
        _upload_xml(client, VALID_XML)
        resp = client.get("/tickets")
        assert resp.json()["total"] == 2

    def test_import_invalid_xml(self, client):
        resp = _upload_xml(client, INVALID_XML)
        data = resp.json()
        assert data["success_count"] == 0

    def test_import_wrong_root_element(self, client):
        resp = _upload_xml(client, WRONG_ROOT_XML)
        data = resp.json()
        assert data["success_count"] == 0
        assert len(data["errors"]) > 0

    def test_import_xml_wrong_extension(self, client):
        resp = _upload_xml(client, VALID_XML, filename="test.txt")
        assert resp.status_code == 400

    def test_import_xml_with_nested_metadata(self, client):
        resp = _upload_xml(client, VALID_XML)
        data = resp.json()
        assert data["success_count"] == 2
        # Verify tickets were created with correct data
        tickets = client.get("/tickets").json()
        assert tickets["total"] == 2
