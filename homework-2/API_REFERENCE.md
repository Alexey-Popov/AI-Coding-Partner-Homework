# Support Ticket System API Reference

## Overview

This document provides comprehensive information for developers integrating with the Support Ticket System API. The API follows REST principles and uses JSON for request and response payloads.

**Base URL**: `http://localhost:8080`

---

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /tickets | Create a new ticket |
| POST | /tickets?autoClassify=true | Create ticket with auto-classification |
| POST | /tickets/import | Bulk import from CSV/JSON/XML |
| GET | /tickets | List tickets (with filters) |
| GET | /tickets/{id} | Get ticket by ID |
| PUT | /tickets/{id} | Update ticket |
| DELETE | /tickets/{id} | Delete ticket |
| POST | /tickets/{id}/auto-classify | Auto-classify a ticket |

---

## Data Models

### Ticket

A ticket represents a customer support request with the following fields:

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| id | UUID | Read-only | - | Unique identifier (auto-generated) |
| customerId | string | Yes | Not blank | Customer's unique identifier |
| customerEmail | string | Yes | Valid email | Customer's email address |
| customerName | string | Yes | Not blank | Customer's full name |
| subject | string | Yes | 1-200 characters | Brief summary of the issue |
| description | string | Yes | 10-2000 characters | Detailed description of the issue |
| category | enum | No | See Category enum | Ticket category (nullable) |
| priority | enum | No | See Priority enum | Ticket priority (default: medium) |
| status | enum | No | See Status enum | Ticket status (default: new) |
| createdAt | datetime | Read-only | ISO 8601 | When the ticket was created |
| updatedAt | datetime | Read-only | ISO 8601 | When the ticket was last updated |
| resolvedAt | datetime | Read-only | ISO 8601 | When the ticket was resolved (nullable) |
| assignedTo | string | No | - | Agent assigned to the ticket (nullable) |
| tags | array | No | Array of strings | Custom tags for categorization |
| metadata | object | No | See Metadata object | Additional contextual information |
| classificationConfidence | double | Read-only | 0.0-1.0 | AI classification confidence score (nullable) |

### Enumerations

#### Category
- `account_access` - Issues related to account login or access
- `technical_issue` - Technical problems with the product
- `billing_question` - Billing and payment inquiries
- `feature_request` - Requests for new features
- `bug_report` - Bug reports
- `other` - Other types of issues

#### Priority
- `urgent` - Requires immediate attention
- `high` - High priority issue
- `medium` - Standard priority (default)
- `low` - Low priority issue

#### Status
- `new` - Newly created ticket (default)
- `in_progress` - Currently being worked on
- `waiting_customer` - Waiting for customer response
- `resolved` - Issue has been resolved
- `closed` - Ticket is closed

#### Source
- `web_form` - Submitted via web form
- `email` - Submitted via email
- `api` - Submitted via API
- `chat` - Submitted via chat
- `phone` - Submitted via phone

#### DeviceType
- `desktop` - Desktop computer
- `mobile` - Mobile phone
- `tablet` - Tablet device

### Metadata Object

| Field | Type | Description |
|-------|------|-------------|
| source | enum | Where the ticket originated (Source enum) |
| browser | string | Browser information |
| deviceType | enum | Device type (DeviceType enum) |

---

## Endpoint Details

### 1. Create Ticket

Create a new support ticket.

**Endpoint**: `POST /tickets`

**Request Body**:
```json
{
  "customerId": "CUST-12345",
  "customerEmail": "john.doe@example.com",
  "customerName": "John Doe",
  "subject": "Unable to login to my account",
  "description": "I have been trying to login for the past hour but keep getting an 'invalid credentials' error even though I'm sure my password is correct.",
  "category": "account_access",
  "priority": "high",
  "status": "new",
  "assignedTo": "agent@company.com",
  "tags": ["login", "urgent"],
  "metadata": {
    "source": "web_form",
    "browser": "Chrome 120.0",
    "deviceType": "desktop"
  }
}
```

**Response**: `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST-12345",
  "customerEmail": "john.doe@example.com",
  "customerName": "John Doe",
  "subject": "Unable to login to my account",
  "description": "I have been trying to login for the past hour but keep getting an 'invalid credentials' error even though I'm sure my password is correct.",
  "category": "account_access",
  "priority": "high",
  "status": "new",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "resolvedAt": null,
  "assignedTo": "agent@company.com",
  "tags": ["login", "urgent"],
  "metadata": {
    "source": "web_form",
    "browser": "Chrome 120.0",
    "deviceType": "desktop"
  },
  "classificationConfidence": null
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-12345",
    "customerEmail": "john.doe@example.com",
    "customerName": "John Doe",
    "subject": "Unable to login to my account",
    "description": "I have been trying to login for the past hour but keep getting an invalid credentials error.",
    "category": "account_access",
    "priority": "high"
  }'
```

---

### 2. Create Ticket with Auto-Classification

Create a new support ticket with automatic AI-powered classification.

**Endpoint**: `POST /tickets?autoClassify=true`

**Query Parameters**:
- `autoClassify` (boolean, default: false): Enable automatic classification

**Request Body**: Same as Create Ticket

**Response**: `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST-12345",
  "customerEmail": "john.doe@example.com",
  "customerName": "John Doe",
  "subject": "Application crashes when uploading files",
  "description": "Every time I try to upload a PDF file larger than 5MB, the application crashes immediately.",
  "category": "technical_issue",
  "priority": "high",
  "status": "new",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "resolvedAt": null,
  "assignedTo": null,
  "tags": [],
  "metadata": null,
  "classificationConfidence": 0.92
}
```

**cURL Example**:
```bash
curl -X POST "http://localhost:8080/tickets?autoClassify=true" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-12345",
    "customerEmail": "john.doe@example.com",
    "customerName": "John Doe",
    "subject": "Application crashes when uploading files",
    "description": "Every time I try to upload a PDF file larger than 5MB, the application crashes immediately."
  }'
```

---

### 3. Bulk Import Tickets

Import multiple tickets from a CSV, JSON, or XML file.

**Endpoint**: `POST /tickets/import`

**Request**: Multipart form data with a file

**Supported Formats**:
- CSV (`.csv`)
- JSON (`.json`)
- XML (`.xml`)

**CSV Format Example**:
```csv
customerId,customerEmail,customerName,subject,description,category,priority
CUST-001,user1@example.com,User One,Password reset,I need to reset my password,account_access,high
CUST-002,user2@example.com,User Two,Bug in search,Search function not working,bug_report,medium
```

**JSON Format Example**:
```json
[
  {
    "customerId": "CUST-001",
    "customerEmail": "user1@example.com",
    "customerName": "User One",
    "subject": "Password reset",
    "description": "I need to reset my password",
    "category": "account_access",
    "priority": "high"
  },
  {
    "customerId": "CUST-002",
    "customerEmail": "user2@example.com",
    "customerName": "User Two",
    "subject": "Bug in search",
    "description": "Search function not working properly",
    "category": "bug_report",
    "priority": "medium"
  }
]
```

**XML Format Example**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<tickets>
  <ticket>
    <customerId>CUST-001</customerId>
    <customerEmail>user1@example.com</customerEmail>
    <customerName>User One</customerName>
    <subject>Password reset</subject>
    <description>I need to reset my password</description>
    <category>account_access</category>
    <priority>high</priority>
  </ticket>
  <ticket>
    <customerId>CUST-002</customerId>
    <customerEmail>user2@example.com</customerEmail>
    <customerName>User Two</customerName>
    <subject>Bug in search</subject>
    <description>Search function not working properly</description>
    <category>bug_report</category>
    <priority>medium</priority>
  </ticket>
</tickets>
```

**Response**: `200 OK`
```json
{
  "total": 5,
  "successful": 4,
  "failed": 1,
  "errors": [
    {
      "line": 3,
      "field": "customerEmail",
      "message": "Invalid email format"
    }
  ]
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/tickets/import \
  -F "file=@tickets.csv"
```

---

### 4. List Tickets

Retrieve a list of all tickets with optional filtering.

**Endpoint**: `GET /tickets`

**Query Parameters**:
- `category` (optional): Filter by category (e.g., `account_access`, `technical_issue`)
- `priority` (optional): Filter by priority (e.g., `urgent`, `high`, `medium`, `low`)
- `status` (optional): Filter by status (e.g., `new`, `in_progress`, `resolved`)

**Response**: `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "customerId": "CUST-12345",
    "customerEmail": "john.doe@example.com",
    "customerName": "John Doe",
    "subject": "Unable to login to my account",
    "description": "I have been trying to login for the past hour but keep getting an 'invalid credentials' error.",
    "category": "account_access",
    "priority": "high",
    "status": "new",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "resolvedAt": null,
    "assignedTo": "agent@company.com",
    "tags": ["login", "urgent"],
    "metadata": {
      "source": "web_form",
      "browser": "Chrome 120.0",
      "deviceType": "desktop"
    },
    "classificationConfidence": null
  }
]
```

**cURL Examples**:
```bash
# Get all tickets
curl -X GET http://localhost:8080/tickets

# Filter by category
curl -X GET "http://localhost:8080/tickets?category=technical_issue"

# Filter by priority
curl -X GET "http://localhost:8080/tickets?priority=urgent"

# Filter by status
curl -X GET "http://localhost:8080/tickets?status=new"

# Combine multiple filters
curl -X GET "http://localhost:8080/tickets?category=technical_issue&priority=high&status=in_progress"
```

---

### 5. Get Ticket by ID

Retrieve a specific ticket by its ID.

**Endpoint**: `GET /tickets/{id}`

**Path Parameters**:
- `id` (UUID): The unique identifier of the ticket

**Response**: `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST-12345",
  "customerEmail": "john.doe@example.com",
  "customerName": "John Doe",
  "subject": "Unable to login to my account",
  "description": "I have been trying to login for the past hour but keep getting an 'invalid credentials' error.",
  "category": "account_access",
  "priority": "high",
  "status": "new",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "resolvedAt": null,
  "assignedTo": "agent@company.com",
  "tags": ["login", "urgent"],
  "metadata": {
    "source": "web_form",
    "browser": "Chrome 120.0",
    "deviceType": "desktop"
  },
  "classificationConfidence": null
}
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/tickets/550e8400-e29b-41d4-a716-446655440000
```

---

### 6. Update Ticket

Update an existing ticket. All fields are optional; only provided fields will be updated.

**Endpoint**: `PUT /tickets/{id}`

**Path Parameters**:
- `id` (UUID): The unique identifier of the ticket

**Request Body**:
```json
{
  "customerEmail": "john.updated@example.com",
  "customerName": "John Doe Updated",
  "subject": "Unable to login to my account - Updated",
  "description": "Updated description with more details about the login issue.",
  "category": "technical_issue",
  "priority": "urgent",
  "status": "in_progress",
  "assignedTo": "senior-agent@company.com",
  "tags": ["login", "urgent", "escalated"]
}
```

**Response**: `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST-12345",
  "customerEmail": "john.updated@example.com",
  "customerName": "John Doe Updated",
  "subject": "Unable to login to my account - Updated",
  "description": "Updated description with more details about the login issue.",
  "category": "technical_issue",
  "priority": "urgent",
  "status": "in_progress",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T11:45:00",
  "resolvedAt": null,
  "assignedTo": "senior-agent@company.com",
  "tags": ["login", "urgent", "escalated"],
  "metadata": {
    "source": "web_form",
    "browser": "Chrome 120.0",
    "deviceType": "desktop"
  },
  "classificationConfidence": null
}
```

**cURL Example**:
```bash
curl -X PUT http://localhost:8080/tickets/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "in_progress",
    "priority": "urgent",
    "assignedTo": "senior-agent@company.com"
  }'
```

---

### 7. Delete Ticket

Delete a ticket by its ID.

**Endpoint**: `DELETE /tickets/{id}`

**Path Parameters**:
- `id` (UUID): The unique identifier of the ticket

**Response**: `204 No Content`

**cURL Example**:
```bash
curl -X DELETE http://localhost:8080/tickets/550e8400-e29b-41d4-a716-446655440000
```

---

### 8. Auto-Classify Ticket

Automatically classify an existing ticket using AI-powered analysis.

**Endpoint**: `POST /tickets/{id}/auto-classify`

**Path Parameters**:
- `id` (UUID): The unique identifier of the ticket

**Response**: `200 OK`
```json
{
  "category": "technical_issue",
  "priority": "high",
  "confidence": 0.85,
  "reasoning": "Classified as technical_issue based on keywords: error, crash",
  "keywordsFound": ["error", "crash"]
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/tickets/550e8400-e29b-41d4-a716-446655440000/auto-classify
```

---

## Error Responses

All error responses follow a consistent format:

### Validation Error (400 Bad Request)

Returned when request validation fails.

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "subject",
      "message": "Subject is required"
    },
    {
      "field": "customerEmail",
      "message": "Invalid email format"
    }
  ]
}
```

### Not Found (404 Not Found)

Returned when a ticket is not found.

```json
{
  "status": 404,
  "message": "Ticket not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "errors": []
}
```

### Bad Request (400 Bad Request)

Returned for malformed requests or invalid data.

```json
{
  "status": 400,
  "message": "Invalid category: invalid_category_name",
  "errors": []
}
```

### Internal Server Error (500 Internal Server Error)

Returned when an unexpected server error occurs.

```json
{
  "status": 500,
  "message": "Internal server error",
  "errors": []
}
```

---

## Validation Rules

### Required Fields (Create Ticket)
- `customerId`: Must not be blank
- `customerEmail`: Must be a valid email address
- `customerName`: Must not be blank
- `subject`: Must be between 1 and 200 characters
- `description`: Must be between 10 and 2000 characters

### Optional Fields
- `category`: Must be a valid Category enum value if provided
- `priority`: Must be a valid Priority enum value if provided (defaults to `medium`)
- `status`: Must be a valid Status enum value if provided (defaults to `new`)
- `assignedTo`: Can be any string
- `tags`: Must be an array of strings
- `metadata`: Must conform to the Metadata object structure

### Update Ticket
All fields are optional. If a field is not provided, it will not be updated.

---

## HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 OK | Request successful |
| 201 Created | Resource successfully created |
| 204 No Content | Resource successfully deleted |
| 400 Bad Request | Invalid request or validation error |
| 404 Not Found | Resource not found |
| 500 Internal Server Error | Server error |

---

## Best Practices

1. **Email Validation**: Always ensure email addresses are properly formatted
2. **Description Length**: Provide meaningful descriptions between 10-2000 characters
3. **Subject Brevity**: Keep subjects concise (1-200 characters)
4. **Auto-Classification**: Use the `autoClassify=true` parameter when creating tickets if you don't have category/priority information
5. **Filtering**: Use query parameters to filter tickets efficiently rather than retrieving all tickets
6. **Error Handling**: Always check the `errors` array in error responses for detailed validation information
7. **Bulk Import**: For large imports, use the bulk import endpoint rather than creating tickets individually
8. **Timestamps**: All timestamps are in ISO 8601 format and should be handled accordingly

---

## Rate Limiting

Currently, there are no rate limits enforced. However, it is recommended to implement appropriate throttling on the client side to avoid overwhelming the server.

---

## Support

For API support or questions, please contact the development team or refer to the system documentation.
