# API Reference - Customer Support Ticket System

**Version:** 1.0.0
**Base URL:** `http://localhost:8000`
**Authentication:** None (public API)

## Table of Contents

1. [Data Models](#data-models)
2. [Ticket Operations](#ticket-operations)
3. [Import Operations](#import-operations)
4. [Classification Operations](#classification-operations)
5. [Error Codes](#error-codes)

---

## Data Models

### Ticket

Complete ticket object with all fields.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | UUID | Auto-generated | Unique ticket identifier |
| `customer_id` | string | Yes | Customer identifier (e.g., "CUST-0001") |
| `customer_email` | string (email) | Yes | Valid email address |
| `customer_name` | string | Yes | Customer name (2-100 chars) |
| `subject` | string | Yes | Ticket title (1-200 chars) |
| `description` | string | Yes | Detailed description (10-2000 chars) |
| `category` | enum | No | Ticket category (default: inferred or "other") |
| `priority` | enum | No | Priority level (default: "medium") |
| `status` | enum | No | Ticket status (default: "new") |
| `tags` | array[string] | No | Tags for organization (default: []) |
| `metadata` | object | No | Additional metadata |
| `assigned_to` | string | No | Agent email (nullable) |
| `created_at` | datetime | Auto-generated | Creation timestamp (ISO 8601) |
| `updated_at` | datetime | Auto-generated | Last update timestamp (ISO 8601) |
| `resolved_at` | datetime | Auto-set | Resolution timestamp (nullable) |

### TicketCreate

Request model for creating a new ticket (subset of Ticket).

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `customer_id` | string | Yes | Customer identifier |
| `customer_email` | string (email) | Yes | Valid email address |
| `customer_name` | string | Yes | Customer name |
| `subject` | string | Yes | Ticket title (1-200 chars) |
| `description` | string | Yes | Description (10-2000 chars) |
| `category` | enum | No | Ticket category |
| `priority` | enum | No | Priority level |
| `tags` | array[string] | No | Tags (default: []) |
| `metadata` | TicketMetadata | No | Additional metadata |

### TicketUpdate

Request model for updating a ticket (all fields optional).

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `subject` | string | No | Updated title |
| `description` | string | No | Updated description |
| `category` | enum | No | Updated category |
| `priority` | enum | No | Updated priority |
| `status` | enum | No | Updated status |
| `tags` | array[string] | No | Updated tags |
| `assigned_to` | string | No | Assigned agent email |

### TicketMetadata

Metadata object for ticket context.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `source` | enum | Yes | Origin: web_form, email, api, chat, phone |
| `browser` | string | No | Browser name (if web_form) |
| `device_type` | enum | No | Device: desktop, mobile, tablet |

### Enums

**TicketCategory:**
- `account_access`
- `technical_issue`
- `billing_question`
- `feature_request`
- `bug_report`
- `other`

**TicketPriority:**
- `urgent`
- `high`
- `medium`
- `low`

**TicketStatus:**
- `new`
- `in_progress`
- `waiting_customer`
- `resolved`
- `closed`

**TicketSource:**
- `web_form`
- `email`
- `api`
- `chat`
- `phone`

### ImportResult

Response model for bulk import operations.

| Field | Type | Description |
|-------|------|-------------|
| `total` | integer | Total records processed |
| `success_count` | integer | Successfully imported tickets |
| `error_count` | integer | Failed imports |
| `errors` | array[object] | Error details (row number, message) |

### ClassificationResult

Response model for auto-classification.

| Field | Type | Description |
|-------|------|-------------|
| `suggested_category` | enum | Recommended category |
| `suggested_priority` | enum | Recommended priority |
| `confidence` | float | Confidence score (0.0-1.0) |
| `reasoning` | string | Explanation of classification |
| `keywords_found` | array[string] | Matched keywords |

---

## Ticket Operations

### Create Ticket

Create a new support ticket.

**Endpoint:** `POST /tickets`

**Request Body:**
```json
{
  "customer_id": "CUST-0001",
  "customer_email": "user@example.com",
  "customer_name": "John Doe",
  "subject": "Cannot access my account",
  "description": "I have been unable to log in since this morning. The system shows an access denied error message.",
  "category": "account_access",
  "priority": "high",
  "tags": ["urgent", "login"],
  "metadata": {
    "source": "email",
    "browser": "Chrome",
    "device_type": "desktop"
  }
}
```

**Response:** `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customer_id": "CUST-0001",
  "customer_email": "user@example.com",
  "customer_name": "John Doe",
  "subject": "Cannot access my account",
  "description": "I have been unable to log in since this morning...",
  "category": "account_access",
  "priority": "high",
  "status": "new",
  "tags": ["urgent", "login"],
  "metadata": {
    "source": "email",
    "browser": "Chrome",
    "device_type": "desktop"
  },
  "assigned_to": null,
  "created_at": "2024-01-15T10:30:00Z",
  "updated_at": "2024-01-15T10:30:00Z",
  "resolved_at": null
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8000/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customer_id": "CUST-0001",
    "customer_email": "user@example.com",
    "customer_name": "John Doe",
    "subject": "Cannot access my account",
    "description": "I have been unable to log in since this morning.",
    "category": "account_access",
    "priority": "high",
    "tags": ["urgent", "login"],
    "metadata": {
      "source": "email"
    }
  }'
```

**Error Responses:**
- `400 Bad Request` - Malformed JSON
- `422 Unprocessable Entity` - Validation errors (invalid email, subject too short, etc.)

---

### List Tickets

Retrieve all tickets with optional filtering.

**Endpoint:** `GET /tickets`

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `category` | enum | No | Filter by category |
| `priority` | enum | No | Filter by priority |
| `status` | enum | No | Filter by status |

**Response:** `200 OK`
```json
{
  "items": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "customer_id": "CUST-0001",
      "customer_email": "user@example.com",
      "customer_name": "John Doe",
      "subject": "Cannot access my account",
      "status": "new",
      "category": "account_access",
      "priority": "high",
      "created_at": "2024-01-15T10:30:00Z",
      "updated_at": "2024-01-15T10:30:00Z"
    }
  ],
  "total": 1
}
```

**cURL Examples:**
```bash
# Get all tickets
curl http://localhost:8000/tickets

# Filter by category
curl http://localhost:8000/tickets?category=account_access

# Filter by priority and status
curl "http://localhost:8000/tickets?priority=urgent&status=new"

# Multiple filters
curl "http://localhost:8000/tickets?category=billing_question&status=in_progress"
```

---

### Get Ticket by ID

Retrieve a single ticket by its UUID.

**Endpoint:** `GET /tickets/{ticket_id}`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `ticket_id` | UUID | Yes | Ticket unique identifier |

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customer_id": "CUST-0001",
  "customer_email": "user@example.com",
  "customer_name": "John Doe",
  "subject": "Cannot access my account",
  "description": "I have been unable to log in...",
  "category": "account_access",
  "priority": "high",
  "status": "new",
  "tags": ["urgent", "login"],
  "metadata": {
    "source": "email",
    "browser": "Chrome",
    "device_type": "desktop"
  },
  "assigned_to": null,
  "created_at": "2024-01-15T10:30:00Z",
  "updated_at": "2024-01-15T10:30:00Z",
  "resolved_at": null
}
```

**cURL Example:**
```bash
curl http://localhost:8000/tickets/550e8400-e29b-41d4-a716-446655440000
```

**Error Responses:**
- `404 Not Found` - Ticket does not exist

---

### Update Ticket

Partially update an existing ticket.

**Endpoint:** `PATCH /tickets/{ticket_id}`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `ticket_id` | UUID | Yes | Ticket unique identifier |

**Request Body:** (all fields optional)
```json
{
  "status": "in_progress",
  "priority": "urgent",
  "assigned_to": "agent@support.com"
}
```

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customer_id": "CUST-0001",
  "customer_email": "user@example.com",
  "customer_name": "John Doe",
  "subject": "Cannot access my account",
  "description": "I have been unable to log in...",
  "category": "account_access",
  "priority": "urgent",
  "status": "in_progress",
  "assigned_to": "agent@support.com",
  "created_at": "2024-01-15T10:30:00Z",
  "updated_at": "2024-01-15T10:35:00Z",
  "resolved_at": null
}
```

**cURL Example:**
```bash
curl -X PATCH http://localhost:8000/tickets/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "in_progress",
    "assigned_to": "agent@support.com"
  }'
```

**Error Responses:**
- `404 Not Found` - Ticket does not exist
- `422 Unprocessable Entity` - Validation errors

---

### Delete Ticket

Permanently delete a ticket.

**Endpoint:** `DELETE /tickets/{ticket_id}`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `ticket_id` | UUID | Yes | Ticket unique identifier |

**Response:** `204 No Content`

**cURL Example:**
```bash
curl -X DELETE http://localhost:8000/tickets/550e8400-e29b-41d4-a716-446655440000
```

**Error Responses:**
- `404 Not Found` - Ticket does not exist

---

### Get Statistics

Retrieve ticket statistics grouped by category, priority, and status.

**Endpoint:** `GET /tickets/stats`

**Response:** `200 OK`
```json
{
  "total": 150,
  "by_category": {
    "account_access": 45,
    "technical_issue": 38,
    "billing_question": 32,
    "feature_request": 20,
    "bug_report": 10,
    "other": 5
  },
  "by_priority": {
    "urgent": 15,
    "high": 40,
    "medium": 70,
    "low": 25
  },
  "by_status": {
    "new": 50,
    "in_progress": 60,
    "waiting_customer": 15,
    "resolved": 20,
    "closed": 5
  }
}
```

**cURL Example:**
```bash
curl http://localhost:8000/tickets/stats
```

---

## Import Operations

### Import from CSV

Bulk import tickets from CSV file.

**Endpoint:** `POST /import/csv`

**Request:** `multipart/form-data`

**CSV Format:**
```csv
customer_id,customer_email,customer_name,subject,description,category,priority,tags,source,browser,device_type
CUST-0001,user@example.com,John Doe,Login issue,Cannot access dashboard,account_access,high,login;urgent,email,,
CUST-0002,jane@example.com,Jane Smith,Feature request,Please add dark mode,feature_request,low,enhancement,web_form,Chrome,desktop
```

**Response:** `200 OK`
```json
{
  "total": 2,
  "success_count": 2,
  "error_count": 0,
  "errors": []
}
```

**Response with Errors:**
```json
{
  "total": 3,
  "success_count": 2,
  "error_count": 1,
  "errors": [
    {
      "row": 3,
      "message": "Invalid email format: not-an-email"
    }
  ]
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8000/import/csv \
  -F "file=@fixtures/sample_tickets.csv"
```

**Error Responses:**
- `400 Bad Request` - Malformed CSV file or missing headers
- `422 Unprocessable Entity` - Invalid data in CSV rows

---

### Import from JSON

Bulk import tickets from JSON file.

**Endpoint:** `POST /import/json`

**Request:** `multipart/form-data`

**JSON Format:**
```json
[
  {
    "customer_id": "CUST-0001",
    "customer_email": "user@example.com",
    "customer_name": "John Doe",
    "subject": "Login issue",
    "description": "Cannot access dashboard after password reset",
    "category": "account_access",
    "priority": "high",
    "tags": ["bug", "login"],
    "metadata": {
      "source": "web_form",
      "browser": "Chrome",
      "device_type": "desktop"
    }
  }
]
```

**Response:** `200 OK`
```json
{
  "total": 1,
  "success_count": 1,
  "error_count": 0,
  "errors": []
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8000/import/json \
  -F "file=@fixtures/sample_tickets.json"
```

---

### Import from XML

Bulk import tickets from XML file.

**Endpoint:** `POST /import/xml`

**Request:** `multipart/form-data`

**XML Format:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<tickets>
  <ticket>
    <customer_id>CUST-0001</customer_id>
    <customer_email>user@example.com</customer_email>
    <customer_name>John Doe</customer_name>
    <subject>Login issue</subject>
    <description>Cannot access dashboard</description>
    <category>account_access</category>
    <priority>high</priority>
    <tags>
      <tag>login</tag>
      <tag>urgent</tag>
    </tags>
    <metadata>
      <source>web_form</source>
      <browser>Chrome</browser>
      <device_type>desktop</device_type>
    </metadata>
  </ticket>
</tickets>
```

**Response:** `200 OK`
```json
{
  "total": 1,
  "success_count": 1,
  "error_count": 0,
  "errors": []
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8000/import/xml \
  -F "file=@fixtures/sample_tickets.xml"
```

---

## Classification Operations

### Classify Single Ticket

Trigger auto-classification for a specific ticket.

**Endpoint:** `POST /tickets/{ticket_id}/classify`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `ticket_id` | UUID | Yes | Ticket unique identifier |

**Response:** `200 OK`
```json
{
  "suggested_category": "billing_question",
  "suggested_priority": "medium",
  "confidence": 0.85,
  "reasoning": "Keywords found: invoice, charge, refund",
  "keywords_found": ["invoice", "charge", "refund"]
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8000/tickets/550e8400-e29b-41d4-a716-446655440000/classify
```

**Error Responses:**
- `404 Not Found` - Ticket does not exist

---

### Classify All Tickets

Auto-classify all tickets in the system.

**Endpoint:** `POST /tickets/classify-all`

**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "suggested_category": "billing_question",
    "suggested_priority": "medium",
    "confidence": 0.85
  },
  {
    "id": "660f9511-f3ac-52e5-b827-557766551111",
    "suggested_category": "technical_issue",
    "suggested_priority": "high",
    "confidence": 0.92
  }
]
```

**cURL Example:**
```bash
curl -X POST http://localhost:8000/tickets/classify-all
```

---

## Error Codes

### HTTP Status Codes

| Code | Description | When It Occurs |
|------|-------------|----------------|
| `200 OK` | Success | Successful GET/PATCH/POST operations |
| `201 Created` | Resource created | Successful ticket creation |
| `204 No Content` | Success, no body | Successful DELETE operation |
| `400 Bad Request` | Malformed request | Invalid JSON, malformed CSV/XML |
| `404 Not Found` | Resource not found | Ticket ID does not exist |
| `422 Unprocessable Entity` | Validation error | Invalid field values, constraint violations |
| `500 Internal Server Error` | Server error | Unexpected server errors |

### Validation Error Response Format

```json
{
  "detail": [
    {
      "loc": ["body", "customer_email"],
      "msg": "value is not a valid email address",
      "type": "value_error.email"
    },
    {
      "loc": ["body", "subject"],
      "msg": "ensure this value has at least 1 characters",
      "type": "value_error.any_str.min_length"
    }
  ]
}
```

### Common Validation Errors

| Field | Error | Solution |
|-------|-------|----------|
| `customer_email` | Invalid email format | Use valid email (user@example.com) |
| `subject` | String too short | Min 1 character, max 200 |
| `description` | String too short | Min 10 characters, max 2000 |
| `category` | Invalid enum value | Use valid category enum |
| `priority` | Invalid enum value | Use valid priority enum |
| `tags` | Invalid type | Must be array of strings |

---

## Rate Limiting

**Current Implementation:** No rate limiting.

**Recommendations for Production:**
- Implement rate limiting (e.g., 100 requests/minute per IP)
- Use API keys for authentication
- Add request throttling for bulk operations

---

## OpenAPI Documentation

Interactive API documentation is available at:
- **Swagger UI:** http://localhost:8000/docs
- **ReDoc:** http://localhost:8000/redoc
- **OpenAPI Schema:** http://localhost:8000/openapi.json

---

## Support

For issues or questions, refer to:
- [README.md](../README.md) - Project overview and quick start
- [TESTING_GUIDE.md](TESTING_GUIDE.md) - Testing and validation
- [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture and design decisions
