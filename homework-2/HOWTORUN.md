# How to Run - Customer Support Ticket System

## Prerequisites

- **Python**: 3.8 or higher
- **pip**: Python package manager

## Installation Steps

### 1. Navigate to project directory

```bash
cd homework-2
```

### 2. (Optional) Create virtual environment

**Windows:**
```bash
python -m venv venv
venv\Scripts\activate
```

**Linux/Mac:**
```bash
python3 -m venv venv
source venv/bin/activate
```

### 3. Install dependencies

```bash
pip install -r requirements.txt
```

## Running the Server

### Option A: Using demo scripts

**Windows:**
```bash
demo\run.bat
```

**Linux/Mac:**
```bash
chmod +x demo/run.sh
./demo/run.sh
```

### Option B: Direct command

```bash
uvicorn src.main:app --reload --host 0.0.0.0 --port 8000
```

## Verify Installation

1. Open browser to http://localhost:8000/docs
2. You should see the Swagger UI documentation
3. Try the health check endpoint: `GET /health`

## Running Tests

### All tests
```bash
pytest
```

### With coverage report
```bash
pytest --cov=src --cov-report=html
```

### Specific test file
```bash
pytest tests/test_ticket_api.py -v
```

### Run with verbose output
```bash
pytest -v
```

## Sample API Requests

Use the included HTTP file with VS Code REST Client:
- Open `demo/sample-requests.http`
- Click "Send Request" on any request

Or use curl:

```bash
# Health check
curl http://localhost:8000/health

# Create ticket
curl -X POST http://localhost:8000/tickets \
  -H "Content-Type: application/json" \
  -d '{"title": "Test ticket title here", "description": "This is a test ticket description that meets the minimum length requirement.", "customer_email": "test@example.com", "category": "technical_issue", "priority": "medium"}'

# List tickets
curl http://localhost:8000/tickets
```

## Troubleshooting

### Port already in use
```bash
# Use a different port
uvicorn src.main:app --reload --port 8001
```

### Module not found
```bash
# Make sure you're in the homework-2 directory
# Make sure virtual environment is activated
# Reinstall dependencies
pip install -r requirements.txt
```

### Permission denied (Linux/Mac)
```bash
chmod +x demo/run.sh
```
