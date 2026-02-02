# API Reference

Base URL: `http://localhost:8080`

## Endpoints

### Create Ticket

**POST** `/tickets`

```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "customerEmail": "user@example.com",
    "customerName": "John Doe",
    "subject": "Login Issue",
    "description": "Cannot login to my account",
    "category": "ACCOUNT_ACCESS",
    "priority": "HIGH",
    "status": "NEW"
  }'
```

**Response** `201 Created`
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customerId": "CUST-001",
  "customerEmail": "user@example.com",
  "customerName": "John Doe",
  "subject": "Login Issue",
  "description": "Cannot login to my account",
  "category": "ACCOUNT_ACCESS",
  "priority": "HIGH",
  "status": "NEW",
  "createdAt": "2026-02-02T10:30:00",
  "updatedAt": "2026-02-02T10:30:00"
}
```

### Create with Auto-Classification

**POST** `/tickets?autoClassify=true`

Automatically determines category and priority based on content.

```bash
curl -X POST "http://localhost:8080/tickets?autoClassify=true" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-002",
    "customerEmail": "user@example.com",
    "customerName": "Jane Smith",
    "subject": "Critical production error",
    "description": "The application crashes when users click submit"
  }'
```

### Bulk Import

**POST** `/tickets/import`

```bash
# CSV Import
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@sample_tickets.csv" \
  -F "format=csv"

# JSON Import
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@sample_tickets.json" \
  -F "format=json"

# XML Import
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@sample_tickets.xml" \
  -F "format=xml"
```

**Response** `200 OK`
```json
{
  "totalRecords": 50,
  "successfulRecords": 48,
  "failedRecords": 2,
  "errors": [
    {
      "lineNumber": 5,
      "recordData": "{customer_id=INVALID...}",
      "errorMessage": "Invalid email format"
    }
  ]
}
```

### Get All Tickets

**GET** `/tickets`

```bash
# Get all tickets
curl http://localhost:8080/tickets

# Filter by category
curl "http://localhost:8080/tickets?category=TECHNICAL_ISSUE"

# Filter by priority
curl "http://localhost:8080/tickets?priority=URGENT"

# Filter by status
curl "http://localhost:8080/tickets?status=NEW"

# Combined filters
curl "http://localhost:8080/tickets?category=BUG_REPORT&priority=HIGH"
```

**Response** `200 OK`
```json
[
  {
    "id": "uuid",
    "customerId": "CUST-001",
    "subject": "Issue title",
    "category": "TECHNICAL_ISSUE",
    "priority": "HIGH",
    "status": "NEW"
  }
]
```

### Get Ticket by ID

**GET** `/tickets/{id}`

```bash
curl http://localhost:8080/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Response** `200 OK` - Full ticket object

### Update Ticket

**PUT** `/tickets/{id}`

```bash
curl -X PUT http://localhost:8080/tickets/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "customerEmail": "user@example.com",
    "customerName": "John Doe",
    "subject": "Updated Subject",
    "description": "Updated description",
    "category": "TECHNICAL_ISSUE",
    "priority": "MEDIUM",
    "status": "RESOLVED"
  }'
```

**Response** `200 OK` - Updated ticket object

### Delete Ticket

**DELETE** `/tickets/{id}`

```bash
curl -X DELETE http://localhost:8080/tickets/{id}
```

**Response** `204 No Content`

### Auto-Classify Ticket

**POST** `/tickets/{id}/auto-classify`

```bash
curl -X POST http://localhost:8080/tickets/{id}/auto-classify
```

**Response** `200 OK`
```json
{
  "category": "TECHNICAL_ISSUE",
  "priority": "URGENT",
  "confidence": 0.95,
  "reasoning": "Classified as TECHNICAL_ISSUE/URGENT based on keywords: crash, error, critical",
  "keywordsFound": ["crash", "error", "critical"]
}
```

## Data Models

### Ticket Schema

```json
{
  "id": "string (UUID)",
  "customerId": "string (required)",
  "customerEmail": "string (email, required)",
  "customerName": "string (required)",
  "subject": "string (1-200 chars, required)",
  "description": "string (10-2000 chars, required)",
  "category": "enum (ACCOUNT_ACCESS | TECHNICAL_ISSUE | BILLING_QUESTION | FEATURE_REQUEST | BUG_REPORT | OTHER)",
  "priority": "enum (URGENT | HIGH | MEDIUM | LOW)",
  "status": "enum (NEW | IN_PROGRESS | WAITING_CUSTOMER | RESOLVED | CLOSED)",
  "createdAt": "datetime (auto)",
  "updatedAt": "datetime (auto)",
  "resolvedAt": "datetime (nullable)",
  "assignedTo": "string (nullable)",
  "tags": ["array of strings"],
  "metadata": {
    "source": "enum (WEB_FORM | EMAIL | API | CHAT | PHONE)",
    "browser": "string",
    "deviceType": "enum (DESKTOP | MOBILE | TABLET)"
  }
}
```

### Import Result Schema

```json
{
  "totalRecords": "number",
  "successfulRecords": "number",
  "failedRecords": "number",
  "errors": [
    {
      "lineNumber": "number",
      "recordData": "string",
      "errorMessage": "string"
    }
  ]
}
```

## Error Responses

### 400 Bad Request
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid email format",
  "status": 400
}
```

### 404 Not Found
```json
{
  "error": "NOT_FOUND",
  "message": "Ticket not found: {id}",
  "status": 404
}
```

### 500 Internal Server Error
```json
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred",
  "status": 500
}
```

## Classification Rules

### Categories

- **ACCOUNT_ACCESS**: login, password, 2FA, authentication
- **TECHNICAL_ISSUE**: bugs, errors, crashes, exceptions
- **BILLING_QUESTION**: payments, invoices, refunds
- **FEATURE_REQUEST**: enhancements, suggestions, improvements
- **BUG_REPORT**: defects with reproduction steps
- **OTHER**: uncategorizable tickets

### Priorities

- **URGENT**: can't access, critical, production down, security
- **HIGH**: important, blocking, ASAP
- **MEDIUM**: default (no keywords matched)
- **LOW**: minor, cosmetic, suggestion
