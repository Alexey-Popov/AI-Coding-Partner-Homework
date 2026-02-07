# Customer Support Ticket System

A RESTful API for managing customer support tickets with multi-format bulk import capabilities and automatic ticket classification based on content analysis.

**Student Name**: Mykhailo Bestiuk  
**Date Submitted**: 2026-01-01  
**Status:** ✅ Complete | **Tests:** All Passing

## Features

- ✅ Full CRUD operations with UUID-based identifiers
- ✅ Multi-format import (CSV, JSON, XML) with partial failure handling
- ✅ Auto-classification with keyword analysis and confidence scoring
- ✅ Advanced filtering by category, priority, and status
- ✅ Statistics dashboard and OpenAPI documentation at `/docs`
- ✅ Comprehensive testing (smoke + integration + performance)

## Quick Start

```bash
# Install dependencies
pip install -r requirements.txt

# Run the server
uvicorn src.main:app --reload

# Access API docs at http://localhost:8000/docs
```

## Installation

```bash
# Navigate to project
cd homework-2

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Run application
demo\run.bat              # Windows
./demo/run.sh             # Linux/Mac
```

For detailed setup instructions, see [HOWTORUN.md](HOWTORUN.md).

## API Overview

**Ticket Operations:**
- `POST /tickets` - Create ticket
- `GET /tickets` - List tickets (with filters)
- `GET /tickets/{id}` - Get ticket by ID
- `PATCH /tickets/{id}` - Update ticket
- `DELETE /tickets/{id}` - Delete ticket
- `GET /tickets/stats` - Get statistics

**Import Operations:**
- `POST /import/csv` - Import from CSV
- `POST /import/json` - Import from JSON
- `POST /import/xml` - Import from XML

**Classification:**
- `POST /tickets/{id}/classify` - Classify single ticket
- `POST /tickets/classify-all` - Classify all tickets

See [API_REFERENCE.md](docs/API_REFERENCE.md) for complete documentation with examples.

## Usage Examples

**Create a ticket:**
```bash
curl -X POST http://localhost:8000/tickets \
  -H "Content-Type: application/json" \
  -d '{"customer_id":"CUST-001","customer_email":"user@example.com","customer_name":"John Doe","subject":"Login issue","description":"Cannot access my account"}'
```

**Import tickets:**
```bash
curl -X POST http://localhost:8000/import/csv -F "file=@fixtures/sample_tickets.csv"
```

**Filter tickets:**
```bash
curl "http://localhost:8000/tickets?priority=urgent&status=new"
```

## Data Models

**Categories:** `account_access` | `technical_issue` | `billing_question` | `feature_request` | `bug_report` | `other`

**Priorities:** `urgent` | `high` | `medium` | `low`

**Statuses:** `new` | `in_progress` | `waiting_customer` | `resolved` | `closed`

## Project Structure

```
homework-2/
├── src/                    # Application source code
│   ├── main.py            # FastAPI application entry
│   ├── models/            # Pydantic models and enums
│   ├── routes/            # API endpoints
│   ├── services/          # Business logic and storage
│   └── validators/        # Custom validation
├── tests/                 # Test suites
├── fixtures/              # Sample data files
├── docs/                  # Detailed documentation
└── demo/                  # Run scripts and examples
```

## Testing

```bash
# Run all tests with coverage
pytest tests/ -v --cov=src

# Run specific test suites
pytest tests/test_smoke.py -v
pytest tests/test_integration.py -v
pytest tests/test_performance.py -v
```

**Test Coverage:** 14 smoke tests + 15+ integration tests + 15+ performance tests = 85%+ coverage

See [TESTING_GUIDE.md](docs/TESTING_GUIDE.md) for details.

## Documentation

- [API Reference](docs/API_REFERENCE.md) - Complete endpoint documentation
- [Testing Guide](docs/TESTING_GUIDE.md) - Test pyramid and benchmarks
- [Architecture](docs/ARCHITECTURE.md) - System design and decisions
- [How to Run](HOWTORUN.md) - Detailed setup instructions

## Key Technologies

**FastAPI** • **Pydantic** • **UUID** • **Pytest** • **Uvicorn**

## License

MIT License - AI Coding Partner Homework
