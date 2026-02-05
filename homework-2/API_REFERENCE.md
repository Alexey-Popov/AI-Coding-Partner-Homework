# API Reference

Complete API documentation for the Customer Support Ticket System.

---

## Base URL

```
http://localhost:3000
```

---

## Ticket Model

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customer_id": "CUST-001",
  "customer_email": "john@example.com",
  "customer_name": "John Doe",
  "subject": "Cannot login to my account",
  "description": "I forgot my password and cannot access my account.",
  "category": "account_access",
  "priority": "high",
  "status": "new",
  "created_at": "2024-01-15T10:30:00.000Z",
  "updated_at": "2024-01-15T10:30:00.000Z",
  "resolved_at": null,
  "assigned_to": null,
  "tags": ["login", "password"],
  "metadata": {
    "source": "web_form",
    "browser": "Chrome",
    "device_type": "desktop"
  }
}
```

### Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | UUID | Auto | Unique ticket identifier |
| `customer_id` | string | Yes | Customer identifier |
| `customer_email` | email | Yes | Valid email address |
| `customer_name` | string | Yes | Customer full name |
| `subject` | string(1-200) | Yes | Ticket subject |
| `description` | string(10-2000) | Yes | Detailed description |
| `category` | enum | No | Ticket category |
| `priority` | enum | No | Priority level |
| `status` | enum | No | Current status |
| `assigned_to` | string | No | Assigned agent ID |
| `tags` | array | No | Array of tag strings |
| `metadata` | object | No | Additional metadata |

### Enum Values

**Category**: `account_access`, `technical_issue`, `billing_question`, `feature_request`, `bug_report`, `other`

**Priority**: `urgent`, `high`, `medium`, `low`

**Status**: `new`, `in_progress`, `waiting_customer`, `resolved`, `closed`

**Source**: `web_form`, `email`, `api`, `chat`, `phone`

**Device Type**: `desktop`, `mobile`, `tablet`

---

## Endpoints

### Create Ticket

Creates a new support ticket.

```
POST /tickets
```

**Query Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `autoClassify` | boolean | Auto-classify after creation |

**Request Body**

```json
{
  "customer_id": "CUST-001",
  "customer_email": "john@example.com",
  "customer_name": "John Doe",
  "subject": "Cannot login to my account",
  "description": "I forgot my password and the reset email never arrives."
}
```

**Response** `201 Created`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customer_id": "CUST-001",
  "customer_email": "john@example.com",
  "customer_name": "John Doe",
  "subject": "Cannot login to my account",
  "description": "I forgot my password and the reset email never arrives.",
  "category": "other",
  "priority": "medium",
  "status": "new",
  "created_at": "2024-01-15T10:30:00.000Z",
  "updated_at": "2024-01-15T10:30:00.000Z",
  "resolved_at": null,
  "assigned_to": null,
  "tags": [],
  "metadata": {
    "source": "api",
    "browser": null,
    "device_type": null
  }
}
```

**cURL Example**

```bash
curl -X POST http://localhost:3000/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customer_id": "CUST-001",
    "customer_email": "john@example.com",
    "customer_name": "John Doe",
    "subject": "Cannot login to my account",
    "description": "I forgot my password and the reset email never arrives."
  }'
```

---

### Bulk Import Tickets

Import multiple tickets from CSV, JSON, or XML files.

```
POST /tickets/import
```

**Query Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `format` | string | File format: `csv`, `json`, `xml` |
| `autoClassify` | boolean | Auto-classify imported tickets |

**Request**

Send file as `multipart/form-data` with field name `file`.

**Response** `200 OK`

```json
{
  "message": "Import completed",
  "summary": {
    "total": 50,
    "successful": 48,
    "failed": 2
  },
  "errors": [
    {
      "row": 5,
      "errors": [
        { "field": "customer_email", "message": "customer_email must be a valid email address" }
      ]
    }
  ]
}
```

**cURL Example**

```bash
# Import CSV with auto-classification
curl -X POST "http://localhost:3000/tickets/import?format=csv&autoClassify=true" \
  -F "file=@data/sample_tickets.csv"

# Import JSON
curl -X POST "http://localhost:3000/tickets/import?format=json" \
  -F "file=@data/sample_tickets.json"

# Import XML
curl -X POST "http://localhost:3000/tickets/import?format=xml" \
  -F "file=@data/sample_tickets.xml"
```

---

### List Tickets

Get all tickets with optional filtering.

```
GET /tickets
```

**Query Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `category` | string | Filter by category |
| `priority` | string | Filter by priority |
| `status` | string | Filter by status |
| `assigned_to` | string | Filter by assigned agent |
| `customer_id` | string | Filter by customer |
| `from` | date | Filter from date (YYYY-MM-DD) |
| `to` | date | Filter to date (YYYY-MM-DD) |

**Response** `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "customer_id": "CUST-001",
    ...
  }
]
```

**cURL Examples**

```bash
# Get all tickets
curl http://localhost:3000/tickets

# Filter by category and priority
curl "http://localhost:3000/tickets?category=technical_issue&priority=high"

# Filter by date range
curl "http://localhost:3000/tickets?from=2024-01-01&to=2024-01-31"
```

---

### Get Ticket by ID

Retrieve a specific ticket.

```
GET /tickets/:id
```

**Response** `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customer_id": "CUST-001",
  ...
}
```

**Error Response** `404 Not Found`

```json
{
  "error": "Not found",
  "message": "Ticket with ID xxx not found"
}
```

**cURL Example**

```bash
curl http://localhost:3000/tickets/550e8400-e29b-41d4-a716-446655440000
```

---

### Update Ticket

Update an existing ticket.

```
PUT /tickets/:id
```

**Request Body** (all fields optional)

```json
{
  "status": "in_progress",
  "priority": "high",
  "assigned_to": "agent-1"
}
```

**Response** `200 OK`

Returns the updated ticket object.

**cURL Example**

```bash
curl -X PUT http://localhost:3000/tickets/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{"status": "in_progress", "assigned_to": "agent-1"}'
```

---

### Delete Ticket

Delete a ticket.

```
DELETE /tickets/:id
```

**Response** `204 No Content`

**cURL Example**

```bash
curl -X DELETE http://localhost:3000/tickets/550e8400-e29b-41d4-a716-446655440000
```

---

### Auto-Classify Ticket

Automatically classify a ticket's category and priority.

```
POST /tickets/:id/auto-classify
```

**Query Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `apply` | boolean | Apply results to ticket (default: true) |

**Response** `200 OK`

```json
{
  "ticket_id": "550e8400-e29b-41d4-a716-446655440000",
  "category": "account_access",
  "category_confidence": 0.8,
  "priority": "urgent",
  "priority_confidence": 0.9,
  "reasoning": "Category 'account_access' detected based on keywords: login, password. Priority 'urgent' assigned based on keywords: can't access.",
  "keywords_found": {
    "category": ["login", "password"],
    "priority": ["can't access"]
  }
}
```

**cURL Example**

```bash
# Classify and apply results
curl -X POST http://localhost:3000/tickets/550e8400-e29b-41d4-a716-446655440000/auto-classify

# Classify without applying (dry run)
curl -X POST "http://localhost:3000/tickets/550e8400-e29b-41d4-a716-446655440000/auto-classify?apply=false"
```

---

## Error Responses

### Validation Error (400)

```json
{
  "error": "Validation failed",
  "details": [
    { "field": "customer_email", "message": "customer_email must be a valid email address" },
    { "field": "description", "message": "description must be 10-2000 characters" }
  ]
}
```

### Not Found (404)

```json
{
  "error": "Not found",
  "message": "Ticket with ID xxx not found"
}
```

### Internal Server Error (500)

```json
{
  "error": "Internal server error"
}
```

---

## Health Check

```
GET /health
```

**Response** `200 OK`

```json
{
  "status": "ok",
  "message": "Customer Support System API is running"
}
```
