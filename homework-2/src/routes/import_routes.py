"""
Import Routes - Bulk import from multiple file formats

Endpoints:
- POST /import/csv - Import from CSV file
- POST /import/json - Import from JSON file
- POST /import/xml - Import from XML file
"""

from fastapi import APIRouter, File, HTTPException, UploadFile

from ..models import ImportResult
from ..services.import_service import import_service


router = APIRouter()


@router.post("/csv", response_model=ImportResult)
async def import_csv(file: UploadFile = File(...)):
    """
    Bulk import tickets from CSV file
    
    Expected CSV format with header:
    customer_id,customer_email,customer_name,subject,description,category,priority,tags,source,browser,device_type
    
    Args:
        file: CSV file upload
        
    Returns:
        ImportResult with success/error counts and details
    """
    if not file.filename.endswith('.csv'):
        raise HTTPException(status_code=400, detail="File must be a CSV file")
    
    result = await import_service.import_csv(file)
    return result


@router.post("/json", response_model=ImportResult)
async def import_json(file: UploadFile = File(...)):
    """
    Bulk import tickets from JSON file
    
    Expected JSON format:
    [
      {
        "customer_id": "...",
        "customer_email": "...",
        "customer_name": "...",
        "subject": "...",
        "description": "...",
        "category": "...",
        "priority": "...",
        "tags": [...],
        "metadata": {"source": "...", "browser": "...", "device_type": "..."}
      }
    ]
    
    Args:
        file: JSON file upload
        
    Returns:
        ImportResult with success/error counts and details
    """
    if not file.filename.endswith('.json'):
        raise HTTPException(status_code=400, detail="File must be a JSON file")
    
    result = await import_service.import_json(file)
    return result


@router.post("/xml", response_model=ImportResult)
async def import_xml(file: UploadFile = File(...)):
    """
    Bulk import tickets from XML file
    
    Expected XML format:
    <tickets>
      <ticket>
        <customer_id>...</customer_id>
        <customer_email>...</customer_email>
        <customer_name>...</customer_name>
        <subject>...</subject>
        <description>...</description>
        <category>...</category>
        <priority>...</priority>
        <tags>
          <tag>tag1</tag>
          <tag>tag2</tag>
        </tags>
        <metadata>
          <source>...</source>
          <browser>...</browser>
          <device_type>...</device_type>
        </metadata>
      </ticket>
    </tickets>
    
    Args:
        file: XML file upload
        
    Returns:
        ImportResult with success/error counts and details
    """
    if not file.filename.endswith('.xml'):
        raise HTTPException(status_code=400, detail="File must be an XML file")
    
    result = await import_service.import_xml(file)
    return result
