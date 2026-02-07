"""
Ticket Service - Business logic for ticket CRUD operations

Storage: In-memory Dict[UUID, Ticket]
- Simple for homework scope
- No database setup required
- Fast for <10K tickets
- Easily replaceable via service abstraction
"""

from datetime import datetime
from typing import Dict, List, Optional
from uuid import UUID

from ..models import Ticket, TicketCreate, TicketUpdate, TicketCategory, TicketPriority, TicketStatus


class TicketService:
    """Service for managing tickets in memory"""
    
    def __init__(self):
        """Initialize empty ticket storage"""
        self._tickets: Dict[UUID, Ticket] = {}
    
    def create_ticket(self, ticket_data: TicketCreate) -> Ticket:
        """
        Create a new ticket with auto-generated UUID
        
        Args:
            ticket_data: Ticket creation data
            
        Returns:
            Created ticket with ID and timestamps
        """
        ticket = Ticket(
            **ticket_data.model_dump(),
            status=TicketStatus.NEW,
            created_at=datetime.now(),
            updated_at=datetime.now(),
        )
        self._tickets[ticket.id] = ticket
        return ticket
    
    def get_ticket(self, ticket_id: UUID) -> Optional[Ticket]:
        """
        Get a ticket by ID
        
        Args:
            ticket_id: UUID of the ticket
            
        Returns:
            Ticket if found, None otherwise
        """
        return self._tickets.get(ticket_id)
    
    def get_all_tickets(
        self,
        category: Optional[TicketCategory] = None,
        priority: Optional[TicketPriority] = None,
        status: Optional[TicketStatus] = None,
    ) -> List[Ticket]:
        """
        Get all tickets with optional filtering
        
        Args:
            category: Filter by category
            priority: Filter by priority
            status: Filter by status
            
        Returns:
            List of tickets matching filters
        """
        tickets = list(self._tickets.values())
        
        if category is not None:
            tickets = [t for t in tickets if t.category == category]
        
        if priority is not None:
            tickets = [t for t in tickets if t.priority == priority]
        
        if status is not None:
            tickets = [t for t in tickets if t.status == status]
        
        return tickets
    
    def update_ticket(self, ticket_id: UUID, update_data: TicketUpdate) -> Optional[Ticket]:
        """
        Update an existing ticket (partial update)
        
        Args:
            ticket_id: UUID of the ticket to update
            update_data: Fields to update
            
        Returns:
            Updated ticket if found, None otherwise
        """
        ticket = self._tickets.get(ticket_id)
        if ticket is None:
            return None
        
        # Update only provided fields
        update_dict = update_data.model_dump(exclude_unset=True)
        for field, value in update_dict.items():
            setattr(ticket, field, value)
        
        ticket.updated_at = datetime.now()
        
        # Set resolved_at if status changed to resolved
        if update_data.status == TicketStatus.RESOLVED and ticket.resolved_at is None:
            ticket.resolved_at = datetime.now()
        
        return ticket
    
    def delete_ticket(self, ticket_id: UUID) -> bool:
        """
        Delete a ticket by ID
        
        Args:
            ticket_id: UUID of the ticket to delete
            
        Returns:
            True if deleted, False if not found
        """
        if ticket_id in self._tickets:
            del self._tickets[ticket_id]
            return True
        return False
    
    def get_statistics(self) -> Dict:
        """
        Get statistics about tickets
        
        Returns:
            Dictionary with counts by category, priority, and status
        """
        tickets = list(self._tickets.values())
        
        stats = {
            "total": len(tickets),
            "by_category": {},
            "by_priority": {},
            "by_status": {},
        }
        
        # Count by category
        for category in TicketCategory:
            count = sum(1 for t in tickets if t.category == category)
            stats["by_category"][category.value] = count
        
        # Count by priority
        for priority in TicketPriority:
            count = sum(1 for t in tickets if t.priority == priority)
            stats["by_priority"][priority.value] = count
        
        # Count by status
        for status in TicketStatus:
            count = sum(1 for t in tickets if t.status == status)
            stats["by_status"][status.value] = count
        
        return stats
    
    def clear_all(self):
        """Clear all tickets (useful for testing)"""
        self._tickets.clear()


# Global service instance (singleton pattern for simplicity)
ticket_service = TicketService()
