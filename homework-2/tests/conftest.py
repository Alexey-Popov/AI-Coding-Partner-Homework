"""
pytest fixtures for Customer Support Ticket System tests
"""

import pytest
from fastapi.testclient import TestClient

from src.main import app
from src.models import (
    Ticket,
    TicketCreate,
    TicketMetadata,
    TicketCategory,
    TicketPriority,
    TicketStatus,
    TicketSource,
)
from src.services.ticket_service import ticket_service


@pytest.fixture
def client():
    """FastAPI test client"""
    return TestClient(app)


@pytest.fixture(autouse=True)
def clear_tickets():
    """Clear all tickets before each test"""
    ticket_service.clear_all()
    yield
    ticket_service.clear_all()


@pytest.fixture
def sample_ticket_data():
    """Sample ticket data for testing"""
    return {
        "customer_id": "CUST-0001",
        "customer_email": "customer@example.com",
        "customer_name": "Test Customer",
        "subject": "Cannot access my account after password reset",
        "description": "I tried to reset my password but now I cannot log in at all. The error message says my account is locked. This is critical for my work and I need immediate access.",
        "category": "account_access",
        "priority": "high",
        "tags": ["urgent", "login"],
        "metadata": {
            "source": "web_form",
            "browser": "Chrome",
            "device_type": "desktop"
        }
    }


@pytest.fixture
def sample_ticket_create(sample_ticket_data):
    """Sample TicketCreate instance"""
    metadata = TicketMetadata(**sample_ticket_data["metadata"])
    return TicketCreate(
        **{**sample_ticket_data, "metadata": metadata}
    )


@pytest.fixture
def sample_tickets_data():
    """Multiple sample tickets for bulk testing"""
    return [
        {
            "customer_id": "CUST-0001",
            "customer_email": "user1@example.com",
            "customer_name": "User One",
            "subject": "Login issues after update",
            "description": "Ever since the last update, I cannot log into my account. I've tried clearing cookies and cache but nothing works. This is urgent as I can't access my work.",
            "category": "account_access",
            "priority": "urgent",
            "tags": ["login", "urgent"],
            "metadata": {"source": "email", "browser": None, "device_type": None}
        },
        {
            "customer_id": "CUST-0002",
            "customer_email": "user2@example.com",
            "customer_name": "User Two",
            "subject": "Request for dark mode feature",
            "description": "It would be really nice if you could add a dark mode option to the application. Many users have been requesting this feature and it would help with eye strain.",
            "category": "feature_request",
            "priority": "low",
            "tags": ["enhancement", "ui"],
            "metadata": {"source": "web_form", "browser": "Firefox", "device_type": "desktop"}
        },
        {
            "customer_id": "CUST-0003",
            "customer_email": "user3@example.com",
            "customer_name": "User Three",
            "subject": "Billing discrepancy on invoice",
            "description": "I noticed that my latest invoice shows charges that don't match what I expected. Please review my account and clarify the charges as soon as possible.",
            "category": "billing_question",
            "priority": "medium",
            "tags": ["billing", "invoice"],
            "metadata": {"source": "chat", "browser": None, "device_type": "mobile"}
        },
    ]


@pytest.fixture
def invalid_ticket_data():
    """Invalid ticket data for negative testing"""
    return {
        "customer_id": "",  # Empty
        "customer_email": "invalid-email",  # Invalid format
        "customer_name": "",  # Empty
        "subject": "",  # Too short
        "description": "Short",  # Too short (< 10 chars)
        "category": "unknown_category",  # Invalid category
        "priority": "super_high",  # Invalid priority
        "tags": [],
        "metadata": {
            "source": "carrier_pigeon",  # Invalid source
        }
    }


@pytest.fixture
def csv_content():
    """Sample CSV content for import testing"""
    return """customer_id,customer_email,customer_name,subject,description,category,priority,tags,source,browser,device_type
CUST-0001,user1@example.com,User One,Cannot access my account,I've been locked out of my account after multiple failed login attempts. This is critical and needs immediate attention.,account_access,urgent,login;urgent,email,,
CUST-0002,user2@example.com,User Two,Request for PDF export feature,Would be great if you could add ability to export reports to PDF format. This would help with documentation and sharing.,feature_request,low,enhancement;pdf,web_form,Chrome,desktop
CUST-0003,user3@example.com,User Three,Unexpected charge on account,I noticed an unexpected charge of $50 on my latest statement. Please investigate and provide details as soon as possible.,billing_question,medium,billing;urgent,chat,,mobile"""


@pytest.fixture
def json_content():
    """Sample JSON content for import testing"""
    return """[
  {
    "customer_id": "CUST-0004",
    "customer_email": "user4@example.com",
    "customer_name": "User Four",
    "subject": "Application crashes on startup",
    "description": "The desktop application crashes immediately when I try to open it. Error message says unexpected error occurred. This started after update.",
    "category": "technical_issue",
    "priority": "high",
    "tags": ["crash", "bug"],
    "metadata": {
      "source": "api",
      "browser": null,
      "device_type": "desktop"
    }
  },
  {
    "customer_id": "CUST-0005",
    "customer_email": "user5@example.com",
    "customer_name": "User Five",
    "subject": "Need invoice for tax purposes",
    "description": "Please send me an official invoice for my recent purchases. I need this for my quarterly tax filing which is due next week.",
    "category": "billing_question",
    "priority": "medium",
    "tags": ["invoice", "tax"],
    "metadata": {
      "source": "email",
      "browser": null,
      "device_type": null
    }
  }
]"""


@pytest.fixture
def xml_content():
    """Sample XML content for import testing"""
    return """<?xml version="1.0" encoding="UTF-8"?>
<tickets>
  <ticket>
    <customer_id>CUST-0006</customer_id>
    <customer_email>user6@example.com</customer_email>
    <customer_name>User Six</customer_name>
    <subject>Button not responding on form</subject>
    <description>The submit button on the contact form doesn't work when clicked. I've tried multiple browsers with the same result and this is blocking.</description>
    <category>bug_report</category>
    <priority>high</priority>
    <tags>
      <tag>bug</tag>
      <tag>ui</tag>
    </tags>
    <metadata>
      <source>web_form</source>
      <browser>Safari</browser>
      <device_type>mobile</device_type>
    </metadata>
  </ticket>
  <ticket>
    <customer_id>CUST-0007</customer_id>
    <customer_email>user7@example.com</customer_email>
    <customer_name>User Seven</customer_name>
    <subject>General inquiry about services</subject>
    <description>I have some questions about your enterprise services and would like to learn more about pricing and features available for large teams.</description>
    <category>other</category>
    <priority>low</priority>
    <tags>
      <tag>enterprise</tag>
      <tag>inquiry</tag>
    </tags>
    <metadata>
      <source>phone</source>
    </metadata>
  </ticket>
</tickets>"""
