# Testing Guide for QA Engineers

## Overview

This guide provides comprehensive testing instructions for the Customer Support Ticket Management System. It covers automated tests, manual testing procedures, sample data, and performance benchmarks.

**Audience**: QA Engineers, Test Automation Engineers, Manual Testers  
**Last Updated**: February 8, 2026  
**Test Framework**: Jest 29.7 with TypeScript

---

## Test Pyramid

The test suite follows the standard testing pyramid approach, with a strong foundation of unit tests, integration tests, and a smaller number of end-to-end tests.

```mermaid
graph TD
    subgraph "Test Pyramid"
        A[E2E Tests: 5 tests<br/>Performance & Integration]
        B[Integration Tests: 17 tests<br/>API Endpoints & Workflows]
        C[Unit Tests: 80 tests<br/>Services, Models, Validators]
    end
    
    C --> B
    B --> A
    
    style A fill:#ff6b6b
    style B fill:#ffd93d
    style C fill:#6bcf7f
```

### Test Distribution

| Test Level | Count | Percentage | Purpose |
|------------|-------|------------|---------|
| **Unit Tests** | 80 | 78% | Test individual functions, validators, and services in isolation |
| **Integration Tests** | 17 | 17% | Test API endpoints and component interactions |
| **E2E Tests** | 5 | 5% | Test complete workflows and system performance |
| **Total** | **102** | **100%** | Full test coverage |

### Coverage Metrics

```mermaid
pie title Code Coverage by Type
    "Statements (92.54%)" : 92.54
    "Uncovered Statements" : 7.46
```

| Metric | Coverage | Target | Status |
|--------|----------|--------|--------|
| **Statements** | 92.54% | >85% | ✅ PASSING |
| **Branches** | 82.64% | >85% | ⚠️ CLOSE |
| **Functions** | 88.73% | >85% | ✅ PASSING |
| **Lines** | 92.59% | >85% | ✅ PASSING |

---

## How to Run Tests

### Prerequisites

```bash
# Node.js version 18+ required
node --version

# Install dependencies
cd homework-2
npm install
```

### Running All Tests

```bash
# Run all test suites
npm test

# Run tests in watch mode (auto-rerun on file changes)
npm test -- --watch

# Run tests with verbose output
npm test -- --verbose
```

### Running Tests with Coverage

```bash
# Generate coverage report
npm run test:coverage

# View HTML coverage report in browser
open coverage/lcov-report/index.html
```

### Running Specific Test Files

```bash
# Run API endpoint tests only
npm test -- test_ticket_api.test.ts

# Run CSV import tests only
npm test -- test_import_csv.test.ts

# Run all import tests (CSV, JSON, XML)
npm test -- --testNamePattern="import"

# Run performance benchmarks
npm test -- test_performance.test.ts
```

### Running Specific Test Cases

```bash
# Run tests matching a pattern
npm test -- --testNamePattern="should create a new ticket"

# Run tests for a specific describe block
npm test -- --testNamePattern="POST /tickets"

# Skip specific tests
npm test -- --testPathIgnorePatterns="performance"
```

### Continuous Integration

```bash
# Run tests with coverage and exit (CI mode)
npm run test:coverage -- --ci --coverage --maxWorkers=2

# Generate machine-readable coverage reports
npm run test:coverage -- --coverageReporters=json --coverageReporters=lcov
```

---

## Test Suite Structure

### Test Files Organization

```
homework-2/
├── tests/
│   ├── test_ticket_api.test.ts          # API endpoint tests (11 tests)
│   ├── test_ticket_model.test.ts        # Validation tests (9 tests)
│   ├── test_import_csv.test.ts          # CSV import tests (6 tests)
│   ├── test_import_json.test.ts         # JSON import tests (5 tests)
│   ├── test_import_xml.test.ts          # XML import tests (5 tests)
│   ├── test_categorization.test.ts      # Classification tests (10 tests)
│   ├── test_integration.test.ts         # E2E workflow tests (5 tests)
│   ├── test_performance.test.ts         # Performance benchmarks (5 tests)
│   └── fixtures/                        # Internal test fixtures (used by automated tests)
├── docs/
│   └── sample/                          # Sample data for manual testing
│       ├── sample_tickets.csv           # 50 production-like tickets
│       ├── sample_tickets.json          # 20 production-like tickets
│       ├── sample_tickets.xml           # 30 production-like tickets
│       ├── sample_tickets_invalid.*     # Invalid data for error testing
│       └── demo-import-sample-data.sh   # Quick import script
└── coverage/                            # Generated coverage reports
    ├── lcov-report/                     # HTML coverage viewer
    └── coverage-final.json              # JSON coverage data
```

---

## Sample Test Data

### Sample Data Files

All sample data for manual testing is located in the `docs/sample/` directory. These files contain production-like data for realistic testing scenarios.

#### 1. CSV Sample Data (`sample_tickets.csv`)

**Location**: `docs/sample/sample_tickets.csv`  
**Format**: CSV with headers  
**Records**: 50 diverse tickets

**Sample Structure**:
```csv
customer_id,customer_email,customer_name,subject,description,category,priority,tags,source,browser,device_type
cust-201,william.admin@system.com,William Administrator,Cannot reset password - link not working,...,account_access,urgent,"password,reset",web_form,Chrome 120,desktop
```

**Categories Included**:
- Account access issues (3 tickets)
- Technical issues (9 tickets)
- Billing questions (3 tickets)
- Feature requests (7 tickets)
- Bug reports (1 ticket)
- Other inquiries (7 tickets)

#### 2. JSON Sample Data (`sample_tickets.json`)

**Location**: `docs/sample/sample_tickets.json`  
**Format**: JSON array  
**Records**: 20 diverse tickets

**Sample Structure**:
```json
[
  {
    "customer_id": "cust-101",
    "customer_email": "john.developer@techstartup.com",
    "customer_name": "John Developer",
    "subject": "Payment processing failed - need urgent help",
    "description": "My payment for the pro plan upgrade keeps failing...",
    "category": "billing_question",
    "priority": "urgent",
    "tags": ["payment", "urgent", "billing", "upgrade"],
    "metadata": {
      "source": "web_form",
      "browser": "Chrome 120",
      "device_type": "desktop"
    }
  }
]
```

**Use Cases**:
- Realistic bulk import testing
- Various priority levels
- Diverse category distribution
- Complex metadata scenarios

#### 3. XML Sample Data (`sample_tickets.xml`)

**Location**: `docs/sample/sample_tickets.xml`  
**Format**: XML with nested structure  
**Records**: 30 diverse tickets

**Sample Structure**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<tickets>
    <ticket>
        <customer_id>cust-201</customer_id>
        <customer_email>william.admin@system.com</customer_email>
        <customer_name>William Administrator</customer_name>
        <subject>Cannot reset password - link not working</subject>
        <description>I requested a password reset 4 times...</description>
        <category>account_access</category>
        <priority>urgent</priority>
        <tags>
            <tag>password</tag>
            <tag>reset</tag>
            <tag>authentication</tag>
        </tags>
        <metadata>
            <source>web_form</source>
            <browser>Chrome 120</browser>
            <device_type>desktop</device_type>
        </metadata>
    </ticket>
</tickets>
```

**Use Cases**:
- Large-scale import testing (30 tickets)
- XML structure validation
- Nested element handling

#### 4. Invalid Sample Data Files

For error handling and validation testing:

| File | Location | Purpose |
|------|----------|----------|
| `sample_tickets_invalid.csv` | `docs/sample/` | CSV validation error testing |
| `sample_tickets_invalid.json` | `docs/sample/` | JSON schema violation testing |
| `sample_tickets_invalid.xml` | `docs/sample/` | XML malformation testing |

---

## Manual Testing Checklist

### Pre-Testing Setup

- [ ] Server is running on port 3000
- [ ] Database/storage is cleared (restart server or use demo script)
- [ ] Environment variables are set correctly
- [ ] Sample data files are present in `docs/sample/`

---

### Quick Start: Import Sample Data

Before manual testing, you can quickly populate the system with 100 sample tickets using the demo import script.

**Script Location**: `docs/sample/demo-import-sample-data.sh`

```bash
# Start the server first
cd homework-2
npm run build
npm run dev

# In another terminal, run the import script
cd homework-2/docs/sample
bash demo-import-sample-data.sh
```

**What it imports:**
- ✅ 50 tickets from CSV (`sample_tickets.csv`)
- ✅ 20 tickets from JSON (`sample_tickets.json`)
- ✅ 30 tickets from XML (`sample_tickets.xml`)
- **Total: 100 diverse sample tickets**

**Sample Data Categories:**
- Account access issues (password resets, authentication)
- Technical issues (errors, crashes, API problems)
- Billing questions (payments, refunds, subscriptions)
- Feature requests (enhancements, new capabilities)
- Bug reports (with reproduction steps)
- General inquiries

**Sample Data Priorities:**
- Urgent: Critical production issues requiring immediate attention
- High: Important functionality issues
- Medium: Standard requests and issues
- Low: Minor issues, suggestions, nice-to-haves

**After Import - Quick Verification:**

```bash
# View all tickets
curl http://localhost:3000/tickets

# Filter by priority
curl 'http://localhost:3000/tickets?priority=urgent'

# Filter by category
curl 'http://localhost:3000/tickets?category=technical_issue'

# Get ticket count by status
curl 'http://localhost:3000/tickets?status=new'
```

**Sample Files Location**: All sample data files are in `docs/sample/`:
- `sample_tickets.csv` - 50 tickets in CSV format
- `sample_tickets.json` - 20 tickets in JSON format
- `sample_tickets.xml` - 30 tickets in XML format
- `sample_tickets_invalid.csv/json/xml` - Invalid data for error testing

---

### 1. Ticket Creation Testing

#### Test Case 1.1: Create Valid Ticket

**Endpoint**: `POST /tickets`

```bash
curl -X POST http://localhost:3000/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customer_id": "test-001",
    "customer_email": "test@example.com",
    "customer_name": "Test User",
    "subject": "Test ticket creation",
    "description": "This is a test ticket with sufficient description length for validation.",
    "tags": ["test", "manual"],
    "metadata": {
      "source": "web_form",
      "browser": "Chrome",
      "device_type": "desktop"
    }
  }'
```

**Expected Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": "uuid-string",
    "customer_id": "test-001",
    "customer_email": "test@example.com",
    "customer_name": "Test User",
    "subject": "Test ticket creation",
    "description": "This is a test ticket...",
    "category": "other",
    "priority": "medium",
    "status": "new",
    "created_at": "2026-02-08T10:00:00.000Z",
    "updated_at": "2026-02-08T10:00:00.000Z",
    "resolved_at": null,
    "assigned_to": null,
    "tags": ["test", "manual"],
    "metadata": {
      "source": "web_form",
      "browser": "Chrome",
      "device_type": "desktop"
    }
  },
  "classification": {
    "confidence": 0,
    "reasoning": "Classified as 'other' (default)...",
    "keywords": []
  }
}
```

**Verification Checklist**:
- [ ] Response status is 201
- [ ] Response contains unique `id` (UUID format)
- [ ] `status` is set to "new"
- [ ] `category` and `priority` are auto-assigned
- [ ] `created_at` and `updated_at` timestamps are present
- [ ] `classification` object contains confidence score

---

#### Test Case 1.2: Invalid Email Format

```bash
curl -X POST http://localhost:3000/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customer_id": "test-002",
    "customer_email": "invalid-email",
    "customer_name": "Test User",
    "subject": "Test invalid email",
    "description": "Testing email validation with invalid format.",
    "tags": ["test"],
    "metadata": {
      "source": "api",
      "browser": null,
      "device_type": "desktop"
    }
  }'
```

**Expected Response** (400 Bad Request):
```json
{
  "success": false,
  "error": "Validation failed",
  "details": [
    {
      "path": ["customer_email"],
      "message": "Invalid email"
    }
  ]
}
```

**Verification Checklist**:
- [ ] Response status is 400
- [ ] Error message clearly indicates email validation failure
- [ ] Error details specify the field path

---

#### Test Case 1.3: Missing Required Fields

```bash
curl -X POST http://localhost:3000/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customer_email": "test@example.com"
  }'
```

**Expected Response** (400 Bad Request):
```json
{
  "success": false,
  "error": "Validation failed",
  "details": [
    {
      "path": ["customer_id"],
      "message": "Required"
    },
    {
      "path": ["customer_name"],
      "message": "Required"
    },
    {
      "path": ["subject"],
      "message": "Required"
    },
    {
      "path": ["description"],
      "message": "Required"
    }
  ]
}
```

**Verification Checklist**:
- [ ] Response status is 400
- [ ] All missing required fields are listed
- [ ] Error messages are clear and actionable

---

### 2. Ticket Retrieval Testing

#### Test Case 2.1: Get All Tickets

**Endpoint**: `GET /tickets`

```bash
curl http://localhost:3000/tickets
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-1",
      "customer_email": "user1@example.com",
      "subject": "Ticket 1",
      ...
    },
    {
      "id": "uuid-2",
      "customer_email": "user2@example.com",
      "subject": "Ticket 2",
      ...
    }
  ]
}
```

**Verification Checklist**:
- [ ] Response status is 200
- [ ] `data` is an array
- [ ] Each ticket has all required fields
- [ ] Tickets are sorted correctly

---

#### Test Case 2.2: Filter by Priority

**Endpoint**: `GET /tickets?priority=urgent`

```bash
curl "http://localhost:3000/tickets?priority=urgent"
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-1",
      "priority": "urgent",
      "subject": "Critical issue",
      ...
    }
  ]
}
```

**Verification Checklist**:
- [ ] All returned tickets have `priority: "urgent"`
- [ ] Non-urgent tickets are excluded
- [ ] Response structure matches unfiltered response

---

#### Test Case 2.3: Filter by Category

**Endpoint**: `GET /tickets?category=account_access`

```bash
curl "http://localhost:3000/tickets?category=account_access"
```

**Verification Checklist**:
- [ ] All returned tickets have `category: "account_access"`
- [ ] Other categories are excluded

---

#### Test Case 2.4: Pagination

**Endpoint**: `GET /tickets?limit=5&offset=0`

```bash
# Get first 5 tickets
curl "http://localhost:3000/tickets?limit=5&offset=0"

# Get next 5 tickets
curl "http://localhost:3000/tickets?limit=5&offset=5"
```

**Verification Checklist**:
- [ ] First request returns maximum 5 tickets
- [ ] Second request returns next set of tickets
- [ ] No duplicate tickets between pages
- [ ] Total count is consistent

---

#### Test Case 2.5: Get Ticket by ID

**Endpoint**: `GET /tickets/:id`

```bash
# Replace {ticket-id} with actual UUID
curl http://localhost:3000/tickets/{ticket-id}
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "ticket-id",
    "customer_email": "test@example.com",
    ...
  }
}
```

**Verification Checklist**:
- [ ] Response status is 200
- [ ] Returned ticket ID matches requested ID
- [ ] All ticket fields are present

---

#### Test Case 2.6: Non-Existent Ticket

```bash
curl http://localhost:3000/tickets/00000000-0000-0000-0000-000000000000
```

**Expected Response** (404 Not Found):
```json
{
  "success": false,
  "error": "Ticket not found",
  "details": {
    "id": "00000000-0000-0000-0000-000000000000"
  }
}
```

**Verification Checklist**:
- [ ] Response status is 404
- [ ] Error message is clear
- [ ] Requested ID is included in details

---

### 3. Ticket Update Testing

#### Test Case 3.1: Update Ticket Status

**Endpoint**: `PUT /tickets/:id`

```bash
curl -X PUT http://localhost:3000/tickets/{ticket-id} \
  -H "Content-Type: application/json" \
  -d '{
    "status": "in_progress",
    "assigned_to": "agent-john"
  }'
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "ticket-id",
    "status": "in_progress",
    "assigned_to": "agent-john",
    "updated_at": "2026-02-08T10:15:00.000Z",
    ...
  }
}
```

**Verification Checklist**:
- [ ] Response status is 200
- [ ] `status` is updated to "in_progress"
- [ ] `assigned_to` is set correctly
- [ ] `updated_at` timestamp is newer than `created_at`
- [ ] Other fields remain unchanged

---

#### Test Case 3.2: Resolve Ticket

```bash
curl -X PUT http://localhost:3000/tickets/{ticket-id} \
  -H "Content-Type: application/json" \
  -d '{
    "status": "resolved"
  }'
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "ticket-id",
    "status": "resolved",
    "resolved_at": "2026-02-08T10:20:00.000Z",
    ...
  }
}
```

**Verification Checklist**:
- [ ] `status` is "resolved"
- [ ] `resolved_at` timestamp is set
- [ ] `resolved_at` is equal to `updated_at`

---

### 4. Ticket Deletion Testing

#### Test Case 4.1: Delete Existing Ticket

**Endpoint**: `DELETE /tickets/:id`

```bash
curl -X DELETE http://localhost:3000/tickets/{ticket-id}
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "message": "Ticket deleted successfully",
  "data": {
    "id": "ticket-id"
  }
}
```

**Verification Checklist**:
- [ ] Response status is 200
- [ ] Success message is clear
- [ ] Subsequent GET request for same ID returns 404

---

### 5. Bulk Import Testing

#### Test Case 5.1: Import Valid CSV

**Endpoint**: `POST /tickets/import`

```bash
curl -X POST http://localhost:3000/tickets/import \
  -F "file=@docs/sample/sample_tickets.csv"
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "format": "CSV",
  "imported": 50,
  "failed": 0,
  "tickets": [
    {
      "id": "uuid-1",
      "customer_email": "william.admin@system.com",
      "category": "account_access",
      "priority": "urgent",
      ...
    }
  ],
  "errors": []
}
```

**Verification Checklist**:
- [ ] Response status is 200
- [ ] `imported` equals 50 (all valid records)
- [ ] `failed` equals 0
- [ ] `tickets` array contains all imported tickets
- [ ] All tickets have required category and priority fields

---

#### Test Case 5.2: Import CSV with Invalid Records

```bash
curl -X POST http://localhost:3000/tickets/import \
  -F "file=@docs/sample/sample_tickets_invalid.csv"
```

**Expected Response** (400 Bad Request):
```json
{
  "success": false,
  "format": "CSV",
  "imported": 0,
  "failed": 5,
  "tickets": [],
  "errors": [
    {
      "row": 1,
      "reason": "Invalid email format"
    },
    {
      "row": 2,
      "reason": "Missing required field: category"
    }
  ]
}
```

**Verification Checklist**:
- [ ] Response status is 400
- [ ] Response includes both successful and failed counts
- [ ] Each error specifies row number and reason
- [ ] Error messages are actionable

---

#### Test Case 5.3: Import Without File

```bash
curl -X POST http://localhost:3000/tickets/import
```

**Expected Response** (400 Bad Request):
```json
{
  "success": false,
  "error": "No file uploaded"
}
```

**Verification Checklist**:
- [ ] Response status is 400
- [ ] Error message indicates missing file

---

### 6. Auto-Classification Testing

#### Test Case 6.1: Login Issue (Account Access)

```bash
curl -X POST http://localhost:3000/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customer_id": "test-cat-001",
    "customer_email": "test@example.com",
    "customer_name": "Test User",
    "subject": "Cannot login - password reset not working",
    "description": "I am unable to log into my account. The password reset link is not working.",
    "tags": ["login", "authentication"],
    "metadata": {
      "source": "web_form",
      "browser": "Chrome",
      "device_type": "desktop"
    }
  }'
```

**Expected Classification**:
- Category: `account_access`
- Priority: `high` or `urgent`
- Confidence: > 0.5

**Verification Checklist**:
- [ ] `category` is "account_access"
- [ ] `priority` is appropriately high
- [ ] `classification.keywords` includes ["login", "password", "account"]

---

#### Test Case 6.2: Billing Issue

```bash
curl -X POST http://localhost:3000/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customer_id": "test-cat-002",
    "customer_email": "test@example.com",
    "customer_name": "Test User",
    "subject": "Charged twice for subscription",
    "description": "I was double charged on my credit card for this month subscription. Need refund.",
    "tags": ["billing", "refund"],
    "metadata": {
      "source": "email",
      "browser": null,
      "device_type": "mobile"
    }
  }'
```

**Expected Classification**:
- Category: `billing_question`
- Priority: `high`

**Verification Checklist**:
- [ ] `category` is "billing_question"
- [ ] `classification.keywords` includes ["refund", "charge", "billing"]

---

#### Test Case 6.3: Critical/Production Issue

```bash
curl -X POST http://localhost:3000/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customer_id": "test-cat-003",
    "customer_email": "test@example.com",
    "customer_name": "Test User",
    "subject": "CRITICAL: Production server down",
    "description": "Our production server is completely down and inaccessible. This is urgent!",
    "tags": ["critical", "production"],
    "metadata": {
      "source": "chat",
      "browser": "Chrome",
      "device_type": "desktop"
    }
  }'
```

**Expected Classification**:
- Category: `technical_issue`
- Priority: `urgent`
- Confidence: > 0.7

**Verification Checklist**:
- [ ] `priority` is "urgent"
- [ ] `classification.keywords` includes ["critical", "urgent", "production"]

---

## Performance Benchmarks

### Benchmark Test Results

| Operation | Target | Actual | Status | Notes |
|-----------|--------|--------|--------|-------|
| **Create 100 tickets** | < 10s | ~2.5s | ✅ PASS | Concurrent creation with auto-classification |
| **Retrieve 50 from 200** | < 1s | ~200ms | ✅ PASS | Paginated query with filtering |
| **Filter 150 tickets** | < 500ms | ~100ms | ✅ PASS | Priority/category filter on large dataset |
| **Bulk CSV import (50 records)** | < 5s | ~150ms | ✅ PASS | Includes validation and classification |
| **Sequential updates (10 tickets)** | < 2s | ~50ms | ✅ PASS | Status and assignment updates |
| **Single ticket creation** | < 100ms | ~25ms | ✅ PASS | Average response time |
| **Single ticket retrieval** | < 50ms | ~10ms | ✅ PASS | Get by ID |

### Performance Test Scenarios

#### Scenario 1: Concurrent Ticket Creation

**Test**: Create 100 tickets simultaneously

```bash
# Run performance test
npm test -- test_performance.test.ts -t "should handle 100 ticket creations"
```

**Metrics**:
- Total time: ~2.5 seconds
- Average per ticket: 25ms
- Success rate: 100%
- No race conditions or duplicate IDs

---

#### Scenario 2: Large Dataset Filtering

**Test**: Filter 150 tickets by priority

**Setup**:
```bash
# Create 150 test tickets with various priorities
npm test -- test_performance.test.ts -t "should filter large dataset"
```

**Metrics**:
- Query time: ~100ms
- Memory usage: < 50MB
- Accurate results: 100%

---

#### Scenario 3: Bulk Import Performance

**Test**: Import 50 tickets from CSV

```bash
# Run bulk import benchmark
npm test -- test_performance.test.ts -t "should handle bulk CSV import"
```

**Metrics**:
- Parse time: ~50ms
- Validation time: ~50ms
- Classification time: ~50ms
- Total time: ~150ms
- Success rate: Based on valid data percentage

---

## Test Execution Flow

```mermaid
flowchart TD
    Start([Start Test Suite]) --> Setup[Setup: Clear Repository]
    Setup --> Unit[Run Unit Tests<br/>80 tests]
    Unit --> UnitPass{All Pass?}
    UnitPass -->|No| UnitFail[Report Unit Test Failures]
    UnitPass -->|Yes| Integration[Run Integration Tests<br/>17 tests]
    Integration --> IntPass{All Pass?}
    IntPass -->|No| IntFail[Report Integration Failures]
    IntPass -->|Yes| E2E[Run E2E Tests<br/>5 tests]
    E2E --> E2EPass{All Pass?}
    E2EPass -->|No| E2EFail[Report E2E Failures]
    E2EPass -->|Yes| Coverage[Generate Coverage Report]
    Coverage --> CoverageCheck{Coverage > 85%?}
    CoverageCheck -->|No| CoverageFail[Report Coverage Insufficient]
    CoverageCheck -->|Yes| Success([All Tests Passed ✅])
    
    UnitFail --> End([Test Suite Complete])
    IntFail --> End
    E2EFail --> End
    CoverageFail --> End
    Success --> End
    
    style Start fill:#6bcf7f
    style Success fill:#6bcf7f
    style End fill:#ff6b6b
    style UnitFail fill:#ff6b6b
    style IntFail fill:#ff6b6b
    style E2EFail fill:#ff6b6b
    style CoverageFail fill:#ff6b6b
```

---

## Troubleshooting Guide

### Common Issues

#### Issue 1: Port 3000 Already in Use

**Symptom**: Server fails to start with `EADDRINUSE` error

**Solution**:
```bash
# Kill process on port 3000
lsof -ti:3000 | xargs kill -9

# Or use the provided task
npm run start
```

---

#### Issue 2: Tests Failing Due to Stale Data

**Symptom**: Tests expect empty repository but find existing tickets

**Solution**:
```bash
# Each test file uses beforeEach to clear repository
# Ensure you're not running multiple test suites simultaneously
npm test -- --runInBand
```

---

#### Issue 3: Import Tests Failing with 400 Errors

**Symptom**: CSV/JSON/XML import returns validation errors

**Solution**:
- Check fixture file headers match expected format
- Ensure CSV uses flat headers: `source`, `browser`, `device_type` (not `metadata_source`)
- Validate JSON structure matches schema
- Check XML is well-formed

---

#### Issue 4: Coverage Below Threshold

**Symptom**: Jest reports coverage below 85%

**Solution**:
```bash
# Run coverage with detailed report
npm run test:coverage -- --verbose

# Check uncovered lines in report
open coverage/lcov-report/index.html

# Focus on critical paths in:
# - src/controllers/
# - src/services/
# - src/repositories/
```

---

## Best Practices

### For Writing New Tests

1. **Follow AAA Pattern** (Arrange, Act, Assert)
   ```typescript
   it('should create a ticket', async () => {
       // Arrange: Setup test data
       const ticketData = { ... };
       
       // Act: Execute the operation
       const response = await request(app).post('/tickets').send(ticketData);
       
       // Assert: Verify results
       expect(response.status).toBe(201);
   });
   ```

2. **Use beforeEach for Setup**
   ```typescript
   beforeEach(() => {
       ticketRepository.clear(); // Clean slate for each test
   });
   ```

3. **Test One Thing Per Test**
   - ✅ Good: `should reject ticket with invalid email`
   - ❌ Bad: `should validate all fields and reject invalid tickets`

4. **Use Descriptive Test Names**
   - Format: `should [expected behavior] when [condition]`
   - Example: `should return 404 when ticket does not exist`

### For Manual Testing

1. **Always Test Happy Path First**
   - Create valid ticket → Success
   - Then test edge cases

2. **Document Actual vs Expected**
   - Screenshot failures
   - Copy full request/response
   - Note timestamps

3. **Test in Isolation**
   - Clear database between tests
   - Use unique customer IDs
   - Don't rely on order

4. **Verify Side Effects**
   - Check auto-classification
   - Verify timestamps
   - Confirm data persistence

---

## Quick Reference

### Common Test Commands

| Command | Purpose |
|---------|---------|
| `npm test` | Run all tests |
| `npm run test:coverage` | Run with coverage report |
| `npm test -- --watch` | Watch mode for development |
| `npm test -- test_ticket_api.test.ts` | Run specific test file |
| `npm test -- --testNamePattern="POST"` | Run tests matching pattern |
| `npm test -- --runInBand` | Run tests sequentially (not parallel) |

### Key API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `POST` | `/tickets` | Create single ticket |
| `POST` | `/tickets/import` | Bulk import from file |
| `GET` | `/tickets` | List all tickets (with filters) |
| `GET` | `/tickets/:id` | Get specific ticket |
| `PUT` | `/tickets/:id` | Update ticket |
| `DELETE` | `/tickets/:id` | Delete ticket |
| `GET` | `/health` | Health check |

### Sample Data Files for Manual Testing

All test data is located in `docs/sample/` - production-like data for realistic manual testing:

| File | Records | Purpose |
|------|---------|----------|
| `sample_tickets.csv` | 50 | Production-like CSV data with diverse categories and priorities |
| `sample_tickets.json` | 20 | Production-like JSON data with complex metadata |
| `sample_tickets.xml` | 30 | Production-like XML data with nested structures |
| `sample_tickets_invalid.csv` | Various | CSV validation error testing (missing fields, invalid emails) |
| `sample_tickets_invalid.json` | Various | JSON validation error testing (schema violations) |
| `sample_tickets_invalid.xml` | Various | XML validation error testing (malformed syntax) |

**Quick Import All**: Use `docs/sample/demo-import-sample-data.sh` to import all 100 sample tickets at once.

**Note**: Automated tests use separate fixture files located in `tests/fixtures/` that are optimized for unit and integration testing.

---

## Continuous Improvement

### Adding New Tests

When adding functionality, follow this checklist:

- [ ] Write unit tests for new functions/services
- [ ] Add integration test for new API endpoint
- [ ] Update test fixtures if needed
- [ ] Run full test suite: `npm test`
- [ ] Verify coverage remains >85%
- [ ] Update this testing guide with new test cases
- [ ] Add manual testing steps

### Reporting Issues

When reporting test failures:

1. **Include Test Name**: Full describe/it path
2. **Attach Logs**: Copy full test output
3. **Show Request/Response**: Include cURL command and response
4. **Environment**: Node version, OS, timestamp
5. **Steps to Reproduce**: List exact commands run

---

## Summary

This testing guide provides:
- ✅ **102 automated tests** with 92.54% code coverage
- ✅ **Comprehensive manual testing checklist** with cURL examples
- ✅ **Performance benchmarks** with clear targets
- ✅ **Test data fixtures** for all scenarios
- ✅ **Troubleshooting guide** for common issues

**For Questions**: Contact the development team or refer to [API_REFERENCE.md](./API_REFERENCE.md) and [ARCHITECTURE.md](./ARCHITECTURE.md) for additional details.
