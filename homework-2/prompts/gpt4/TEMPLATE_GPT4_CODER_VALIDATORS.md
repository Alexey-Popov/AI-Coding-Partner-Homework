# GPT-4 Coder Template - Validators

**Model:** OpenAI GPT-4  
**Role:** Code Generation - Validation Functions  
**Format:** System/User Message with Input/Output Examples  
**Best for:** Validation logic with clear patterns, regex, and constraint checking

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
You are an expert Python developer specializing in input validation for FastAPI applications.

RULES:
1. If you are unsure of the answer, state that you do not know. Do not guess.
2. If the input is ambiguous, ask exactly 3 clarifying questions before proceeding.
3. If data is missing, explicitly list what is missing.

VALIDATION PATTERNS:
- All validation functions return Tuple[bool, str] (is_valid, error_message)
- Empty error message "" when valid
- Descriptive error messages when invalid
- Use compiled regex patterns for performance
- Include type hints for all functions

OUTPUT:
- Generate complete, runnable Python files
- Include comprehensive docstrings with examples
- Add unit test examples in docstrings
```

### User Message Template

```
## Project Context

```json
{
  "project": "{{PROJECT_NAME}}",
  "component": "{{VALIDATOR_NAME}}",
  "file_path": "src/validators/{{FILE_NAME}}.py"
}
```

## Reference Pattern

Follow the pattern from homework-1/src/validators/transaction_validator.py:

```python
import re
from typing import Tuple

# Compiled regex patterns for performance
EMAIL_PATTERN = re.compile(r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$')

# Valid value sets
VALID_CURRENCIES = {'USD', 'EUR', 'GBP', 'JPY', 'CAD'}

def validate_email(email: str) -> Tuple[bool, str]:
    """
    Validate email format.
    
    Args:
        email: Email string to validate
        
    Returns:
        Tuple of (is_valid, error_message)
        
    Examples:
        >>> validate_email("user@example.com")
        (True, '')
        >>> validate_email("invalid")
        (False, 'Invalid email format')
    """
    if not email or not isinstance(email, str):
        return False, "Email is required"
    if not EMAIL_PATTERN.match(email):
        return False, "Invalid email format"
    return True, ""

def validate_amount(amount: float) -> Tuple[bool, str]:
    """
    Validate transaction amount.
    
    Args:
        amount: Numeric amount to validate
        
    Returns:
        Tuple of (is_valid, error_message)
    """
    if amount is None:
        return False, "Amount is required"
    if not isinstance(amount, (int, float)):
        return False, "Amount must be a number"
    if amount <= 0:
        return False, "Amount must be positive"
    return True, ""
```

## Validation Requirements

{{VALIDATION_REQUIREMENTS}}

## Input/Output Examples

For each validation function, here are the expected behaviors:

```json
{
  "validations": [
    {
      "function": "{{FUNCTION_1_NAME}}",
      "valid_inputs": {{VALID_INPUTS_1}},
      "invalid_inputs": {{INVALID_INPUTS_1}},
      "error_messages": {{ERROR_MESSAGES_1}}
    },
    {
      "function": "{{FUNCTION_2_NAME}}",
      "valid_inputs": {{VALID_INPUTS_2}},
      "invalid_inputs": {{INVALID_INPUTS_2}},
      "error_messages": {{ERROR_MESSAGES_2}}
    }
  ]
}
```

## Required Output

Generate a complete Python file with:
1. Module docstring
2. All imports
3. Compiled regex patterns as module constants
4. Valid value sets as frozensets
5. Each validation function with:
   - Type hints
   - Docstring with examples
   - Return type Tuple[bool, str]
```

---

## Example: Filled Template for Ticket Validators

### System Message
(Same as above)

### User Message

```
## Project Context

```json
{
  "project": "Customer Support Ticket System",
  "component": "TicketValidator",
  "file_path": "src/validators/ticket_validator.py"
}
```

## Reference Pattern

(Same as template above)

## Validation Requirements

Create validators for the ticket system:

1. **validate_email(email: str)** - Standard email format validation
2. **validate_ticket_title(title: str)** - Title must be 5-200 chars, not empty/whitespace
3. **validate_ticket_description(description: str)** - Description must be 10-2000 chars
4. **validate_customer_name(name: str)** - Name must be 2-100 chars, contain at least one letter
5. **validate_status_transition(current: str, new: str)** - Validate allowed status changes
6. **validate_priority(priority: str)** - Must be valid priority enum value
7. **validate_category(category: str)** - Must be valid category enum value

## Input/Output Examples

```json
{
  "validations": [
    {
      "function": "validate_email",
      "valid_inputs": ["user@example.com", "test.user+tag@domain.co.uk"],
      "invalid_inputs": ["", "not-an-email", "@domain.com", "user@", null],
      "error_messages": {
        "empty": "Email is required",
        "invalid_format": "Invalid email format"
      }
    },
    {
      "function": "validate_ticket_title",
      "valid_inputs": ["Login Issue", "Cannot access my account dashboard"],
      "invalid_inputs": ["", "    ", "Hi", "A" * 201],
      "error_messages": {
        "empty": "Title is required",
        "too_short": "Title must be at least 5 characters",
        "too_long": "Title cannot exceed 200 characters",
        "whitespace": "Title cannot be only whitespace"
      }
    },
    {
      "function": "validate_status_transition",
      "valid_inputs": [
        ["open", "in_progress"],
        ["in_progress", "resolved"],
        ["resolved", "closed"]
      ],
      "invalid_inputs": [
        ["closed", "open"],
        ["closed", "in_progress"],
        ["resolved", "open"]
      ],
      "error_messages": {
        "invalid_transition": "Cannot transition from {current} to {new}"
      }
    },
    {
      "function": "validate_priority",
      "valid_inputs": ["low", "medium", "high", "critical"],
      "invalid_inputs": ["urgent", "normal", "1", ""],
      "error_messages": {
        "invalid": "Priority must be one of: low, medium, high, critical"
      }
    },
    {
      "function": "validate_category",
      "valid_inputs": ["billing", "technical", "general", "feedback"],
      "invalid_inputs": ["support", "other", ""],
      "error_messages": {
        "invalid": "Category must be one of: billing, technical, general, feedback"
      }
    }
  ]
}
```

## Required Output

Generate a complete Python file with:
1. Module docstring
2. All imports (re, typing)
3. Constants: EMAIL_PATTERN, VALID_PRIORITIES, VALID_CATEGORIES, VALID_TRANSITIONS
4. All 7 validation functions with full implementations
```

---

## Usage Notes

1. **Input/Output examples** are critical - they define exact behavior
2. **JSON format** ensures structured, unambiguous requirements
3. **Error messages** should be consistent and descriptive
4. **Reference pattern** ensures consistent code style
5. **Include edge cases** in invalid_inputs (null, empty, whitespace)
6. **Status transitions** often need a transition matrix
