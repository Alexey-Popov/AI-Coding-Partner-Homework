"""
Integration tests for Customer Support Ticket System

Tests complete end-to-end workflows:
1. Complete CRUD lifecycle (create → read → update → delete)
2. Bulk import and filtering workflow
3. Classification integration with ticket creation
4. Multi-step ticket status workflow
5. Edge cases and error scenarios
"""

import pytest
import io
from uuid import UUID


class TestTicketLifecycle:
    """Test complete ticket CRUD lifecycle"""

    def test_complete_ticket_lifecycle_success(self, client, sample_ticket_data):
        """Test full lifecycle: create → read → update → delete → verify deletion"""
        # Arrange - sample data provided by fixture

        # Act - Create
        create_response = client.post("/tickets", json=sample_ticket_data)
        assert create_response.status_code == 201
        created = create_response.json()
        ticket_id = created["id"]

        # Assert - Creation successful
        assert UUID(ticket_id)  # Valid UUID
        assert created["subject"] == sample_ticket_data["subject"]
        assert created["status"] == "new"
        assert created["category"] == sample_ticket_data["category"]

        # Act - Read
        get_response = client.get(f"/tickets/{ticket_id}")
        assert get_response.status_code == 200
        retrieved = get_response.json()

        # Assert - Retrieved matches created
        assert retrieved["id"] == ticket_id
        assert retrieved["subject"] == sample_ticket_data["subject"]
        assert retrieved["customer_email"] == sample_ticket_data["customer_email"]

        # Act - Update
        update_data = {
            "status": "in_progress",
            "priority": "urgent",
            "assigned_to": "agent@support.com"
        }
        update_response = client.patch(f"/tickets/{ticket_id}", json=update_data)
        assert update_response.status_code == 200
        updated = update_response.json()

        # Assert - Update successful
        assert updated["status"] == "in_progress"
        assert updated["priority"] == "urgent"
        assert updated["assigned_to"] == "agent@support.com"
        assert updated["subject"] == sample_ticket_data["subject"]  # Unchanged fields preserved

        # Act - Delete
        delete_response = client.delete(f"/tickets/{ticket_id}")
        assert delete_response.status_code == 204

        # Assert - Verify deletion
        get_deleted_response = client.get(f"/tickets/{ticket_id}")
        assert get_deleted_response.status_code == 404


    def test_ticket_status_workflow(self, client, sample_ticket_data):
        """Test ticket progressing through status workflow: new → in_progress → waiting_customer → resolved → closed"""
        # Arrange & Act - Create ticket
        create_response = client.post("/tickets", json=sample_ticket_data)
        ticket_id = create_response.json()["id"]

        # Assert - Starts as 'new'
        ticket = client.get(f"/tickets/{ticket_id}").json()
        assert ticket["status"] == "new"
        assert ticket["resolved_at"] is None

        # Act - Move to in_progress
        client.patch(f"/tickets/{ticket_id}", json={"status": "in_progress"})
        ticket = client.get(f"/tickets/{ticket_id}").json()
        assert ticket["status"] == "in_progress"

        # Act - Move to waiting_customer
        client.patch(f"/tickets/{ticket_id}", json={"status": "waiting_customer"})
        ticket = client.get(f"/tickets/{ticket_id}").json()
        assert ticket["status"] == "waiting_customer"

        # Act - Move to resolved
        client.patch(f"/tickets/{ticket_id}", json={"status": "resolved"})
        ticket = client.get(f"/tickets/{ticket_id}").json()
        assert ticket["status"] == "resolved"
        assert ticket["resolved_at"] is not None  # Timestamp set

        # Act - Move to closed
        client.patch(f"/tickets/{ticket_id}", json={"status": "closed"})
        ticket = client.get(f"/tickets/{ticket_id}").json()
        assert ticket["status"] == "closed"


class TestBulkImportAndFiltering:
    """Test bulk import workflows and filtering"""

    def test_bulk_import_csv_and_filter_workflow(self, client, csv_content):
        """Test CSV import creates all tickets and filtering works correctly"""
        # Arrange
        csv_file = io.BytesIO(csv_content.encode('utf-8'))
        csv_file.name = "test_tickets.csv"

        # Act - Import CSV
        import_response = client.post(
            "/import/csv",
            files={"file": ("test_tickets.csv", csv_file, "text/csv")}
        )

        # Assert - Import successful
        assert import_response.status_code == 200
        import_result = import_response.json()
        assert import_result["total"] == 3
        assert import_result["success_count"] == 3
        assert import_result["error_count"] == 0

        # Act - List all tickets
        list_response = client.get("/tickets")
        assert list_response.status_code == 200
        all_tickets = list_response.json()
        assert all_tickets["total"] == 3

        # Act - Filter by category
        filter_response = client.get("/tickets?category=account_access")
        assert filter_response.status_code == 200
        filtered = filter_response.json()
        assert filtered["total"] == 1
        assert filtered["items"][0]["category"] == "account_access"

        # Act - Filter by priority
        filter_response = client.get("/tickets?priority=urgent")
        assert filter_response.status_code == 200
        filtered = filter_response.json()
        assert filtered["total"] == 1
        assert filtered["items"][0]["priority"] == "urgent"

        # Act - Update 2 tickets to in_progress
        tickets = all_tickets["items"]
        client.patch(f"/tickets/{tickets[0]['id']}", json={"status": "in_progress"})
        client.patch(f"/tickets/{tickets[1]['id']}", json={"status": "in_progress"})

        # Assert - Filter by status shows correct counts
        new_tickets = client.get("/tickets?status=new").json()
        assert new_tickets["total"] == 1

        in_progress_tickets = client.get("/tickets?status=in_progress").json()
        assert in_progress_tickets["total"] == 2


    def test_bulk_import_json_creates_all_tickets(self, client, json_content):
        """Test JSON import creates all tickets successfully"""
        # Arrange
        json_file = io.BytesIO(json_content.encode('utf-8'))
        json_file.name = "test_tickets.json"

        # Act - Import JSON
        import_response = client.post(
            "/import/json",
            files={"file": ("test_tickets.json", json_file, "application/json")}
        )

        # Assert - Import successful
        assert import_response.status_code == 200
        import_result = import_response.json()
        assert import_result["total"] == 2
        assert import_result["success_count"] == 2
        assert import_result["error_count"] == 0

        # Assert - All tickets created
        list_response = client.get("/tickets")
        assert list_response.status_code == 200
        all_tickets = list_response.json()
        assert all_tickets["total"] == 2

        # Verify specific ticket details
        tickets_by_category = {}
        for ticket in all_tickets["items"]:
            tickets_by_category[ticket["category"]] = ticket

        assert "technical_issue" in tickets_by_category
        assert "billing_question" in tickets_by_category


    def test_bulk_import_xml_creates_all_tickets(self, client, xml_content):
        """Test XML import creates all tickets successfully"""
        # Arrange
        xml_file = io.BytesIO(xml_content.encode('utf-8'))
        xml_file.name = "test_tickets.xml"

        # Act - Import XML
        import_response = client.post(
            "/import/xml",
            files={"file": ("test_tickets.xml", xml_file, "application/xml")}
        )

        # Assert - Import successful
        assert import_response.status_code == 200
        import_result = import_response.json()
        assert import_result["total"] == 2
        assert import_result["success_count"] == 2
        assert import_result["error_count"] == 0

        # Assert - All tickets created
        list_response = client.get("/tickets")
        assert list_response.status_code == 200
        all_tickets = list_response.json()
        assert all_tickets["total"] == 2

        # Verify tags were parsed correctly
        for ticket in all_tickets["items"]:
            assert isinstance(ticket["tags"], list)
            assert len(ticket["tags"]) > 0


class TestClassificationIntegration:
    """Test auto-classification integration"""

    def test_classify_ticket_with_billing_keywords(self, client):
        """Test classification correctly identifies billing category"""
        # Arrange
        billing_ticket = {
            "customer_id": "CUST-9999",
            "customer_email": "billing@example.com",
            "customer_name": "Billing Customer",
            "subject": "Question about invoice charges",
            "description": "I need a refund for the incorrect charge on my latest invoice. The payment was double-billed on my credit card.",
            "category": "other",
            "priority": "low",
            "tags": [],
            "metadata": {"source": "email"}
        }

        # Act - Create ticket
        create_response = client.post("/tickets", json=billing_ticket)
        ticket_id = create_response.json()["id"]

        # Act - Classify ticket
        classify_response = client.post(f"/tickets/{ticket_id}/classify")
        assert classify_response.status_code == 200
        classification = classify_response.json()

        # Assert - Billing category suggested
        assert classification["suggested_category"] == "billing_question"
        assert classification["confidence"] > 0.5
        assert "keywords_found" in classification
        assert len(classification["keywords_found"]) > 0


    def test_classify_ticket_with_technical_keywords(self, client):
        """Test classification correctly identifies technical issue"""
        # Arrange
        technical_ticket = {
            "customer_id": "CUST-8888",
            "customer_email": "tech@example.com",
            "customer_name": "Tech Customer",
            "subject": "Application error on dashboard",
            "description": "The dashboard keeps crashing when I try to load reports. Getting timeout errors and slow performance. The system is completely broken and unresponsive.",
            "category": "other",
            "priority": "low",
            "tags": [],
            "metadata": {"source": "web_form", "browser": "Chrome", "device_type": "desktop"}
        }

        # Act - Create and classify
        create_response = client.post("/tickets", json=technical_ticket)
        ticket_id = create_response.json()["id"]

        classify_response = client.post(f"/tickets/{ticket_id}/classify")
        classification = classify_response.json()

        # Assert - Technical issue identified
        assert classification["suggested_category"] == "technical_issue"
        assert classification["confidence"] > 0.5


    def test_classify_all_tickets_workflow(self, client, sample_tickets_data):
        """Test classifying multiple tickets at once"""
        # Arrange - Create multiple tickets
        for ticket_data in sample_tickets_data:
            client.post("/tickets", json=ticket_data)

        # Act - Classify all
        classify_all_response = client.post("/tickets/classify-all")
        assert classify_all_response.status_code == 200
        results = classify_all_response.json()

        # Assert - All tickets classified
        assert isinstance(results, list)
        assert len(results) == 3

        # Assert - Each result has required fields
        for result in results:
            assert "suggested_category" in result
            assert "suggested_priority" in result
            assert "confidence" in result
            assert "reasoning" in result
            assert "keywords_found" in result
            assert 0.0 <= result["confidence"] <= 1.0


class TestEdgeCasesAndErrors:
    """Test edge cases and error scenarios"""

    def test_get_nonexistent_ticket_returns_404(self, client):
        """Test getting a ticket that doesn't exist"""
        # Arrange
        fake_uuid = "00000000-0000-0000-0000-000000000000"

        # Act
        response = client.get(f"/tickets/{fake_uuid}")

        # Assert
        assert response.status_code == 404


    def test_update_nonexistent_ticket_returns_404(self, client):
        """Test updating a ticket that doesn't exist"""
        # Arrange
        fake_uuid = "00000000-0000-0000-0000-000000000000"
        update_data = {"status": "in_progress"}

        # Act
        response = client.patch(f"/tickets/{fake_uuid}", json=update_data)

        # Assert
        assert response.status_code == 404


    def test_delete_nonexistent_ticket_returns_404(self, client):
        """Test deleting a ticket that doesn't exist"""
        # Arrange
        fake_uuid = "00000000-0000-0000-0000-000000000000"

        # Act
        response = client.delete(f"/tickets/{fake_uuid}")

        # Assert
        assert response.status_code == 404


    def test_create_ticket_with_invalid_data_returns_422(self, client, invalid_ticket_data):
        """Test creating ticket with invalid data"""
        # Act
        response = client.post("/tickets", json=invalid_ticket_data)

        # Assert
        assert response.status_code == 422


    def test_filter_with_no_matches_returns_empty_list(self, client, sample_ticket_data):
        """Test filtering with criteria that matches no tickets"""
        # Arrange - Create one ticket
        client.post("/tickets", json=sample_ticket_data)

        # Act - Filter by category that doesn't exist in data
        response = client.get("/tickets?category=bug_report")

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert data["total"] == 0
        assert data["items"] == []


    def test_statistics_with_multiple_tickets(self, client, sample_tickets_data):
        """Test statistics endpoint with multiple tickets"""
        # Arrange - Create multiple tickets
        for ticket_data in sample_tickets_data:
            client.post("/tickets", json=ticket_data)

        # Act - Get statistics
        response = client.get("/tickets/stats")

        # Assert
        assert response.status_code == 200
        stats = response.json()
        assert stats["total"] == 3
        assert "by_category" in stats
        assert "by_priority" in stats
        assert "by_status" in stats

        # Verify counts
        assert stats["by_category"]["account_access"] == 1
        assert stats["by_category"]["feature_request"] == 1
        assert stats["by_category"]["billing_question"] == 1
        assert stats["by_status"]["new"] == 3


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
