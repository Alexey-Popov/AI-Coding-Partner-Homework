# GitHub Copilot Coder Template - API Routes

**Model:** GitHub Copilot  
**Role:** Code Generation - FastAPI Routes  
**Format:** Comment-Driven with Inline Context  
**Best for:** API route scaffolding, pattern-following, IDE-integrated development

---

## Guardrails
Follow the rules defined in [GUARDRAILS.md](../GUARDRAILS.md):
- Do not guess if uncertain
- Ask 3 clarifying questions for ambiguous input
- List all missing data explicitly

---

## How to Use This Template

1. Create a new file: `src/routes/{{route_name}}.py`
2. Copy the template below into the file
3. Place cursor after each `# TODO:` comment
4. Let Copilot generate the implementation
5. Review and adjust generated code

---

## Template

```python
# ============================================================
# FILE: src/routes/{{ROUTE_NAME}}.py
# PURPOSE: {{ROUTE_PURPOSE}}
# PATTERNS: Follow homework-1/src/routes/transactions.py
# ============================================================

"""
{{ROUTE_DESCRIPTION}}

This router provides endpoints for:
- {{ENDPOINT_1_DESCRIPTION}}
- {{ENDPOINT_2_DESCRIPTION}}
- {{ENDPOINT_3_DESCRIPTION}}

Reference: homework-1/src/routes/transactions.py
"""

from typing import List, Optional
from fastapi import APIRouter, HTTPException, Query, status
from models.{{MODEL_FILE}} import {{MODEL_IMPORTS}}
from services.{{SERVICE_FILE}} import {{SERVICE_CLASS}}

# Router configuration - follow pattern from transactions.py
router = APIRouter(
    prefix="/{{ROUTE_PREFIX}}",
    tags=["{{TAG_NAME}}"],
    responses={404: {"description": "Not found"}}
)

# Service instance - singleton pattern
{{SERVICE_INSTANCE}} = {{SERVICE_CLASS}}()


# ============================================================
# POST /{{ROUTE_PREFIX}}/ - Create new {{RESOURCE_NAME}}
# Request: {{CREATE_MODEL}} (required fields: {{REQUIRED_FIELDS}})
# Response: {{RESPONSE_MODEL}} with status 201
# ============================================================
@router.post("/", response_model={{RESPONSE_MODEL}}, status_code=status.HTTP_201_CREATED)
def create_{{RESOURCE_NAME_SNAKE}}({{PARAM_NAME}}: {{CREATE_MODEL}}):
    """
    Create a new {{RESOURCE_NAME}}.
    
    - **{{FIELD_1}}**: {{FIELD_1_DESC}}
    - **{{FIELD_2}}**: {{FIELD_2_DESC}}
    
    Returns the created {{RESOURCE_NAME}} with generated ID and timestamps.
    """
    # TODO: Implement using service.create_{{RESOURCE_NAME_SNAKE}}()
    pass


# ============================================================
# GET /{{ROUTE_PREFIX}}/ - List all {{RESOURCE_NAME_PLURAL}}
# Query params: {{FILTER_PARAMS}}
# Response: List[{{RESPONSE_MODEL}}]
# ============================================================
@router.get("/", response_model=List[{{RESPONSE_MODEL}}])
def get_all_{{RESOURCE_NAME_PLURAL_SNAKE}}(
    {{FILTER_PARAM_1}}: Optional[{{FILTER_TYPE_1}}] = Query(None, description="{{FILTER_DESC_1}}"),
    {{FILTER_PARAM_2}}: Optional[{{FILTER_TYPE_2}}] = Query(None, description="{{FILTER_DESC_2}}"),
    {{FILTER_PARAM_3}}: Optional[{{FILTER_TYPE_3}}] = Query(None, description="{{FILTER_DESC_3}}")
):
    """
    Retrieve all {{RESOURCE_NAME_PLURAL}} with optional filtering.
    
    Filters can be combined. All filters are optional.
    """
    # TODO: Implement using service.get_all_{{RESOURCE_NAME_PLURAL_SNAKE}}()
    # Pass filter parameters to service method
    pass


# ============================================================
# GET /{{ROUTE_PREFIX}}/{{{ID_PARAM}}} - Get single {{RESOURCE_NAME}}
# Path param: {{ID_PARAM}} (UUID string)
# Response: {{RESPONSE_MODEL}} or 404
# ============================================================
@router.get("/{{{ID_PARAM}}}", response_model={{RESPONSE_MODEL}})
def get_{{RESOURCE_NAME_SNAKE}}_by_id({{ID_PARAM}}: str):
    """
    Retrieve a {{RESOURCE_NAME}} by its unique identifier.
    
    Raises HTTPException 404 if not found.
    """
    # TODO: Implement using service.get_{{RESOURCE_NAME_SNAKE}}_by_id()
    # Raise HTTPException(status_code=404) if not found
    pass


# ============================================================
# PATCH /{{ROUTE_PREFIX}}/{{{ID_PARAM}}} - Update {{RESOURCE_NAME}}
# Path param: {{ID_PARAM}}
# Request: {{UPDATE_MODEL}} (all fields optional)
# Response: {{RESPONSE_MODEL}} or 404
# ============================================================
@router.patch("/{{{ID_PARAM}}}", response_model={{RESPONSE_MODEL}})
def update_{{RESOURCE_NAME_SNAKE}}({{ID_PARAM}}: str, updates: {{UPDATE_MODEL}}):
    """
    Update an existing {{RESOURCE_NAME}}.
    
    Only provided fields will be updated.
    {{ADDITIONAL_UPDATE_CONSTRAINTS}}
    """
    # TODO: Implement using service.update_{{RESOURCE_NAME_SNAKE}}()
    # Handle validation errors and not found
    pass


# ============================================================
# DELETE /{{ROUTE_PREFIX}}/{{{ID_PARAM}}} - Delete {{RESOURCE_NAME}}
# Path param: {{ID_PARAM}}
# Response: 204 No Content or 404
# ============================================================
@router.delete("/{{{ID_PARAM}}}", status_code=status.HTTP_204_NO_CONTENT)
def delete_{{RESOURCE_NAME_SNAKE}}({{ID_PARAM}}: str):
    """
    Delete a {{RESOURCE_NAME}} by ID.
    
    Returns 204 on success, 404 if not found.
    """
    # TODO: Implement using service.delete_{{RESOURCE_NAME_SNAKE}}()
    pass


# ============================================================
# Additional endpoints below (import, classify, etc.)
# ============================================================

{{ADDITIONAL_ENDPOINTS}}
```

---

## Example: Filled Template for Ticket Routes

```python
# ============================================================
# FILE: src/routes/tickets.py
# PURPOSE: REST API endpoints for support ticket management
# PATTERNS: Follow homework-1/src/routes/transactions.py
# ============================================================

"""
Ticket Management API Routes.

This router provides endpoints for:
- CRUD operations for support tickets
- Bulk import from CSV, JSON, XML formats
- Auto-classification triggering

Reference: homework-1/src/routes/transactions.py
"""

from typing import List, Optional
from fastapi import APIRouter, HTTPException, Query, status, UploadFile, File
from models.ticket import Ticket, TicketCreate, TicketUpdate, TicketStatus, TicketCategory, TicketPriority
from services.ticket_service import TicketService
from services.import_service import ImportService

# Router configuration - follow pattern from transactions.py
router = APIRouter(
    prefix="/tickets",
    tags=["tickets"],
    responses={404: {"description": "Ticket not found"}}
)

# Service instances - singleton pattern
ticket_service = TicketService()
import_service = ImportService()


# ============================================================
# POST /tickets/ - Create new ticket
# Request: TicketCreate (required: title, description, customer_email, customer_name)
# Response: Ticket with status 201
# Auto-classifies if category not provided
# ============================================================
@router.post("/", response_model=Ticket, status_code=status.HTTP_201_CREATED)
def create_ticket(ticket: TicketCreate):
    """
    Create a new support ticket.
    
    - **title**: Ticket title (5-200 characters)
    - **description**: Detailed description (10-2000 characters)
    - **customer_email**: Valid email address
    - **customer_name**: Customer's name (2-100 characters)
    - **priority**: Optional - low, medium, high, critical
    - **category**: Optional - billing, technical, general, feedback
    
    If category is not provided, auto-classification will be attempted.
    Returns the created ticket with generated ID and timestamps.
    """
    # TODO: Implement using ticket_service.create_ticket()
    # Return the created ticket
    pass


# ============================================================
# GET /tickets/ - List all tickets
# Query params: status, category, priority, limit, offset
# Response: List[Ticket]
# ============================================================
@router.get("/", response_model=List[Ticket])
def get_all_tickets(
    status: Optional[TicketStatus] = Query(None, description="Filter by ticket status"),
    category: Optional[TicketCategory] = Query(None, description="Filter by category"),
    priority: Optional[TicketPriority] = Query(None, description="Filter by priority"),
    limit: int = Query(100, ge=1, le=1000, description="Maximum tickets to return"),
    offset: int = Query(0, ge=0, description="Number of tickets to skip")
):
    """
    Retrieve all tickets with optional filtering.
    
    Filters can be combined. All filters are optional.
    Supports pagination via limit and offset parameters.
    """
    # TODO: Implement using ticket_service.get_all_tickets()
    # Pass filter parameters: status, category, priority
    # Apply limit and offset for pagination
    pass


# ============================================================
# GET /tickets/{ticket_id} - Get single ticket
# Path param: ticket_id (UUID string)
# Response: Ticket or 404
# ============================================================
@router.get("/{ticket_id}", response_model=Ticket)
def get_ticket_by_id(ticket_id: str):
    """
    Retrieve a ticket by its unique identifier.
    
    Raises HTTPException 404 if ticket not found.
    """
    # TODO: Implement using ticket_service.get_ticket_by_id()
    # Raise HTTPException(status_code=404, detail="Ticket not found") if None
    pass


# ============================================================
# PATCH /tickets/{ticket_id} - Update ticket
# Path param: ticket_id
# Request: TicketUpdate (all fields optional)
# Response: Ticket or 404
# Note: Closed tickets cannot be modified
# ============================================================
@router.patch("/{ticket_id}", response_model=Ticket)
def update_ticket(ticket_id: str, updates: TicketUpdate):
    """
    Update an existing ticket.
    
    Only provided fields will be updated.
    Raises 400 if attempting to modify a closed ticket.
    Raises 404 if ticket not found.
    """
    # TODO: Implement using ticket_service.update_ticket()
    # Handle ValueError for closed ticket -> HTTPException 400
    # Handle None return -> HTTPException 404
    pass


# ============================================================
# DELETE /tickets/{ticket_id} - Delete ticket
# Path param: ticket_id
# Response: 204 No Content or 404
# ============================================================
@router.delete("/{ticket_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_ticket(ticket_id: str):
    """
    Delete a ticket by ID.
    
    Returns 204 on success, 404 if not found.
    """
    # TODO: Implement using ticket_service.delete_ticket()
    # If returns False, raise HTTPException 404
    pass


# ============================================================
# POST /tickets/import/csv - Bulk import from CSV
# Request: CSV content as text/csv
# Response: ImportResult with success/error counts
# ============================================================
@router.post("/import/csv")
def import_tickets_csv(csv_content: str):
    """
    Import tickets from CSV format.
    
    Expected CSV headers: title,description,customer_email,customer_name,priority,category
    Processing continues even if individual rows fail validation.
    
    Returns count of successful imports and list of errors.
    """
    # TODO: Implement using import_service.import_csv()
    pass


# ============================================================
# POST /tickets/import/json - Bulk import from JSON
# Request: JSON with {"tickets": [...]}
# Response: ImportResult
# ============================================================
@router.post("/import/json")
def import_tickets_json(data: dict):
    """
    Import tickets from JSON array.
    
    Expected format: {"tickets": [{...}, {...}]}
    """
    # TODO: Implement using import_service.import_json()
    pass


# ============================================================
# POST /tickets/import/xml - Bulk import from XML
# Request: XML with <tickets><ticket>...</ticket></tickets>
# Response: ImportResult
# ============================================================
@router.post("/import/xml")
def import_tickets_xml(xml_content: str):
    """
    Import tickets from XML format.
    
    Expected format: <tickets><ticket>...</ticket></tickets>
    """
    # TODO: Implement using import_service.import_xml()
    pass


# ============================================================
# POST /tickets/{ticket_id}/classify - Trigger auto-classification
# Path param: ticket_id
# Response: ClassificationResult
# ============================================================
@router.post("/{ticket_id}/classify")
def classify_ticket(ticket_id: str):
    """
    Re-run auto-classification on an existing ticket.
    
    Updates the ticket's category and priority based on content analysis.
    Returns classification result with confidence scores.
    """
    # TODO: Get ticket by ID
    # Call classification_service.classify_ticket()
    # Update ticket with new classification
    # Return classification result
    pass
```

---

## Usage Notes for Copilot

1. **Place cursor after `# TODO:` comment** and press Enter
2. **Accept suggestions with Tab** or modify as needed
3. **Copilot reads context** from:
   - File header comments
   - Import statements
   - Docstrings
   - Nearby code patterns
4. **Reference files mentioned** in header are used for pattern matching
5. **Descriptive comments** before each endpoint help Copilot understand intent
6. **Type hints** improve suggestion accuracy
