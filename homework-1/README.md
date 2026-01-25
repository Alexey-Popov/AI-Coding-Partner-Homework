# Banking Transactions API

> **Student Name**: Sviatoslav Glushchenko
> **Date Submitted**: 25.01.2026
> **AI Tools Used**: GitHub Copilot, Claude Code

---

## Project Overview

This project implements a comprehensive REST API for banking transactions in **two technology stacks**:

1. **Node.js** with Express.js
2. **Python** with FastAPI

Both implementations provide identical functionality and API endpoints, allowing comparison of AI-assisted development across different technology stacks.

### Key Features Implemented

#### Core Features (Task 1-3)
- **Transaction Management**: Create, retrieve, and list transactions
- **Advanced Filtering**: Filter by account, type, and date range
- **Robust Validation**: Comprehensive validation for amounts, accounts, currencies, and transaction types
- **Account Management**: Track balances and transaction history per account

#### Additional Features (Task 4)

1. **Transaction Summary Endpoint** (Option A)
   - `GET /accounts/:accountId/summary`
   - Returns total deposits, withdrawals, transaction count, and recent activity

2. **Simple Interest Calculation** (Option B)
   - `GET /accounts/:accountId/interest?rate=0.05&days=30`
   - Calculate simple interest on current balance

3. **Transaction Export** (Option C)
   - `GET /transactions/export?format=csv`
   - Export all transactions in CSV format for reporting

---

## Project Structure

```
homework-1/
├── README.md                    # This file
├── HOWTORUN.md                 # Detailed run instructions
├── TASKS.md                    # Assignment requirements
├── .gitignore                  # Git ignore rules
├── docs/
│   └── screenshots/            # Screenshots of API and AI tools
│
├── nodejs/                     # Node.js Implementation
│   ├── package.json           # Dependencies
│   ├── src/
│   │   ├── index.js          # Main entry point
│   │   ├── routes.js         # Transaction routes
│   │   ├── accountRoutes.js  # Account routes
│   │   ├── transaction.js    # Business logic & storage
│   │   └── validators.js     # Validation utilities
│   └── demo/
│       ├── run.sh            # Startup script
│       ├── sample-requests.sh
│       ├── sample-requests.http
│       └── sample-data.json
│
└── python/                     # Python Implementation
    ├── requirements.txt       # Dependencies
    ├── src/
    │   ├── main.py           # FastAPI application
    │   ├── models.py         # Pydantic models
    │   ├── validators.py     # Validation utilities
    │   ├── routes/
    │   │   ├── transactions.py
    │   │   └── accounts.py
    │   └── services/
    │       └── transaction_service.py
    └── demo/
        ├── run.sh            # Startup script
        ├── sample-requests.sh
        ├── sample-requests.http
        └── sample-data.json
```

---

## Technology Stacks

### Node.js Implementation
- **Runtime**: Node.js
- **Framework**: Express.js
- **Storage**: In-memory (JavaScript arrays/objects)
- **UUID**: uuid package for transaction IDs

### Python Implementation
- **Runtime**: Python 3.8+
- **Framework**: FastAPI
- **Models**: Pydantic for validation
- **Server**: Uvicorn ASGI server
- **Storage**: In-memory (Python lists/dicts)

---

## API Endpoints

### Transaction Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/transactions` | Create a new transaction |
| `GET` | `/transactions` | List all transactions (with filtering) |
| `GET` | `/transactions/:id` | Get a specific transaction by ID |
| `GET` | `/transactions/export?format=csv` | Export transactions as CSV |

### Account Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/accounts/:accountId/balance` | Get account balance |
| `GET` | `/accounts/:accountId/summary` | Get account summary with statistics |
| `GET` | `/accounts/:accountId/interest` | Calculate simple interest |

### System Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/health` | API health check |
| `GET` | `/docs` | API documentation (Python only) |

---

## Validation Rules

### Transaction Validation
- Amount must be positive
- Amount must have maximum 2 decimal places
- Account format: `ACC-XXXXX` (where X is alphanumeric uppercase)
- Currency must be valid ISO 4217 code (USD, EUR, GBP, JPY, etc.)
- Transaction type must be: `deposit`, `withdrawal`, or `transfer`
- From and To accounts must differ for transfers

### Error Response Format
```json
{
  "error": "Validation failed",
  "details": [
    {"field": "amount", "message": "Amount must be a positive number"},
    {"field": "currency", "message": "Invalid currency code"}
  ]
}
```

---

## Query Filters

The `GET /transactions` endpoint supports multiple filters:

- `?accountId=ACC-12345` - Filter by account
- `?type=transfer` - Filter by transaction type
- `?from=2024-01-01` - Start date (ISO 8601)
- `?to=2024-12-31` - End date (ISO 8601)

Example: `GET /transactions?accountId=ACC-12345&type=transfer`

---

## Data Models

### Transaction Object
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 150.50,
  "currency": "USD",
  "type": "transfer",
  "timestamp": "2026-01-25T10:30:00.000Z",
  "status": "completed"
}
```

### Account Summary Object
```json
{
  "accountId": "ACC-12345",
  "totalDeposits": 5000.00,
  "totalWithdrawals": 1500.00,
  "numberOfTransactions": 8,
  "mostRecentTransactionDate": "2026-01-25T10:30:00.000Z",
  "currentBalance": 3500.00
}
```

---

## AI-Assisted Development

This project was developed using two AI coding assistants:

### GitHub Copilot (Node.js Implementation)
- Code structure and module organization
- Validation logic and error handling
- RESTful endpoint implementation
- Documentation and inline comments

### Claude Code (Python Implementation)
- FastAPI application architecture
- Pydantic model definitions
- Service layer implementation
- Comprehensive validation

---

*This project was completed as part of the AI-Assisted Development course.*
