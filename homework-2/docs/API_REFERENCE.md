# API Reference

Complete reference for the Customer Support Ticket Management API.

**Base URL:** `http://localhost:3000`

**Authentication:** Not implemented yet. All endpoints are currently public.

**Content-Type:** All requests and responses use `application/json` unless specified otherwise.

---

## Table of Contents

- [API Reference](#api-reference)
  - [Table of Contents](#table-of-contents)
  - [Endpoints](#endpoints)
    - [Create Ticket](#create-ticket)
    - [Import Tickets](#import-tickets)
    - [List Tickets](#list-tickets)
    - [Get Ticket by ID](#get-ticket-by-id)
    - [Update Ticket](#update-ticket)
    - [Delete Ticket](#delete-ticket)
    - [Auto-Classify Ticket](#auto-classify-ticket)
    - [Health Check](#health-check)
  - [Data Models](#data-models)
    - [Ticket Schema](#ticket-schema)
    - [Metadata Schema](#metadata-schema)
  - [Error Handling](#error-handling)
    - [Standard Error Response](#standard-error-response)
    - [HTTP Status Codes](#http-status-codes)
    - [Validation Error Example](#validation-error-example)
    - [JSON Parse Error](#json-parse-error)
  - [Pagination](#pagination)
  - [Rate Limiting](#rate-limiting)
  - [Versioning](#versioning)
  - [Support](#support)

---

## Endpoints

### Create Ticket

Creates a new support ticket with automatic category and priority classification.

**HTTP Method:** `POST`

**Path:** `/tickets`

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
    "customer_id": "string (required)",
    "customer_email": "string (required, valid email)",
    "customer_name": "string (required)",
    "subject": "string (required, 1-200 characters)",
    "description": "string (required, 10-2000 characters)",
    "category": "account_access | technical_issue | billing_question | feature_request | bug_report | other (optional)",
    "priority": "urgent | high | medium | low (optional)",
    "metadata": {
        "source": "web_form | email | api | chat | phone (required)",
        "browser": "string (optional)",
        "device_type": "desktop | mobile | tablet (required)"
    }
}
```

**Classification Behavior:**
- **Automatic Classification (default):** If `category` and `priority` are NOT provided, the system automatically classifies the ticket based on the subject and description content. The response will include classification metadata (confidence, reasoning, keywords).
- **Manual Override:** If BOTH `category` and `priority` are provided, the system uses those values instead of automatic classification. The ticket's `classification_source` will be set to `"manual"`.
- **Partial Override:** If only one of `category` or `priority` is provided, automatic classification is used for both fields.

**Note:** `tags` and `assigned_to` are system-generated fields that cannot be set during creation. They start as empty array `[]` and `null` respectively.

**Success Response:**

**Status Code:** `201 Created`

**With Automatic Classification:**
```json
{
    "success": true,
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "customer_id": "cust-001",
        "customer_email": "john.doe@example.com",
        "customer_name": "John Doe",
        "subject": "Cannot access my account",
        "description": "I've been locked out after multiple failed login attempts.",
        "category": "account_access",
        "priority": "urgent",
        "status": "new",
        "created_at": "2026-02-08T10:30:00.000Z",
        "updated_at": "2026-02-08T10:30:00.000Z",
        "resolved_at": null,
        "assigned_to": null,
        "tags": [],
        "classification_source": "automatic",
        "metadata": {
            "source": "web_form",
            "browser": "Chrome 121",
            "device_type": "desktop"
        }
    },
    "classification": {
        "confidence": 0.95,
        "reasoning": "Keywords 'locked out', 'login' indicate account access issue.",
        "keywords": ["locked", "login", "urgent", "account"]
    }
}
```

**With Manual Override:**
```json
{
    "success": true,
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "customer_id": "cust-002",
        "customer_email": "jane.smith@example.com",
        "customer_name": "Jane Smith",
        "subject": "Feature suggestion",
        "description": "It would be great to have dark mode.",
        "category": "billing_question",
        "priority": "urgent",
        "status": "new",
        "created_at": "2026-02-08T10:35:00.000Z",
        "updated_at": "2026-02-08T10:35:00.000Z",
        "resolved_at": null,
        "assigned_to": null,
        "tags": [],
        "classification_source": "manual",
        "metadata": {
            "source": "web_form",
            "browser": "Chrome 121",
            "device_type": "desktop"
        }
    }
}
```

**Note:** Manual override response does NOT include the `classification` object since classification was not performed.

**Error Responses:**

**Status Code:** `400 Bad Request` (Validation Error)

```json
{
    "success": false,
    "error": "Validation failed",
    "details": [
        {
            "field": "customer_email",
            "message": "Invalid email"
        },
        {
            "field": "description",
            "message": "Description must be at least 10 characters"
        }
    ]
}
```

**Status Code:** `500 Internal Server Error`

```json
{
    "success": false,
    "error": "Internal server error",
    "message": "An unexpected error occurred"
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:3000/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customer_id": "cust-001",
    "customer_email": "john.doe@example.com",
    "customer_name": "John Doe",
    "subject": "Cannot access my account",
    "description": "I have been locked out of my account after multiple failed login attempts. Need urgent help.",
    "tags": ["login", "urgent"],
    "metadata": {
      "source": "web_form",
      "browser": "Chrome 121",
      "device_type": "desktop"
    }
  }'
```

---

### Import Tickets

Bulk import tickets from CSV, JSON, or XML files. All imported tickets are automatically classified.

**HTTP Method:** `POST`

**Path:** `/tickets/import`

**Request Headers:**
```
Content-Type: multipart/form-data
```

**Request Body:**

Form data with a single file field:
- **Field name:** `file`
- **Accepted formats:** `.csv`, `.json`, `.xml`

**CSV Format Example:**
```csv
customer_id,customer_email,customer_name,subject,description,tags,source,browser,device_type
cust-001,alice@example.com,Alice Johnson,Login issue,Cannot login to my account,login;urgent,web_form,Chrome,desktop
```

**JSON Format Example:**
```json
[
    {
        "customer_id": "cust-001",
        "customer_email": "alice@example.com",
        "customer_name": "Alice Johnson",
        "subject": "Login issue",
        "description": "Cannot login to my account",
        "tags": ["login", "urgent"],
        "metadata": {
            "source": "web_form",
            "browser": "Chrome",
            "device_type": "desktop"
        }
    }
]
```

**XML Format Example:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<tickets>
    <ticket>
        <customer_id>cust-001</customer_id>
        <customer_email>alice@example.com</customer_email>
        <customer_name>Alice Johnson</customer_name>
        <subject>Login issue</subject>
        <description>Cannot login to my account</description>
        <tags>login,urgent</tags>
        <metadata>
            <source>web_form</source>
            <browser>Chrome</browser>
            <device_type>desktop</device_type>
        </metadata>
    </ticket>
</tickets>
```

**Success Response:**

**Status Code:** `200 OK`

```json
{
    "success": true,
    "message": "CSV import completed successfully",
    "format": "CSV",
    "imported": 45,
    "failed": 5,
    "errors": [
        {
            "row": 3,
            "reason": "Invalid email format"
        },
        {
            "row": 7,
            "reason": "Missing required field: subject"
        }
    ],
    "tickets": [
        {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "customer_id": "cust-001",
            "customer_email": "alice@example.com",
            "customer_name": "Alice Johnson",
            "subject": "Login issue",
            "description": "Cannot login to my account",
            "category": "account_access",
            "priority": "high",
            "status": "new",
            "created_at": "2026-02-08T10:30:00.000Z",
            "updated_at": "2026-02-08T10:30:00.000Z",
            "resolved_at": null,
            "assigned_to": null,
            "tags": ["login", "urgent"],
            "metadata": {
                "source": "web_form",
                "browser": "Chrome",
                "device_type": "desktop"
            }
        }
    ]
}
```

**Error Responses:**

**Status Code:** `400 Bad Request` (No File)

```json
{
    "success": false,
    "error": "No file uploaded",
    "details": {
        "message": "Please upload a file using the 'file' field"
    }
}
```

**Status Code:** `400 Bad Request` (Invalid File Type)

```json
{
    "success": false,
    "error": "Invalid file type",
    "details": {
        "message": "Only CSV, JSON, and XML files are allowed"
    }
}
```

**Status Code:** `400 Bad Request` (Import Failed)

```json
{
    "success": false,
    "error": "CSV import failed",
    "imported": 0,
    "failed": 10,
    "errors": [
        {
            "row": 1,
            "reason": "Invalid email format"
        }
    ]
}
```

**cURL Example:**

```bash
curl -X POST http://localhost:3000/tickets/import \
  -F "file=@sample_tickets.csv"
```

---

### List Tickets

Retrieve all tickets with optional filtering and pagination.

**HTTP Method:** `GET`

**Path:** `/tickets`

**Query Parameters:**

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `status` | string | Filter by ticket status | `new`, `in_progress`, `resolved` |
| `priority` | string | Filter by priority level | `urgent`, `high`, `medium`, `low` |
| `category` | string | Filter by category | `account_access`, `technical_issue` |
| `limit` | number | Maximum number of results | `10`, `50`, `100` |
| `offset` | number | Number of results to skip | `0`, `10`, `20` |

**Success Response:**

**Status Code:** `200 OK`

```json
{
    "success": true,
    "data": [
        {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "customer_id": "cust-001",
            "customer_email": "john.doe@example.com",
            "customer_name": "John Doe",
            "subject": "Cannot access my account",
            "description": "I've been locked out of my account",
            "category": "account_access",
            "priority": "urgent",
            "status": "new",
            "created_at": "2026-02-08T10:30:00.000Z",
            "updated_at": "2026-02-08T10:30:00.000Z",
            "resolved_at": null,
            "assigned_to": null,
            "tags": ["login"],
            "metadata": {
                "source": "web_form",
                "browser": "Chrome 121",
                "device_type": "desktop"
            }
        }
    ]
}
```

**Error Responses:**

**Status Code:** `500 Internal Server Error`

```json
{
    "success": false,
    "error": "Internal server error",
    "message": "An unexpected error occurred"
}
```

**cURL Examples:**

```bash
# Get all tickets
curl http://localhost:3000/tickets

# Filter by status
curl "http://localhost:3000/tickets?status=new"

# Filter by priority
curl "http://localhost:3000/tickets?priority=urgent"

# Filter by category
curl "http://localhost:3000/tickets?category=technical_issue"

# Multiple filters with pagination
curl "http://localhost:3000/tickets?status=new&priority=high&limit=10&offset=0"

# Combine all filters
curl "http://localhost:3000/tickets?status=in_progress&priority=urgent&category=account_access&limit=20&offset=10"
```

---

### Get Ticket by ID

Retrieve a specific ticket by its unique identifier.

**HTTP Method:** `GET`

**Path:** `/tickets/:id`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Unique ticket identifier |

**Success Response:**

**Status Code:** `200 OK`

```json
{
    "success": true,
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "customer_id": "cust-001",
        "customer_email": "john.doe@example.com",
        "customer_name": "John Doe",
        "subject": "Cannot access my account",
        "description": "I've been locked out of my account after multiple failed login attempts.",
        "category": "account_access",
        "priority": "urgent",
        "status": "new",
        "created_at": "2026-02-08T10:30:00.000Z",
        "updated_at": "2026-02-08T10:30:00.000Z",
        "resolved_at": null,
        "assigned_to": null,
        "tags": ["login", "urgent"],
        "metadata": {
            "source": "web_form",
            "browser": "Chrome 121",
            "device_type": "desktop"
        }
    }
}
```

**Error Responses:**

**Status Code:** `400 Bad Request` (Invalid UUID)

```json
{
    "success": false,
    "error": "Invalid ticket ID format",
    "details": {
        "field": "id",
        "message": "Must be a valid UUID"
    }
}
```

**Status Code:** `404 Not Found`

```json
{
    "success": false,
    "error": "Ticket not found",
    "details": {
        "id": "550e8400-e29b-41d4-a716-446655440000"
    }
}
```

**cURL Example:**

```bash
curl http://localhost:3000/tickets/550e8400-e29b-41d4-a716-446655440000
```

---

### Update Ticket

Update an existing ticket's information. All fields except `id`, `created_at`, and `customer_id` can be updated.

**HTTP Method:** `PUT`

**Path:** `/tickets/:id`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Unique ticket identifier |

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**

All fields are optional. Only include fields you want to update.

```json
{
    "customer_email": "string (optional, valid email)",
    "customer_name": "string (optional)",
    "subject": "string (optional, 1-200 characters)",
    "description": "string (optional, 10-2000 characters)",
    "category": "string (optional)",
    "priority": "string (optional)",
    "status": "string (optional)",
    "resolved_at": "datetime (optional, nullable)",
    "assigned_to": "string (optional, nullable)",
    "tags": ["string"] (optional),
    "metadata": {
        "source": "string (optional)",
        "browser": "string (optional)",
        "device_type": "string (optional)"
    }
}
```

**Note:** When `status` is changed to `resolved` or `closed`, the `resolved_at` timestamp is automatically set.

**Success Response:**

**Status Code:** `200 OK`

```json
{
    "success": true,
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "customer_id": "cust-001",
        "customer_email": "john.doe@example.com",
        "customer_name": "John Doe",
        "subject": "Cannot access my account",
        "description": "I've been locked out of my account after multiple failed login attempts.",
        "category": "account_access",
        "priority": "urgent",
        "status": "in_progress",
        "created_at": "2026-02-08T10:30:00.000Z",
        "updated_at": "2026-02-08T11:45:00.000Z",
        "resolved_at": null,
        "assigned_to": "agent-123",
        "tags": ["login", "urgent"],
        "metadata": {
            "source": "web_form",
            "browser": "Chrome 121",
            "device_type": "desktop"
        }
    }
}
```

**Error Responses:**

**Status Code:** `400 Bad Request` (Invalid UUID)

```json
{
    "success": false,
    "error": "Invalid ticket ID format"
}
```

**Status Code:** `400 Bad Request` (Validation Error)

```json
{
    "success": false,
    "error": "Validation failed",
    "details": [
        {
            "field": "customer_email",
            "message": "Invalid email"
        }
    ]
}
```

**Status Code:** `404 Not Found`

```json
{
    "success": false,
    "error": "Ticket not found",
    "details": {
        "id": "550e8400-e29b-41d4-a716-446655440000"
    }
}
```

**cURL Examples:**

```bash
# Update ticket status
curl -X PUT http://localhost:3000/tickets/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "in_progress",
    "assigned_to": "agent-123"
  }'

# Update priority and add tags
curl -X PUT http://localhost:3000/tickets/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "priority": "high",
    "tags": ["login", "urgent", "escalated"]
  }'

# Mark as resolved
curl -X PUT http://localhost:3000/tickets/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "resolved"
  }'
```

---

### Delete Ticket

Permanently delete a ticket from the system.

**HTTP Method:** `DELETE`

**Path:** `/tickets/:id`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Unique ticket identifier |

**Success Response:**

**Status Code:** `200 OK`

```json
{
    "success": true,
    "message": "Ticket deleted successfully",
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440000"
    }
}
```

**Error Responses:**

**Status Code:** `400 Bad Request` (Invalid UUID)

```json
{
    "success": false,
    "error": "Invalid ticket ID format",
    "details": {
        "field": "id",
        "message": "Must be a valid UUID"
    }
}
```

**Status Code:** `404 Not Found`

```json
{
    "success": false,
    "error": "Ticket not found",
    "details": {
        "id": "550e8400-e29b-41d4-a716-446655440000"
    }
}
```

**cURL Example:**

```bash
curl -X DELETE http://localhost:3000/tickets/550e8400-e29b-41d4-a716-446655440000
```

---

### Auto-Classify Ticket

Re-run automatic classification on an existing ticket. This endpoint analyzes the ticket's subject and description to determine the most appropriate category and priority, overriding any previous classification (manual or automatic).

**HTTP Method:** `POST`

**Path:** `/tickets/:id/auto-classify`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Unique ticket identifier |

**Success Response:**

**Status Code:** `200 OK`

```json
{
    "success": true,
    "data": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "customer_id": "cust-001",
        "customer_email": "john.doe@example.com",
        "customer_name": "John Doe",
        "subject": "Cannot login to my account",
        "description": "I am having trouble logging in. The system says my password is incorrect.",
        "category": "account_access",
        "priority": "high",
        "status": "new",
        "created_at": "2026-02-08T10:30:00.000Z",
        "updated_at": "2026-02-08T11:45:00.000Z",
        "resolved_at": null,
        "assigned_to": null,
        "tags": [],
        "metadata": {
            "source": "web_form",
            "browser": "Chrome 121",
            "device_type": "desktop"
        },
        "classification_source": "automatic"
    },
    "classification": {
        "confidence": 0.9,
        "reasoning": "Keywords indicate account access issue",
        "keywords": ["login", "password", "account"]
    }
}
```

**Response Fields:**

The `classification` object contains metadata about the auto-classification:

| Field | Type | Description |
|-------|------|-------------|
| `confidence` | number | Classification confidence score (0-1) |
| `reasoning` | string | Explanation of why this category/priority was chosen |
| `keywords` | array | Keywords found that influenced the classification |

**Error Responses:**

**Status Code:** `400 Bad Request` (Invalid UUID)

```json
{
    "success": false,
    "error": "Invalid ticket ID format",
    "details": {
        "field": "id",
        "message": "Must be a valid UUID"
    }
}
```

**Status Code:** `404 Not Found`

```json
{
    "success": false,
    "error": "Ticket not found",
    "details": {
        "id": "550e8400-e29b-41d4-a716-446655440000"
    }
}
```

**cURL Example:**

```bash
# Auto-classify an existing ticket
curl -X POST http://localhost:3000/tickets/550e8400-e29b-41d4-a716-446655440000/auto-classify

# Example with response
curl -X POST http://localhost:3000/tickets/550e8400-e29b-41d4-a716-446655440000/auto-classify \
  | jq '.classification'
```

**Use Cases:**

- Re-classify tickets that were manually categorized incorrectly
- Update classification after ticket description is edited
- Batch re-classification of old tickets when classification rules change
- Quality assurance checks on manual classifications

---

### Health Check

Check if the API server is running and responding.

**HTTP Method:** `GET`

**Path:** `/health`

**Success Response:**

**Status Code:** `200 OK`

```json
{
    "success": true,
    "message": "Server is running",
    "timestamp": "2026-02-08T10:30:00.000Z"
}
```

**cURL Example:**

```bash
curl http://localhost:3000/health
```

---

## Data Models

### Ticket Schema

Complete ticket object structure:

```json
{
    "id": "UUID (auto-generated)",
    "customer_id": "string",
    "customer_email": "string (valid email)",
    "customer_name": "string",
    "subject": "string (1-200 characters)",
    "description": "string (10-2000 characters)",
    "category": "account_access | technical_issue | billing_question | feature_request | bug_report | other",
    "priority": "urgent | high | medium | low",
    "status": "new | in_progress | waiting_customer | resolved | closed",
    "created_at": "ISO 8601 datetime (auto-generated)",
    "updated_at": "ISO 8601 datetime (auto-updated)",
    "resolved_at": "ISO 8601 datetime (nullable, auto-set when resolved)",
    "assigned_to": "string (nullable)",
    "tags": ["string"],
    "metadata": {
        "source": "web_form | email | api | chat | phone",
        "browser": "string",
        "device_type": "desktop | mobile | tablet"
    }
}
```

**Field Descriptions:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | UUID | Auto | Unique identifier generated by the system |
| `customer_id` | string | Yes | External customer identifier |
| `customer_email` | string | Yes | Valid email address |
| `customer_name` | string | Yes | Customer's full name |
| `subject` | string | Yes | Brief description (1-200 chars) |
| `description` | string | Yes | Detailed description (10-2000 chars) |
| `category` | enum | Auto | Auto-classified category |
| `priority` | enum | Auto | Auto-assigned priority level |
| `status` | enum | Auto | Current ticket status (starts as "new") |
| `created_at` | datetime | Auto | Ticket creation timestamp |
| `updated_at` | datetime | Auto | Last update timestamp |
| `resolved_at` | datetime | Auto | Resolution timestamp (null until resolved) |
| `assigned_to` | string | No | Agent/team identifier |
| `tags` | array | No | Custom tags for categorization |
| `metadata` | object | Yes | Additional context information |

### Metadata Schema

```json
{
    "source": "web_form | email | api | chat | phone",
    "browser": "string (e.g., 'Chrome 121', 'Firefox 122')",
    "device_type": "desktop | mobile | tablet"
}
```

---

## Error Handling

All error responses follow a consistent format:

### Standard Error Response

```json
{
    "success": false,
    "error": "Human-readable error message",
    "details": {} or []
}
```

### HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| `200` | OK | Successful GET, PUT, DELETE |
| `201` | Created | Successful POST (new resource) |
| `400` | Bad Request | Invalid input, validation errors |
| `404` | Not Found | Resource doesn't exist |
| `500` | Internal Server Error | Server-side error |

### Validation Error Example

```json
{
    "success": false,
    "error": "Validation failed",
    "details": [
        {
            "field": "customer_email",
            "message": "Invalid email"
        },
        {
            "field": "description",
            "message": "Description must be at least 10 characters"
        }
    ]
}
```

### JSON Parse Error

```json
{
    "success": false,
    "error": "Invalid JSON format",
    "details": {
        "message": "Request body must be valid JSON"
    }
}
```

---

## Pagination

The API supports pagination for the `GET /tickets` endpoint.

**Query Parameters:**

- `limit`: Number of results to return (default: all)
- `offset`: Number of results to skip (default: 0)

**Example:**

```bash
# Get first 10 tickets
curl "http://localhost:3000/tickets?limit=10&offset=0"

# Get next 10 tickets
curl "http://localhost:3000/tickets?limit=10&offset=10"

# Get tickets 21-30
curl "http://localhost:3000/tickets?limit=10&offset=20"
```

**Note:** The response does not currently include pagination metadata (total count, page info). This may be added in future versions.

**Best Practices:**
- Use reasonable `limit` values (10-100) for better performance
- Implement client-side pagination based on response length
- Combine with filters to narrow down results

---

## Rate Limiting

**Status:** Not implemented yet.

Rate limiting will be added in a future version to prevent abuse and ensure fair usage.

---

## Versioning

**Current Version:** v1 (implicit)

The API does not currently use versioning in the URL. Future versions may introduce versioning like `/v1/tickets`.

---

## Support

For questions or issues with the API:
- Check the [README](../README.md) for setup instructions
- Review [ARCHITECTURE.md](ARCHITECTURE.md) for technical details
- Contact: Not configured yet

---

**Last Updated:** February 8, 2026
