# GPT-4 Documenter Template - Testing Guide

**Model:** OpenAI GPT-4  
**Role:** Documentation Generation - QA Testing Guide  
**Format:** System/User Message with Test Specifications  
**Best for:** Test pyramid documentation, manual testing checklists, benchmark tables

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
You are an expert QA engineer and technical writer specializing in testing documentation.

RULES:
1. If you are unsure of the answer, state that you do not know. Do not guess.
2. If the input is ambiguous, ask exactly 3 clarifying questions before proceeding.
3. If data is missing, explicitly list what is missing.

DOCUMENTATION STANDARDS:
- Use clear, actionable language for test procedures
- Include Mermaid diagrams for test architecture
- Provide tables for benchmarks and coverage metrics
- List exact commands to run tests
- Include sample data locations and formats

OUTPUT:
- Generate complete TESTING_GUIDE.md in Markdown
- Include test pyramid diagram (Mermaid)
- Provide manual testing checklist
- Document performance benchmarks
```

### User Message Template

```
## Project Context

```json
{
  "project": "{{PROJECT_NAME}}",
  "framework": "{{FRAMEWORK}}",
  "testing_framework": "{{TESTING_FRAMEWORK}}",
  "coverage_target": {{COVERAGE_TARGET}}
}
```

## Test Suite Overview

```json
{
  "test_files": [
    {
      "file": "{{TEST_FILE_1}}",
      "type": "{{TEST_TYPE_1}}",
      "test_count": {{COUNT_1}},
      "description": "{{DESCRIPTION_1}}"
    }
  ],
  "total_tests": {{TOTAL_TESTS}},
  "coverage_target": "{{COVERAGE_TARGET}}%"
}
```

## Test Categories

### Unit Tests
{{UNIT_TEST_DETAILS}}

### Integration Tests
{{INTEGRATION_TEST_DETAILS}}

### Performance Tests
{{PERFORMANCE_TEST_DETAILS}}

## Sample Data

```json
{
  "fixtures": [
    {
      "file": "{{FIXTURE_FILE}}",
      "format": "{{FORMAT}}",
      "record_count": {{COUNT}},
      "description": "{{FIXTURE_DESC}}"
    }
  ]
}
```

## Performance Benchmarks

| Operation | Target | Acceptable | Unacceptable |
|-----------|--------|------------|--------------|
| {{OPERATION_1}} | {{TARGET_1}} | {{ACCEPTABLE_1}} | {{UNACCEPTABLE_1}} |

## Required Output

Generate TESTING_GUIDE.md with:

1. **Overview**
   - Testing philosophy
   - Coverage requirements
   - Test pyramid diagram (Mermaid)

2. **Quick Start**
   - Prerequisites
   - How to run all tests
   - How to run specific test categories

3. **Test Structure**
   - Directory layout
   - Naming conventions
   - Test file descriptions

4. **Test Categories**
   - Unit tests (what they cover)
   - Integration tests (workflows)
   - Performance tests (benchmarks)

5. **Sample Data**
   - Fixture file locations
   - Data formats
   - How to generate new test data

6. **Manual Testing Checklist**
   - Pre-deployment checklist
   - API endpoint verification
   - Edge case testing

7. **Performance Benchmarks**
   - Benchmark table
   - How to run benchmarks
   - Interpreting results

8. **Troubleshooting**
   - Common test failures
   - Environment issues
   - Debug tips
```

---

## Example: Filled Template for Ticket System Testing Guide

### System Message
(Same as above)

### User Message

```
## Project Context

```json
{
  "project": "Customer Support Ticket System",
  "framework": "FastAPI",
  "testing_framework": "pytest",
  "coverage_target": 85
}
```

## Test Suite Overview

```json
{
  "test_files": [
    {
      "file": "test_ticket_api.py",
      "type": "API/Integration",
      "test_count": 11,
      "description": "REST API endpoint tests for all CRUD operations"
    },
    {
      "file": "test_ticket_model.py",
      "type": "Unit",
      "test_count": 9,
      "description": "Pydantic model validation and field constraints"
    },
    {
      "file": "test_import_csv.py",
      "type": "Unit",
      "test_count": 6,
      "description": "CSV file parsing and import functionality"
    },
    {
      "file": "test_import_json.py",
      "type": "Unit",
      "test_count": 5,
      "description": "JSON file parsing and import functionality"
    },
    {
      "file": "test_import_xml.py",
      "type": "Unit",
      "test_count": 5,
      "description": "XML file parsing and import functionality"
    },
    {
      "file": "test_categorization.py",
      "type": "Unit",
      "test_count": 10,
      "description": "Auto-classification service keyword matching and confidence"
    },
    {
      "file": "test_integration.py",
      "type": "Integration",
      "test_count": 5,
      "description": "End-to-end workflows and concurrent operations"
    },
    {
      "file": "test_performance.py",
      "type": "Performance",
      "test_count": 5,
      "description": "Benchmark tests for response times and throughput"
    }
  ],
  "total_tests": 56,
  "coverage_target": "85%"
}
```

## Test Categories

### Unit Tests
- **Model validation**: Test all Pydantic field validators, enum constraints, nested models
- **Import parsing**: Test CSV/JSON/XML parsing with valid, invalid, and edge case data
- **Classification logic**: Test keyword matching, confidence scoring, priority assignment
- **Validators**: Test email format, string length constraints, enum values

### Integration Tests
- **Ticket lifecycle**: Create → Update status → Resolve → Close
- **Bulk import + classification**: Import CSV → Auto-classify all → Verify categories
- **Concurrent requests**: 20+ simultaneous API calls
- **Combined filtering**: Filter by category AND priority AND status

### Performance Tests
- **Single ticket creation**: Target < 50ms
- **Bulk import (100 tickets)**: Target < 2s
- **Classification (single)**: Target < 100ms
- **List with filters (1000 tickets)**: Target < 500ms
- **Concurrent requests (20)**: All complete < 5s

## Sample Data

```json
{
  "fixtures": [
    {
      "file": "fixtures/sample_tickets.csv",
      "format": "CSV",
      "record_count": 50,
      "description": "Valid tickets covering all categories and priorities"
    },
    {
      "file": "fixtures/sample_tickets.json",
      "format": "JSON",
      "record_count": 20,
      "description": "Valid tickets with nested metadata"
    },
    {
      "file": "fixtures/sample_tickets.xml",
      "format": "XML",
      "record_count": 30,
      "description": "Valid tickets in XML format"
    },
    {
      "file": "fixtures/invalid_tickets.csv",
      "format": "CSV",
      "record_count": 10,
      "description": "Invalid tickets for negative testing"
    },
    {
      "file": "fixtures/malformed.csv",
      "format": "CSV",
      "record_count": 5,
      "description": "Malformed CSV for error handling tests"
    }
  ]
}
```

## Performance Benchmarks

| Operation | Target | Acceptable | Unacceptable |
|-----------|--------|------------|--------------|
| Create single ticket | < 50ms | < 100ms | > 200ms |
| Get ticket by ID | < 20ms | < 50ms | > 100ms |
| List tickets (no filter) | < 100ms | < 200ms | > 500ms |
| List tickets (filtered) | < 150ms | < 300ms | > 500ms |
| Bulk import 100 tickets | < 2s | < 5s | > 10s |
| Auto-classify single | < 100ms | < 200ms | > 500ms |
| 20 concurrent requests | < 5s total | < 10s | > 20s |

## Required Output

Generate TESTING_GUIDE.md with all sections including:
- Mermaid test pyramid diagram
- Complete manual testing checklist
- All benchmark tables
- Troubleshooting section
```

---

## Usage Notes

1. **Test file list** should match actual test suite structure
2. **Coverage target** drives detail level in documentation
3. **Benchmark tables** should include three thresholds (target/acceptable/unacceptable)
4. **Manual checklist** covers scenarios hard to automate
5. **Mermaid diagram** visualizes test pyramid (unit > integration > e2e)
6. **Sample data section** helps new team members find test fixtures
