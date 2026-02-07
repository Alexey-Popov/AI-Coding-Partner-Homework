"""
Custom validators for ticket fields

Provides validation functions for:
- Email format (RFC 5322)
- Subject length (1-200 chars)
- Description length (10-2000 chars)
- Category enum values
- Priority enum values
- Tags (non-empty strings)
- Metadata structure
"""

import re
from typing import Any, Dict, List

from ..models import TicketCategory, TicketPriority, TicketSource


# RFC 5322 compliant email regex (simplified)
EMAIL_REGEX = re.compile(
    r"^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
)


def validate_email(email: str) -> tuple[bool, str]:
    """
    Validate email format (RFC 5322)
    
    Args:
        email: Email address to validate
        
    Returns:
        Tuple of (is_valid, error_message)
    """
    if not email:
        return False, "Email is required"
    
    if len(email) > 254:  # RFC 5321
        return False, "Email address too long (max 254 characters)"
    
    if not EMAIL_REGEX.match(email):
        return False, "Invalid email format"
    
    return True, ""


def validate_subject(subject: str) -> tuple[bool, str]:
    """
    Validate subject length (1-200 characters)
    
    Args:
        subject: Subject string to validate
        
    Returns:
        Tuple of (is_valid, error_message)
    """
    if not subject:
        return False, "Subject is required"
    
    if len(subject) < 1:
        return False, "Subject must be at least 1 character"
    
    if len(subject) > 200:
        return False, "Subject must not exceed 200 characters"
    
    return True, ""


def validate_description(description: str) -> tuple[bool, str]:
    """
    Validate description length (10-2000 characters)
    
    Args:
        description: Description string to validate
        
    Returns:
        Tuple of (is_valid, error_message)
    """
    if not description:
        return False, "Description is required"
    
    if len(description) < 10:
        return False, "Description must be at least 10 characters"
    
    if len(description) > 2000:
        return False, "Description must not exceed 2000 characters"
    
    return True, ""


def validate_category(category: str) -> tuple[bool, str]:
    """
    Validate that category is a valid TicketCategory enum value
    
    Args:
        category: Category string to validate
        
    Returns:
        Tuple of (is_valid, error_message)
    """
    try:
        TicketCategory(category)
        return True, ""
    except ValueError:
        valid_values = [c.value for c in TicketCategory]
        return False, f"Invalid category. Must be one of: {', '.join(valid_values)}"


def validate_priority(priority: str) -> tuple[bool, str]:
    """
    Validate that priority is a valid TicketPriority enum value
    
    Args:
        priority: Priority string to validate
        
    Returns:
        Tuple of (is_valid, error_message)
    """
    try:
        TicketPriority(priority)
        return True, ""
    except ValueError:
        valid_values = [p.value for p in TicketPriority]
        return False, f"Invalid priority. Must be one of: {', '.join(valid_values)}"


def validate_tags(tags: List[str]) -> tuple[bool, str]:
    """
    Validate that tags is an array of non-empty strings
    
    Args:
        tags: List of tag strings
        
    Returns:
        Tuple of (is_valid, error_message)
    """
    if not isinstance(tags, list):
        return False, "Tags must be an array"
    
    for i, tag in enumerate(tags):
        if not isinstance(tag, str):
            return False, f"Tag at index {i} must be a string"
        if not tag.strip():
            return False, f"Tag at index {i} cannot be empty"
    
    return True, ""


def validate_metadata(metadata: Dict[str, Any]) -> tuple[bool, str]:
    """
    Validate metadata structure and source field
    
    Args:
        metadata: Metadata dictionary
        
    Returns:
        Tuple of (is_valid, error_message)
    """
    if not isinstance(metadata, dict):
        return False, "Metadata must be an object"
    
    if "source" not in metadata:
        return False, "Metadata must include 'source' field"
    
    source = metadata["source"]
    try:
        TicketSource(source)
    except ValueError:
        valid_values = [s.value for s in TicketSource]
        return False, f"Invalid source. Must be one of: {', '.join(valid_values)}"
    
    # Validate device_type if present
    if "device_type" in metadata and metadata["device_type"] is not None:
        if metadata["device_type"] not in ["desktop", "mobile", "tablet"]:
            return False, "device_type must be 'desktop', 'mobile', or 'tablet'"
    
    return True, ""


def validate_ticket_data(data: Dict[str, Any]) -> List[Dict[str, str]]:
    """
    Validate all fields in a ticket data dictionary
    
    Args:
        data: Dictionary containing ticket fields
        
    Returns:
        List of error dictionaries with 'field' and 'message' keys
    """
    errors = []
    
    # Required fields
    required_fields = ["subject", "description", "customer_id", "customer_email", 
                       "customer_name", "category", "metadata"]
    
    for field in required_fields:
        if field not in data or data[field] is None:
            errors.append({"field": field, "message": f"{field} is required"})
    
    # Validate subject
    if "subject" in data:
        is_valid, message = validate_subject(data["subject"])
        if not is_valid:
            errors.append({"field": "subject", "message": message})
    
    # Validate description
    if "description" in data:
        is_valid, message = validate_description(data["description"])
        if not is_valid:
            errors.append({"field": "description", "message": message})
    
    # Validate email
    if "customer_email" in data:
        is_valid, message = validate_email(data["customer_email"])
        if not is_valid:
            errors.append({"field": "customer_email", "message": message})
    
    # Validate category
    if "category" in data:
        is_valid, message = validate_category(data["category"])
        if not is_valid:
            errors.append({"field": "category", "message": message})
    
    # Validate priority (if present)
    if "priority" in data and data["priority"] is not None:
        is_valid, message = validate_priority(data["priority"])
        if not is_valid:
            errors.append({"field": "priority", "message": message})
    
    # Validate tags (if present)
    if "tags" in data and data["tags"] is not None:
        is_valid, message = validate_tags(data["tags"])
        if not is_valid:
            errors.append({"field": "tags", "message": message})
    
    # Validate metadata
    if "metadata" in data:
        is_valid, message = validate_metadata(data["metadata"])
        if not is_valid:
            errors.append({"field": "metadata", "message": message})
    
    return errors
