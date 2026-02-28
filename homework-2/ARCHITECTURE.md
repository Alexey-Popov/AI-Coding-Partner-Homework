# Architecture Document

Technical architecture overview of the Customer Support System (CSS) for technical leads and system architects.

---

## Table of Contents

- [High-Level Architecture](#high-level-architecture)
- [Component Descriptions](#component-descriptions)
- [Data Flow Diagrams](#data-flow-diagrams)
- [Design Decisions and Trade-offs](#design-decisions-and-trade-offs)

---

## High-Level Architecture

The system follows a classic **layered architecture** with clear separation of concerns across Controller, Service, and Repository layers. All components are managed by Spring's IoC container.

```mermaid
graph TD
    Client["Client<br/>(cURL / Browser / Postman)"]

    subgraph Presentation Layer
        Controller["TicketController<br/>7 REST endpoints"]
        ExHandler["GlobalExceptionHandler<br/>@ControllerAdvice"]
        JacksonCfg["JacksonConfig<br/>snake_case + JavaTimeModule"]
    end

    subgraph Service Layer
        TicketSvc["TicketService<br/>CRUD + orchestration"]
        ValidationSvc["TicketValidationService<br/>email, lengths, enums"]
        ClassifySvc["TicketClassificationService<br/>keyword-based classifier"]
        ImportRouter["TicketImportService<br/>format router"]
    end

    subgraph Import Subsystem
        CsvSvc["CsvImportService"]
        JsonSvc["JsonImportService"]
        XmlSvc["XmlImportService<br/>(XXE-protected)"]
    end

    subgraph Data Layer
        Repo["TicketRepository<br/>ConcurrentHashMap"]
    end

    Client -->|HTTP| Controller
    Controller --> ExHandler
    Controller --> TicketSvc
    TicketSvc --> ValidationSvc
    TicketSvc --> ClassifySvc
    TicketSvc --> ImportRouter
    ImportRouter --> CsvSvc
    ImportRouter --> JsonSvc
    ImportRouter --> XmlSvc
    TicketSvc --> Repo
    CsvSvc --> Repo
    JsonSvc --> Repo
    XmlSvc --> Repo

    style Client fill:#e1bee7,stroke:#333
    style Controller fill:#bbdefb,stroke:#333
    style ExHandler fill:#ffcdd2,stroke:#333
    style JacksonCfg fill:#b3e5fc,stroke:#333
    style TicketSvc fill:#c8e6c9,stroke:#333
    style ValidationSvc fill:#dcedc8,stroke:#333
    style ClassifySvc fill:#f0f4c3,stroke:#333
    style ImportRouter fill:#fff9c4,stroke:#333
    style Repo fill:#ffe0b2,stroke:#333
```

---

## Component Descriptions

### Presentation Layer

| Component | Responsibility |
|-----------|----------------|
| **TicketController** | Exposes 7 REST endpoints under `/tickets`. Handles request/response mapping, delegates all business logic to services. Stateless. |
| **GlobalExceptionHandler** | `@ControllerAdvice` that intercepts exceptions and maps them to consistent `ErrorResponse` DTOs with appropriate HTTP status codes (400, 404, 500). |
| **JacksonConfig** | Configures Jackson `ObjectMapper` with `snake_case` naming strategy and `JavaTimeModule` for `LocalDateTime` serialization. Also sets default content type to `application/json`. |

### Service Layer

| Component | Responsibility |
|-----------|----------------|
| **TicketService** | Core business logic: create, read, update, delete tickets. Orchestrates validation and optional auto-classification during creation. Manages `resolved_at` timestamps on status transitions. |
| **TicketValidationService** | Validates all required fields: email format (regex), string lengths (subject 1–200, description 10–2000), and required fields. Provides separate methods for create requests, update requests (partial), and raw data maps (for importers). |
| **TicketClassificationService** | Keyword-based classification engine. Scans ticket `subject` + `description` for category/priority keywords. Produces a `ClassificationResult` with category, priority, confidence (0–1), reasoning, and matched keywords. |
| **TicketImportService** | Routes uploaded files to format-specific importers based on file extension. Validates filename presence and supported formats (`.csv`, `.json`, `.xml`). |

### Import Subsystem

| Component | Responsibility |
|-----------|----------------|
| **CsvImportService** | Parses CSV files with a custom line parser supporting quoted fields and escape characters. Maps header columns to ticket fields, validates each row, saves valid tickets, and tracks failures with detailed error information. |
| **JsonImportService** | Deserializes JSON arrays using Jackson `ObjectMapper` (lenient, snake_case). Uses inner `JsonTicketData` class for mapping. Falls back to defaults for unknown enum values. |
| **XmlImportService** | Parses XML using DOM parser with **XXE protection** (external entities disabled). Processes `<ticket>` elements, handles nested `<tags>` and `<metadata>` structures. |

### Data Layer

| Component | Responsibility |
|-----------|----------------|
| **TicketRepository** | In-memory store using `ConcurrentHashMap<UUID, Ticket>`. Provides CRUD operations and stream-based filtering by category, priority, status, and customer ID. Thread-safe for concurrent access. |

### Domain Model

| Component | Description |
|-----------|-------------|
| **Ticket** | Core entity with UUID, customer info, subject/description, category, priority, status, timestamps, tags, metadata, and classification fields. |
| **TicketMetadata** | Value object: source channel, browser, device type. |
| **Enums** | `TicketCategory` (6 values), `TicketPriority` (4), `TicketStatus` (5), `TicketSource` (5), `DeviceType` (3). All use `@JsonValue`/`@JsonCreator` for snake_case serialization with case-insensitive parsing. |

### DTOs

| Component | Purpose |
|-----------|---------|
| **CreateTicketRequest** | Inbound DTO for ticket creation. Includes optional `auto_classify` flag and nested `MetadataRequest`. |
| **UpdateTicketRequest** | Inbound DTO for partial updates. Null fields are skipped during update. |
| **ImportResult** | Outbound DTO for import operations: success/failure counts, error details, imported IDs. |
| **ErrorResponse** | Outbound DTO for all error responses: status, message, timestamp, optional field errors map. |
| **ClassificationResult** | Outbound DTO for classification: category, priority, confidence, reasoning, keywords. |

---

## Data Flow Diagrams

### Ticket Creation Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant TC as TicketController
    participant TS as TicketService
    participant VS as ValidationService
    participant CS as ClassificationService
    participant R as TicketRepository

    C->>TC: POST /tickets (JSON body)
    TC->>TS: createTicket(request)
    TS->>VS: validateCreateRequest(request)
    alt Validation fails
        VS-->>TS: throw ValidationException
        TS-->>TC: propagate exception
        TC-->>C: 400 Bad Request + field errors
    end
    VS-->>TS: valid
    TS->>TS: Build Ticket entity (defaults: status=NEW, id=UUID)
    opt auto_classify = true
        TS->>CS: classify(ticket)
        CS->>CS: Scan subject + description for keywords
        CS-->>TS: ClassificationResult (category, priority, confidence)
        TS->>TS: Apply classification to ticket
    end
    TS->>R: save(ticket)
    R-->>TS: saved ticket
    TS-->>TC: Ticket
    TC-->>C: 201 Created + Ticket JSON
```

### Bulk Import Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant TC as TicketController
    participant IS as TicketImportService
    participant FI as Format-Specific Importer
    participant VS as ValidationService
    participant R as TicketRepository

    C->>TC: POST /tickets/import (multipart file)
    TC->>IS: importTickets(file)
    IS->>IS: Detect format from extension (.csv/.json/.xml)
    alt Unsupported format
        IS-->>TC: throw ImportException
        TC-->>C: 400 Bad Request
    end
    IS->>FI: importFromFile(inputStream)
    loop For each record in file
        FI->>FI: Parse record
        FI->>VS: validateRawData(fields)
        alt Valid
            FI->>R: save(ticket)
            FI->>FI: Add ID to successList
        else Invalid
            FI->>FI: Add error to failList
        end
    end
    FI-->>IS: ImportResult (total, success, failed, errors, ids)
    IS-->>TC: ImportResult
    TC-->>C: 200 OK + ImportResult JSON
```

---

## Design Decisions and Trade-offs

### 1. In-Memory Storage (ConcurrentHashMap)

**Decision:** Use `ConcurrentHashMap` instead of a database.

| Pros | Cons |
|------|------|
| Zero infrastructure dependencies | Data lost on restart |
| Sub-millisecond read/write | No persistence guarantee |
| Simple deployment (single JAR) | Limited query capabilities (no SQL) |
| Thread-safe by default | Memory-bound capacity |

**Rationale:** For a homework/prototype, this eliminates database setup complexity while demonstrating proper repository patterns that can be swapped for JPA/JDBC later.

### 2. Keyword-Based Classification (vs. ML)

**Decision:** Use static keyword dictionaries instead of machine learning.

| Pros | Cons |
|------|------|
| Deterministic and testable | Limited accuracy for ambiguous text |
| No model training needed | Requires manual keyword maintenance |
| Fast execution (O(n) string scan) | No learning from feedback |
| Easy to extend | Cannot handle misspellings |

**Rationale:** Keyword-based classification provides predictable, debuggable behavior suitable for demonstration. The confidence score (matched keywords / 3.0, capped at 1.0) provides a transparent metric.

### 3. Layered Architecture

**Decision:** Strict Controller → Service → Repository separation.

**Rationale:** Enables independent unit testing of each layer with mocks. Services are reusable across different entry points. Controllers remain thin and only handle HTTP concerns.

### 4. Strategy Pattern for Importers

**Decision:** `TicketImportService` routes to format-specific importers based on file extension.

**Rationale:** Adding a new format (e.g., YAML) only requires a new importer class and a case in the router. Existing importers remain untouched (Open/Closed Principle).

### 5. Partial Updates (PUT)

**Decision:** `PUT /tickets/{id}` applies only non-null fields from the request body.

**Rationale:** Avoids requiring clients to send the full ticket on every update. Simplifies the API for common operations like status changes or reassignment. Note: this is more PATCH-like semantics on a PUT endpoint — a pragmatic trade-off for simplicity.

### 6. snake_case JSON Convention

**Decision:** Global Jackson configuration with `PropertyNamingStrategies.SNAKE_CASE`.

**Rationale:** Aligns with the ticket model specification and common REST API conventions. Applied globally via `JacksonConfig` so all endpoints are consistent without per-field annotations.
