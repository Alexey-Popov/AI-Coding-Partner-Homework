# ▶️ How to Run the Banking Transactions API

## Prerequisites

- Python 3.8 or higher
- pip (Python package manager)
- Terminal/Command line access (macOS zsh)

## Quick Start (Easiest Method)

### Option 1: Using the provided run script

```bash
cd homework-1
chmod +x demo/run.sh
./demo/run.sh
```

The script will:
- Install dependencies
- Start the server at http://localhost:8000
- Print links to Swagger UI and ReDoc

### Option 2: Manual setup

```bash
# 1. Navigate to the project directory
cd homework-1

# 2. (Recommended) Create and activate a virtual environment
python3 -m venv .venv
source .venv/bin/activate

# 3. Install dependencies
pip install -r requirements.txt

# 4. Start the server
python3 -m uvicorn src.main:app --host 0.0.0.0 --port 8000 --reload

# 5. Open browser to:
# - API: http://localhost:8000
# - Docs: http://localhost:8000/docs
# - ReDoc: http://localhost:8000/redoc
```

## Verify the API is Running

Open in browser or terminal:
```bash
curl http://localhost:8000/health
```

Expected response:
```json
{
  "status": "healthy",
  "timestamp": "<ISO timestamp>"
}
```

## API Endpoints

| Endpoint | Purpose |
|----------|---------|
| `GET http://localhost:8000/health` | Health check |
| `GET http://localhost:8000/` | API info |
| `POST http://localhost:8000/transactions` | Create transaction |
| `GET http://localhost:8000/transactions` | List transactions |
| `GET http://localhost:8000/transactions/{id}` | Get transaction |
| `GET http://localhost:8000/accounts/{accountId}/balance` | Get balance |

## Testing the API

### Using curl

```bash
# Create a transaction
curl -X POST http://localhost:8000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "transfer"
  }'

# List all transactions
curl http://localhost:8000/transactions

# Get account balance
curl http://localhost:8000/accounts/ACC-12345/balance
```

### Using VS Code REST Client

1. Install "REST Client" extension
2. Open `demo/sample-requests.http`
3. Click "Send Request" above each request

### Using Postman

1. Open Postman
2. Create new request
3. Method: POST
4. URL: `http://localhost:8000/transactions`
5. Headers: `Content-Type: application/json`
6. Body (raw JSON):
```json
{
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer"
}
```

### Using Python requests

```python
import requests

BASE_URL = "http://localhost:8000"

# Create transaction
response = requests.post(f"{BASE_URL}/transactions", json={
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "transfer"
})
print(response.status_code)
print(response.json())

# Get all transactions
response = requests.get(f"{BASE_URL}/transactions")
print(response.json())

# Get account balance
response = requests.get(f"{BASE_URL}/accounts/ACC-12345/balance")
print(response.json())
```

## Interactive API Documentation

- Swagger UI: http://localhost:8000/docs (try requests in browser)
- ReDoc: http://localhost:8000/redoc

## Stopping the Server

Press `Ctrl+C` in the terminal where the server is running.

## Troubleshooting

### "Port 8000 already in use"
```bash
# Use a different port
python3 -m uvicorn src.main:app --host 0.0.0.0 --port 8001 --reload
```

### "Module not found" errors
```bash
# Reinstall dependencies
pip install --force-reinstall -r requirements.txt

# Make sure you're in the correct directory
pwd  # Should end with /homework-1
```

### Python version issues
```bash
# Check Python version (should be 3.8+)
python3 --version
```

### Permission denied on run.sh
```bash
# Make the script executable
chmod +x demo/run.sh

# Then run it
./demo/run.sh
```

## Project Structure

```
homework-1/
├── src/
│   ├── main.py              # FastAPI application
│   ├── models/
│   │   └── transaction.py   # Transaction model
│   ├── schemas/
│   │   └── transaction.py   # Pydantic validators
│   ├── routes/
│   │   ├── transactions.py  # API endpoints
│   │   └── accounts.py      # Balance endpoint
│   ├── storage/
│   │   └── store.py         # In-memory storage
│   └── utils/
│       ├── exceptions.py    # Custom exceptions
│       └── rate_limiter.py  # Rate limiting
├── demo/
│   ├── run.sh               # Start script
│   ├── sample-requests.http # Sample API calls
│   └── sample-data.json     # Sample data
├── requirements.txt         # Dependencies
├── README.md                # Project documentation
└── HOWTORUN.md             # This file
```

## Status

- Task 1: Core API — Complete
- Task 2: Transaction Validation — Complete
- Task 3: Transaction Filtering — Complete
- Task 4: Rate Limiting — Complete
