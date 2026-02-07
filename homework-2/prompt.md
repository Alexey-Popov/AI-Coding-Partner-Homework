You are a Senior Java Engineer. Build a production-ready Java 21 + Spring Boot 3.x application for:

🎧 Homework 2: Intelligent Customer Support System
- Customer support ticket management system
- Multi-format import (CSV/JSON/XML)
- Auto-classification (category + priority + confidence + reasoning + keywords)
- Strong layered architecture: controller -> service -> repository
- API returns DTOs only; repository works with Entities only
- Mapping entity <-> DTO must happen in service layer (or dedicated mapper used by service)
- Add unit tests + integration tests, aiming for >85% overall coverage
- Add multi-level docs + Mermaid diagrams

========================
0) HARD RULES (must follow)
   ========================
   A. Layering:
- Controllers: HTTP only (validation, request/response, status codes), no persistence logic.
- Services: business logic + orchestration + mapping entity<->dto + import parsing orchestration.
- Repositories: Spring Data JPA, entity persistence only, no DTOs.
- Domain model:
    - TicketEntity (JPA)
    - TicketDto (API response)
    - CreateTicketRequest / UpdateTicketRequest (API requests)
    - Import endpoints accept MultipartFile; import service parses and validates.
      B. API contract:
- Controllers MUST NOT return entities or JPA objects.
- Controllers MUST return DTOs + error format.
  C. Testing:
- Include unit tests for service logic, validation, parsing (CSV/JSON/XML), classification rules.
- Include integration tests using Testcontainers (PostgreSQL) + MockMvc.
- Provide coverage tooling (JaCoCo) and verify threshold >85%.
  D. Production readiness:
- Global exception handling (ControllerAdvice), structured error responses.
- Logging with correlation id (MDC) for important operations (import, classify).
- Validation via jakarta validation annotations.
- Clean project structure, clear naming, maintainable code.
  E. No shortcuts:
- Don’t skip XML import.
- Don’t skip malformed file handling.
- Don’t skip docs files required.

========================
1) OUTPUTS REQUIRED
   ========================
1) Source code repository layout (single repo)
2) Tests and fixtures
3) JaCoCo coverage config + instructions
4) Documentation files:
    - README.md
    - API_REFERENCE.md
    - ARCHITECTURE.md
    - TESTING_GUIDE.md
      (Include at least 3 Mermaid diagrams across docs: architecture, sequence/data flow, test pyramid)
5) Sample data files:
    - sample_tickets.csv (50)
    - sample_tickets.json (20)
    - sample_tickets.xml (30)
    - invalid_*.{csv,json,xml} for negative tests
6) Import summary response:
    - totalRecords, successfulRecords, failedRecords
    - failures list with line/record index + error messages

========================
2) BUSINESS MODEL
   ========================
   Ticket fields (align exactly):
   {
   "id": "UUID",
   "customer_id": "string",
   "customer_email": "email",
   "customer_name": "string",
   "subject": "string (1-200 chars)",
   "description": "string (10-2000 chars)",
   "category": "account_access | technical_issue | billing_question | feature_request | bug_report | other",
   "priority": "urgent | high | medium | low",
   "status": "new | in_progress | waiting_customer | resolved | closed",
   "created_at": "datetime",
   "updated_at": "datetime",
   "resolved_at": "datetime (nullable)",
   "assigned_to": "string (nullable)",
   "tags": ["array"],
   "metadata": {
   "source": "web_form | email | api | chat | phone",
   "browser": "string",
   "device_type": "desktop | mobile | tablet"
   }
   }

Validation rules:
- customer_email must be valid email
- subject length 1..200
- description length 10..2000
- enums strictly validated
- created_at/updated_at managed by backend (do not trust client timestamps)
- tags optional (default empty)
- metadata optional but if present validate enums

========================
3) REST API (must implement)
   ========================
   POST   /tickets                 create ticket
   POST   /tickets/import           bulk import CSV/JSON/XML via multipart file
   GET    /tickets                 list tickets with filtering + paging/sorting
   GET    /tickets/{id}            get ticket by id
   PUT    /tickets/{id}            update ticket
   DELETE /tickets/{id}            delete ticket
   POST   /tickets/{id}/auto-classify  run classifier and persist result

Filtering for GET /tickets:
- category, priority, status, customer_email, assigned_to, created_at from/to, tags contains
- paging: page, size; sorting: sort=created_at,desc

HTTP codes:
- 201 for create/import success
- 200 for read/update/classify
- 204 for delete
- 400 for validation/malformed file
- 404 for missing ticket
- 415 for unsupported media/import type
- 500 for unexpected

Error response format (standardize):
{
"timestamp": "...",
"status": 400,
"error": "Bad Request",
"message": "Validation failed",
"path": "/tickets/import",
"details": [ ... ]
}

========================
4) AUTO-CLASSIFICATION RULES
   ========================
   Categories:
- account_access: login, password, 2FA
- technical_issue: bugs, errors, crashes
- billing_question: payments, invoices, refunds
- feature_request: enhancements, suggestions
- bug_report: defects with reproduction steps
- other: fallback

Priority rules (keyword-based, case-insensitive):
- urgent: "can't access", "cannot access", "critical", "production down", "security"
- high: "important", "blocking", "asap"
- medium: default
- low: "minor", "cosmetic", "suggestion"

Endpoint response must include:
- category, priority, confidence (0..1), reasoning, keywords_found[]
  Persist classification to DB (store confidence, reasoning, keywords, last_classified_at).
  Allow manual override: if user explicitly set category/priority in update, keep it unless re-classify called.
  Log all classification decisions (ticket id, inputs used, outputs).

Confidence strategy (simple but deterministic):
- score based on matched keywords count + severity; cap 1.0.
- Provide reasoning text explaining matches.

========================
5) IMPORT FORMATS
   ========================
   CSV:
- header row required
- tags can be semicolon separated in one field or JSON-like array; document your choice
  JSON:
- accept array of ticket objects
  XML:
- accept <tickets><ticket>...</ticket></tickets>

Import must:
- validate each record
- create tickets in DB for valid records
- return summary with per-record errors for invalid ones
- handle malformed file gracefully (e.g., invalid XML) with 400 + error details
- support auto-classify option via query param: /tickets/import?autoClassify=true
- ensure transactional integrity per record (do NOT rollback all if one fails). Use per-record transaction or save valid subset with error collection.

========================
6) DATA & PERSISTENCE
   ========================
   Use Spring Data JPA + PostgreSQL.
- Use UUID as primary key.
- Entity should include embedded metadata fields (can be @Embeddable) and tags as element collection.
- Add indexes for common filters: category, priority, status, customer_email, created_at.
  Use Flyway for migrations.

========================
7) TESTING REQUIREMENTS (aim >85% coverage)
   ========================
   Testing stack:
- JUnit 5, Spring Boot Test, MockMvc
- Testcontainers PostgreSQL for integration tests
- Use Jackson for JSON tests
- Fixtures in src/test/resources/fixtures

Create tests comparable to the homework’s intent:
- Ticket API tests: CRUD + filtering + status codes (>= 11 tests)
- Model validation tests (>= 9)
- Import CSV tests (>= 6)
- Import JSON tests (>= 5)
- Import XML tests (>= 5)
- Categorization tests (>= 10)
- Integration workflow tests (>= 5)
- Performance-ish tests (>= 5): simple JMH is optional; otherwise timed tests with clear boundaries and disabled by default using @Tag("performance")

Also:
- Add a coverage report generation step and enforce JaCoCo minimum coverage 0.85 in build.

========================
8) DOCUMENTATION REQUIRED (generate files)
   ========================
   README.md (developers):
- overview, features, how to run, how to test, project structure
- Mermaid: architecture overview

API_REFERENCE.md (API consumers):
- all endpoints with request/response + curl
- model schema + error examples

ARCHITECTURE.md (tech leads):
- components and layering + trade-offs
- Mermaid: component diagram + sequence diagram (import and classify flows)
- security/performance considerations

TESTING_GUIDE.md (QA):
- Mermaid: test pyramid
- how to run unit/integration/performance tests
- where fixtures are
- manual checklist
- benchmarks table format

========================
9) STEP-BY-STEP PLAN (MUST WRITE FIRST)
   ========================
   Before writing code, output a numbered PLAN with steps. After each step, include:
- “Verification checklist” (commands to run, what should pass)
- “Acceptance criteria” for that step
  Then implement step-by-step. At the end, provide:
- how to run locally
- how to run tests
- how to generate coverage report
- how to confirm coverage >85%

IMPORTANT: Keep the implementation cohesive. Don’t leave TODOs. Don’t omit any endpoint, import format, or docs file.

Now start by writing the PLAN, then proceed implementing the full solution with code blocks and file paths.