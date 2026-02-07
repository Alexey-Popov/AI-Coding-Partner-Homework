"""
Ticket CRUD Routes

Endpoints:
- POST /tickets - Create new ticket
- GET /tickets - List tickets (with filters)
- GET /tickets/{id} - Get ticket by ID
- PATCH /tickets/{id} - Update ticket
- DELETE /tickets/{id} - Delete ticket
- GET /tickets/stats - Get statistics
"""

from typing import Optional
from uuid import UUID

from fastapi import APIRouter, HTTPException, Query

from ..models import Ticket, TicketCreate, TicketUpdate, TicketList, TicketCategory, TicketPriority, TicketStatus
from ..services.ticket_service import ticket_service


router = APIRouter()


@router.post("", response_model=Ticket, status_code=201)
async def create_ticket(ticket_data: TicketCreate):
    """
    Create a new ticket
    
    Returns:
        Created ticket with auto-generated UUID
    """
    try:
        ticket = ticket_service.create_ticket(ticket_data)
        return ticket
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.get("", response_model=TicketList)
async def list_tickets(
    category: Optional[TicketCategory] = Query(None, description="Filter by category"),
    priority: Optional[TicketPriority] = Query(None, description="Filter by priority"),
    status: Optional[TicketStatus] = Query(None, description="Filter by status"),
):
    """
    List all tickets with optional filtering
    
    Query parameters:
        - category: Filter by ticket category
        - priority: Filter by priority level
        - status: Filter by status
    
    Returns:
        List of tickets matching filters
    """
    tickets = ticket_service.get_all_tickets(
        category=category,
        priority=priority,
        status=status,
    )
    
    return TicketList(items=tickets, total=len(tickets))


@router.get("/stats")
async def get_statistics():
    """
    Get ticket statistics
    
    Returns:
        Statistics grouped by category, priority, and status
    """
    return ticket_service.get_statistics()


@router.get("/{ticket_id}", response_model=Ticket)
async def get_ticket(ticket_id: UUID):
    """
    Get a ticket by ID
    
    Args:
        ticket_id: UUID of the ticket
        
    Returns:
        Ticket details
        
    Raises:
        404: Ticket not found
    """
    ticket = ticket_service.get_ticket(ticket_id)
    if ticket is None:
        raise HTTPException(status_code=404, detail="Ticket not found")
    return ticket


@router.patch("/{ticket_id}", response_model=Ticket)
async def update_ticket(ticket_id: UUID, update_data: TicketUpdate):
    """
    Update a ticket (partial update)
    
    Args:
        ticket_id: UUID of the ticket
        update_data: Fields to update
        
    Returns:
        Updated ticket
        
    Raises:
        404: Ticket not found
    """
    ticket = ticket_service.update_ticket(ticket_id, update_data)
    if ticket is None:
        raise HTTPException(status_code=404, detail="Ticket not found")
    return ticket


@router.delete("/{ticket_id}", status_code=204)
async def delete_ticket(ticket_id: UUID):
    """
    Delete a ticket
    
    Args:
        ticket_id: UUID of the ticket
        
    Returns:
        204 No Content on success
        
    Raises:
        404: Ticket not found
    """
    success = ticket_service.delete_ticket(ticket_id)
    if not success:
        raise HTTPException(status_code=404, detail="Ticket not found")
    return None
