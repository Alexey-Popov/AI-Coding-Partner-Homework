"""
Pydantic models for Customer Support Ticket System

Enums:
- TicketCategory: account_access, technical_issue, billing_question, feature_request, bug_report, other
- TicketPriority: urgent, high, medium, low  
- TicketStatus: new, in_progress, waiting_customer, resolved, closed
- TicketSource: web_form, email, api, chat, phone

Models:
- TicketBase: Common fields
- TicketCreate: For POST requests
- TicketUpdate: For PATCH requests (all optional)
- Ticket: Full model with UUID and timestamps
- TicketList: Response wrapper
- ClassificationResult: Auto-classification output
- ImportResult: Bulk import result
"""

from datetime import datetime
from enum import Enum
from typing import Any, Dict, List, Optional
from uuid import UUID, uuid4

from pydantic import BaseModel, EmailStr, Field


class TicketCategory(str, Enum):
    """Categories for support tickets"""
    ACCOUNT_ACCESS = "account_access"
    TECHNICAL_ISSUE = "technical_issue"
    BILLING_QUESTION = "billing_question"
    FEATURE_REQUEST = "feature_request"
    BUG_REPORT = "bug_report"
    OTHER = "other"


class TicketPriority(str, Enum):
    """Priority levels for tickets"""
    URGENT = "urgent"
    HIGH = "high"
    MEDIUM = "medium"
    LOW = "low"


class TicketStatus(str, Enum):
    """Status values for ticket lifecycle"""
    NEW = "new"
    IN_PROGRESS = "in_progress"
    WAITING_CUSTOMER = "waiting_customer"
    RESOLVED = "resolved"
    CLOSED = "closed"


class TicketSource(str, Enum):
    """Source channels for tickets"""
    WEB_FORM = "web_form"
    EMAIL = "email"
    API = "api"
    CHAT = "chat"
    PHONE = "phone"


class TicketMetadata(BaseModel):
    """Metadata about ticket source and context"""
    source: TicketSource = Field(..., description="Source channel")
    browser: Optional[str] = Field(None, description="Browser used (if web_form)")
    device_type: Optional[str] = Field(None, description="Device type: desktop, mobile, or tablet")


class TicketBase(BaseModel):
    """Base model with common ticket fields"""
    subject: str = Field(..., min_length=1, max_length=200, description="Ticket subject")
    description: str = Field(..., min_length=10, max_length=2000, description="Detailed description")
    customer_id: str = Field(..., description="Customer identifier")
    customer_email: EmailStr = Field(..., description="Customer email address")
    customer_name: str = Field(..., description="Customer name")
    category: TicketCategory = Field(..., description="Ticket category")
    priority: TicketPriority = Field(default=TicketPriority.MEDIUM, description="Ticket priority")
    tags: List[str] = Field(default_factory=list, description="Tags for categorization")
    metadata: TicketMetadata = Field(..., description="Source and context metadata")


class TicketCreate(TicketBase):
    """Model for creating a new ticket"""
    pass


class TicketUpdate(BaseModel):
    """Model for updating an existing ticket (all fields optional)"""
    subject: Optional[str] = Field(None, min_length=1, max_length=200)
    description: Optional[str] = Field(None, min_length=10, max_length=2000)
    customer_email: Optional[EmailStr] = None
    customer_name: Optional[str] = None
    category: Optional[TicketCategory] = None
    priority: Optional[TicketPriority] = None
    status: Optional[TicketStatus] = None
    tags: Optional[List[str]] = None
    assigned_to: Optional[str] = None


class Ticket(TicketBase):
    """Full ticket model with all fields"""
    id: UUID = Field(default_factory=uuid4, description="Unique ticket identifier (UUID)")
    status: TicketStatus = Field(default=TicketStatus.NEW, description="Current status")
    created_at: datetime = Field(default_factory=datetime.now, description="Creation timestamp")
    updated_at: datetime = Field(default_factory=datetime.now, description="Last update timestamp")
    resolved_at: Optional[datetime] = Field(None, description="Resolution timestamp")
    assigned_to: Optional[str] = Field(None, description="Assigned staff member")

    class Config:
        from_attributes = True


class TicketList(BaseModel):
    """Response model for listing tickets"""
    items: List[Ticket] = Field(default_factory=list, description="List of tickets")
    total: int = Field(..., description="Total number of tickets")


class ClassificationResult(BaseModel):
    """Result of auto-classification"""
    ticket_id: UUID = Field(..., description="ID of classified ticket")
    suggested_category: TicketCategory = Field(..., description="Suggested category")
    suggested_priority: TicketPriority = Field(..., description="Suggested priority")
    confidence: float = Field(..., ge=0.0, le=1.0, description="Confidence score 0-1")
    reasoning: str = Field(..., description="Explanation of classification")
    keywords_found: List[str] = Field(default_factory=list, description="Keywords that matched")


class ImportError(BaseModel):
    """Error details for failed import row"""
    row: int = Field(..., description="Row number (1-indexed)")
    errors: List[str] = Field(default_factory=list, description="List of validation errors")


class ImportResult(BaseModel):
    """Result of bulk import operation"""
    total: int = Field(..., description="Total number of rows attempted")
    success_count: int = Field(..., description="Number of successfully imported tickets")
    error_count: int = Field(..., description="Number of failed imports")
    errors: List[ImportError] = Field(default_factory=list, description="Details of import errors")
    imported_ids: List[UUID] = Field(default_factory=list, description="IDs of imported tickets")
