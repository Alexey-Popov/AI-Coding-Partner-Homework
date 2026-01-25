# How to Run the Application

This project includes two implementations. Choose one to run:

---

## Node.js Implementation (Express.js)

### Prerequisites

- **Node.js** (version 14 or higher) - [Download](https://nodejs.org/)
- **npm** (comes with Node.js)

Verify installation:
```bash
node --version
npm --version
```

### Quick Start

```bash
# Navigate to Node.js directory
cd nodejs

# Install dependencies
npm install

# Start the server
npm start
```

Or use the demo script:
```bash
./nodejs/demo/run.sh
```

### Expected Output

```
Banking Transactions API running on http://localhost:3000
```

---

## Python Implementation (FastAPI)

### Prerequisites

- **Python 3.8+** - [Download](https://www.python.org/)
- **pip** (comes with Python)

Verify installation:
```bash
python3 --version
pip3 --version
```

### Quick Start

```bash
# Navigate to Python directory
cd python

# Create virtual environment
python3 -m venv venv

# Activate virtual environment
source venv/bin/activate  # macOS/Linux
# venv\Scripts\activate   # Windows

# Install dependencies
pip install -r requirements.txt

# Start the server
uvicorn src.main:app --host 0.0.0.0 --port 3000 --reload
```

Or use the demo script:
```bash
./python/demo/run.sh
```

### Expected Output

```
INFO:     Uvicorn running on http://0.0.0.0:3000 (Press CTRL+C to quit)
INFO:     Started reloader process
```

### API Documentation (Python Only)

FastAPI provides automatic interactive documentation:
- **Swagger UI**: http://localhost:3000/docs
- **ReDoc**: http://localhost:3000/redoc

---

## Testing the API

Both implementations run on port 3000 and provide identical endpoints.

### Using cURL

Test health endpoint:
```bash
curl http://localhost:3000/health
```

Create a deposit:
```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": 1000.00,
    "currency": "USD",
    "type": "deposit"
  }'
```

Create a transfer:
```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 150.50,
    "currency": "USD",
    "type": "transfer"
  }'
```

Get all transactions:
```bash
curl http://localhost:3000/transactions
```

Get transactions filtered by account:
```bash
curl "http://localhost:3000/transactions?accountId=ACC-12345"
```

Get account balance:
```bash
curl http://localhost:3000/accounts/ACC-12345/balance
```

Get account summary:
```bash
curl http://localhost:3000/accounts/ACC-12345/summary
```

Calculate interest:
```bash
curl "http://localhost:3000/accounts/ACC-12345/interest?rate=0.05&days=30"
```

Export as CSV:
```bash
curl "http://localhost:3000/transactions/export?format=csv"
```

### Using VS Code REST Client

Open the appropriate file and click "Send Request":
- Node.js: `nodejs/demo/sample-requests.http`
- Python: `python/demo/sample-requests.http`

### Run Sample Scripts

Node.js:
```bash
./nodejs/demo/sample-requests.sh
```

Python:
```bash
./python/demo/sample-requests.sh
```

---

## Validation Examples

### Invalid Account Format

```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "INVALID",
    "amount": 100,
    "currency": "USD",
    "type": "deposit"
  }'
```

Response (400 Bad Request):
```json
{
  "error": "Validation failed",
  "details": [
    {
      "field": "toAccount",
      "message": "Account must follow format ACC-XXXXX (5 alphanumeric characters)"
    }
  ]
}
```

### Negative Amount

```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": -100,
    "currency": "USD",
    "type": "deposit"
  }'
```

### Invalid Currency

```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": 100,
    "currency": "INVALID",
    "type": "deposit"
  }'
```

---

## Stopping the Application

Press `Ctrl + C` in your terminal.

---

## Troubleshooting

### Port Already in Use

Node.js:
```bash
PORT=3001 npm start
```

Python:
```bash
uvicorn src.main:app --port 3001
```

### Permission Denied on Scripts

```bash
chmod +x nodejs/demo/run.sh nodejs/demo/sample-requests.sh
chmod +x python/demo/run.sh python/demo/sample-requests.sh
```

### Python Virtual Environment Issues

```bash
# Remove and recreate
rm -rf venv
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

### Node Modules Not Found

```bash
cd nodejs
rm -rf node_modules
npm install
```
