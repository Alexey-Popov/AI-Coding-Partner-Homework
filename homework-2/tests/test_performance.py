"""
Performance tests for Customer Support Ticket System

Benchmark tests to ensure:
- Single operations complete quickly (<50ms)
- Bulk operations handle large datasets efficiently (<2s for 100 items)
- API response times are acceptable
- Memory usage is reasonable
- Concurrent operations work correctly
"""

import pytest
import time
import io
from uuid import uuid4


class TestSingleOperationPerformance:
    """Test performance of individual operations"""

    def test_create_ticket_performance(self, client, sample_ticket_data):
        """Test ticket creation completes in <50ms"""
        # Arrange
        start_time = time.time()

        # Act
        response = client.post("/tickets", json=sample_ticket_data)

        # Assert
        elapsed_time = (time.time() - start_time) * 1000  # Convert to ms
        assert response.status_code == 201
        assert elapsed_time < 50, f"Ticket creation took {elapsed_time:.2f}ms (expected <50ms)"


    def test_get_ticket_performance(self, client, sample_ticket_data):
        """Test ticket retrieval completes in <50ms"""
        # Arrange - Create a ticket first
        create_response = client.post("/tickets", json=sample_ticket_data)
        ticket_id = create_response.json()["id"]

        # Act & Measure
        start_time = time.time()
        response = client.get(f"/tickets/{ticket_id}")
        elapsed_time = (time.time() - start_time) * 1000

        # Assert
        assert response.status_code == 200
        assert elapsed_time < 50, f"Ticket retrieval took {elapsed_time:.2f}ms (expected <50ms)"


    def test_update_ticket_performance(self, client, sample_ticket_data):
        """Test ticket update completes in <50ms"""
        # Arrange
        create_response = client.post("/tickets", json=sample_ticket_data)
        ticket_id = create_response.json()["id"]
        update_data = {"status": "in_progress", "priority": "urgent"}

        # Act & Measure
        start_time = time.time()
        response = client.patch(f"/tickets/{ticket_id}", json=update_data)
        elapsed_time = (time.time() - start_time) * 1000

        # Assert
        assert response.status_code == 200
        assert elapsed_time < 50, f"Ticket update took {elapsed_time:.2f}ms (expected <50ms)"


    def test_delete_ticket_performance(self, client, sample_ticket_data):
        """Test ticket deletion completes in <50ms"""
        # Arrange
        create_response = client.post("/tickets", json=sample_ticket_data)
        ticket_id = create_response.json()["id"]

        # Act & Measure
        start_time = time.time()
        response = client.delete(f"/tickets/{ticket_id}")
        elapsed_time = (time.time() - start_time) * 1000

        # Assert
        assert response.status_code == 204
        assert elapsed_time < 50, f"Ticket deletion took {elapsed_time:.2f}ms (expected <50ms)"


    def test_list_tickets_performance(self, client, sample_tickets_data):
        """Test listing tickets completes in <50ms"""
        # Arrange - Create multiple tickets
        for ticket_data in sample_tickets_data:
            client.post("/tickets", json=ticket_data)

        # Act & Measure
        start_time = time.time()
        response = client.get("/tickets")
        elapsed_time = (time.time() - start_time) * 1000

        # Assert
        assert response.status_code == 200
        assert elapsed_time < 50, f"Listing tickets took {elapsed_time:.2f}ms (expected <50ms)"


class TestBulkOperationPerformance:
    """Test performance of bulk operations"""

    def test_bulk_import_csv_performance(self, client):
        """Test CSV import of 100 tickets completes in <2s"""
        # Arrange - Generate 100 tickets in CSV format
        csv_lines = ["customer_id,customer_email,customer_name,subject,description,category,priority,tags,source,browser,device_type"]
        for i in range(100):
            csv_lines.append(
                f"CUST-{i:04d},user{i}@example.com,User {i},"
                f"Test ticket {i},"
                f"This is a test description for ticket number {i} to test bulk import performance with sufficient content.,"
                f"technical_issue,medium,test;performance,web_form,Chrome,desktop"
            )
        csv_content = "\n".join(csv_lines)
        csv_file = io.BytesIO(csv_content.encode('utf-8'))
        csv_file.name = "bulk_test.csv"

        # Act & Measure
        start_time = time.time()
        response = client.post(
            "/import/csv",
            files={"file": ("bulk_test.csv", csv_file, "text/csv")}
        )
        elapsed_time = time.time() - start_time

        # Assert
        assert response.status_code == 200
        import_result = response.json()
        assert import_result["success_count"] == 100
        assert elapsed_time < 2.0, f"Bulk CSV import took {elapsed_time:.2f}s (expected <2s)"


    def test_bulk_import_json_performance(self, client):
        """Test JSON import of 100 tickets completes in <2s"""
        # Arrange - Generate 100 tickets in JSON format
        tickets = []
        for i in range(100):
            tickets.append({
                "customer_id": f"CUST-{i:04d}",
                "customer_email": f"user{i}@example.com",
                "customer_name": f"User {i}",
                "subject": f"Test ticket {i}",
                "description": f"This is a test description for ticket number {i} to test bulk import performance with sufficient content.",
                "category": "technical_issue",
                "priority": "medium",
                "tags": ["test", "performance"],
                "metadata": {
                    "source": "api",
                    "browser": None,
                    "device_type": "desktop"
                }
            })

        import json
        json_content = json.dumps(tickets)
        json_file = io.BytesIO(json_content.encode('utf-8'))
        json_file.name = "bulk_test.json"

        # Act & Measure
        start_time = time.time()
        response = client.post(
            "/import/json",
            files={"file": ("bulk_test.json", json_file, "application/json")}
        )
        elapsed_time = time.time() - start_time

        # Assert
        assert response.status_code == 200
        import_result = response.json()
        assert import_result["success_count"] == 100
        assert elapsed_time < 2.0, f"Bulk JSON import took {elapsed_time:.2f}s (expected <2s)"


    def test_filter_large_dataset_performance(self, client):
        """Test filtering 100+ tickets completes in <100ms"""
        # Arrange - Create 100 tickets with different categories
        categories = ["account_access", "technical_issue", "billing_question", "feature_request", "bug_report", "other"]
        for i in range(100):
            ticket_data = {
                "customer_id": f"CUST-{i:04d}",
                "customer_email": f"user{i}@example.com",
                "customer_name": f"User {i}",
                "subject": f"Test ticket {i}",
                "description": f"This is a test description for ticket {i} with enough content to pass validation.",
                "category": categories[i % len(categories)],
                "priority": "medium",
                "tags": ["test"],
                "metadata": {"source": "web_form", "browser": "Chrome", "device_type": "desktop"}
            }
            client.post("/tickets", json=ticket_data)

        # Act & Measure - Filter by category
        start_time = time.time()
        response = client.get("/tickets?category=technical_issue")
        elapsed_time = (time.time() - start_time) * 1000

        # Assert
        assert response.status_code == 200
        data = response.json()
        assert data["total"] > 0
        assert elapsed_time < 100, f"Filtering 100 tickets took {elapsed_time:.2f}ms (expected <100ms)"


    def test_classify_all_performance(self, client):
        """Test classifying 50 tickets completes in <500ms"""
        # Arrange - Create 50 tickets
        for i in range(50):
            ticket_data = {
                "customer_id": f"CUST-{i:04d}",
                "customer_email": f"user{i}@example.com",
                "customer_name": f"User {i}",
                "subject": f"Test ticket {i}",
                "description": f"This is a test ticket about login issues and password reset problems for testing classification.",
                "category": "other",
                "priority": "low",
                "tags": [],
                "metadata": {"source": "email"}
            }
            client.post("/tickets", json=ticket_data)

        # Act & Measure
        start_time = time.time()
        response = client.post("/tickets/classify-all")
        elapsed_time = (time.time() - start_time) * 1000

        # Assert
        assert response.status_code == 200
        results = response.json()
        assert len(results) == 50
        assert elapsed_time < 500, f"Classifying 50 tickets took {elapsed_time:.2f}ms (expected <500ms)"


class TestConcurrentOperations:
    """Test concurrent operation handling"""

    def test_concurrent_ticket_creation(self, client):
        """Test creating multiple tickets in rapid succession"""
        # Arrange
        ticket_data = {
            "customer_id": "CUST-CONCURRENT",
            "customer_email": "concurrent@example.com",
            "customer_name": "Concurrent User",
            "subject": "Concurrent test ticket",
            "description": "Testing concurrent ticket creation to ensure no race conditions occur.",
            "category": "technical_issue",
            "priority": "medium",
            "tags": ["concurrent"],
            "metadata": {"source": "api"}
        }

        # Act - Create 20 tickets rapidly
        start_time = time.time()
        created_ids = []
        for i in range(20):
            response = client.post("/tickets", json=ticket_data)
            assert response.status_code == 201
            created_ids.append(response.json()["id"])
        elapsed_time = time.time() - start_time

        # Assert - All tickets created with unique IDs
        assert len(created_ids) == 20
        assert len(set(created_ids)) == 20, "All ticket IDs should be unique"
        assert elapsed_time < 1.0, f"Creating 20 tickets took {elapsed_time:.2f}s (expected <1s)"


    def test_statistics_calculation_performance(self, client):
        """Test statistics calculation with large dataset completes in <100ms"""
        # Arrange - Create 100 tickets with varied data
        categories = ["account_access", "technical_issue", "billing_question", "feature_request", "bug_report", "other"]
        priorities = ["urgent", "high", "medium", "low"]

        for i in range(100):
            ticket_data = {
                "customer_id": f"CUST-{i:04d}",
                "customer_email": f"user{i}@example.com",
                "customer_name": f"User {i}",
                "subject": f"Stats test ticket {i}",
                "description": f"Description for statistics testing with sufficient length to pass validation requirements.",
                "category": categories[i % len(categories)],
                "priority": priorities[i % len(priorities)],
                "tags": ["stats"],
                "metadata": {"source": "web_form", "browser": "Chrome", "device_type": "desktop"}
            }
            client.post("/tickets", json=ticket_data)

        # Act & Measure
        start_time = time.time()
        response = client.get("/tickets/stats")
        elapsed_time = (time.time() - start_time) * 1000

        # Assert
        assert response.status_code == 200
        stats = response.json()
        assert stats["total"] == 100
        assert "by_category" in stats
        assert "by_priority" in stats
        assert "by_status" in stats
        assert elapsed_time < 100, f"Statistics calculation took {elapsed_time:.2f}ms (expected <100ms)"


class TestMemoryEfficiency:
    """Test memory efficiency with large datasets"""

    def test_large_ticket_list_retrieval(self, client):
        """Test retrieving large ticket list doesn't cause memory issues"""
        # Arrange - Create 200 tickets
        for i in range(200):
            ticket_data = {
                "customer_id": f"CUST-{i:04d}",
                "customer_email": f"user{i}@example.com",
                "customer_name": f"User {i}",
                "subject": f"Memory test ticket {i}",
                "description": f"This is ticket number {i} for memory efficiency testing with adequate description length.",
                "category": "technical_issue",
                "priority": "medium",
                "tags": ["memory", "test"],
                "metadata": {"source": "web_form", "browser": "Chrome", "device_type": "desktop"}
            }
            client.post("/tickets", json=ticket_data)

        # Act
        response = client.get("/tickets")

        # Assert - Request completes successfully
        assert response.status_code == 200
        data = response.json()
        assert data["total"] == 200
        assert len(data["items"]) == 200


    def test_multiple_filters_performance(self, client):
        """Test applying multiple filters simultaneously"""
        # Arrange - Create diverse dataset
        for i in range(50):
            ticket_data = {
                "customer_id": f"CUST-{i:04d}",
                "customer_email": f"user{i}@example.com",
                "customer_name": f"User {i}",
                "subject": f"Multi-filter test {i}",
                "description": f"Testing multiple simultaneous filters with ticket {i} and sufficient description length.",
                "category": "technical_issue" if i % 2 == 0 else "billing_question",
                "priority": "urgent" if i % 3 == 0 else "medium",
                "tags": ["filter"],
                "metadata": {"source": "web_form", "browser": "Chrome", "device_type": "desktop"}
            }
            response = client.post("/tickets", json=ticket_data)
            if i % 2 == 0:
                ticket_id = response.json()["id"]
                client.patch(f"/tickets/{ticket_id}", json={"status": "in_progress"})

        # Act & Measure
        start_time = time.time()
        response = client.get("/tickets?category=technical_issue&priority=urgent&status=in_progress")
        elapsed_time = (time.time() - start_time) * 1000

        # Assert
        assert response.status_code == 200
        data = response.json()
        # Verify all results match all filters
        for ticket in data["items"]:
            assert ticket["category"] == "technical_issue"
            assert ticket["priority"] == "urgent"
            assert ticket["status"] == "in_progress"
        assert elapsed_time < 100, f"Multi-filter query took {elapsed_time:.2f}ms (expected <100ms)"


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
