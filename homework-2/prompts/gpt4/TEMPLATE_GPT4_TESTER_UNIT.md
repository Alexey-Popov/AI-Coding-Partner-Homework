# GPT-4 Tester Template - Unit Tests + Performance

**Model:** OpenAI GPT-4  
**Role:** Test Generation - Unit Tests and Performance Benchmarks  
**Format:** System/User Message with Test Specifications  
**Best for:** Structured unit tests with clear assertions, performance benchmarks

---

## Guardrails
Follow the rules defined in [GUARDRAILS.md](../GUARDRAILS.md):
- Do not guess if uncertain
- Ask 3 clarifying questions for ambiguous input
- List all missing data explicitly

---

## Template

### System Message

```
You are an expert Python test engineer specializing in pytest for FastAPI applications.

RULES:
1. If you are unsure of the answer, state that you do not know. Do not guess.
2. If the input is ambiguous, ask exactly 3 clarifying questions before proceeding.
3. If data is missing, explicitly list what is missing.

TESTING PATTERNS:
- Use pytest with fixtures for setup
- Follow AAA pattern (Arrange, Act, Assert)
- Use parametrized tests for multiple inputs
- Include both positive and negative test cases
- Performance tests use pytest-benchmark or time measurements
- Aim for high code coverage with strategic test selection

OUTPUT:
- Generate complete pytest files
- Include descriptive test names
- Add docstrings explaining test purpose
- Group related tests in classes
```

### User Message Template

```
## Project Context

```json
{
  "project": "{{PROJECT_NAME}}",
  "test_file": "tests/{{TEST_FILE_NAME}}.py",
  "component_under_test": "{{COMPONENT_PATH}}",
  "coverage_target": {{COVERAGE_PERCENTAGE}}
}
```

## Component to Test

```python
{{COMPONENT_CODE}}
```

## Test Specifications

```json
{
  "test_categories": [
    {
      "category": "{{CATEGORY_1}}",
      "tests": [
        {
          "name": "{{TEST_NAME}}",
          "description": "{{TEST_DESCRIPTION}}",
          "input": {{TEST_INPUT}},
          "expected": {{EXPECTED_OUTPUT}},
          "assertion_type": "equals|raises|contains|true|false"
        }
      ]
    }
  ],
  "performance_tests": [
    {
      "name": "{{PERF_TEST_NAME}}",
      "operation": "{{OPERATION_DESCRIPTION}}",
      "target_time_ms": {{TARGET_TIME}},
      "iterations": {{ITERATIONS}}
    }
  ]
}
```

## Test Data Fixtures

```json
{
  "fixtures": [
    {
      "name": "{{FIXTURE_NAME}}",
      "data": {{FIXTURE_DATA}}
    }
  ]
}
```

## Required Output

Generate a complete pytest file with:
1. Module docstring
2. All imports
3. Fixtures
4. Test classes organized by category
5. Parametrized tests where applicable
6. Performance benchmark tests
7. Coverage markers for critical paths
```

---

## Example: Filled Template for Ticket Model Tests

### System Message
(Same as above)

### User Message

```
## Project Context

```json
{
  "project": "Customer Support Ticket System",
  "test_file": "tests/test_ticket_model.py",
  "component_under_test": "src/models/ticket.py",
  "coverage_target": 85
}
```

## Component to Test

```python
from enum import Enum
from typing import Optional, Dict, Any
from pydantic import BaseModel, Field, field_validator
from datetime import datetime

class TicketStatus(str, Enum):
    OPEN = "open"
    IN_PROGRESS = "in_progress"
    RESOLVED = "resolved"
    CLOSED = "closed"

class TicketPriority(str, Enum):
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"
    CRITICAL = "critical"

class TicketCategory(str, Enum):
    BILLING = "billing"
    TECHNICAL = "technical"
    GENERAL = "general"
    FEEDBACK = "feedback"

class Ticket(BaseModel):
    id: Optional[str] = Field(None, description="UUID")
    title: str = Field(..., min_length=5, max_length=200)
    description: str = Field(..., min_length=10, max_length=2000)
    customer_email: str = Field(..., description="Valid email")
    customer_name: str = Field(..., min_length=2, max_length=100)
    status: TicketStatus = Field(default=TicketStatus.OPEN)
    priority: Optional[TicketPriority] = None
    category: Optional[TicketCategory] = None
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    
    @field_validator('customer_email')
    @classmethod
    def validate_email(cls, v):
        import re
        if not re.match(r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$', v):
            raise ValueError('Invalid email format')
        return v
    
    @field_validator('title')
    @classmethod
    def title_not_whitespace(cls, v):
        if not v.strip():
            raise ValueError('Title cannot be only whitespace')
        return v.strip()
```

## Test Specifications

```json
{
  "test_categories": [
    {
      "category": "valid_ticket_creation",
      "tests": [
        {
          "name": "test_create_ticket_with_required_fields",
          "description": "Create ticket with only required fields",
          "input": {
            "title": "Login Issue",
            "description": "Cannot login to dashboard since morning",
            "customer_email": "user@example.com",
            "customer_name": "John Doe"
          },
          "expected": {"status": "open", "priority": null, "category": null},
          "assertion_type": "equals"
        },
        {
          "name": "test_create_ticket_with_all_fields",
          "description": "Create ticket with all optional fields",
          "input": {
            "title": "Billing Question",
            "description": "Need help understanding my invoice charges",
            "customer_email": "customer@company.com",
            "customer_name": "Jane Smith",
            "status": "open",
            "priority": "high",
            "category": "billing"
          },
          "expected": {"priority": "high", "category": "billing"},
          "assertion_type": "equals"
        }
      ]
    },
    {
      "category": "field_validation",
      "tests": [
        {
          "name": "test_title_too_short_raises_error",
          "description": "Title under 5 chars should fail",
          "input": {"title": "Hi", "description": "Valid description here", "customer_email": "a@b.com", "customer_name": "Jo"},
          "expected": "String should have at least 5 characters",
          "assertion_type": "raises"
        },
        {
          "name": "test_invalid_email_raises_error",
          "description": "Invalid email format should fail",
          "input": {"title": "Valid Title", "description": "Valid description here", "customer_email": "not-an-email", "customer_name": "John"},
          "expected": "Invalid email format",
          "assertion_type": "raises"
        },
        {
          "name": "test_whitespace_title_raises_error",
          "description": "Title with only whitespace should fail",
          "input": {"title": "     ", "description": "Valid description here", "customer_email": "a@b.com", "customer_name": "John"},
          "expected": "Title cannot be only whitespace",
          "assertion_type": "raises"
        }
      ]
    },
    {
      "category": "enum_validation",
      "tests": [
        {
          "name": "test_valid_status_values",
          "description": "All valid status enum values work",
          "input": ["open", "in_progress", "resolved", "closed"],
          "expected": true,
          "assertion_type": "parametrized"
        },
        {
          "name": "test_invalid_status_raises_error",
          "description": "Invalid status value should fail",
          "input": {"status": "pending"},
          "expected": "Input should be 'open', 'in_progress', 'resolved' or 'closed'",
          "assertion_type": "raises"
        }
      ]
    }
  ],
  "performance_tests": [
    {
      "name": "test_ticket_creation_performance",
      "operation": "Create 1000 ticket instances",
      "target_time_ms": 100,
      "iterations": 1000
    },
    {
      "name": "test_ticket_validation_performance",
      "operation": "Validate ticket with all field validators",
      "target_time_ms": 1,
      "iterations": 100
    }
  ]
}
```

## Test Data Fixtures

```json
{
  "fixtures": [
    {
      "name": "valid_ticket_data",
      "data": {
        "title": "Test Ticket Title",
        "description": "This is a valid test description for the ticket",
        "customer_email": "test@example.com",
        "customer_name": "Test User"
      }
    },
    {
      "name": "minimal_ticket_data",
      "data": {
        "title": "Short",
        "description": "Min length",
        "customer_email": "a@b.co",
        "customer_name": "AB"
      }
    }
  ]
}
```

## Required Output

Generate a complete pytest file with:
1. Module docstring
2. Imports (pytest, pydantic ValidationError, models)
3. Fixtures for test data
4. TestTicketCreation class
5. TestFieldValidation class
6. TestEnumValidation class
7. TestTicketPerformance class with timing assertions
```

---

## Usage Notes

1. **Test specifications in JSON** ensure structured, complete coverage
2. **assertion_type** guides how to write the assertion
3. **Parametrized tests** reduce code duplication for similar cases
4. **Performance tests** include target times and iterations
5. **Fixtures** should be reusable across test classes
6. **Coverage target** guides how many edge cases to include
