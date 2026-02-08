# Architecture Documentation

This document explains the system architecture, design decisions, and trade-offs for the Customer Support Ticket Management System.

---

## Table of Contents

- [System Overview](#system-overview)
- [High-Level Architecture](#high-level-architecture)
- [Components](#components)
- [Data Flow](#data-flow)
- [Classification Logic](#classification-logic)
- [Design Decisions](#design-decisions)
- [Trade-offs](#trade-offs)
- [Security Considerations](#security-considerations)
- [Performance Considerations](#performance-considerations)
- [Future Improvements](#future-improvements)

---

## System Overview

The Customer Support Ticket Management System is a RESTful API built with Node.js, Express, and TypeScript. It provides automatic ticket categorization and priority assignment using keyword-based classification, supports multiple import formats (CSV, JSON, XML), and maintains tickets in an in-memory data store.

**Key Features:**
- Automatic ticket classification by category and priority
- Multi-format bulk import (CSV, JSON, XML)
- CRUD operations with filtering and pagination
- Input validation with Zod schemas
- Clean architecture with separation of concerns

---

## High-Level Architecture

The system follows a layered architecture pattern with clear separation of concerns.

```mermaid
graph TB
    Client[Client Applications]
    
    subgraph "API Layer"
        Routes[Route Handlers<br/>tickets.ts]
    end
    
    subgraph "Controller Layer"
        TicketController[Ticket Controller<br/>CRUD Operations]
        ImportController[Import Controller<br/>Bulk Operations]
    end
    
    subgraph "Service Layer"
        ClassificationService[Classification Service<br/>Auto-categorization]
        CsvImportService[CSV Import Service]
        JsonImportService[JSON Import Service]
        XmlImportService[XML Import Service]
    end
    
    subgraph "Data Access Layer"
        Repository[Ticket Repository<br/>In-Memory Storage]
    end
    
    subgraph "Utility Layer"
        Validation[Validation Utils<br/>UUID & Input Checks]
        Response[Response Utils<br/>Standardized Responses]
    end
    
    subgraph "Model Layer"
        TicketModel[Ticket Model<br/>Type Definitions]
        Validator[Ticket Validator<br/>Zod Schemas]
    end
    
    Client -->|HTTP Requests| Routes
    Routes --> TicketController
    Routes --> ImportController
    
    TicketController --> ClassificationService
    TicketController --> Repository
    TicketController --> Validation
    TicketController --> Response
    
    ImportController --> CsvImportService
    ImportController --> JsonImportService
    ImportController --> XmlImportService
    ImportController --> ClassificationService
    ImportController --> Repository
    
    CsvImportService --> Validator
    JsonImportService --> Validator
    XmlImportService --> Validator
    
    Repository --> TicketModel
    
    style Routes fill:#e1f5ff
    style TicketController fill:#fff4e1
    style ImportController fill:#fff4e1
    style ClassificationService fill:#e8f5e9
    style Repository fill:#fce4ec
```

---

## Components

### 1. API Layer

**Routes** (`src/routes/tickets.ts`)
- Defines HTTP endpoints and maps them to controller methods
- Configures middleware (e.g., multer for file uploads)
- Initializes dependencies (controllers, services, repository)
- Minimal logic - acts as a thin routing layer

### 2. Controller Layer

**Ticket Controller** (`src/controllers/ticket.controller.ts`)
- Handles HTTP request/response for CRUD operations
- Orchestrates service calls and data validation
- Delegates business logic to services
- Uses utility functions for consistent responses

**Import Controller** (`src/controllers/import.controller.ts`)
- Handles bulk ticket imports from files
- Determines file type and routes to appropriate service
- Coordinates classification for imported tickets
- Provides detailed import results with success/failure counts

### 3. Service Layer

**Classification Service** (`src/services/ClassificationService.ts`)
- Analyzes ticket subject and description
- Assigns category based on keyword matching
- Assigns priority based on urgency indicators
- Returns confidence score and reasoning

**Import Services** (`src/services/*ImportService.ts`)
- Parse different file formats (CSV, JSON, XML)
- Validate imported data against schemas
- Return structured results with error details
- Extensible design for adding new formats

### 4. Data Access Layer

**Ticket Repository** (`src/repositories/ticket.repository.ts`)
- Encapsulates all data operations
- Provides filtering and pagination
- Maintains in-memory ticket storage
- Easily replaceable with database implementation

### 5. Utility Layer

**Validation Utils** (`src/utils/validation.utils.ts`)
- UUID format validation
- Standardized validation error responses

**Response Utils** (`src/utils/response.utils.ts`)
- Consistent success/error response formatting
- Reduces duplication across controllers
- Simplifies HTTP status code management

### 6. Model Layer

**Ticket Model** (`src/models/Ticket.ts`)
- TypeScript interfaces and type definitions
- Enum definitions for categories, priorities, statuses
- Type safety throughout the application

**Ticket Validator** (`src/models/TicketValidator.ts`)
- Zod schemas for runtime validation
- Separate schemas for create/update operations
- Validation helper functions

---

## Data Flow

### Ticket Creation Flow

This diagram shows the complete flow when a client creates a new ticket with automatic classification.

```mermaid
sequenceDiagram
    participant Client
    participant Routes
    participant TicketController
    participant Validator
    participant ClassificationService
    participant Repository
    participant Response
    
    Client->>Routes: POST /tickets
    Routes->>TicketController: createTicket()
    
    TicketController->>Validator: safeValidateCreateTicket(body)
    Validator-->>TicketController: validation result
    
    alt Validation Failed
        TicketController->>Response: handleValidationError()
        Response-->>Client: 400 Bad Request
    else Validation Successful
        TicketController->>ClassificationService: classify(subject, description)
        
        Note over ClassificationService: Analyze keywords<br/>Match patterns<br/>Calculate confidence
        
        ClassificationService-->>TicketController: {category, priority, confidence, reasoning}
        
        TicketController->>TicketController: buildNewTicket()
        Note over TicketController: Generate UUID<br/>Set timestamps<br/>Apply classification
        
        TicketController->>Repository: create(newTicket)
        Repository-->>TicketController: created ticket
        
        TicketController->>Response: sendCreated(ticket, classification)
        Response-->>Client: 201 Created
    end
```

### Bulk Import Flow

```mermaid
sequenceDiagram
    participant Client
    participant Routes
    participant ImportController
    participant ImportService
    participant Validator
    participant ClassificationService
    participant Repository
    
    Client->>Routes: POST /tickets/import (file)
    Routes->>ImportController: importTickets()
    
    ImportController->>ImportController: getImportService(filename)
    Note over ImportController: Determine format:<br/>CSV, JSON, or XML
    
    ImportController->>ImportService: importFromFile(path)
    
    ImportService->>ImportService: parseFile()
    loop For each record
        ImportService->>Validator: safeValidateCreateTicket()
        alt Valid
            ImportService->>ImportService: Add to validTickets[]
        else Invalid
            ImportService->>ImportService: Add to errors[]
        end
    end
    
    ImportService-->>ImportController: {validTickets, imported, failed, errors}
    
    loop For each valid ticket
        ImportController->>ClassificationService: classify()
        ClassificationService-->>ImportController: classification
        ImportController->>ImportController: buildTicket()
    end
    
    ImportController->>Repository: addBulk(tickets)
    Repository-->>ImportController: success
    
    ImportController-->>Client: 200 OK (summary + tickets)
```

---

## Classification Logic

The classification service uses a keyword-based algorithm to automatically categorize tickets and assign priorities.

```mermaid
flowchart TD
    Start([Receive Subject + Description])
    
    Start --> Normalize[Normalize Text:<br/>- Convert to lowercase<br/>- Extract keywords]
    
    Normalize --> CheckCategory{Match Category<br/>Keywords?}
    
    CheckCategory -->|Account Access| AccountAccess[Category: account_access<br/>Keywords: login, password,<br/>2fa, locked, access]
    CheckCategory -->|Technical| Technical[Category: technical_issue<br/>Keywords: error, crash,<br/>bug, 500, broken]
    CheckCategory -->|Billing| Billing[Category: billing_question<br/>Keywords: charge, refund,<br/>payment, invoice]
    CheckCategory -->|Feature| Feature[Category: feature_request<br/>Keywords: feature, add,<br/>request, suggestion]
    CheckCategory -->|Bug| Bug[Category: bug_report<br/>Keywords: bug, reproduce,<br/>steps, defect]
    CheckCategory -->|No Match| Other[Category: other]
    
    AccountAccess --> CheckPriority{Match Priority<br/>Keywords?}
    Technical --> CheckPriority
    Billing --> CheckPriority
    Feature --> CheckPriority
    Bug --> CheckPriority
    Other --> CheckPriority
    
    CheckPriority -->|Urgent Keywords| Urgent[Priority: urgent<br/>Keywords: critical, urgent,<br/>production, security,<br/>can't access]
    CheckPriority -->|High Keywords| High[Priority: high<br/>Keywords: important,<br/>blocking, asap]
    CheckPriority -->|Low Keywords| Low[Priority: low<br/>Keywords: minor, cosmetic,<br/>suggestion, nice-to-have]
    CheckPriority -->|No Match| Medium[Priority: medium<br/>Default priority]
    
    Urgent --> CalcConfidence[Calculate Confidence:<br/>Based on keyword matches<br/>and text length]
    High --> CalcConfidence
    Medium --> CalcConfidence
    Low --> CalcConfidence
    
    CalcConfidence --> Return([Return:<br/>category, priority,<br/>confidence, reasoning,<br/>matched keywords])
    
    style Start fill:#e1f5ff
    style Return fill:#e1f5ff
    style CheckCategory fill:#fff4e1
    style CheckPriority fill:#fff4e1
    style AccountAccess fill:#e8f5e9
    style Technical fill:#e8f5e9
    style Billing fill:#e8f5e9
    style Feature fill:#e8f5e9
    style Bug fill:#e8f5e9
    style Other fill:#ffebee
    style Urgent fill:#ffcdd2
    style High fill:#fff9c4
    style Medium fill:#c8e6c9
    style Low fill:#b3e5fc
```

---

## Design Decisions

### 1. Keyword-Based Classification (Not Machine Learning)

**Decision:** Use simple keyword matching for ticket classification instead of machine learning models.

**Reasoning:**

✅ **Simplicity**
- No training data required
- No model training pipeline needed
- Easy to understand and debug
- Predictable behavior

✅ **Fast Performance**
- Classification completes in milliseconds
- No model loading overhead
- Suitable for real-time API responses

✅ **Easy Maintenance**
- Keywords can be updated instantly
- No retraining required
- Non-technical staff can adjust rules
- Clear reasoning for each classification

✅ **Resource Efficiency**
- Minimal CPU usage
- No GPU requirements
- Low memory footprint
- Cost-effective for small-to-medium scale

**When to Reconsider:**
- If classification accuracy drops below 70%
- If business requires complex pattern recognition
- If handling 10,000+ diverse tickets daily
- If users frequently override classifications

### 2. In-Memory Storage (Not Database)

**Decision:** Store tickets in a JavaScript array in memory rather than using a database.

**Reasoning:**

✅ **Development Speed**
- No database setup required
- No connection management
- No migration scripts
- Faster prototyping

✅ **Simplicity**
- Zero external dependencies
- Easy to test and debug
- No schema management
- Clear data flow

✅ **Performance**
- Instant read/write operations
- No network latency
- No connection pooling issues
- Ideal for demos and testing

✅ **Easy to Replace**
- Repository pattern abstracts storage
- Swap implementation without changing controllers
- Clear interface for database migration

**Limitations:**
- ⚠️ Data lost on server restart
- ⚠️ No persistence across deployments
- ⚠️ Limited to single server instance
- ⚠️ No scalability beyond memory limits

**Migration Path to Database:**
```typescript
// Current
class TicketRepository {
    private tickets: Ticket[] = [];
    // ... methods
}

// Future with PostgreSQL
class TicketRepository {
    constructor(private db: Database) {}
    // Same interface, different implementation
}
```

### 3. Layered Architecture

**Decision:** Organize code into distinct layers (Routes → Controllers → Services → Repository).

**Reasoning:**

✅ **Separation of Concerns**
- Each layer has a single responsibility
- Easy to locate and modify code
- Reduces coupling between components

✅ **Testability**
- Each layer can be tested independently
- Easy to mock dependencies
- Clear inputs and outputs

✅ **Maintainability**
- Changes isolated to specific layers
- New features fit into existing structure
- Consistent patterns across codebase

✅ **Scalability**
- Easy to add new services
- Controllers stay thin
- Business logic centralized

### 4. Zod for Validation

**Decision:** Use Zod for runtime type validation instead of class-validator or custom validators.

**Reasoning:**

✅ **Type Inference**
- TypeScript types automatically derived from schemas
- Single source of truth for types and validation
- Reduces code duplication

✅ **Composability**
- Easy to reuse and extend schemas
- Separate schemas for create/update operations
- Built-in transformations

✅ **Developer Experience**
- Clear, declarative syntax
- Excellent error messages
- Well-documented and maintained

✅ **Runtime Safety**
- Catches invalid data at runtime
- Prevents type coercion issues
- Validates complex nested objects

### 5. Express.js Framework

**Decision:** Use Express.js as the web framework.

**Reasoning:**

✅ **Maturity**
- Battle-tested in production
- Extensive ecosystem
- Large community support
- Well-documented

✅ **Flexibility**
- Minimal opinions
- Easy to structure as needed
- Compatible with many libraries

✅ **Simplicity**
- Straightforward routing
- Minimal boilerplate
- Easy to learn and teach

**Alternatives Considered:**
- **Fastify**: Better performance, but less mature ecosystem
- **NestJS**: More opinionated, steeper learning curve
- **Koa**: Modern but smaller community

---

## Trade-offs

### 1. Performance vs. Accuracy

**Keyword-Based Classification**

**Advantages:**
- ⚡ Sub-millisecond classification
- 💰 No computational overhead
- 📊 Consistent response times
- 🔄 Real-time processing

**Disadvantages:**
- 🎯 ~70-85% accuracy vs. 90-95% for ML
- 🔤 Limited to exact keyword matches
- 🌐 No understanding of context or synonyms
- 📝 Struggles with ambiguous descriptions

**Impact:**
- Acceptable for MVP and small teams
- Users can manually override incorrect classifications
- Good enough for most common support scenarios
- May need ML upgrade as ticket volume grows

### 2. Simplicity vs. Features

**In-Memory Storage**

**Advantages:**
- 🚀 Zero setup time
- 🧪 Easy to test
- 🔧 Simple to debug
- 📦 No external dependencies

**Disadvantages:**
- 💾 No data persistence
- 📈 Limited scalability
- 🔄 No concurrent server instances
- 🔍 No complex queries (joins, aggregations)

**Impact:**
- Perfect for development and demos
- Not production-ready for real applications
- Must migrate to database for production use
- Repository pattern makes migration straightforward

### 3. Flexibility vs. Structure

**Layered Architecture**

**Advantages:**
- 🏗️ Clear organization
- ✅ Easy to navigate
- 🧩 Reusable components
- 📚 Predictable patterns

**Disadvantages:**
- 📝 More files and folders
- 🔄 More indirection
- ⏱️ Slightly more setup time
- 🎓 Learning curve for new developers

**Impact:**
- Worth it for teams and long-term projects
- Might be overkill for tiny scripts
- Pays off as project grows
- Easier onboarding for new developers

### 4. Type Safety vs. Development Speed

**TypeScript + Zod**

**Advantages:**
- 🛡️ Compile-time error detection
- 🔍 Better IDE autocomplete
- 📖 Self-documenting code
- 🐛 Fewer runtime bugs

**Disadvantages:**
- ⏱️ Initial setup time
- 📚 Learning curve for JavaScript developers
- 🔄 Build step required
- ⚙️ More configuration

**Impact:**
- Significant long-term productivity gain
- Catches bugs before production
- Better refactoring confidence
- Industry best practice for Node.js APIs

---

## Security Considerations

### Current State

**Not Implemented:**
- ⚠️ No authentication or authorization
- ⚠️ No rate limiting
- ⚠️ No input sanitization beyond validation
- ⚠️ No HTTPS enforcement
- ⚠️ No CORS configuration

**Minimal Protection:**
- ✅ Input validation prevents some injection attacks
- ✅ UUID format validation prevents path traversal
- ✅ Error messages don't expose stack traces (in production)

### Recommendations for Production

1. **Authentication & Authorization**
   - Implement JWT or session-based auth
   - Role-based access control (customer, agent, admin)
   - API key authentication for bulk imports

2. **Rate Limiting**
   - Prevent abuse and DDoS attacks
   - Different limits for different endpoints
   - Consider express-rate-limit library

3. **Input Sanitization**
   - Escape HTML in ticket content
   - Prevent XSS attacks
   - Validate file uploads thoroughly

4. **HTTPS & CORS**
   - Enforce HTTPS in production
   - Configure CORS for specific origins
   - Set security headers (helmet.js)

5. **Logging & Monitoring**
   - Log all API requests
   - Track failed authentication attempts
   - Monitor for suspicious patterns

---

## Performance Considerations

### Current Performance Profile

**Strengths:**
- ⚡ Fast classification (~1-2ms)
- 💾 In-memory operations (~0.1ms)
- 🔄 No database latency
- 📦 Minimal dependencies

**Bottlenecks:**
- 📁 File parsing for large imports (CSV/XML)
- 🔄 Synchronous classification in loops
- 💾 Memory usage grows with ticket count
- 🚫 No caching layer

### Optimization Strategies

1. **For Large File Imports:**
   ```typescript
   // Stream processing instead of loading entire file
   // Batch classifications (process 100 at a time)
   // Use worker threads for parallel processing
   ```

2. **For High Traffic:**
   ```typescript
   // Add Redis for caching frequent queries
   // Implement response compression (gzip)
   // Use clustering for multi-core systems
   ```

3. **For Scale:**
   ```typescript
   // Migrate to PostgreSQL with indexes
   // Add read replicas for query distribution
   // Implement pagination properly
   // Use CDN for static assets
   ```

### Current Limits

- **Max Tickets in Memory:** ~100,000 (depending on RAM)
- **Max Import File Size:** Limited by multer config (default: unlimited - should set limit!)
- **Concurrent Requests:** Limited by Node.js event loop
- **Classification Speed:** ~1000 tickets/second

---

## Future Improvements

### Short-Term (1-3 Months)

1. **Database Migration**
   - Implement PostgreSQL or MongoDB
   - Add proper indexing
   - Implement connection pooling

2. **Enhanced Classification**
   - Add more keyword patterns
   - Implement confidence thresholds
   - Allow custom rules per organization

3. **Better File Handling**
   - Stream large files
   - Validate file size limits
   - Add progress tracking for imports

### Medium-Term (3-6 Months)

1. **Authentication & Authorization**
   - JWT-based authentication
   - Role-based access control
   - API key management

2. **Advanced Features**
   - Full-text search
   - Ticket assignments and workflows
   - SLA tracking
   - Email notifications

3. **Monitoring & Observability**
   - Structured logging
   - Metrics and dashboards
   - Error tracking (Sentry)
   - Performance monitoring

### Long-Term (6-12 Months)

1. **Machine Learning Classification**
   - Train models on historical data
   - A/B test against keyword-based
   - Gradual rollout

2. **Microservices Architecture**
   - Separate classification service
   - Separate import service
   - Message queue for async processing

3. **Multi-Tenancy**
   - Organization isolation
   - Custom fields per organization
   - White-label capabilities

---

## Conclusion

This architecture prioritizes **simplicity, maintainability, and rapid development** over features and scale. It's ideal for:

- ✅ MVP and proof-of-concept
- ✅ Internal tools with limited users
- ✅ Learning and experimentation
- ✅ Development and testing

For production use with real customers, plan to:
- 🔄 Migrate to a persistent database
- 🔐 Implement authentication and authorization
- 📈 Add monitoring and logging
- 🚀 Optimize for performance and scale

The clean separation of concerns makes these migrations straightforward without requiring a complete rewrite.

---

**Document Version:** 1.0  
**Last Updated:** February 8, 2026  
**Authors:** Development Team
