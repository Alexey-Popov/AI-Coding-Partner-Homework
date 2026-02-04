# Implementation Plan — Intelligent Customer Support System

**Stack:** Java 17 + Spring Boot 3.x, Gradle, H2 (in-memory), JUnit 5 + Mockito

---

## Context-Model-Prompt Strategy for Tests & Documentation

The **Context-Model-Prompt** framework is applied when generating tests and documentation with AI tools. The table below maps each generation task to its C-M-P choices.

### Test Generation (Tasks 3 & 5)

| Artifact | Context | Model | Prompt |
|----------|---------|-------|--------|
| **TicketApiTest** (11 tests) | `TicketController.java`, DTOs, `GlobalExceptionHandler.java` | Claude Sonnet | "Generate @WebMvcTest JUnit 5 tests for this controller. Cover: valid creation (201), missing fields (400), invalid email (400), list with filters, get by ID (200/404), update (200/404), delete (204). Mock the service layer." |
| **TicketModelTest** (9 tests) | `Ticket.java`, `CreateTicketRequest.java`, validation annotations | Claude Sonnet | "Generate unit tests validating Bean Validation constraints: blank subject, subject >200 chars, description <10 and >2000 chars, invalid email, invalid enum values for category/priority/status." |
| **CsvImportTest** (6 tests) | `CsvImportParser.java`, `TicketImportService.java`, sample CSV fixtures | Claude Sonnet | "Generate tests for CSV import: valid file, missing columns, invalid data with partial import, empty file, malformed quoting, large file (100+ rows)." |
| **JsonImportTest** (5 tests) | `JsonImportParser.java`, sample JSON fixtures | Claude Sonnet | "Generate tests for JSON import: valid array, invalid syntax, missing fields, empty array, single object instead of array." |
| **XmlImportTest** (5 tests) | `XmlImportParser.java`, sample XML fixtures | Claude Sonnet | "Generate tests for XML import: valid XML, malformed XML, missing elements, empty document, invalid encoding." |
| **CategorizationTest** (10 tests) | `ClassificationService.java`, keyword lists + disambiguation rules from PLAN.md | Claude Sonnet | "Generate tests for classification: one test per category (account_access, technical_issue, billing, feature_request, bug_report, other), plus one test per priority level (urgent, high, low, default medium). Assert confidence scores and keywords found." |
| **IntegrationTest** (5 tests) | Full endpoint contracts, all service files, sample data file paths | Claude Opus | "Generate @SpringBootTest integration tests for: create-then-retrieve, CSV import-then-list, create with autoClassify=true, full lifecycle (create→classify→update→resolve→close), bulk import with validation errors. Use TestRestTemplate." |
| **PerformanceTest** (5 tests) | Endpoint contracts, sample data files, concurrency requirements (20+ threads) | Claude Opus | "Generate performance tests: 100 ticket creations, 500-row CSV import, filtered list on 1000 tickets, 20 concurrent POST requests via ExecutorService, classification of 50 tickets. Assert timing thresholds." |

### Documentation Generation (Task 4)

| Document | Context | Model | Prompt |
|----------|---------|-------|--------|
| **README.md** | Project directory tree, `build.gradle`, `application.properties`, feature list from TASKS.md | Claude Sonnet | Audience: **developers**. "Write a project README with: overview, feature list, Mermaid architecture diagram, prerequisites (Java 17, Gradle), build/run commands, how to run tests and generate coverage, project structure tree." |
| **API_REFERENCE.md** | `TicketController.java`, all DTO classes, `GlobalExceptionHandler.java` | Claude Sonnet | Audience: **API consumers**. "Generate API reference docs: endpoint table (method, URL, description), request/response JSON bodies for each endpoint, all HTTP status codes, cURL examples, data model tables with field types and constraints, error response format." |
| **ARCHITECTURE.md** | Full service layer, repository, model, parser classes, `ClassificationService.java`, design decisions from PLAN.md | Claude Opus | Audience: **technical leads**. "Write architecture documentation: Mermaid component diagram showing controller→service→repository layers, Mermaid sequence diagram for ticket creation flow, Mermaid sequence diagram for bulk import flow, design decisions (H2 for simplicity, keyword-based classification rationale, strategy pattern for parsers), security considerations (input validation, JPA parameterized queries), performance considerations (pagination, bulk operations)." |
| **TESTING_GUIDE.md** | All test source files, JaCoCo config from `build.gradle`, `fixtures/` directory listing, coverage report output | Claude Sonnet | Audience: **QA engineers**. "Write a testing guide: Mermaid test pyramid diagram, commands to run unit/integration/all tests, fixture file locations and formats, manual testing checklist organized by endpoint, performance benchmark results table with expected thresholds." |

### Key Principles

- **Context**: scope to only the source files relevant to the artifact being generated — not the full repo. Narrower context produces more accurate, less hallucinated output.
- **Model**: Sonnet for repetitive, pattern-based generation (unit tests, reference docs). Opus for tasks requiring multi-step reasoning (integration tests, architecture trade-offs).
- **Prompt**: state the target audience, expected output format, and quantitative constraints (test counts, coverage %, Mermaid diagram count) directly in the prompt.

---

## Task 1: Multi-Format Ticket Import API

### 1.1 Project Scaffolding
- Initialize Spring Boot project with Gradle (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `h2`, `jackson-dataformat-xml`, `opencsv`)
- Configure `application.properties` (H2 console, JPA settings, multipart upload limits)
- Create base package structure:
  ```
  com.support.ticket
  ├── controller/
  ├── service/
  ├── repository/
  ├── model/
  ├── dto/
  ├── exception/
  └── config/
  ```

### 1.2 Domain Model
- Create `Ticket` JPA entity with all fields from the spec (UUID id, customer_id, customer_email, customer_name, subject, description, category, priority, status, timestamps, assigned_to, tags, metadata)
- Create enums: `Category`, `Priority`, `Status`, `Source`, `DeviceType`
- Store `tags` as a comma-separated string or `@ElementCollection`
- Store `metadata` as an `@Embedded` object (`TicketMetadata` with source, browser, device_type`)

### 1.3 DTOs & Validation
- `CreateTicketRequest` — with Bean Validation annotations (`@Email`, `@Size`, `@NotBlank`, enum validation)
- `UpdateTicketRequest` — partial update DTO
- `TicketResponse` — response mapping
- `BulkImportResponse` — `{ total, successful, failed, errors: [{line, field, message}] }`

### 1.4 Repository
- `TicketRepository extends JpaRepository<Ticket, UUID>`
- Add query methods for filtering: `findByCategory`, `findByPriority`, `findByStatus`, `findByCategoryAndPriority`
- Use Spring Data JPA Specifications for combined filtering

### 1.5 Service Layer
- `TicketService` — CRUD operations, filtering logic
- `TicketImportService` — orchestrates parsing + validation for bulk import
- `CsvImportParser` — uses OpenCSV to parse CSV files, maps rows to DTOs
- `JsonImportParser` — uses Jackson to parse JSON arrays
- `XmlImportParser` — uses Jackson XML to parse XML documents
- Each parser returns `List<CreateTicketRequest>`, service validates each and collects results/errors

### 1.6 Controller
- `TicketController` with all 6 endpoints from the spec
- `POST /tickets` — creates single ticket, returns 201
- `POST /tickets/import` — accepts `MultipartFile`, detects format by extension/content-type, delegates to import service
- `GET /tickets` — supports query params: `category`, `priority`, `status`, `page`, `size`
- `GET /tickets/{id}` — returns 404 if not found
- `PUT /tickets/{id}` — partial update, returns 404 if not found
- `DELETE /tickets/{id}` — returns 204, or 404

### 1.7 Error Handling
- `GlobalExceptionHandler` with `@ControllerAdvice`
- Handle `MethodArgumentNotValidException`, `NotFoundException`, `ImportException`, generic errors
- Consistent error response format: `{ status, message, errors[] }`

---

## Task 2: Auto-Classification

### 2.1 Classification Engine
- `ClassificationService` with keyword + pattern-based classification logic
- Category detection: scan `subject` + `description` using **weighted keyword groups**
  - `account_access` — keywords: "login", "password", "2fa", "locked out", "sign in", "authentication", "reset password", "can't log in", "access denied", "account locked"
  - `technical_issue` — keywords: "error", "crash", "broken", "not working", "exception", "slow", "freeze", "unresponsive", "500", "timeout". These describe **user-reported symptoms** without structured reproduction info.
  - `billing_question` — keywords: "payment", "invoice", "refund", "charge", "subscription", "billing", "receipt", "pricing", "plan", "upgrade"
  - `feature_request` — keywords: "feature", "suggestion", "enhance", "would be nice", "add support", "request", "wish", "improve", "could you add"
  - `bug_report` — distinguished from `technical_issue` by **structural patterns**, not just keywords. Look for: "steps to reproduce", "expected", "actual", "reproduce", "defect", "regression", "STR:", numbered reproduction steps (regex `\d+\.\s`). A ticket that says "app crashes" is `technical_issue`; one that includes reproduction steps is `bug_report`.
  - `other` — fallback when **no category exceeds the confidence threshold** (0.3)

- **Disambiguation rules** (for overlapping categories):
  - If both `technical_issue` and `bug_report` match: prefer `bug_report` if structural patterns are found, otherwise `technical_issue`
  - If both `feature_request` and `bug_report` match: prefer `bug_report` if "defect"/"regression" present
  - If `feature_request` keywords appear alongside `billing_question` keywords: use the one with more keyword hits

### 2.2 Priority Detection
- Keyword scan per spec:
  - **Urgent**: "can't access", "critical", "production down", "security", "data loss", "outage", "emergency"
  - **High**: "important", "blocking", "asap", "need immediately"
  - **Low**: "minor", "cosmetic", "suggestion", "nice to have", "when you get a chance"
  - **Medium**: default when no priority keywords found

### 2.3 Confidence Scoring
- Score 0.0–1.0 based on:
  - Number of matching keywords (more matches = higher confidence)
  - Keyword specificity weight (e.g., "steps to reproduce" is strongly `bug_report`, weighted higher than "bug")
  - Category competition: if top two categories have similar scores, lower confidence (ambiguous ticket)
- **Threshold**: if best category score < 0.3, classify as `other` with that low confidence
- Store `classificationConfidence` field on the `Ticket` entity

### 2.4 Classification Response DTO
- `ClassificationResult`: `{ category, priority, confidence, reasoning, keywordsFound[] }`

### 2.5 Endpoint & Integration
- `POST /tickets/{id}/auto-classify` — runs classification, updates ticket, returns result
- Add `autoClassify` boolean query param to `POST /tickets` — if true, classify on creation
- `ClassificationLog` entity (optional) to persist classification decisions for auditing

### 2.6 Manual Override
- `PUT /tickets/{id}` already allows setting category/priority manually, which overrides auto-classification

---

## Task 3: AI-Generated Test Suite

> **C-M-P approach**: Feed each source file individually as context to the AI model. Use Sonnet for unit/model tests (pattern-heavy, fast). Use Opus for integration tests (requires reasoning about multi-step workflows). Prompt with explicit test counts, coverage target (>85%), and framework conventions (JUnit 5 + @WebMvcTest / @SpringBootTest).

### 3.1 Test Infrastructure
- Add test dependencies: `spring-boot-starter-test`, `mockito`, JaCoCo plugin for coverage
- Configure JaCoCo in `build.gradle` with minimum 85% coverage threshold
- Create `src/test/resources/fixtures/` directory for sample data

### 3.2 Test Files
- **TicketApiTest** (11 tests) — `@WebMvcTest(TicketController.class)`
  1. Create ticket — valid input — 201
  2. Create ticket — missing required fields — 400
  3. Create ticket — invalid email — 400
  4. Get all tickets — 200 with list
  5. Get all tickets — filter by category
  6. Get all tickets — filter by priority
  7. Get ticket by ID — exists — 200
  8. Get ticket by ID — not found — 404
  9. Update ticket — valid — 200
  10. Update ticket — not found — 404
  11. Delete ticket — 204

- **TicketModelTest** (9 tests) — unit tests for validation
  1. Valid ticket passes validation
  2. Blank subject fails
  3. Subject too long (>200) fails
  4. Description too short (<10) fails
  5. Description too long (>2000) fails
  6. Invalid email fails
  7. Invalid category fails
  8. Invalid priority fails
  9. Invalid status fails

- **CsvImportTest** (6 tests)
  1. Valid CSV file — all records imported
  2. CSV with missing columns — error reported
  3. CSV with invalid data — partial import with errors
  4. Empty CSV file — returns empty result
  5. Malformed CSV (bad quoting) — graceful error
  6. Large CSV (100+ rows) — performance ok

- **JsonImportTest** (5 tests)
  1. Valid JSON array — all imported
  2. Invalid JSON syntax — error
  3. JSON with missing fields — partial import
  4. Empty JSON array — empty result
  5. Single object (not array) — handle gracefully

- **XmlImportTest** (5 tests)
  1. Valid XML — all imported
  2. Malformed XML — error
  3. XML with missing elements — partial import
  4. Empty XML — empty result
  5. XML with invalid encoding — error

- **CategorizationTest** (10 tests)
  1. Account access keywords — correct category
  2. Technical issue keywords — correct category
  3. Billing keywords — correct category
  4. Feature request keywords — correct category
  5. Bug report keywords — correct category
  6. No keywords — category "other"
  7. Urgent priority keywords — correct priority
  8. High priority keywords — correct priority
  9. Low priority keywords — correct priority
  10. Default priority — medium

- **IntegrationTest** (5 tests) — `@SpringBootTest` with full context
  1. Create ticket then retrieve it
  2. Import CSV then list tickets
  3. Create + auto-classify
  4. Full lifecycle: create → classify → update → resolve → close
  5. Bulk import with validation errors

- **PerformanceTest** (5 tests)
  1. Create 100 tickets under 2 seconds
  2. Bulk import 500-row CSV under 5 seconds
  3. List with filters on 1000 tickets under 1 second
  4. Concurrent 20 create requests
  5. Classification of 50 tickets under 3 seconds

### 3.3 Test Fixtures
- `fixtures/valid_tickets.csv`
- `fixtures/valid_tickets.json`
- `fixtures/valid_tickets.xml`
- `fixtures/invalid_tickets.csv`
- `fixtures/invalid_tickets.json`
- `fixtures/invalid_tickets.xml`
- `fixtures/malformed.csv`
- `fixtures/malformed.xml`

---

## Task 4: Multi-Level Documentation

> **C-M-P approach**: Each doc targets a different audience, so each uses a tailored prompt with audience-specific language and depth. Use different models: Sonnet for reference-style docs (README, API_REFERENCE, TESTING_GUIDE) and Opus for ARCHITECTURE.md which requires reasoning about trade-offs. Supply only the relevant source files as context for each doc — not the full codebase.

### 4.1 README.md
- **Context fed to AI**: project structure listing, build.gradle, application.properties, feature list from TASKS.md
- **Prompt audience**: developers onboarding to the project
- Contents: project overview, features, Mermaid architecture diagram, quick start (prerequisites, build, run), test commands, project structure

### 4.2 API_REFERENCE.md
- **Context fed to AI**: TicketController.java, all DTO files, GlobalExceptionHandler.java
- **Prompt audience**: API consumers integrating with the service
- Contents: endpoint table, request/response bodies, cURL examples, data model tables (fields, types, constraints), error format

### 4.3 ARCHITECTURE.md
- **Context fed to AI**: full service layer, repository, model, parser classes, classification service, this PLAN.md (design decisions section)
- **Prompt audience**: technical leads evaluating the system design
- Contents: Mermaid component diagram, sequence diagrams (ticket creation, bulk import), design decisions (H2, keyword classification, parser strategy pattern), security considerations, performance considerations

### 4.4 TESTING_GUIDE.md
- **Context fed to AI**: all test files, JaCoCo config in build.gradle, fixtures directory listing, coverage report output
- **Prompt audience**: QA engineers running and extending the test suite
- Contents: Mermaid test pyramid diagram, commands to run tests, fixture locations and formats, manual testing checklist, performance benchmark results table

---

## Task 5: Integration & Performance Tests

### 5.1 End-to-End Integration Tests
- **Ticket lifecycle**: create → auto-classify → assign → update status → resolve → close
- **Bulk import + classification**: import CSV → auto-classify all → verify categories/priorities
- **Combined filtering**: create tickets with various categories/priorities → filter by combinations → verify results

### 5.2 Concurrent Operations
- Use `ExecutorService` with 20+ threads making simultaneous POST requests
- Verify all succeed with no data corruption
- Check that concurrent reads during writes return consistent data

### 5.3 Sample Data Deliverables
- `sample_tickets.csv` — 50 tickets with realistic support data
- `sample_tickets.json` — 20 tickets
- `sample_tickets.xml` — 30 tickets
- Invalid variants for negative testing

---

## Project Structure (Final)

```
homework-2/
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat
├── gradle/wrapper/
├── src/
│   ├── main/java/com/support/ticket/
│   │   ├── TicketApplication.java
│   │   ├── config/
│   │   ├── controller/
│   │   │   └── TicketController.java
│   │   ├── dto/
│   │   │   ├── CreateTicketRequest.java
│   │   │   ├── UpdateTicketRequest.java
│   │   │   ├── TicketResponse.java
│   │   │   ├── BulkImportResponse.java
│   │   │   └── ClassificationResult.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── TicketNotFoundException.java
│   │   ├── model/
│   │   │   ├── Ticket.java
│   │   │   ├── TicketMetadata.java
│   │   │   ├── Category.java
│   │   │   ├── Priority.java
│   │   │   ├── Status.java
│   │   │   ├── Source.java
│   │   │   └── DeviceType.java
│   │   ├── repository/
│   │   │   └── TicketRepository.java
│   │   └── service/
│   │       ├── TicketService.java
│   │       ├── TicketImportService.java
│   │       ├── ClassificationService.java
│   │       └── parser/
│   │           ├── CsvImportParser.java
│   │           ├── JsonImportParser.java
│   │           └── XmlImportParser.java
│   └── main/resources/
│       └── application.properties
├── src/test/java/com/support/ticket/
│   ├── controller/TicketApiTest.java
│   ├── model/TicketModelTest.java
│   ├── service/
│   │   ├── CsvImportTest.java
│   │   ├── JsonImportTest.java
│   │   ├── XmlImportTest.java
│   │   └── CategorizationTest.java
│   └── integration/
│       ├── IntegrationTest.java
│       └── PerformanceTest.java
├── src/test/resources/fixtures/
├── sample_tickets.csv
├── sample_tickets.json
├── sample_tickets.xml
├── README.md
├── API_REFERENCE.md
├── ARCHITECTURE.md
├── TESTING_GUIDE.md
├── TASKS.md
└── PLAN.md
```

---

## Implementation Order

1. **Task 1** — Project setup, model, repository, service, controller, error handling
2. **Task 2** — Classification service, endpoint, integration with ticket creation
3. **Task 3** — AI-generated test suite *(apply C-M-P per the test generation table above)*
4. **Task 5** — Integration & performance tests *(apply C-M-P; Opus for multi-step and concurrency reasoning)*
5. **Task 4** — AI-generated documentation *(apply C-M-P per the documentation table above; written last since it references final implementation)*
