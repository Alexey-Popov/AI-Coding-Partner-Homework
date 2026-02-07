# GPT-4 Planner Template

**Model:** OpenAI GPT-4  
**Role:** Implementation Planning  
**Format:** System/User Message with JSON Context  
**Best for:** Structured planning with clear task breakdowns, JSON-formatted outputs

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
You are an expert software architect and Python/FastAPI developer. Your role is to create detailed, actionable implementation plans.

RULES:
1. If you are unsure of the answer, state that you do not know. Do not guess.
2. If the input is ambiguous, ask exactly 3 clarifying questions before proceeding.
3. If data is missing, explicitly list what is missing before generating the plan.

OUTPUT FORMAT:
- Use structured Markdown with clear headings
- Include JSON blocks for structured data where appropriate
- Provide Mermaid diagrams for architecture visualization
- Reference specific template files for each task
```

### User Message Template

```
## Project Context

```json
{
  "project": {
    "name": "{{PROJECT_NAME}}",
    "description": "{{PROJECT_DESCRIPTION}}",
    "tech_stack": {
      "language": "Python 3.8+",
      "framework": "FastAPI",
      "validation": "Pydantic",
      "testing": "pytest"
    }
  },
  "constraints": [
    "{{CONSTRAINT_1}}",
    "{{CONSTRAINT_2}}",
    "{{CONSTRAINT_3}}"
  ],
  "timeline": "{{TIMELINE}}"
}
```

## Existing Patterns

Reference implementation from homework-1:
- models/: Pydantic models with enums and validators
- routes/: FastAPI routers with prefix and tags
- services/: Business logic with in-memory storage
- validators/: Validation functions returning Tuple[bool, str]

## Task

Create a comprehensive implementation plan for: {{PLANNING_OBJECTIVE}}

### Functional Requirements
{{FUNCTIONAL_REQUIREMENTS}}

### Non-Functional Requirements
{{NON_FUNCTIONAL_REQUIREMENTS}}

### Expected Deliverables
{{EXPECTED_DELIVERABLES}}

## Required Output

Provide the plan with:

1. **Executive Summary** - Brief overview of approach

2. **Phase Breakdown** - For each phase:
   ```json
   {
     "phase": "Phase Name",
     "objective": "What this phase achieves",
     "tasks": [
       {
         "id": "P1-T1",
         "name": "Task name",
         "description": "What to do",
         "dependencies": [],
         "complexity": "Low|Medium|High",
         "ai_tool": "Claude|GPT-4|Copilot",
         "template": "template_file.md"
       }
     ]
   }
   ```

3. **Component Architecture** - File structure with Mermaid diagram

4. **Risk Assessment** - Table of risks with mitigation

5. **Success Criteria** - Measurable completion criteria
```

---

## Example: Filled Template

### System Message
(Same as above)

### User Message

```
## Project Context

```json
{
  "project": {
    "name": "Customer Support Ticket System",
    "description": "REST API for ticket management with multi-format import and auto-classification",
    "tech_stack": {
      "language": "Python 3.8+",
      "framework": "FastAPI",
      "validation": "Pydantic",
      "testing": "pytest"
    }
  },
  "constraints": [
    "In-memory storage only (no database)",
    "Test coverage must exceed 85%",
    "All endpoints must have OpenAPI documentation",
    "Must handle malformed input gracefully"
  ],
  "timeline": "4 development phases over 2 weeks"
}
```

## Existing Patterns

Reference implementation from homework-1:
```python
# models/transaction.py
class TransactionType(str, Enum):
    INCOME = "income"
    EXPENSE = "expense"

class Transaction(BaseModel):
    id: Optional[str] = None
    type: TransactionType
    amount: float = Field(..., gt=0)

# services/transaction_service.py
class TransactionService:
    def __init__(self):
        self.transactions: List[Transaction] = []
```

## Task

Create a comprehensive implementation plan for the Customer Support Ticket System API.

### Functional Requirements
- CRUD operations for support tickets
- Bulk import from CSV, JSON, and XML formats
- Auto-categorization based on ticket content
- Priority assignment based on keywords
- Filtering by status, category, and priority

### Non-Functional Requirements
- Response time < 200ms for single operations
- Handle 1000 tickets in bulk import
- Comprehensive error messages
- Consistent API response format

### Expected Deliverables
- Implementation plan document (AI-PLAN.md)
- File structure specification
- API contract definition
- Test strategy document
- AI prompt mapping table

## Required Output

Provide the plan with:

1. **Executive Summary**
2. **Phase Breakdown** with JSON task definitions
3. **Component Architecture** with Mermaid diagram
4. **Risk Assessment** table
5. **Success Criteria**
```

---

## Usage Notes

1. **JSON context blocks** help GPT-4 understand structured requirements
2. **System message** sets consistent behavior rules
3. **Explicit output format** ensures structured, usable response
4. **Include code examples** from existing patterns
5. **Request JSON in responses** for machine-parseable task lists
