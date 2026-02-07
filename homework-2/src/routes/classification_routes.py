"""
Classification Routes - Auto-categorization endpoints

Endpoints:
- POST /tickets/{id}/classify - Classify single ticket
- POST /tickets/classify-all - Classify all tickets
"""

from typing import List
from uuid import UUID

from fastapi import APIRouter, HTTPException

from ..models import ClassificationResult
from ..services.classification_service import classification_service
from ..services.ticket_service import ticket_service


router = APIRouter()


@router.post("/{ticket_id}/classify", response_model=ClassificationResult)
async def classify_ticket(ticket_id: UUID):
    """
    Classify a single ticket
    
    Analyzes ticket content and suggests:
    - Category (based on keyword matching)
    - Priority (based on urgency keywords)
    - Confidence score
    - Reasoning
    
    Args:
        ticket_id: UUID of the ticket to classify
        
    Returns:
        ClassificationResult with suggestions
        
    Raises:
        404: Ticket not found
    """
    ticket = ticket_service.get_ticket(ticket_id)
    if ticket is None:
        raise HTTPException(status_code=404, detail="Ticket not found")
    
    result = classification_service.classify_ticket(ticket)
    return result


@router.post("/classify-all", response_model=List[ClassificationResult])
async def classify_all_tickets():
    """
    Auto-classify all tickets
    
    Analyzes all tickets in the system and provides classification
    suggestions for each one.
    
    Returns:
        List of classification results for all tickets
    """
    results = classification_service.auto_classify_all()
    return results


@router.post("/{ticket_id}/apply-classification", response_model=dict)
async def apply_classification(ticket_id: UUID):
    """
    Classify a ticket and apply the suggestions
    
    This endpoint both classifies the ticket and updates it with
    the suggested category and priority.
    
    Args:
        ticket_id: UUID of the ticket
        
    Returns:
        Success message with applied classification
        
    Raises:
        404: Ticket not found
    """
    ticket = ticket_service.get_ticket(ticket_id)
    if ticket is None:
        raise HTTPException(status_code=404, detail="Ticket not found")
    
    # Classify
    result = classification_service.classify_ticket(ticket)
    
    # Apply
    success = classification_service.apply_classification(ticket_id, result)
    if not success:
        raise HTTPException(status_code=500, detail="Failed to apply classification")
    
    return {
        "message": "Classification applied successfully",
        "ticket_id": str(ticket_id),
        "suggested_category": result.suggested_category.value,
        "suggested_priority": result.suggested_priority.value,
        "confidence": result.confidence,
    }
