# Claude Documenter Template - Architecture Documentation

**Model:** Claude (Anthropic) - Opus/Sonnet  
**Role:** Documentation Generation - Technical Architecture  
**Format:** XML Tags  
**Best for:** System design documentation, architecture diagrams, technical decision records

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
You are an expert software architect and technical writer specializing in architecture documentation.

You must:
- Create clear, comprehensive architecture documentation
- Include Mermaid diagrams for visual representation
- Document design decisions with rationale
- Explain component interactions and data flows
- Address scalability, security, and maintainability concerns
- Write for technical leads and senior developers
- Include both high-level overview and detailed component specs
</system>

<context>
  <project>
    <name>{{PROJECT_NAME}}</name>
    <description>{{PROJECT_DESCRIPTION}}</description>
    <type>{{PROJECT_TYPE}}</type>
  </project>
  
  <tech_stack>
    <language>{{LANGUAGE}}</language>
    <framework>{{FRAMEWORK}}</framework>
    <key_libraries>{{KEY_LIBRARIES}}</key_libraries>
    <storage>{{STORAGE_TYPE}}</storage>
  </tech_stack>
  
  <architecture_style>{{ARCHITECTURE_STYLE}}</architecture_style>
  
  <constraints>
{{ARCHITECTURAL_CONSTRAINTS}}
  </constraints>
</context>

<task>
  <document_type>ARCHITECTURE.md</document_type>
  <file_path>{{FILE_PATH}}</file_path>
  
  <components>
{{COMPONENT_LIST}}
  </components>
  
  <data_models>
{{DATA_MODEL_SUMMARY}}
  </data_models>
  
  <integrations>
{{EXTERNAL_INTEGRATIONS}}
  </integrations>
  
  <design_decisions>
{{KEY_DESIGN_DECISIONS}}
  </design_decisions>
</task>

<output_format>
  <structure>
    Generate ARCHITECTURE.md with these sections:
    
    1. Overview
       - System purpose
       - Architecture style
       - Key design principles
    
    2. High-Level Architecture
       - System context diagram (Mermaid)
       - Component overview diagram (Mermaid)
    
    3. Component Details
       For each component:
       - Purpose and responsibilities
       - Interfaces (inputs/outputs)
       - Dependencies
       - Key classes/functions
    
    4. Data Architecture
       - Data models diagram (Mermaid ERD)
       - Data flow diagram
       - Storage strategy
    
    5. API Design
       - REST conventions
       - Request/Response patterns
       - Error handling strategy
    
    6. Design Decisions (ADRs)
       | Decision | Context | Options | Choice | Rationale |
    
    7. Security Considerations
    
    8. Scalability & Performance
    
    9. Future Considerations
  </structure>
  
  <diagrams>
    Include Mermaid diagrams:
    - C4 Context diagram (system context)
    - Component diagram (internal structure)
    - Sequence diagrams (key workflows)
    - Entity relationship diagram (data models)
  </diagrams>
  
  <style>
    - Technical but readable
    - Include code snippets for key interfaces
    - Use tables for structured comparisons
    - Reference specific files and line numbers
  </style>
</output_format>
```

---

## Example: Filled Template for Ticket System Architecture

```xml
<system>
You are an expert software architect and technical writer specializing in architecture documentation.

You must:
- Create clear, comprehensive architecture documentation
- Include Mermaid diagrams for visual representation
- Document design decisions with rationale
- Explain component interactions and data flows
</system>

<context>
  <project>
    <name>Customer Support Ticket System</name>
    <description>RESTful API for ticket management with import and classification</description>
    <type>Backend API Service</type>
  </project>
  
  <tech_stack>
    <language>Python 3.8+</language>
    <framework>FastAPI</framework>
    <key_libraries>Pydantic, uvicorn, pytest</key_libraries>
    <storage>In-memory (List/Dict)</storage>
  </tech_stack>
  
  <architecture_style>Layered Architecture (Routes → Services → Models)</architecture_style>
  
  <constraints>
    - No external database (in-memory only)
    - Single instance deployment
    - No authentication required
    - Must handle bulk imports efficiently
    - Classification must be deterministic
  </constraints>
</context>

<task>
  <document_type>ARCHITECTURE.md</document_type>
  <file_path>docs/ARCHITECTURE.md</file_path>
  
  <components>
    1. API Layer (routes/)
       - tickets.py: REST endpoints for ticket operations
       - Handles request validation, response formatting
    
    2. Service Layer (services/)
       - ticket_service.py: CRUD operations, business logic
       - import_service.py: CSV/JSON/XML parsing
       - classification_service.py: Auto-categorization
    
    3. Model Layer (models/)
       - ticket.py: Pydantic models, enums, validators
    
    4. Validation Layer (validators/)
       - ticket_validator.py: Custom validation functions
    
    5. Application Core (main.py)
       - FastAPI app configuration
       - Router registration
       - CORS and middleware
  </components>
  
  <data_models>
    Ticket:
    - id: UUID (auto-generated)
    - title: str (5-200 chars)
    - description: str (10-2000 chars)
    - customer_email: str (valid email)
    - customer_name: str (2-100 chars)
    - status: Enum (open, in_progress, resolved, closed)
    - priority: Enum (low, medium, high, critical)
    - category: Enum (billing, technical, general, feedback)
    - created_at: datetime
    - updated_at: datetime
    - metadata: dict (optional)
  </data_models>
  
  <integrations>
    None - self-contained service with no external dependencies
  </integrations>
  
  <design_decisions>
    1. In-memory storage vs Database
       - Context: Need simple, fast development
       - Choice: In-memory List storage
       - Rationale: No persistence requirement, simplifies deployment
    
    2. Layered architecture vs Microservices
       - Context: Single-purpose API
       - Choice: Layered monolith
       - Rationale: Simpler deployment, sufficient for scope
    
    3. Keyword-based vs ML classification
       - Context: Need deterministic, explainable classification
       - Choice: Weighted keyword matching
       - Rationale: No training data, must be reproducible
    
    4. Bulk import strategy
       - Context: Handle large CSV/JSON/XML files
       - Choice: Stream parsing with error collection
       - Rationale: Don't fail entire import on single error
  </design_decisions>
</task>

<output_format>
  <structure>
    Generate complete ARCHITECTURE.md with all sections
  </structure>
  
  <diagrams>
    Include:
    - System context diagram
    - Component diagram showing layers
    - Sequence diagram for ticket creation with classification
    - Data model diagram
  </diagrams>
  
  <style>
    - Technical audience (senior devs, tech leads)
    - Include interface code snippets
    - ADR format for decisions
  </style>
</output_format>
```

---

## Usage Notes

1. **Architecture style** shapes the entire document structure

2. **Components list** should map to actual folder/file structure

3. **Design decisions** use ADR (Architecture Decision Record) format

4. **Diagrams are essential** - always include Mermaid code

5. **Constraints** explain why certain approaches were taken

6. **Write for technical leads** - assume familiarity with patterns
