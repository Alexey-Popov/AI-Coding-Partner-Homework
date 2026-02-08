# Support Ticket Management System - High-Level Plan

## Architecture Overview

A REST API built with **Python Flask** that manages customer support tickets with
multi-format bulk import capabilities (CSV, JSON, XML).

## Project Structure

```
homework-2/
├── PLAN.md                  # This file
├── HOW-TO-RUN.md            # Instructions to run the application
├── requirements.txt         # Python dependencies
├── app/
│   ├── __init__.py          # Flask app factory
│   ├── config.py            # Application configuration
│   ├── models/
│   │   ├── __init__.py
│   │   └── ticket.py        # Ticket model + validation logic
│   ├── routes/
│   │   ├── __init__.py
│   │   └── tickets.py       # Ticket API endpoints (CRUD + import)
│   ├── services/
│   │   ├── __init__.py
│   │   ├── ticket_service.py    # Business logic for tickets
│   │   └── import_service.py    # Multi-format file parsing (CSV/JSON/XML)
│   └── utils/
│       ├── __init__.py
│       └── validators.py    # Field validation helpers
└── run.py                   # Application entry point
```

## Components

### 1. Ticket Model (`app/models/ticket.py`)
- Define the ticket data structure with all required fields
- UUID generation for ticket IDs
- Timestamp management (created_at, updated_at, resolved_at)
- Serialization to/from dict for JSON responses

### 2. Validation Layer (`app/utils/validators.py`)
- Email format validation (regex-based)
- String length enforcement (subject: 1-200, description: 10-2000)
- Enum validation for category, priority, status, source, device_type
- Required field checks
- Returns structured error messages

### 3. Ticket Service (`app/services/ticket_service.py`)
- In-memory storage using a dict (keyed by UUID)
- CRUD operations: create, get_by_id, get_all, update, delete
- Filtering support: by status, priority, category, assigned_to, customer_email
- Business rules: auto-set timestamps on create/update, set resolved_at on status change

### 4. Import Service (`app/services/import_service.py`)
- **CSV Parser**: Use Python `csv` module, map headers to ticket fields
- **JSON Parser**: Use `json` module, expect array of ticket objects
- **XML Parser**: Use `xml.etree.ElementTree`, parse `<ticket>` elements
- File format detection via Content-Type header or file extension
- Per-record validation with error collection
- Return summary: `{ total, successful, failed, errors: [{row, field, message}] }`

### 5. API Routes (`app/routes/tickets.py`)

| Method | Endpoint           | Status Codes       |
|--------|--------------------|---------------------|
| POST   | /tickets           | 201, 400            |
| POST   | /tickets/import    | 200, 400            |
| GET    | /tickets           | 200                 |
| GET    | /tickets/:id       | 200, 404            |
| PUT    | /tickets/:id       | 200, 400, 404       |
| DELETE | /tickets/:id       | 200, 404            |

### 6. Data Storage
- In-memory dictionary (no database dependency for simplicity)
- Thread-safe operations are not required for this scope

## Implementation Order

1. Set up Flask project skeleton + dependencies
2. Build the ticket model and validators
3. Implement the ticket service (in-memory CRUD)
4. Build the import service (CSV, JSON, XML parsers)
5. Wire up all API routes
6. Create HOW-TO-RUN documentation

## Key Design Decisions

- **In-memory storage**: Keeps the project self-contained with zero external dependencies
- **Service layer pattern**: Separates business logic from route handlers for clarity
- **Per-record error reporting on import**: Partial success is allowed; valid records are imported even if some fail
- **Flask Blueprints**: Routes organized as a Blueprint for modularity
