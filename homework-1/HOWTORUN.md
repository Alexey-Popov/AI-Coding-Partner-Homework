# ▶️ How to Run the Banking Transactions API

## Prerequisites

- Python 3.8 or higher
- pip (Python package manager)
- Terminal/Command line access

## Quick Start (Easiest Method)

### Option 1: Using the provided run script

```bash
cd homework-1
chmod +x demo/run.sh
./demo/run.sh
```

The script will automatically:
- Install dependencies
- Start the server on http://localhost:8000

### Option 2: Manual setup

```bash
# 1. Navigate to the project directory
cd homework-1

# 2. Install dependencies
pip install -r requirements.txt

# 3. Start the server
python3 -m uvicorn src.main:app --host 0.0.0.0 --port 8000 --reload

# 4. Open browser to:
# - API: http://localhost:8000
# - Docs: http://localhost:8000/docs
# - ReDoc: http://localhost:8000/redoc
```

## Detailed Setup Steps

### Step 1: Install Python Dependencies

```bash
cd homework-1
pip install -r requirements.txt
```

**Dependencies installed:**
- `fastapi` - Web framework
- `uvicorn` - ASGI server
- `pydantic` - Data validation
- `python-dateutil` - Date handling
- `slowapi` - Rate limiting

### Step 2: Start the API Server

**Method A: Using Uvicorn directly**
```bash
python3 -m uvicorn src.main:app --host 0.0.0.0 --port 8000 --reload
```

**Method B: Using Python module execution**
```bash
python3 src/main.py
```

**Method C: Using the demo script**
```bash
./demo/run.sh
```

### Step 3: Verify the API is Running

Open in browser or terminal:
```bash
curl http://localhost:8000/health
```

You should see:
```json
{
  "status": "healthy",
  "timestamp": "2026-01-25T10:30:45.123456"
}
```

## API Endpoints

Once running, access the API at:

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

1. Install "REST Client" extension in VS Code
2. Open `demo/sample-requests.http`
3. Click "Send Request" above each request

### Using Postman

1. Open Postman
2. Create new request
3. Choose method: POST
4. URL: `http://localhost:8000/transactions`
5. Headers: `Content-Type: application/json`
6. Body (raw):
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
import json

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

The API includes automatic interactive documentation:

- **Swagger UI** (Recommended): http://localhost:8000/docs
  - Try requests directly in the browser
  - See request/response schemas
  - Auto-generated from code

- **ReDoc**: http://localhost:8000/redoc
  - Alternative documentation viewer
  - Better for reading

## Stopping the Server

Press `Ctrl+C` in the terminal where the server is running.

## Troubleshooting

### "Port 8000 already in use"
```bash
# Use a different port
python3 -m uvicorn src.main:app --host 0.0.0.0 --port 8001
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

# If error, try:
python --version
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
│   │   └── transactions.py  # API endpoints
│   ├── storage/
│   │   └── store.py         # In-memory storage
│   └── utils/
│       └── exceptions.py    # Custom exceptions
├── demo/
│   ├── run.sh               # Start script
│   ├── sample-requests.http # Sample API calls
│   └── sample-data.json     # Sample data
├── requirements.txt         # Dependencies
├── README.md                # Project documentation
└── HOWTORUN.md             # This file
```

## Next Steps

1. ✅ **Task 1 Complete**: Core API implementation
2. 🔄 **Task 2**: Add transaction validation with detailed errors
3. 🔄 **Task 3**: Implement transaction filtering
4. 🔄 **Task 4**: Add rate limiting

---

For more details, see [README.md](README.md)
