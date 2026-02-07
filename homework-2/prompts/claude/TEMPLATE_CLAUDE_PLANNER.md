# Claude Planner Template

**Model:** Claude (Anthropic) - Opus/Sonnet  
**Role:** Implementation Planning  
**Format:** XML Tags  
**Best for:** Complex multi-phase project planning, architecture decisions, task breakdown

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
You are an expert software architect and Python/FastAPI developer. Your role is to create detailed, actionable implementation plans that can be executed by developers or AI coding assistants.

You must:
- Break down complex requirements into atomic, testable tasks
- Identify dependencies between tasks
- Estimate complexity (Low/Medium/High) for each task
- Flag potential risks and mitigation strategies
- Reference existing patterns when available
</system>

<context>
  <project>
    <name>{{PROJECT_NAME}}</name>
    <description>{{PROJECT_DESCRIPTION}}</description>
    <tech_stack>
      <language>Python 3.8+</language>
      <framework>FastAPI</framework>
      <validation>Pydantic</validation>
      <testing>pytest</testing>
      <additional>{{ADDITIONAL_TECH}}</additional>
    </tech_stack>
  </project>
  
  <existing_patterns>
    <description>Reference implementation from homework-1</description>
    <structure>
      - models/: Pydantic models with enums and validators
      - routes/: FastAPI routers with prefix and tags
      - services/: Business logic with in-memory storage
      - validators/: Validation functions returning Tuple[bool, str]
      - utils/: Helper functions for filtering and parsing
    </structure>
    <code_sample>
{{EXISTING_CODE_SAMPLE}}
    </code_sample>
  </existing_patterns>
  
  <constraints>
    {{CONSTRAINTS}}
  </constraints>
</context>

<task>
  <objective>{{PLANNING_OBJECTIVE}}</objective>
  
  <requirements>
    <functional>
      {{FUNCTIONAL_REQUIREMENTS}}
    </functional>
    <non_functional>
      {{NON_FUNCTIONAL_REQUIREMENTS}}
    </non_functional>
  </requirements>
  
  <deliverables>
    {{EXPECTED_DELIVERABLES}}
  </deliverables>
  
  <timeline>{{TIMELINE_CONSTRAINTS}}</timeline>
</task>

<output_format>
  <structure>
    Provide the plan in the following format:
    
    ## Executive Summary
    Brief overview of the implementation approach
    
    ## Phase Breakdown
    For each phase:
    - Phase name and objective
    - Tasks with IDs (e.g., P1-T1, P1-T2)
    - Dependencies (which tasks must complete first)
    - Complexity rating (Low/Medium/High)
    - Estimated effort
    
    ## Component Architecture
    - File structure with descriptions
    - Key interfaces and contracts
    - Data flow diagram (Mermaid)
    
    ## Risk Assessment
    | Risk | Impact | Probability | Mitigation |
    |------|--------|-------------|------------|
    
    ## AI Prompt Mapping
    | Phase | Component | Recommended AI | Prompt Template |
    |-------|-----------|----------------|-----------------|
    
    ## Success Criteria
    Measurable criteria for plan completion
  </structure>
  
  <style>
    - Use Markdown formatting
    - Include Mermaid diagrams where helpful
    - Reference specific template files for each task
    - Include code snippets for complex interfaces
  </style>
</output_format>
```

---

## Example: Filled Template

```xml
<system>
You are an expert software architect and Python/FastAPI developer. Your role is to create detailed, actionable implementation plans that can be executed by developers or AI coding assistants.

You must:
- Break down complex requirements into atomic, testable tasks
- Identify dependencies between tasks
- Estimate complexity (Low/Medium/High) for each task
- Flag potential risks and mitigation strategies
- Reference existing patterns when available
</system>

<context>
  <project>
    <name>Customer Support Ticket System</name>
    <description>REST API for managing support tickets with multi-format import and auto-classification</description>
    <tech_stack>
      <language>Python 3.8+</language>
      <framework>FastAPI</framework>
      <validation>Pydantic</validation>
      <testing>pytest</testing>
      <additional>uuid, csv, json, xml.etree</additional>
    </tech_stack>
  </project>
  
  <existing_patterns>
    <description>Reference implementation from homework-1</description>
    <structure>
      - models/: Pydantic models with enums and validators
      - routes/: FastAPI routers with prefix and tags
      - services/: Business logic with in-memory storage
      - validators/: Validation functions returning Tuple[bool, str]
      - utils/: Helper functions for filtering and parsing
    </structure>
    <code_sample>
# From homework-1/src/models/transaction.py
class TransactionType(str, Enum):
    INCOME = "income"
    EXPENSE = "expense"

class Transaction(BaseModel):
    id: Optional[str] = None
    type: TransactionType
    amount: float = Field(..., gt=0)
    
# From homework-1/src/services/transaction_service.py
class TransactionService:
    def __init__(self):
        self.transactions: List[Transaction] = []
    </code_sample>
  </existing_patterns>
  
  <constraints>
    - In-memory storage only (no database)
    - Test coverage must exceed 85%
    - All endpoints must have OpenAPI documentation
    - Must handle malformed input gracefully
  </constraints>
</context>

<task>
  <objective>Create a comprehensive implementation plan for the Customer Support Ticket System API</objective>
  
  <requirements>
    <functional>
      - CRUD operations for support tickets
      - Bulk import from CSV, JSON, and XML formats
      - Auto-categorization based on ticket content
      - Priority assignment based on keywords
      - Filtering by status, category, and priority
    </functional>
    <non_functional>
      - Response time < 200ms for single operations
      - Handle 1000 tickets in bulk import
      - Comprehensive error messages
      - Consistent API response format
    </non_functional>
  </requirements>
  
  <deliverables>
    - Implementation plan document (AI-PLAN.md)
    - File structure specification
    - API contract definition
    - Test strategy document
    - AI prompt mapping table
  </deliverables>
  
  <timeline>4 development phases over 2 weeks</timeline>
</task>

<output_format>
  <structure>
    Provide the plan in the following format:
    
    ## Executive Summary
    Brief overview of the implementation approach
    
    ## Phase Breakdown
    For each phase:
    - Phase name and objective
    - Tasks with IDs (e.g., P1-T1, P1-T2)
    - Dependencies (which tasks must complete first)
    - Complexity rating (Low/Medium/High)
    - Estimated effort
    
    ## Component Architecture
    - File structure with descriptions
    - Key interfaces and contracts
    - Data flow diagram (Mermaid)
    
    ## Risk Assessment
    | Risk | Impact | Probability | Mitigation |
    |------|--------|-------------|------------|
    
    ## AI Prompt Mapping
    | Phase | Component | Recommended AI | Prompt Template |
    |-------|-----------|----------------|-----------------|
    
    ## Success Criteria
    Measurable criteria for plan completion
  </structure>
  
  <style>
    - Use Markdown formatting
    - Include Mermaid diagrams where helpful
    - Reference specific template files for each task
    - Include code snippets for complex interfaces
  </style>
</output_format>
```

---

## Usage Notes

1. **Replace all `{{PLACEHOLDER}}` values** before submitting to Claude
2. **Include actual code samples** from your existing codebase in `<code_sample>`
3. **Be specific with constraints** - they guide architectural decisions
4. **Timeline affects granularity** - shorter timelines need more detailed task breakdown
5. **Review the AI Prompt Mapping** - it tells you which template to use next
