"""
Import Service - Multi-format bulk import (CSV, JSON, XML)

Handles:
- CSV with header row
- JSON array format
- XML <tickets><ticket>...</ticket></tickets> format

Error handling:
- Continue processing on individual row failure
- Return partial success with error details
- Malformed file (unparseable) returns immediate error
"""

import csv
import json
import xml.etree.ElementTree as ET
from io import StringIO
from typing import Any, Dict, List
from uuid import UUID

from fastapi import UploadFile

from ..models import TicketCreate, TicketMetadata, ImportResult, ImportError
from ..validators.ticket_validator import validate_ticket_data
from .ticket_service import ticket_service


class ImportService:
    """Service for importing tickets from various file formats"""
    
    def __init__(self):
        """Initialize import service"""
        pass
    
    async def import_csv(self, file: UploadFile) -> ImportResult:
        """
        Import tickets from CSV file
        
        Expected CSV columns:
        customer_id,customer_email,customer_name,subject,description,category,priority,tags,source,browser,device_type
        
        Args:
            file: Uploaded CSV file
            
        Returns:
            ImportResult with success/error counts and details
        """
        try:
            content = await file.read()
            content_str = content.decode('utf-8')
        except Exception as e:
            return ImportResult(
                total=0,
                success_count=0,
                error_count=0,
                errors=[ImportError(row=0, errors=[f"Failed to read file: {str(e)}"])],
                imported_ids=[]
            )
        
        # Parse CSV
        try:
            csv_reader = csv.DictReader(StringIO(content_str))
            rows = list(csv_reader)
        except Exception as e:
            return ImportResult(
                total=0,
                success_count=0,
                error_count=0,
                errors=[ImportError(row=0, errors=[f"Failed to parse CSV: {str(e)}"])],
                imported_ids=[]
            )
        
        return self._process_rows(rows)
    
    async def import_json(self, file: UploadFile) -> ImportResult:
        """
        Import tickets from JSON file (array format)
        
        Expected format: Array of ticket objects
        [
          {
            "customer_id": "...",
            "customer_email": "...",
            ...
          }
        ]
        
        Args:
            file: Uploaded JSON file
            
        Returns:
            ImportResult with success/error counts and details
        """
        try:
            content = await file.read()
            content_str = content.decode('utf-8')
        except Exception as e:
            return ImportResult(
                total=0,
                success_count=0,
                error_count=0,
                errors=[ImportError(row=0, errors=[f"Failed to read file: {str(e)}"])],
                imported_ids=[]
            )
        
        # Parse JSON
        try:
            data = json.loads(content_str)
            if not isinstance(data, list):
                return ImportResult(
                    total=0,
                    success_count=0,
                    error_count=0,
                    errors=[ImportError(row=0, errors=["JSON must be an array of ticket objects"])],
                    imported_ids=[]
                )
            rows = data
        except Exception as e:
            return ImportResult(
                total=0,
                success_count=0,
                error_count=0,
                errors=[ImportError(row=0, errors=[f"Failed to parse JSON: {str(e)}"])],
                imported_ids=[]
            )
        
        return self._process_rows(rows)
    
    async def import_xml(self, file: UploadFile) -> ImportResult:
        """
        Import tickets from XML file
        
        Expected format:
        <tickets>
          <ticket>
            <customer_id>...</customer_id>
            <customer_email>...</customer_email>
            ...
          </ticket>
        </tickets>
        
        Args:
            file: Uploaded XML file
            
        Returns:
            ImportResult with success/error counts and details
        """
        try:
            content = await file.read()
            content_str = content.decode('utf-8')
        except Exception as e:
            return ImportResult(
                total=0,
                success_count=0,
                error_count=0,
                errors=[ImportError(row=0, errors=[f"Failed to read file: {str(e)}"])],
                imported_ids=[]
            )
        
        # Parse XML
        try:
            root = ET.fromstring(content_str)
            if root.tag != "tickets":
                return ImportResult(
                    total=0,
                    success_count=0,
                    error_count=0,
                    errors=[ImportError(row=0, errors=["XML root element must be <tickets>"])],
                    imported_ids=[]
                )
            
            rows = []
            for ticket_elem in root.findall("ticket"):
                row = {}
                for child in ticket_elem:
                    # Handle tags (array)
                    if child.tag == "tags":
                        row["tags"] = [tag.text for tag in child.findall("tag") if tag.text]
                    # Handle metadata (nested object)
                    elif child.tag == "metadata":
                        row["metadata"] = {}
                        for meta_child in child:
                            row["metadata"][meta_child.tag] = meta_child.text
                    else:
                        row[child.tag] = child.text
                rows.append(row)
        except Exception as e:
            return ImportResult(
                total=0,
                success_count=0,
                error_count=0,
                errors=[ImportError(row=0, errors=[f"Failed to parse XML: {str(e)}"])],
                imported_ids=[]
            )
        
        return self._process_rows(rows)
    
    def _parse_csv_row(self, row: Dict[str, Any]) -> Dict[str, Any]:
        """
        Convert CSV row to ticket data dictionary
        
        CSV format has flat structure, need to convert to nested for metadata
        
        Args:
            row: CSV row dictionary
            
        Returns:
            Ticket data dictionary
        """
        # Parse tags (comma-separated string)
        tags = []
        if "tags" in row and row["tags"]:
            tags = [tag.strip() for tag in row["tags"].split(",") if tag.strip()]
        
        # Build metadata object
        metadata = {
            "source": row.get("source", "api"),
        }
        if "browser" in row and row["browser"]:
            metadata["browser"] = row["browser"]
        if "device_type" in row and row["device_type"]:
            metadata["device_type"] = row["device_type"]
        
        return {
            "customer_id": row.get("customer_id"),
            "customer_email": row.get("customer_email"),
            "customer_name": row.get("customer_name"),
            "subject": row.get("subject"),
            "description": row.get("description"),
            "category": row.get("category"),
            "priority": row.get("priority", "medium"),
            "tags": tags,
            "metadata": metadata,
        }
    
    def _process_rows(self, rows: List[Dict[str, Any]]) -> ImportResult:
        """
        Process rows and create tickets, handling errors gracefully
        
        Args:
            rows: List of row dictionaries
            
        Returns:
            ImportResult with success/error counts
        """
        total = len(rows)
        success_count = 0
        error_count = 0
        errors: List[ImportError] = []
        imported_ids: List[UUID] = []
        
        for i, row in enumerate(rows):
            row_number = i + 1
            
            try:
                # Parse row (especially for CSV format)
                if "metadata" not in row:
                    ticket_data = self._parse_csv_row(row)
                else:
                    ticket_data = row
                
                # Validate
                validation_errors = validate_ticket_data(ticket_data)
                if validation_errors:
                    error_count += 1
                    errors.append(ImportError(
                        row=row_number,
                        errors=[f"{err['field']}: {err['message']}" for err in validation_errors]
                    ))
                    continue
                
                # Create ticket
                metadata = TicketMetadata(**ticket_data["metadata"])
                ticket_create = TicketCreate(
                    customer_id=ticket_data["customer_id"],
                    customer_email=ticket_data["customer_email"],
                    customer_name=ticket_data["customer_name"],
                    subject=ticket_data["subject"],
                    description=ticket_data["description"],
                    category=ticket_data["category"],
                    priority=ticket_data.get("priority", "medium"),
                    tags=ticket_data.get("tags", []),
                    metadata=metadata,
                )
                
                ticket = ticket_service.create_ticket(ticket_create)
                success_count += 1
                imported_ids.append(ticket.id)
                
            except Exception as e:
                error_count += 1
                errors.append(ImportError(
                    row=row_number,
                    errors=[f"Unexpected error: {str(e)}"]
                ))
        
        return ImportResult(
            total=total,
            success_count=success_count,
            error_count=error_count,
            errors=errors,
            imported_ids=imported_ids,
        )


# Global service instance
import_service = ImportService()
