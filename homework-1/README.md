# 🏦 Banking Transactions API

> **Student Name**: [Your Name]
> **Date Submitted**: January 25, 2026
> **AI Tools Used**: GitHub Copilot, Claude

---

## 📋 Project Overview

A REST API for managing banking transactions built with **FastAPI** and **Python**. This implementation provides endpoints for creating transactions, retrieving transaction history, calculating account balances, and filtering transactions. The API uses in-memory storage for simplicity and includes validation, error handling, and proper HTTP status codes.

### 🎯 Implemented Features

#### ✅ **Task 1: Core API Implementation** (COMPLETE)
- [x] POST `/transactions` - Create new transaction (HTTP 201)
- [x] GET `/transactions` - List all transactions (HTTP 200)
- [x] GET `/transactions/{id}` - Get specific transaction (HTTP 200/404)
- [x] GET `/accounts/{accountId}/balance` - Calculate account balance (HTTP 200/404)
- [x] In-memory transaction storage using list-based data structure
- [x] Proper HTTP status codes (201, 200, 404, 400)
- [x] Basic error handling with meaningful error messages

#### ✅ **Task 2: Transaction Validation** (COMPLETE)
- [x] Amount validation (positive, max 2 decimal places)
- [x] Account format validation (`ACC-XXXXX`)
- [x] Currency validation (ISO 4217 codes)
- [x] Detailed error response with field-level messages

#### ✅ **Task 3: Transaction Filtering** (COMPLETE)
- [x] Filter by account ID
- [x] Filter by transaction type
- [x] Filter by date range
- [x] Combine multiple filters

#### ⏳ **Task 4: Rate Limiting** (PLANNED)
- [ ] Rate limit: 100 requests per minute per IP
- [ ] Return 429 status when exceeded

---

## 🏗️ Architecture

### Project Structure
```
homework-1/
├── src/
│   ├── main.py                 # FastAPI app initialization
│   ├── models/
│   │   └── transaction.py      # Transaction model
│   ├── schemas/
│   │   └── transaction.py      # Pydantic request/response schemas
│   ├── routes/
│   │   └── transactions.py     # API endpoints
│   ├── storage/
│   │   └── store.py            # In-memory transaction store
│   ├── validators/
│   │   └── transaction_validator.py  # Transaction validation logic
│   └── utils/
│       ├── exceptions.py       # Custom exceptions & error handlers
│       └── date_utils.py       # Date parsing utilities
├── demo/
│   ├── run.sh                  # Script to start API
│   ├── sample-requests.http    # Sample API calls
│   └── sample-data.json        # Sample transaction data
├── requirements.txt            # Python dependencies
├── .gitignore                  # Git ignore file
├── README.md                   # This file
└── HOWTORUN.md                # Setup instructions
```

### Design Patterns

1. **Singleton Pattern** - TransactionStore maintains single instance
2. **Request/Response Validation** - Pydantic schemas enforce data integrity
3. **RESTful API Design** - Proper HTTP methods and status codes
4. **Separation of Concerns** - Models, routes, storage, validation separated

### Key Components

#### Transaction Model (`src/models/transaction.py`)
- Represents a banking transaction
- Fields: id, fromAccount, toAccount, amount, currency, type, timestamp, status
- Methods: `to_dict()`, `from_dict()`

#### Pydantic Schemas (`src/schemas/transaction.py`)
- `TransactionCreate` - Validates incoming POST requests
- `TransactionResponse` - Validates outgoing responses
- `BalanceResponse` - Account balance response format

#### TransactionStore (`src/storage/store.py`)
- In-memory list-based storage
- Methods: `add()`, `get_by_id()`, `get_all()`, `find_by_account()`, `calculate_balance()`
- Balance calculation logic:
  - Only counts "completed" transactions
  - Deposits add to balance
  - Withdrawals subtract from balance
  - Transfers affect both source and destination accounts

#### Routes (`src/routes/transactions.py`)
- `POST /transactions` - Create transaction (201)
- `GET /transactions` - List all (200)
- `GET /transactions/{id}` - Get one (200/404)
- `GET /accounts/{accountId}/balance` - Get balance (200/404)

#### Validators (`src/validators/transaction_validator.py`)
- `validate_amount()` - Ensures positive amount with max 2 decimal places
- `validate_account_format()` - Validates ACC-XXXXX format
- `validate_currency_code()` - Validates against ISO 4217 currency codes

---

## ✅ Validation Rules (Task 2)

| Field | Rule | Example Valid | Example Invalid |
|-------|------|---------------|-----------------|
| `amount` | Positive, max 2 decimal places | `100.50`, `25` | `-10`, `100.555` |
| `fromAccount` | Format: `ACC-XXXXX` | `ACC-12345`, `ACC-AB1c2` | `12345`, `ACC-1234` |
| `toAccount` | Format: `ACC-XXXXX` | `ACC-67890`, `ACC-XyZ99` | `ACC12345`, `ACC-` |
| `currency` | Valid ISO 4217 code | `USD`, `EUR`, `GBP` | `ABC`, `DOLLAR` |

### Validation Error Response Format
```json
{
  "error": "Validation failed",
  "details": [
    {"field": "amount", "message": "Amount must have maximum 2 decimal places"},
    {"field": "fromAccount", "message": "Account must follow format ACC-XXXXX (where X is alphanumeric)"},
    {"field": "currency", "message": "Invalid currency code. Must be a valid ISO 4217 code (e.g., USD, EUR, GBP)"}
  ]
}
```

---

## 🔍 Transaction Filtering (Task 3)

The `GET /transactions` endpoint supports optional query parameters for filtering:

| Parameter | Description | Example |
|-----------|-------------|---------|
| `accountId` | Filter by account (matches fromAccount OR toAccount) | `?accountId=ACC-12345` |
| `type` | Filter by transaction type | `?type=transfer` |
| `from` | Filter transactions on or after date (YYYY-MM-DD) | `?from=2024-01-01` |
| `to` | Filter transactions on or before date (YYYY-MM-DD) | `?to=2024-01-31` |

### Filter Examples

```bash
# Get all transactions for an account
GET /transactions?accountId=ACC-12345

# Get only transfer transactions
GET /transactions?type=transfer

# Get transactions in January 2024
GET /transactions?from=2024-01-01&to=2024-01-31

# Combined: transfers for ACC-12345 in January
GET /transactions?accountId=ACC-12345&type=transfer&from=2024-01-01&to=2024-01-31
```

### Filter Behavior
- All filters are optional and can be combined
- `accountId` matches both `fromAccount` and `toAccount`
- Date range is inclusive (includes both from and to dates)
- Returns empty array `[]` if no transactions match
- Returns 400 Bad Request for invalid date formats or if `from` > `to`

---

## 🔄 API Endpoints

### Create Transaction
```
POST /transactions
Content-Type: application/json

{
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer"
}

Response: 201 Created
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer",
  "timestamp": "2026-01-25T10:30:45.123456",
  "status": "completed"
}
```

### List All Transactions
```
GET /transactions

Response: 200 OK
[
  { transaction object 1 },
  { transaction object 2 },
  ...
]
```

### Get Specific Transaction
```
GET /transactions/550e8400-e29b-41d4-a716-446655440000

Response: 200 OK
{ transaction object }

Or 404 Not Found if transaction doesn't exist
```

### Get Account Balance
```
GET /accounts/ACC-12345/balance

Response: 200 OK
{
  "accountId": "ACC-12345",
  "balance": 450.75
}

Or 404 Not Found if account has no transactions
```

---

## 🛠️ Technology Stack

| Component | Technology |
|-----------|-----------|
| Framework | FastAPI 0.128+ |
| Server | Uvicorn |
| Validation | Pydantic v2 |
| Language | Python 3.8+ |
| Rate Limiting | slowapi |

---

## 💻 Installation & Running

See [HOWTORUN.md](HOWTORUN.md) for detailed setup instructions.

Quick start:
```bash
# Install dependencies
pip install -r requirements.txt

# Start the server
python3 -m uvicorn src.main:app --host 0.0.0.0 --port 8000 --reload

# Or use the demo script
./demo/run.sh
```

The API will be available at `http://localhost:8000`

---

## 📚 API Documentation

When the server is running, interactive API documentation is available at:
- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

---

## 🧪 Testing

Sample requests are provided in `demo/sample-requests.http`. Test using:
- **VS Code REST Client**: Install extension and click "Send Request"
- **curl**: `curl http://localhost:8000/transactions`
- **Postman**: Import the HTTP file
- **Python requests**: See examples below

```python
import requests

# Create transaction
response = requests.post(
    "http://localhost:8000/transactions",
    json={
        "fromAccount": "ACC-12345",
        "toAccount": "ACC-67890",
        "amount": 100.50,
        "currency": "USD",
        "type": "transfer"
    }
)
print(response.json())

# Get balance
response = requests.get("http://localhost:8000/accounts/ACC-12345/balance")
print(response.json())
```

---

## 📝 Response Codes

| Status | Meaning | When |
|--------|---------|------|
| 200 | OK | Successful GET/retrieve operations |
| 201 | Created | Successfully created transaction |
| 400 | Bad Request | Invalid input data |
| 404 | Not Found | Transaction/account doesn't exist |
| 500 | Server Error | Unexpected server error |

---

## 🚀 Future Enhancements

- [x] **Task 2**: Enhanced validation with detailed error messages
- [x] **Task 3**: Transaction filtering and search
- [ ] **Task 4**: Rate limiting (100 req/min per IP)
- [ ] Database persistence (PostgreSQL/SQLite)
- [ ] User authentication
- [ ] Transaction status updates
- [ ] Unit tests with pytest

<div align="center">

*This project was completed as part of the AI-Assisted Development course.*

</div>
