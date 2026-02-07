# SupportIQ Ticket System - API Reference

**Base URL:** `http://localhost:8080`

All request and response bodies use **snake_case** property naming. Timestamps are serialized as ISO 8601 strings (e.g., `"2025-01-15T10:30:00Z"`).

---

## Table of Contents

1. [Create Ticket](#1-create-ticket)
2. [Get Ticket by ID](#2-get-ticket-by-id)
3. [List Tickets](#3-list-tickets)
4. [Update Ticket](#4-update-ticket)
5. [Delete Ticket](#5-delete-ticket)
6. [Auto-Classify Ticket](#6-auto-classify-ticket)
7. [Import Tickets](#7-import-tickets)
8. [Error Response Format](#error-response-format)
9. [Enums](#enums)

---

## 1. Create Ticket

Creates a new support ticket.

**Endpoint:** `POST /api/tickets`

**Status:** `201 Created`

### Request Body

| Field            | Type     | Required | Constraints                          |
|------------------|----------|----------|--------------------------------------|
| `customer_name`  | string   | Yes      | Max 255 characters                   |
| `customer_email` | string   | Yes      | Must be a valid email                |
| `subject`        | string   | Yes      | Max 500 characters                   |
| `description`    | string   | Yes      | Max 10,000 characters                |
| `category`       | string   | No       | See [TicketCategory](#ticketcategory) |
| `priority`       | string   | No       | See [TicketPriority](#ticketpriority) |
| `source`         | string   | No       | See [Source](#source)                 |
| `browser`        | string   | No       |                                      |
| `device_type`    | string   | No       | See [DeviceType](#devicetype)         |
| `tags`           | string[] | No       | Set of tag strings                   |

### Request Body Example

```json
{
  "customer_name": "Jane Doe",
  "customer_email": "jane.doe@example.com",
  "subject": "Cannot access my account after password reset",
  "description": "I reset my password yesterday but now I cannot log in. I keep getting an 'invalid credentials' error even though I am using the new password.",
  "category": "account_access",
  "priority": "high",
  "source": "web_form",
  "browser": "Chrome 120",
  "device_type": "desktop",
  "tags": ["login", "password-reset"]
}
```

### Response Body Example

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customer_name": "Jane Doe",
  "customer_email": "jane.doe@example.com",
  "subject": "Cannot access my account after password reset",
  "description": "I reset my password yesterday but now I cannot log in. I keep getting an 'invalid credentials' error even though I am using the new password.",
  "category": "account_access",
  "priority": "high",
  "status": "new",
  "source": "web_form",
  "browser": "Chrome 120",
  "device_type": "desktop",
  "tags": ["login", "password-reset"],
  "classification_confidence": null,
  "classification_reasoning": null,
  "created_at": "2025-01-15T10:30:00Z",
  "updated_at": "2025-01-15T10:30:00Z"
}
```

### curl Example

```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customer_name": "Jane Doe",
    "customer_email": "jane.doe@example.com",
    "subject": "Cannot access my account after password reset",
    "description": "I reset my password yesterday but now I cannot log in. I keep getting an invalid credentials error even though I am using the new password.",
    "category": "account_access",
    "priority": "high",
    "source": "web_form",
    "browser": "Chrome 120",
    "device_type": "desktop",
    "tags": ["login", "password-reset"]
  }'
```

---

## 2. Get Ticket by ID

Retrieves a single ticket by its UUID.

**Endpoint:** `GET /api/tickets/{id}`

**Status:** `200 OK`

### Path Parameters

| Parameter | Type | Description              |
|-----------|------|--------------------------|
| `id`      | UUID | The unique ticket ID     |

### Response Body Example

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customer_name": "Jane Doe",
  "customer_email": "jane.doe@example.com",
  "subject": "Cannot access my account after password reset",
  "description": "I reset my password yesterday but now I cannot log in. I keep getting an 'invalid credentials' error even though I am using the new password.",
  "category": "account_access",
  "priority": "high",
  "status": "new",
  "source": "web_form",
  "browser": "Chrome 120",
  "device_type": "desktop",
  "tags": ["login", "password-reset"],
  "classification_confidence": null,
  "classification_reasoning": null,
  "created_at": "2025-01-15T10:30:00Z",
  "updated_at": "2025-01-15T10:30:00Z"
}
```

### curl Example

```bash
curl http://localhost:8080/api/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

### Error Response (404)

Returned when the ticket ID does not exist.

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Ticket not found with id: a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "details": null,
  "timestamp": "2025-01-15T10:35:00Z"
}
```

---

## 3. List Tickets

Returns a paginated list of tickets with optional filtering.

**Endpoint:** `GET /api/tickets`

**Status:** `200 OK`

### Query Parameters

| Parameter       | Type    | Required | Description                                        |
|-----------------|---------|----------|----------------------------------------------------|
| `category`      | string  | No       | Filter by category (see [TicketCategory](#ticketcategory)) |
| `priority`      | string  | No       | Filter by priority (see [TicketPriority](#ticketpriority)) |
| `status`        | string  | No       | Filter by status (see [TicketStatus](#ticketstatus))       |
| `email`         | string  | No       | Filter by customer email                           |
| `createdAfter`  | string  | No       | ISO 8601 timestamp lower bound (inclusive)         |
| `createdBefore` | string  | No       | ISO 8601 timestamp upper bound (inclusive)         |
| `tag`           | string  | No       | Filter by tag                                      |
| `page`          | integer | No       | Page number (0-indexed, default: `0`)              |
| `size`          | integer | No       | Page size (default: `20`)                          |

### Response Body Example

```json
{
  "content": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "customer_name": "Jane Doe",
      "customer_email": "jane.doe@example.com",
      "subject": "Cannot access my account after password reset",
      "description": "I reset my password yesterday but now I cannot log in.",
      "category": "account_access",
      "priority": "high",
      "status": "new",
      "source": "web_form",
      "browser": "Chrome 120",
      "device_type": "desktop",
      "tags": ["login", "password-reset"],
      "classification_confidence": null,
      "classification_reasoning": null,
      "created_at": "2025-01-15T10:30:00Z",
      "updated_at": "2025-01-15T10:30:00Z"
    }
  ],
  "pageable": {
    "page_number": 0,
    "page_size": 20,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "total_elements": 1,
  "total_pages": 1,
  "last": true,
  "size": 20,
  "number": 0,
  "sort": {
    "empty": true,
    "sorted": false,
    "unsorted": true
  },
  "number_of_elements": 1,
  "first": true,
  "empty": false
}
```

### curl Examples

List all tickets (default pagination):

```bash
curl http://localhost:8080/api/tickets
```

Filter by category and priority with custom page size:

```bash
curl "http://localhost:8080/api/tickets?category=account_access&priority=high&page=0&size=10"
```

Filter by date range:

```bash
curl "http://localhost:8080/api/tickets?createdAfter=2025-01-01T00:00:00Z&createdBefore=2025-01-31T23:59:59Z"
```

Filter by tag and status:

```bash
curl "http://localhost:8080/api/tickets?tag=login&status=new"
```

Filter by customer email:

```bash
curl "http://localhost:8080/api/tickets?email=jane.doe@example.com"
```

---

## 4. Update Ticket

Updates an existing ticket. All fields are optional; only provided fields are updated.

**Endpoint:** `PUT /api/tickets/{id}`

**Status:** `200 OK`

### Path Parameters

| Parameter | Type | Description          |
|-----------|------|----------------------|
| `id`      | UUID | The unique ticket ID |

### Request Body

| Field            | Type     | Required | Constraints                          |
|------------------|----------|----------|--------------------------------------|
| `customer_name`  | string   | No       | Max 255 characters                   |
| `customer_email` | string   | No       | Must be a valid email                |
| `subject`        | string   | No       | Max 500 characters                   |
| `description`    | string   | No       | Max 10,000 characters                |
| `category`       | string   | No       | See [TicketCategory](#ticketcategory) |
| `priority`       | string   | No       | See [TicketPriority](#ticketpriority) |
| `status`         | string   | No       | See [TicketStatus](#ticketstatus)     |
| `source`         | string   | No       | See [Source](#source)                 |
| `browser`        | string   | No       |                                      |
| `device_type`    | string   | No       | See [DeviceType](#devicetype)         |
| `tags`           | string[] | No       | Replaces existing tags               |

### Request Body Example

```json
{
  "status": "in_progress",
  "priority": "urgent",
  "tags": ["login", "password-reset", "escalated"]
}
```

### Response Body Example

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customer_name": "Jane Doe",
  "customer_email": "jane.doe@example.com",
  "subject": "Cannot access my account after password reset",
  "description": "I reset my password yesterday but now I cannot log in. I keep getting an 'invalid credentials' error even though I am using the new password.",
  "category": "account_access",
  "priority": "urgent",
  "status": "in_progress",
  "source": "web_form",
  "browser": "Chrome 120",
  "device_type": "desktop",
  "tags": ["login", "password-reset", "escalated"],
  "classification_confidence": null,
  "classification_reasoning": null,
  "created_at": "2025-01-15T10:30:00Z",
  "updated_at": "2025-01-15T11:00:00Z"
}
```

### curl Example

```bash
curl -X PUT http://localhost:8080/api/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "in_progress",
    "priority": "urgent",
    "tags": ["login", "password-reset", "escalated"]
  }'
```

---

## 5. Delete Ticket

Deletes a ticket by its UUID.

**Endpoint:** `DELETE /api/tickets/{id}`

**Status:** `204 No Content`

### Path Parameters

| Parameter | Type | Description          |
|-----------|------|----------------------|
| `id`      | UUID | The unique ticket ID |

### Response

No response body is returned on success.

### curl Example

```bash
curl -X DELETE http://localhost:8080/api/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

### Error Response (404)

Returned when the ticket ID does not exist.

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Ticket not found with id: a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "details": null,
  "timestamp": "2025-01-15T10:35:00Z"
}
```

---

## 6. Auto-Classify Ticket

Runs the automatic classification engine on a ticket to assign a category, priority, confidence score, reasoning, and extracted keywords. The ticket is updated in place with the classification results.

**Endpoint:** `POST /api/tickets/{id}/classify`

**Status:** `200 OK`

### Path Parameters

| Parameter | Type | Description          |
|-----------|------|----------------------|
| `id`      | UUID | The unique ticket ID |

### Request Body

None.

### Response Body

| Field        | Type     | Description                                     |
|--------------|----------|-------------------------------------------------|
| `category`   | string   | Assigned category (see [TicketCategory](#ticketcategory)) |
| `priority`   | string   | Assigned priority (see [TicketPriority](#ticketpriority)) |
| `confidence` | number   | Confidence score between 0.0 and 1.0            |
| `reasoning`  | string   | Human-readable explanation of the classification |
| `keywords`   | string[] | Keywords extracted from the ticket content       |

### Response Body Example

```json
{
  "category": "account_access",
  "priority": "high",
  "confidence": 0.92,
  "reasoning": "Ticket describes a login failure after password reset, indicating an account access issue with high urgency.",
  "keywords": ["password", "reset", "login", "invalid credentials", "account"]
}
```

### curl Example

```bash
curl -X POST http://localhost:8080/api/tickets/a1b2c3d4-e5f6-7890-abcd-ef1234567890/classify
```

---

## 7. Import Tickets

Imports tickets in bulk from a file upload. Supports CSV, JSON, and XML formats. Optionally auto-classifies each imported ticket.

**Endpoint:** `POST /api/tickets/import`

**Status:** `201 Created`

**Content-Type:** `multipart/form-data`

### Request Parameters

| Parameter       | Type    | Required | Description                                           |
|-----------------|---------|----------|-------------------------------------------------------|
| `file`          | file    | Yes      | The import file (CSV, JSON, or XML). Max size: 10 MB. |
| `autoClassify`  | boolean | No       | Whether to auto-classify imported tickets (default: `false`) |

### Supported File Formats

- **CSV** - `.csv` files with headers matching ticket fields
- **JSON** - `.json` files containing an array of ticket objects
- **XML** - `.xml` files with ticket elements

### Response Body

| Field             | Type     | Description                                      |
|-------------------|----------|--------------------------------------------------|
| `total_records`   | integer  | Total number of records in the import file        |
| `successful`      | integer  | Number of successfully imported tickets           |
| `failed`          | integer  | Number of records that failed validation          |
| `failures`        | array    | List of failure details (see below)               |
| `created_tickets` | array    | List of successfully created ticket objects       |

Each entry in `failures` contains:

| Field          | Type     | Description                                |
|----------------|----------|--------------------------------------------|
| `record_index` | integer  | Index of the failed record in the file     |
| `errors`       | string[] | List of validation error messages          |

### Response Body Example

```json
{
  "total_records": 3,
  "successful": 2,
  "failed": 1,
  "failures": [
    {
      "record_index": 2,
      "errors": ["Customer name is required", "Customer email is required"]
    }
  ],
  "created_tickets": [
    {
      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "customer_name": "Alice Smith",
      "customer_email": "alice@example.com",
      "subject": "Billing discrepancy on invoice #1234",
      "description": "I was charged twice for my monthly subscription.",
      "category": "billing_question",
      "priority": "medium",
      "status": "new",
      "source": "email",
      "browser": null,
      "device_type": null,
      "tags": ["billing"],
      "classification_confidence": null,
      "classification_reasoning": null,
      "created_at": "2025-01-15T12:00:00Z",
      "updated_at": "2025-01-15T12:00:00Z"
    },
    {
      "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
      "customer_name": "Bob Johnson",
      "customer_email": "bob@example.com",
      "subject": "App crashes on startup",
      "description": "The mobile app crashes immediately after the splash screen on Android 14.",
      "category": "bug_report",
      "priority": "high",
      "status": "new",
      "source": "chat",
      "browser": null,
      "device_type": "mobile",
      "tags": ["crash", "android"],
      "classification_confidence": null,
      "classification_reasoning": null,
      "created_at": "2025-01-15T12:00:00Z",
      "updated_at": "2025-01-15T12:00:00Z"
    }
  ]
}
```

### curl Example

Import from a CSV file:

```bash
curl -X POST http://localhost:8080/api/tickets/import \
  -F "file=@tickets.csv" \
  -F "autoClassify=false"
```

Import from a JSON file with auto-classification enabled:

```bash
curl -X POST http://localhost:8080/api/tickets/import \
  -F "file=@tickets.json" \
  -F "autoClassify=true"
```

Import from an XML file:

```bash
curl -X POST http://localhost:8080/api/tickets/import \
  -F "file=@tickets.xml"
```

---

## Error Response Format

All error responses follow a consistent structure.

### Schema

| Field       | Type     | Description                                         |
|-------------|----------|-----------------------------------------------------|
| `status`    | integer  | HTTP status code                                    |
| `error`     | string   | HTTP status reason phrase                           |
| `message`   | string   | Human-readable error message                        |
| `details`   | string[] | Additional details (e.g., field validation errors). May be `null`. |
| `timestamp` | string   | ISO 8601 timestamp of when the error occurred       |

### Example: Validation Error (400)

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": [
    "customer_name: Customer name is required",
    "customer_email: Customer email must be valid",
    "subject: Subject is required",
    "description: Description is required"
  ],
  "timestamp": "2025-01-15T10:35:00Z"
}
```

### Example: Not Found (404)

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Ticket not found with id: a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "details": null,
  "timestamp": "2025-01-15T10:35:00Z"
}
```

### Example: Unsupported Media Type (415)

Returned when an import file has an unsupported format.

```json
{
  "status": 415,
  "error": "Unsupported Media Type",
  "message": "Unsupported import file format. Supported formats: CSV, JSON, XML",
  "details": null,
  "timestamp": "2025-01-15T10:35:00Z"
}
```

### Example: Internal Server Error (500)

```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "details": null,
  "timestamp": "2025-01-15T10:35:00Z"
}
```

### Error Status Code Summary

| Status Code | Error                  | Cause                                              |
|-------------|------------------------|-----------------------------------------------------|
| 400         | Bad Request            | Validation failure, malformed import file, illegal argument, file size exceeded |
| 404         | Not Found              | Ticket with the given ID does not exist             |
| 415         | Unsupported Media Type | Import file format is not CSV, JSON, or XML         |
| 500         | Internal Server Error  | Unexpected server-side error                        |

---

## Enums

### TicketCategory

Classifies the type of support ticket.

| Value              | Description                    |
|--------------------|--------------------------------|
| `account_access`   | Account access issues          |
| `technical_issue`  | Technical problems             |
| `billing_question` | Billing and payment inquiries  |
| `feature_request`  | Feature suggestions            |
| `bug_report`       | Bug reports                    |
| `other`            | Uncategorized tickets          |

### TicketPriority

Indicates the urgency of a ticket.

| Value    | Description              |
|----------|--------------------------|
| `urgent` | Requires immediate attention |
| `high`   | High priority            |
| `medium` | Medium priority          |
| `low`    | Low priority             |

### TicketStatus

Tracks the lifecycle state of a ticket.

| Value              | Description                          |
|--------------------|--------------------------------------|
| `new`              | Newly created ticket (default)       |
| `in_progress`      | Being actively worked on             |
| `waiting_customer` | Awaiting customer response           |
| `resolved`         | Issue has been resolved              |
| `closed`           | Ticket is closed                     |

### Source

Identifies the channel through which the ticket was submitted.

| Value      | Description           |
|------------|-----------------------|
| `web_form` | Submitted via web form |
| `email`    | Submitted via email   |
| `api`      | Created through API   |
| `chat`     | Submitted via chat    |
| `phone`    | Submitted via phone   |

### DeviceType

Identifies the device type used by the customer.

| Value     | Description    |
|-----------|----------------|
| `desktop` | Desktop device |
| `mobile`  | Mobile device  |
| `tablet`  | Tablet device  |
