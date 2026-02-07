# GitHub Copilot Tester Template - Quick Test Generation

**Model:** GitHub Copilot  
**Role:** Test Generation - Fast Unit Tests  
**Format:** Comment-Driven with Function Signatures  
**Best for:** Quick test scaffolding from function signatures, following pytest patterns

---

## Guardrails
Follow the rules defined in [GUARDRAILS.md](../GUARDRAILS.md):
- Do not guess if uncertain
- Ask 3 clarifying questions for ambiguous input
- List all missing data explicitly

---

## How to Use This Template

1. Create a new test file: `tests/test_{{component}}.py`
2. Copy the template below
3. Import the functions/classes to test
4. Add function signatures with descriptive names
5. Let Copilot generate the test implementations
6. Review and add edge cases as needed

---

## Template

```python
# ============================================================
# FILE: tests/test_{{COMPONENT}}.py
# PURPOSE: Unit tests for {{COMPONENT_PATH}}
# COVERAGE TARGET: {{COVERAGE_PERCENTAGE}}%
# PATTERNS: pytest with fixtures, AAA pattern
# ============================================================

"""
Unit tests for {{COMPONENT_NAME}}.

Test categories:
- {{CATEGORY_1}}: {{CATEGORY_1_DESC}}
- {{CATEGORY_2}}: {{CATEGORY_2_DESC}}
- {{CATEGORY_3}}: {{CATEGORY_3_DESC}}

Run tests:
    pytest tests/test_{{COMPONENT}}.py -v
    pytest tests/test_{{COMPONENT}}.py -v --cov={{COMPONENT_PATH}}
"""

import pytest
from typing import Dict, Any
{{IMPORTS}}


# ============================================================
# FIXTURES - Reusable test data
# ============================================================

@pytest.fixture
def {{FIXTURE_1_NAME}}() -> {{FIXTURE_1_TYPE}}:
    """{{FIXTURE_1_DESC}}"""
    # TODO: Return sample data for tests
    return {{FIXTURE_1_DATA}}


@pytest.fixture
def {{FIXTURE_2_NAME}}() -> {{FIXTURE_2_TYPE}}:
    """{{FIXTURE_2_DESC}}"""
    # TODO: Return sample data for tests
    return {{FIXTURE_2_DATA}}


@pytest.fixture
def {{SERVICE_FIXTURE}}() -> {{SERVICE_CLASS}}:
    """Create fresh service instance for each test."""
    return {{SERVICE_CLASS}}()


# ============================================================
# TEST CLASS: {{TEST_CLASS_1}}
# Testing: {{TEST_CLASS_1_DESC}}
# ============================================================

class {{TEST_CLASS_1}}:
    """Tests for {{TEST_CLASS_1_DESC}}."""
    
    # Test: Valid input produces expected output
    def test_{{FUNCTION_1}}_with_valid_input_returns_expected(self, {{FIXTURES}}):
        """{{TEST_1_DESC}}"""
        # Arrange
        {{ARRANGE_1}}
        
        # Act
        # TODO: Call function with valid input
        
        # Assert
        # TODO: Assert expected output
        pass
    
    # Test: Another valid scenario
    def test_{{FUNCTION_1}}_with_{{SCENARIO_2}}_returns_{{EXPECTED_2}}(self, {{FIXTURES}}):
        """{{TEST_2_DESC}}"""
        # Arrange - Act - Assert
        # TODO: Implement test
        pass
    
    # Test: Invalid input raises error
    def test_{{FUNCTION_1}}_with_invalid_input_raises_{{ERROR_TYPE}}(self, {{FIXTURES}}):
        """{{TEST_3_DESC}}"""
        # Arrange
        {{ARRANGE_3}}
        
        # Act & Assert
        with pytest.raises({{ERROR_TYPE}}):
            # TODO: Call function with invalid input
            pass
    
    # Test: Edge case handling
    def test_{{FUNCTION_1}}_with_{{EDGE_CASE}}_handles_correctly(self, {{FIXTURES}}):
        """{{TEST_4_DESC}}"""
        # TODO: Test edge case
        pass


# ============================================================
# TEST CLASS: {{TEST_CLASS_2}}
# Testing: {{TEST_CLASS_2_DESC}}
# ============================================================

class {{TEST_CLASS_2}}:
    """Tests for {{TEST_CLASS_2_DESC}}."""
    
    # Parametrized test for multiple inputs
    @pytest.mark.parametrize("input_value,expected", [
        ({{INPUT_1}}, {{EXPECTED_1}}),
        ({{INPUT_2}}, {{EXPECTED_2}}),
        ({{INPUT_3}}, {{EXPECTED_3}}),
    ])
    def test_{{FUNCTION_2}}_with_various_inputs(self, input_value, expected, {{FIXTURES}}):
        """Test {{FUNCTION_2}} with multiple input variations."""
        # Act
        result = {{FUNCTION_CALL}}
        
        # Assert
        assert result == expected
    
    # Test: Empty input handling
    def test_{{FUNCTION_2}}_with_empty_input_returns_{{EMPTY_RESULT}}(self, {{FIXTURES}}):
        """{{EMPTY_INPUT_DESC}}"""
        # TODO: Test with empty/None input
        pass
    
    # Test: Boundary conditions
    def test_{{FUNCTION_2}}_at_boundary_{{BOUNDARY_DESC}}(self, {{FIXTURES}}):
        """Test boundary condition: {{BOUNDARY_DESC}}"""
        # TODO: Test at boundary values
        pass


# ============================================================
# ADDITIONAL TESTS
# Add more test methods below as needed
# ============================================================

{{ADDITIONAL_TESTS}}
```

---

## Example: Filled Template for TicketService Tests

```python
# ============================================================
# FILE: tests/test_ticket_service.py
# PURPOSE: Unit tests for src/services/ticket_service.py
# COVERAGE TARGET: 85%
# PATTERNS: pytest with fixtures, AAA pattern
# ============================================================

"""
Unit tests for TicketService.

Test categories:
- Creation: Creating tickets with various inputs
- Retrieval: Getting tickets by ID and with filters
- Updates: Modifying tickets and business rules
- Deletion: Removing tickets
- Bulk Operations: Import and statistics

Run tests:
    pytest tests/test_ticket_service.py -v
    pytest tests/test_ticket_service.py -v --cov=src/services/ticket_service
"""

import pytest
from typing import Dict, Any
from datetime import datetime
from models.ticket import Ticket, TicketCreate, TicketUpdate, TicketStatus, TicketCategory, TicketPriority
from services.ticket_service import TicketService


# ============================================================
# FIXTURES - Reusable test data
# ============================================================

@pytest.fixture
def valid_ticket_data() -> TicketCreate:
    """Valid ticket data for creation tests."""
    return TicketCreate(
        title="Test Ticket Title",
        description="This is a valid test description for the ticket",
        customer_email="test@example.com",
        customer_name="Test User"
    )


@pytest.fixture
def ticket_with_classification() -> TicketCreate:
    """Ticket data that should trigger billing classification."""
    return TicketCreate(
        title="Billing Issue with Invoice",
        description="I was charged twice for my subscription payment",
        customer_email="billing@test.com",
        customer_name="Billing Customer"
    )


@pytest.fixture
def ticket_service() -> TicketService:
    """Create fresh service instance for each test."""
    return TicketService()


@pytest.fixture
def service_with_tickets(ticket_service, valid_ticket_data) -> TicketService:
    """Service pre-populated with sample tickets."""
    # Create 3 tickets with different statuses
    t1 = ticket_service.create_ticket(valid_ticket_data)
    
    t2_data = TicketCreate(
        title="Second Ticket",
        description="Another test ticket description",
        customer_email="user2@test.com",
        customer_name="User Two"
    )
    t2 = ticket_service.create_ticket(t2_data)
    ticket_service.update_ticket(t2.id, TicketUpdate(status=TicketStatus.IN_PROGRESS))
    
    t3_data = TicketCreate(
        title="Third Ticket",
        description="Yet another test ticket here",
        customer_email="user3@test.com",
        customer_name="User Three",
        category=TicketCategory.BILLING
    )
    ticket_service.create_ticket(t3_data)
    
    return ticket_service


# ============================================================
# TEST CLASS: TestTicketCreation
# Testing: Creating tickets with valid/invalid data
# ============================================================

class TestTicketCreation:
    """Tests for ticket creation functionality."""
    
    # Test: Valid input produces ticket with ID
    def test_create_ticket_with_valid_input_returns_ticket_with_id(self, ticket_service, valid_ticket_data):
        """Creating ticket with valid data should return ticket with generated ID."""
        # Arrange - data from fixture
        
        # Act
        # TODO: Call ticket_service.create_ticket(valid_ticket_data)
        
        # Assert
        # TODO: Assert result.id is not None
        # Assert result.title == valid_ticket_data.title
        # Assert result.status == TicketStatus.OPEN
        pass
    
    # Test: Ticket gets timestamp on creation
    def test_create_ticket_sets_created_at_timestamp(self, ticket_service, valid_ticket_data):
        """Created ticket should have created_at timestamp set."""
        # Arrange
        before = datetime.utcnow()
        
        # Act
        # TODO: Create ticket
        
        # Assert
        # TODO: Assert created_at is between before and after
        pass
    
    # Test: Auto-classification triggers when no category
    def test_create_ticket_without_category_triggers_classification(self, ticket_service, ticket_with_classification):
        """Ticket without category should be auto-classified."""
        # Act
        # TODO: Create ticket with billing keywords but no category
        
        # Assert
        # TODO: Assert result.category is not None (classification ran)
        pass
    
    # Test: Provided category is preserved
    def test_create_ticket_with_category_preserves_category(self, ticket_service):
        """Ticket with explicit category should not be overwritten."""
        # Arrange
        data = TicketCreate(
            title="Technical Issue",
            description="App crashes on startup",
            customer_email="tech@test.com",
            customer_name="Tech User",
            category=TicketCategory.FEEDBACK  # Intentionally wrong
        )
        
        # Act
        # TODO: Create ticket
        
        # Assert
        # TODO: Assert result.category == TicketCategory.FEEDBACK
        pass


# ============================================================
# TEST CLASS: TestTicketRetrieval
# Testing: Getting tickets by ID and with filters
# ============================================================

class TestTicketRetrieval:
    """Tests for ticket retrieval functionality."""
    
    # Test: Get existing ticket by ID
    def test_get_ticket_by_id_returns_correct_ticket(self, service_with_tickets):
        """Getting ticket by valid ID should return that ticket."""
        # Arrange
        tickets = service_with_tickets.get_all_tickets()
        target_id = tickets[0].id
        
        # Act
        # TODO: Call get_ticket_by_id(target_id)
        
        # Assert
        # TODO: Assert result.id == target_id
        pass
    
    # Test: Get non-existent ticket returns None
    def test_get_ticket_by_id_with_invalid_id_returns_none(self, ticket_service):
        """Getting ticket with non-existent ID should return None."""
        # Act
        # TODO: Call get_ticket_by_id("non-existent-id")
        
        # Assert
        # TODO: Assert result is None
        pass
    
    # Parametrized test for filters
    @pytest.mark.parametrize("filter_field,filter_value,expected_count", [
        ("status", TicketStatus.OPEN, 2),
        ("status", TicketStatus.IN_PROGRESS, 1),
        ("category", TicketCategory.BILLING, 1),
    ])
    def test_get_all_tickets_with_filter(self, service_with_tickets, filter_field, filter_value, expected_count):
        """Test filtering tickets by various fields."""
        # Act
        kwargs = {filter_field: filter_value}
        result = service_with_tickets.get_all_tickets(**kwargs)
        
        # Assert
        assert len(result) == expected_count
    
    # Test: Empty service returns empty list
    def test_get_all_tickets_with_empty_service_returns_empty_list(self, ticket_service):
        """Empty service should return empty list, not error."""
        # Act
        result = ticket_service.get_all_tickets()
        
        # Assert
        assert result == []


# ============================================================
# TEST CLASS: TestTicketUpdates
# Testing: Modifying tickets and business rules
# ============================================================

class TestTicketUpdates:
    """Tests for ticket update functionality."""
    
    # Test: Update existing ticket
    def test_update_ticket_modifies_fields(self, service_with_tickets):
        """Updating ticket should modify specified fields."""
        # Arrange
        tickets = service_with_tickets.get_all_tickets()
        target_id = tickets[0].id
        new_title = "Updated Title"
        
        # Act
        # TODO: Update ticket with new title
        
        # Assert
        # TODO: Assert result.title == new_title
        # Assert result.updated_at is not None
        pass
    
    # Test: Update non-existent ticket returns None
    def test_update_ticket_with_invalid_id_returns_none(self, ticket_service):
        """Updating non-existent ticket should return None."""
        # Act
        result = ticket_service.update_ticket("invalid-id", TicketUpdate(title="New"))
        
        # Assert
        assert result is None
    
    # Test: Cannot update closed ticket
    def test_update_closed_ticket_raises_value_error(self, service_with_tickets):
        """Updating closed ticket should raise ValueError."""
        # Arrange
        tickets = service_with_tickets.get_all_tickets()
        ticket_id = tickets[0].id
        # First close the ticket
        service_with_tickets.update_ticket(ticket_id, TicketUpdate(status=TicketStatus.CLOSED))
        
        # Act & Assert
        with pytest.raises(ValueError):
            # TODO: Try to update the closed ticket
            pass


# ============================================================
# TEST CLASS: TestTicketDeletion
# Testing: Removing tickets
# ============================================================

class TestTicketDeletion:
    """Tests for ticket deletion functionality."""
    
    # Test: Delete existing ticket
    def test_delete_ticket_removes_from_storage(self, service_with_tickets):
        """Deleting ticket should remove it from storage."""
        # Arrange
        initial_count = len(service_with_tickets.get_all_tickets())
        ticket_id = service_with_tickets.get_all_tickets()[0].id
        
        # Act
        result = service_with_tickets.delete_ticket(ticket_id)
        
        # Assert
        assert result is True
        assert len(service_with_tickets.get_all_tickets()) == initial_count - 1
    
    # Test: Delete non-existent ticket returns False
    def test_delete_ticket_with_invalid_id_returns_false(self, ticket_service):
        """Deleting non-existent ticket should return False."""
        # Act
        result = ticket_service.delete_ticket("invalid-id")
        
        # Assert
        assert result is False


# ============================================================
# TEST CLASS: TestBulkOperations
# Testing: Import and statistics
# ============================================================

class TestBulkOperations:
    """Tests for bulk import and statistics."""
    
    # Test: Bulk import with valid data
    def test_bulk_import_creates_all_tickets(self, ticket_service):
        """Bulk import should create all valid tickets."""
        # Arrange
        tickets = [
            TicketCreate(title=f"Ticket {i}", description=f"Description {i}" * 3, 
                        customer_email=f"user{i}@test.com", customer_name=f"User {i}")
            for i in range(5)
        ]
        
        # Act
        # TODO: Call bulk_import(tickets)
        
        # Assert
        # TODO: Assert result["success_count"] == 5
        # Assert result["error_count"] == 0
        pass
    
    # Test: Bulk import with partial failures
    def test_bulk_import_continues_on_error(self, ticket_service):
        """Bulk import should continue processing after individual failures."""
        # Arrange - Include some invalid tickets
        # TODO: Create mix of valid and invalid tickets
        
        # Act
        # TODO: Call bulk_import
        
        # Assert
        # TODO: Assert success_count > 0 and error_count > 0
        pass
    
    # Test: Statistics calculation
    def test_get_ticket_stats_returns_counts(self, service_with_tickets):
        """Statistics should return correct counts by category."""
        # Act
        # TODO: Call get_ticket_stats()
        
        # Assert
        # TODO: Assert stats contain counts by status, category, priority
        pass
```

---

## Usage Notes for Copilot

1. **Descriptive test names** guide implementation (test_X_with_Y_returns_Z)
2. **AAA comments** (Arrange/Act/Assert) structure the test
3. **Fixtures** provide reusable, consistent test data
4. **pytest.mark.parametrize** reduces duplication
5. **TODO comments** tell Copilot exactly what to generate
6. **Type hints on fixtures** improve suggestions
7. **Test class organization** by feature keeps tests maintainable
