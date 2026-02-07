# GPT-4 Documenter Template - API Reference

**Model:** OpenAI GPT-4  
**Role:** Documentation Generation - API Reference  
**Format:** System/User Message with Endpoint Specifications  
**Best for:** Tabular API documentation, endpoint reference, request/response examples

---

## Guardrails
Follow the rules defined in [GUARDRAILS.md](../GUARDRAILS.md):
- Do not guess if uncertain
- Ask 3 clarifying questions for ambiguous input
- List all missing data explicitly

---

## Template

### System Message

```
You are an expert technical writer specializing in API documentation.

RULES:
1. If you are unsure of the answer, state that you do not know. Do not guess.
2. If the input is ambiguous, ask exactly 3 clarifying questions before proceeding.
3. If data is missing, explicitly list what is missing.

DOCUMENTATION STANDARDS:
- Use consistent formatting for all endpoints
- Include request/response examples in JSON
- Document all query parameters and their types
- Include error response examples
- Use tables for parameter documentation
- Provide curl examples for each endpoint

OUTPUT:
- Generate complete API reference in Markdown
- Include table of contents
- Group endpoints by resource
- Add authentication section (if applicable)
```

### User Message Template

```
## Project Context

```json
{
  "project": "{{PROJECT_NAME}}",
  "api_version": "{{API_VERSION}}",
  "base_url": "{{BASE_URL}}",
  "authentication": "{{AUTH_TYPE}}"
}
```

## Endpoints to Document

```json
{
  "endpoints": [
    {
      "method": "{{HTTP_METHOD}}",
      "path": "{{PATH}}",
      "summary": "{{SUMMARY}}",
      "description": "{{DESCRIPTION}}",
      "request_body": {{REQUEST_BODY_SCHEMA}},
      "query_params": {{QUERY_PARAMS}},
      "path_params": {{PATH_PARAMS}},
      "responses": {
        "{{STATUS_CODE}}": {{RESPONSE_SCHEMA}}
      }
    }
  ]
}
```

## Data Models

```json
{
  "models": [
    {
      "name": "{{MODEL_NAME}}",
      "fields": {{FIELD_DEFINITIONS}}
    }
  ]
}
```

## Required Output

Generate API_REFERENCE.md with:
1. Title and version
2. Base URL and authentication
3. Table of Contents
4. Data Models section
5. Endpoints grouped by resource
6. Each endpoint with:
   - Summary and description
   - Parameters table
   - Request body (if applicable)
   - Response examples (success and error)
   - Curl example
7. Error codes reference
```

---

## Example: Filled Template for Ticket API Reference

### System Message
(Same as above)

### User Message

```
## Project Context

```json
{
  "project": "Customer Support Ticket System",
  "api_version": "v1",
  "base_url": "http://localhost:8000",
  "authentication": "None (public API)"
}
```

## Endpoints to Document

```json
{
  "endpoints": [
    {
      "method": "POST",
      "path": "/tickets/",
      "summary": "Create a new ticket",
      "description": "Creates a new support ticket. The ticket will be automatically assigned a UUID and creation timestamp. If category is not provided, auto-classification will be attempted.",
      "request_body": {
        "type": "object",
        "required": ["title", "description", "customer_email", "customer_name"],
        "properties": {
          "title": {"type": "string", "minLength": 5, "maxLength": 200},
          "description": {"type": "string", "minLength": 10, "maxLength": 2000},
          "customer_email": {"type": "string", "format": "email"},
          "customer_name": {"type": "string", "minLength": 2, "maxLength": 100},
          "priority": {"type": "string", "enum": ["low", "medium", "high", "critical"]},
          "category": {"type": "string", "enum": ["billing", "technical", "general", "feedback"]}
        }
      },
      "query_params": [],
      "path_params": [],
      "responses": {
        "201": {
          "description": "Ticket created successfully",
          "example": {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "title": "Login Issue",
            "description": "Cannot access dashboard",
            "customer_email": "user@example.com",
            "customer_name": "John Doe",
            "status": "open",
            "priority": "medium",
            "category": "technical",
            "created_at": "2024-01-15T10:30:00Z",
            "updated_at": null
          }
        },
        "422": {
          "description": "Validation error",
          "example": {
            "detail": [{"loc": ["body", "title"], "msg": "String should have at least 5 characters", "type": "string_too_short"}]
          }
        }
      }
    },
    {
      "method": "GET",
      "path": "/tickets/",
      "summary": "List all tickets",
      "description": "Retrieves a list of all tickets. Supports filtering by status, category, and priority.",
      "request_body": null,
      "query_params": [
        {"name": "status", "type": "string", "required": false, "description": "Filter by status (open, in_progress, resolved, closed)"},
        {"name": "category", "type": "string", "required": false, "description": "Filter by category (billing, technical, general, feedback)"},
        {"name": "priority", "type": "string", "required": false, "description": "Filter by priority (low, medium, high, critical)"},
        {"name": "limit", "type": "integer", "required": false, "description": "Maximum number of tickets to return (default: 100)"},
        {"name": "offset", "type": "integer", "required": false, "description": "Number of tickets to skip (default: 0)"}
      ],
      "path_params": [],
      "responses": {
        "200": {
          "description": "List of tickets",
          "example": [
            {"id": "uuid-1", "title": "Ticket 1", "status": "open"},
            {"id": "uuid-2", "title": "Ticket 2", "status": "closed"}
          ]
        }
      }
    },
    {
      "method": "GET",
      "path": "/tickets/{ticket_id}",
      "summary": "Get ticket by ID",
      "description": "Retrieves a single ticket by its unique identifier.",
      "request_body": null,
      "query_params": [],
      "path_params": [
        {"name": "ticket_id", "type": "string", "required": true, "description": "UUID of the ticket"}
      ],
      "responses": {
        "200": {
          "description": "Ticket details",
          "example": {"id": "uuid", "title": "Ticket", "status": "open"}
        },
        "404": {
          "description": "Ticket not found",
          "example": {"detail": "Ticket not found"}
        }
      }
    },
    {
      "method": "PATCH",
      "path": "/tickets/{ticket_id}",
      "summary": "Update ticket",
      "description": "Updates an existing ticket. Only provided fields will be updated. Closed tickets cannot be modified.",
      "request_body": {
        "type": "object",
        "properties": {
          "title": {"type": "string"},
          "description": {"type": "string"},
          "status": {"type": "string"},
          "priority": {"type": "string"},
          "category": {"type": "string"}
        }
      },
      "query_params": [],
      "path_params": [
        {"name": "ticket_id", "type": "string", "required": true, "description": "UUID of the ticket"}
      ],
      "responses": {
        "200": {"description": "Updated ticket"},
        "404": {"description": "Ticket not found"},
        "400": {"description": "Cannot modify closed ticket"}
      }
    },
    {
      "method": "DELETE",
      "path": "/tickets/{ticket_id}",
      "summary": "Delete ticket",
      "description": "Permanently deletes a ticket.",
      "request_body": null,
      "query_params": [],
      "path_params": [
        {"name": "ticket_id", "type": "string", "required": true, "description": "UUID of the ticket"}
      ],
      "responses": {
        "204": {"description": "Ticket deleted (no content)"},
        "404": {"description": "Ticket not found"}
      }
    },
    {
      "method": "POST",
      "path": "/tickets/import/csv",
      "summary": "Bulk import from CSV",
      "description": "Import multiple tickets from CSV format. Continues processing even if individual rows fail validation.",
      "request_body": {
        "content_type": "text/csv",
        "description": "CSV file with headers: title,description,customer_email,customer_name,priority,category"
      },
      "query_params": [],
      "path_params": [],
      "responses": {
        "200": {
          "description": "Import result",
          "example": {
            "success_count": 8,
            "error_count": 2,
            "errors": [
              {"row": 3, "error": "Invalid email format"},
              {"row": 7, "error": "Title too short"}
            ]
          }
        }
      }
    },
    {
      "method": "POST",
      "path": "/tickets/import/json",
      "summary": "Bulk import from JSON",
      "description": "Import multiple tickets from JSON array.",
      "request_body": {
        "content_type": "application/json",
        "schema": {"tickets": [{"title": "...", "description": "..."}]}
      },
      "query_params": [],
      "path_params": [],
      "responses": {
        "200": {"description": "Import result with success/error counts"}
      }
    },
    {
      "method": "POST",
      "path": "/tickets/import/xml",
      "summary": "Bulk import from XML",
      "description": "Import multiple tickets from XML format.",
      "request_body": {
        "content_type": "application/xml",
        "schema": "<tickets><ticket>...</ticket></tickets>"
      },
      "query_params": [],
      "path_params": [],
      "responses": {
        "200": {"description": "Import result with success/error counts"}
      }
    },
    {
      "method": "POST",
      "path": "/tickets/{ticket_id}/classify",
      "summary": "Trigger auto-classification",
      "description": "Re-runs the auto-classification algorithm on an existing ticket to update category and priority.",
      "request_body": null,
      "query_params": [],
      "path_params": [
        {"name": "ticket_id", "type": "string", "required": true, "description": "UUID of the ticket"}
      ],
      "responses": {
        "200": {
          "description": "Classification result",
          "example": {
            "category": "technical",
            "category_confidence": 85.5,
            "priority": "high",
            "priority_confidence": 72.0,
            "matched_keywords": ["error", "crash", "urgent"]
          }
        }
      }
    }
  ]
}
```

## Data Models

```json
{
  "models": [
    {
      "name": "Ticket",
      "fields": [
        {"name": "id", "type": "string (UUID)", "required": false, "description": "Unique identifier, auto-generated"},
        {"name": "title", "type": "string", "required": true, "description": "Ticket title, 5-200 characters"},
        {"name": "description", "type": "string", "required": true, "description": "Detailed description, 10-2000 characters"},
        {"name": "customer_email", "type": "string (email)", "required": true, "description": "Customer's email address"},
        {"name": "customer_name", "type": "string", "required": true, "description": "Customer's name, 2-100 characters"},
        {"name": "status", "type": "enum", "required": false, "description": "open, in_progress, resolved, closed (default: open)"},
        {"name": "priority", "type": "enum", "required": false, "description": "low, medium, high, critical"},
        {"name": "category", "type": "enum", "required": false, "description": "billing, technical, general, feedback"},
        {"name": "created_at", "type": "datetime", "required": false, "description": "Creation timestamp (ISO 8601)"},
        {"name": "updated_at", "type": "datetime", "required": false, "description": "Last update timestamp (ISO 8601)"}
      ]
    },
    {
      "name": "ImportResult",
      "fields": [
        {"name": "success_count", "type": "integer", "description": "Number of successfully imported tickets"},
        {"name": "error_count", "type": "integer", "description": "Number of failed imports"},
        {"name": "errors", "type": "array", "description": "List of error details with row numbers"}
      ]
    },
    {
      "name": "ClassificationResult",
      "fields": [
        {"name": "category", "type": "enum", "description": "Assigned category"},
        {"name": "category_confidence", "type": "float", "description": "Confidence score 0-100"},
        {"name": "priority", "type": "enum", "description": "Assigned priority"},
        {"name": "priority_confidence", "type": "float", "description": "Confidence score 0-100"},
        {"name": "matched_keywords", "type": "array", "description": "Keywords that triggered classification"}
      ]
    }
  ]
}
```

## Required Output

Generate API_REFERENCE.md with all sections, tables, and curl examples for each endpoint.
```

---

## Usage Notes

1. **Endpoint JSON spec** ensures consistent documentation
2. **Include all response codes** - success and errors
3. **Curl examples** are essential for developers
4. **Data models section** reduces repetition in endpoint docs
5. **Query parameters table** should include defaults
6. **Error examples** help with debugging
