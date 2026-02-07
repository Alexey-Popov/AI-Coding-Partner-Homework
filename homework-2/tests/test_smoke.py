"""
Basic smoke tests to verify API functionality

Tests basic functionality of all endpoints:
- Health check
- Create ticket
- List tickets
- Get ticket
- Update ticket
- Delete ticket
- Import (basic)
- Classification (basic)
"""

import pytest
from uuid import UUID


def test_health_check(client):
    """Test health check endpoint"""
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert data["version"] == "1.0.0"


def test_root_endpoint(client):
    """Test root endpoint"""
    response = client.get("/")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"


def test_create_ticket(client, sample_ticket_data):
    """Test creating a ticket"""
    response = client.post("/tickets", json=sample_ticket_data)
    assert response.status_code == 201
    data = response.json()
    assert "id" in data
    assert data["subject"] == sample_ticket_data["subject"]
    assert data["category"] == sample_ticket_data["category"]
    assert data["status"] == "new"


def test_list_tickets_empty(client):
    """Test listing tickets when none exist"""
    response = client.get("/tickets")
    assert response.status_code == 200
    data = response.json()
    assert data["total"] == 0
    assert data["items"] == []


def test_list_tickets_with_data(client, sample_ticket_data):
    """Test listing tickets after creating some"""
    # Create a ticket
    client.post("/tickets", json=sample_ticket_data)
    
    # List tickets
    response = client.get("/tickets")
    assert response.status_code == 200
    data = response.json()
    assert data["total"] == 1
    assert len(data["items"]) == 1


def test_get_ticket(client, sample_ticket_data):
    """Test getting a ticket by ID"""
    # Create a ticket
    create_response = client.post("/tickets", json=sample_ticket_data)
    ticket_id = create_response.json()["id"]
    
    # Get the ticket
    response = client.get(f"/tickets/{ticket_id}")
    assert response.status_code == 200
    data = response.json()
    assert data["id"] == ticket_id
    assert data["subject"] == sample_ticket_data["subject"]


def test_get_nonexistent_ticket(client):
    """Test getting a ticket that doesn't exist"""
    fake_uuid = "00000000-0000-0000-0000-000000000000"
    response = client.get(f"/tickets/{fake_uuid}")
    assert response.status_code == 404


def test_update_ticket(client, sample_ticket_data):
    """Test updating a ticket"""
    # Create a ticket
    create_response = client.post("/tickets", json=sample_ticket_data)
    ticket_id = create_response.json()["id"]
    
    # Update the ticket
    update_data = {
        "status": "in_progress",
        "priority": "urgent"
    }
    response = client.patch(f"/tickets/{ticket_id}", json=update_data)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "in_progress"
    assert data["priority"] == "urgent"


def test_delete_ticket(client, sample_ticket_data):
    """Test deleting a ticket"""
    # Create a ticket
    create_response = client.post("/tickets", json=sample_ticket_data)
    ticket_id = create_response.json()["id"]
    
    # Delete the ticket
    response = client.delete(f"/tickets/{ticket_id}")
    assert response.status_code == 204
    
    # Verify it's gone
    get_response = client.get(f"/tickets/{ticket_id}")
    assert get_response.status_code == 404


def test_filter_by_category(client, sample_tickets_data):
    """Test filtering tickets by category"""
    # Create multiple tickets
    for ticket_data in sample_tickets_data:
        client.post("/tickets", json=ticket_data)
    
    # Filter by account_access
    response = client.get("/tickets?category=account_access")
    assert response.status_code == 200
    data = response.json()
    assert data["total"] == 1
    assert data["items"][0]["category"] == "account_access"


def test_get_statistics(client, sample_tickets_data):
    """Test getting ticket statistics"""
    # Create multiple tickets
    for ticket_data in sample_tickets_data:
        client.post("/tickets", json=ticket_data)
    
    # Get statistics
    response = client.get("/tickets/stats")
    assert response.status_code == 200
    data = response.json()
    assert data["total"] == 3
    assert "by_category" in data
    assert "by_priority" in data
    assert "by_status" in data


def test_classification(client, sample_ticket_data):
    """Test ticket classification"""
    # Create a ticket
    create_response = client.post("/tickets", json=sample_ticket_data)
    ticket_id = create_response.json()["id"]
    
    # Classify the ticket
    response = client.post(f"/tickets/{ticket_id}/classify")
    assert response.status_code == 200
    data = response.json()
    assert "suggested_category" in data
    assert "suggested_priority" in data
    assert "confidence" in data
    assert 0.0 <= data["confidence"] <= 1.0


def test_classify_all(client, sample_tickets_data):
    """Test classifying all tickets"""
    # Create multiple tickets
    for ticket_data in sample_tickets_data:
        client.post("/tickets", json=ticket_data)
    
    # Classify all
    response = client.post("/tickets/classify-all")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    assert len(data) == 3


def test_invalid_ticket_data(client):
    """Test creating ticket with invalid data"""
    invalid_data = {
        "customer_id": "",
        "customer_email": "not-an-email",
        "customer_name": "",
        "subject": "",
        "description": "x",
        "category": "invalid",
        "priority": "super_high",
        "tags": [],
        "metadata": {"source": "invalid"}
    }
    response = client.post("/tickets", json=invalid_data)
    assert response.status_code == 422  # Unprocessable Entity


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
