# GPT-4 Tester Template - Import Parsing Tests

**Model:** OpenAI GPT-4  
**Role:** Test Generation - CSV/JSON/XML Import Tests  
**Format:** System/User Message with Format Specifications  
**Best for:** Multi-format parsing tests, data transformation validation

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
You are an expert Python test engineer specializing in data import and parsing tests.

RULES:
1. If you are unsure of the answer, state that you do not know. Do not guess.
2. If the input is ambiguous, ask exactly 3 clarifying questions before proceeding.
3. If data is missing, explicitly list what is missing.

TESTING PATTERNS:
- Test each format (CSV, JSON, XML) with valid and invalid data
- Use pytest fixtures for sample file content
- Test edge cases: empty files, malformed data, encoding issues
- Verify correct field mapping
- Test bulk operations with partial failures
- Use tmp_path fixture for file operations

OUTPUT:
- Generate complete pytest files for import testing
- Include sample data as fixtures
- Test both success and error scenarios
```

### User Message Template

```
## Project Context

```json
{
  "project": "{{PROJECT_NAME}}",
  "import_service": "{{IMPORT_SERVICE_PATH}}",
  "supported_formats": ["csv", "json", "xml"],
  "target_model": "{{TARGET_MODEL}}"
}
```

## Import Service Interface

```python
{{IMPORT_SERVICE_INTERFACE}}
```

## Data Format Specifications

### CSV Format
```
{{CSV_SCHEMA}}
```

### JSON Format
```json
{{JSON_SCHEMA}}
```

### XML Format
```xml
{{XML_SCHEMA}}
```

## Test Specifications

```json
{
  "csv_tests": [
    {
      "name": "{{TEST_NAME}}",
      "description": "{{DESCRIPTION}}",
      "input_data": "{{CSV_CONTENT}}",
      "expected_count": {{COUNT}},
      "expected_errors": {{ERRORS}}
    }
  ],
  "json_tests": [
    {
      "name": "{{TEST_NAME}}",
      "description": "{{DESCRIPTION}}",
      "input_data": {{JSON_CONTENT}},
      "expected_count": {{COUNT}},
      "expected_errors": {{ERRORS}}
    }
  ],
  "xml_tests": [
    {
      "name": "{{TEST_NAME}}",
      "description": "{{DESCRIPTION}}",
      "input_data": "{{XML_CONTENT}}",
      "expected_count": {{COUNT}},
      "expected_errors": {{ERRORS}}
    }
  ]
}
```

## Required Output

Generate separate test files for each format:
1. tests/test_import_csv.py
2. tests/test_import_json.py
3. tests/test_import_xml.py

Each file should include:
- Module docstring
- Sample data fixtures
- Valid import tests
- Invalid data handling tests
- Edge case tests (empty, partial, malformed)
- Encoding tests (if applicable)
```

---

## Example: Filled Template for Ticket Import Tests

### System Message
(Same as above)

### User Message

```
## Project Context

```json
{
  "project": "Customer Support Ticket System",
  "import_service": "src/services/import_service.py",
  "supported_formats": ["csv", "json", "xml"],
  "target_model": "Ticket"
}
```

## Import Service Interface

```python
from typing import List, Dict, Any
from models.ticket import Ticket, TicketCreate

class ImportService:
    """Service for importing tickets from various formats."""
    
    def import_csv(self, csv_content: str) -> Dict[str, Any]:
        """
        Import tickets from CSV content.
        
        Returns:
            {
                "success_count": int,
                "error_count": int,
                "tickets": List[Ticket],
                "errors": List[{"row": int, "error": str}]
            }
        """
        pass
    
    def import_json(self, json_content: str) -> Dict[str, Any]:
        """Import tickets from JSON string."""
        pass
    
    def import_xml(self, xml_content: str) -> Dict[str, Any]:
        """Import tickets from XML string."""
        pass
```

## Data Format Specifications

### CSV Format
```
title,description,customer_email,customer_name,priority,category
"Login Issue","Cannot access dashboard","user@example.com","John Doe","high","technical"
```

Required columns: title, description, customer_email, customer_name
Optional columns: priority, category, status

### JSON Format
```json
{
  "tickets": [
    {
      "title": "string (required)",
      "description": "string (required)",
      "customer_email": "string (required)",
      "customer_name": "string (required)",
      "priority": "string (optional)",
      "category": "string (optional)"
    }
  ]
}
```

### XML Format
```xml
<?xml version="1.0" encoding="UTF-8"?>
<tickets>
  <ticket>
    <title>string (required)</title>
    <description>string (required)</description>
    <customer_email>string (required)</customer_email>
    <customer_name>string (required)</customer_name>
    <priority>string (optional)</priority>
    <category>string (optional)</category>
  </ticket>
</tickets>
```

## Test Specifications

```json
{
  "csv_tests": [
    {
      "name": "test_import_valid_csv_single_row",
      "description": "Import single valid ticket from CSV",
      "input_data": "title,description,customer_email,customer_name\nLogin Bug,Cannot login,user@test.com,John Doe",
      "expected_count": 1,
      "expected_errors": 0
    },
    {
      "name": "test_import_valid_csv_multiple_rows",
      "description": "Import multiple valid tickets",
      "input_data": "title,description,customer_email,customer_name\nBug 1,Desc 1,a@b.com,User 1\nBug 2,Desc 2,c@d.com,User 2\nBug 3,Desc 3,e@f.com,User 3",
      "expected_count": 3,
      "expected_errors": 0
    },
    {
      "name": "test_import_csv_with_optional_fields",
      "description": "Import CSV with priority and category",
      "input_data": "title,description,customer_email,customer_name,priority,category\nBilling Issue,Charge wrong,x@y.com,Jane,high,billing",
      "expected_count": 1,
      "expected_errors": 0
    },
    {
      "name": "test_import_csv_with_invalid_email",
      "description": "Row with invalid email should be skipped",
      "input_data": "title,description,customer_email,customer_name\nValid,Valid desc,valid@test.com,User\nInvalid,Invalid desc,not-an-email,User2",
      "expected_count": 1,
      "expected_errors": 1
    },
    {
      "name": "test_import_csv_empty_file",
      "description": "Empty CSV should return zero tickets",
      "input_data": "",
      "expected_count": 0,
      "expected_errors": 0
    },
    {
      "name": "test_import_csv_header_only",
      "description": "CSV with only header should return zero tickets",
      "input_data": "title,description,customer_email,customer_name",
      "expected_count": 0,
      "expected_errors": 0
    }
  ],
  "json_tests": [
    {
      "name": "test_import_valid_json_array",
      "description": "Import array of valid tickets",
      "input_data": {"tickets": [{"title": "Test", "description": "Test description", "customer_email": "a@b.com", "customer_name": "User"}]},
      "expected_count": 1,
      "expected_errors": 0
    },
    {
      "name": "test_import_json_missing_required_field",
      "description": "Ticket missing title should error",
      "input_data": {"tickets": [{"description": "No title", "customer_email": "a@b.com", "customer_name": "User"}]},
      "expected_count": 0,
      "expected_errors": 1
    },
    {
      "name": "test_import_json_invalid_format",
      "description": "Malformed JSON should raise error",
      "input_data": "not valid json",
      "expected_count": 0,
      "expected_errors": 1
    },
    {
      "name": "test_import_json_empty_array",
      "description": "Empty tickets array",
      "input_data": {"tickets": []},
      "expected_count": 0,
      "expected_errors": 0
    },
    {
      "name": "test_import_json_mixed_valid_invalid",
      "description": "Mix of valid and invalid tickets",
      "input_data": {"tickets": [
        {"title": "Valid", "description": "Valid ticket", "customer_email": "good@email.com", "customer_name": "Valid User"},
        {"title": "Bad", "description": "Bad ticket", "customer_email": "bad-email", "customer_name": "Bad User"}
      ]},
      "expected_count": 1,
      "expected_errors": 1
    }
  ],
  "xml_tests": [
    {
      "name": "test_import_valid_xml_single_ticket",
      "description": "Import single ticket from XML",
      "input_data": "<?xml version='1.0'?><tickets><ticket><title>XML Test</title><description>XML Description</description><customer_email>xml@test.com</customer_email><customer_name>XML User</customer_name></ticket></tickets>",
      "expected_count": 1,
      "expected_errors": 0
    },
    {
      "name": "test_import_xml_malformed",
      "description": "Malformed XML should raise error",
      "input_data": "<tickets><ticket><title>Unclosed",
      "expected_count": 0,
      "expected_errors": 1
    },
    {
      "name": "test_import_xml_missing_element",
      "description": "Missing required element should error",
      "input_data": "<?xml version='1.0'?><tickets><ticket><title>No Email</title><description>Desc</description><customer_name>User</customer_name></ticket></tickets>",
      "expected_count": 0,
      "expected_errors": 1
    },
    {
      "name": "test_import_xml_empty_root",
      "description": "Empty tickets element",
      "input_data": "<?xml version='1.0'?><tickets></tickets>",
      "expected_count": 0,
      "expected_errors": 0
    },
    {
      "name": "test_import_xml_with_cdata",
      "description": "XML with CDATA in description",
      "input_data": "<?xml version='1.0'?><tickets><ticket><title>CDATA Test</title><description><![CDATA[Description with <special> chars]]></description><customer_email>a@b.com</customer_email><customer_name>User</customer_name></ticket></tickets>",
      "expected_count": 1,
      "expected_errors": 0
    }
  ]
}
```

## Required Output

Generate three test files:

### tests/test_import_csv.py
- TestCSVImportValid class
- TestCSVImportInvalid class
- TestCSVImportEdgeCases class
- Fixtures for sample CSV content

### tests/test_import_json.py
- TestJSONImportValid class
- TestJSONImportInvalid class
- TestJSONImportEdgeCases class
- Fixtures for sample JSON content

### tests/test_import_xml.py
- TestXMLImportValid class
- TestXMLImportInvalid class
- TestXMLImportEdgeCases class
- Fixtures for sample XML content
```

---

## Usage Notes

1. **Format specifications** define the expected schema for each format
2. **Test each format separately** for cleaner test organization
3. **Include malformed data tests** - they catch parsing edge cases
4. **Test partial success scenarios** - some rows valid, some invalid
5. **Use fixtures for sample data** - keeps tests readable
6. **Test encoding issues** if supporting international characters
