# Claude Coder Template - Pydantic Models

**Model:** Claude (Anthropic) - Opus/Sonnet  
**Role:** Code Generation - Data Models  
**Format:** XML Tags  
**Best for:** Complex Pydantic models with enums, validators, and nested schemas

---

## Guardrails
Follow the rules defined in [GUARDRAILS.md](../GUARDRAILS.md):
- Do not guess if uncertain
- Ask 3 clarifying questions for ambiguous input
- List all missing data explicitly

---

## Template

```xml
<system>
You are an expert Python developer specializing in Pydantic data models for FastAPI applications.

You must:
- Create type-safe models with comprehensive validation
- Use appropriate Field constraints (gt, lt, min_length, max_length, regex)
- Implement custom validators with @field_validator decorator
- Include clear docstrings for all models and fields
- Follow Python naming conventions (PascalCase for classes, snake_case for fields)
- Provide example instances in docstrings
</system>

<context>
  <project>
    <name>{{PROJECT_NAME}}</name>
    <framework>FastAPI with Pydantic v2</framework>
  </project>
  
  <existing_patterns>
    <description>Follow patterns from homework-1/src/models/transaction.py</description>
    <reference_code>
from enum import Enum
from typing import Optional, List
from pydantic import BaseModel, Field, field_validator
from datetime import datetime

class TransactionType(str, Enum):
    """Enumeration of valid transaction types."""
    INCOME = "income"
    EXPENSE = "expense"

class Transaction(BaseModel):
    """
    Represents a financial transaction.
    
    Example:
        >>> t = Transaction(type=TransactionType.INCOME, amount=100.50, description="Salary")
    """
    id: Optional[str] = Field(None, description="Unique identifier (UUID)")
    type: TransactionType = Field(..., description="Transaction type")
    amount: float = Field(..., gt=0, description="Transaction amount (positive)")
    description: str = Field(..., min_length=1, max_length=200)
    created_at: Optional[datetime] = None
    
    @field_validator('description')
    @classmethod
    def description_not_empty(cls, v: str) -> str:
        if not v.strip():
            raise ValueError('Description cannot be empty or whitespace')
        return v.strip()
    </reference_code>
  </existing_patterns>
  
  <coding_standards>
    - All fields must have descriptions
    - Use Optional[] for nullable fields
    - Include type hints for all validator methods
    - Use Enum classes for fixed value sets
    - Validators should return cleaned values
  </coding_standards>
</context>

<task>
  <component>{{MODEL_NAME}}</component>
  <file_path>src/models/{{FILE_NAME}}.py</file_path>
  
  <requirements>
    <fields>
{{FIELD_DEFINITIONS}}
    </fields>
    
    <enums>
{{ENUM_DEFINITIONS}}
    </enums>
    
    <validations>
{{VALIDATION_RULES}}
    </validations>
    
    <relationships>
{{MODEL_RELATIONSHIPS}}
    </relationships>
  </requirements>
</task>

<output_format>
  <structure>
    Generate a complete Python file with:
    1. Module docstring
    2. All imports (standard library, then third-party, then local)
    3. Enum classes (if any)
    4. Main model class(es)
    5. Response/Request wrapper models (if needed)
  </structure>
  
  <style>
    - Include comprehensive docstrings
    - Add usage examples in docstrings
    - Group related validators together
    - Use descriptive error messages in validators
  </style>
  
  <example_output>
    The output should be a complete, runnable Python file
    that can be saved directly to the specified file_path
  </example_output>
</output_format>
```

---

## Example: Filled Template for Ticket Model

```xml
<system>
You are an expert Python developer specializing in Pydantic data models for FastAPI applications.

You must:
- Create type-safe models with comprehensive validation
- Use appropriate Field constraints (gt, lt, min_length, max_length, regex)
- Implement custom validators with @field_validator decorator
- Include clear docstrings for all models and fields
- Follow Python naming conventions (PascalCase for classes, snake_case for fields)
- Provide example instances in docstrings
</system>

<context>
  <project>
    <name>Customer Support Ticket System</name>
    <framework>FastAPI with Pydantic v2</framework>
  </project>
  
  <existing_patterns>
    <description>Follow patterns from homework-1/src/models/transaction.py</description>
    <reference_code>
from enum import Enum
from typing import Optional, List
from pydantic import BaseModel, Field, field_validator
from datetime import datetime

class TransactionType(str, Enum):
    INCOME = "income"
    EXPENSE = "expense"

class Transaction(BaseModel):
    id: Optional[str] = Field(None, description="Unique identifier (UUID)")
    type: TransactionType = Field(..., description="Transaction type")
    amount: float = Field(..., gt=0, description="Transaction amount")
    
    @field_validator('description')
    @classmethod
    def description_not_empty(cls, v: str) -> str:
        if not v.strip():
            raise ValueError('Description cannot be empty')
        return v.strip()
    </reference_code>
  </existing_patterns>
  
  <coding_standards>
    - All fields must have descriptions
    - Use Optional[] for nullable fields
    - Include type hints for all validator methods
    - Use Enum classes for fixed value sets
  </coding_standards>
</context>

<task>
  <component>Ticket</component>
  <file_path>src/models/ticket.py</file_path>
  
  <requirements>
    <fields>
      - id: UUID string, auto-generated, optional on create
      - title: string, 5-200 chars, required
      - description: string, 10-2000 chars, required
      - customer_email: valid email format, required
      - customer_name: string, 2-100 chars, required
      - status: enum (open, in_progress, resolved, closed), default: open
      - priority: enum (low, medium, high, critical), optional
      - category: enum (billing, technical, general, feedback), optional
      - created_at: datetime, auto-generated
      - updated_at: datetime, auto-updated
      - metadata: dict for additional fields, optional
    </fields>
    
    <enums>
      - TicketStatus: open, in_progress, resolved, closed
      - TicketPriority: low, medium, high, critical
      - TicketCategory: billing, technical, general, feedback
    </enums>
    
    <validations>
      - Email must be valid format (regex)
      - Title and description cannot be only whitespace
      - Customer name must contain at least one letter
      - Status transitions: closed tickets cannot be reopened
    </validations>
    
    <relationships>
      - TicketCreate: model for POST requests (no id, timestamps)
      - TicketUpdate: model for PATCH requests (all optional)
      - TicketResponse: model with all fields for responses
    </relationships>
  </requirements>
</task>

<output_format>
  <structure>
    Generate a complete Python file with:
    1. Module docstring
    2. All imports
    3. Enum classes (TicketStatus, TicketPriority, TicketCategory)
    4. Main Ticket model
    5. TicketCreate, TicketUpdate, TicketResponse models
  </structure>
  
  <style>
    - Include comprehensive docstrings
    - Add usage examples in docstrings
    - Group related validators together
  </style>
</output_format>
```

---

## Usage Notes

1. **Field definitions format:**
   ```
   - field_name: type, constraints, required/optional
   ```

2. **Enum definitions format:**
   ```
   - EnumName: value1, value2, value3
   ```

3. **Validation rules should be specific** - describe exact behavior expected

4. **Include relationships** if you need request/response variations of the model

5. **Reference code helps Claude** match your existing patterns exactly
