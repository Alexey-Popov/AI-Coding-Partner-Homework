# Banking Transactions API

> **Student Name**: Denys Mokhrin
> **Date Submitted**: 26.01.2026
> **AI Tools Used**: Claude Code

---

## Project Overview

A REST API for managing banking transactions built with Node.js and Express.js. This API supports creating transactions, querying transaction history with filters, checking account balances, and exporting transactions to CSV format.

### Features Implemented

- **Task 1: Core API** - All CRUD endpoints for transactions
- **Task 2: Validation** - Amount, account format, and currency validation
- **Task 3: Filtering** - Filter by account, type, and date range
- **Task 4: CSV Export** - Export transactions as CSV file
- **Web Frontend** - Single-page HTML interface for testing all API operations

---

## API Endpoints

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/transactions` | Create a new transaction |
| `GET` | `/transactions` | List all transactions (with optional filters) |
| `GET` | `/transactions/:id` | Get a specific transaction by ID |
| `GET` | `/transactions/export?format=csv` | Export transactions as CSV |

### Accounts

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/accounts/:accountId/balance` | Get account balance |

---

## Transaction Model

```json
{
  "id": "TXN-000001",
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer",
  "timestamp": "2024-01-15T10:30:00.000Z",
  "status": "completed"
}
```

---

## Validation Rules

| Field | Rule |
|-------|------|
| `amount` | Must be positive, max 2 decimal places |
| `fromAccount` | Format: `ACC-XXXXX` (X is alphanumeric) |
| `toAccount` | Format: `ACC-XXXXX` (X is alphanumeric) |
| `currency` | Valid ISO 4217 code (USD, EUR, GBP, etc.) |
| `type` | One of: `deposit`, `withdrawal`, `transfer` |

### Validation Error Response

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

## Query Parameters for Filtering

| Parameter | Example | Description |
|-----------|---------|-------------|
| `accountId` | `?accountId=ACC-12345` | Filter by account (from or to) |
| `type` | `?type=transfer` | Filter by transaction type |
| `from` | `?from=2024-01-01` | Filter from date (inclusive) |
| `to` | `?to=2024-01-31` | Filter to date (inclusive) |

Filters can be combined: `?accountId=ACC-12345&type=transfer&from=2024-01-01`

---

## Web Frontend

The application includes a built-in web interface for testing all API operations. After starting the server, open `http://localhost:3000` in your browser.

### Features

| Section | Description |
|---------|-------------|
| **Create Transaction** | Form to create new transactions with validation |
| **Get Transaction by ID** | Look up a specific transaction |
| **Get Account Balance** | Check balance for any account |
| **All Transactions** | View, filter, and export transactions |

### How to Use

1. **Start the server**: `npm start`
2. **Open browser**: Navigate to `http://localhost:3000`
3. **Create transactions**: Fill the form and click "Create Transaction"
4. **View transactions**: Click "Load Transactions" to see the list
5. **Apply filters**: Use the filter fields and click "Load Transactions"
6. **Export data**: Click "Export CSV" to download transactions

---

## Project Structure

```
homework-1/
├── package.json
├── README.md
├── HOWTORUN.md
├── src/
│   ├── index.js                # Express app entry point
│   ├── backend/
│   │   ├── routes/
│   │   │   ├── transactions.js # Transaction endpoints
│   │   │   └── accounts.js     # Account endpoints
│   │   ├── validators/
│   │   │   └── transactionValidator.js
│   │   └── data/
│   │       └── store.js        # In-memory data storage
│   └── frontend/
│       └── index.html          # Web UI for testing API
├── demo/
│   ├── run.bat                 # Windows start script
│   ├── run.sh                  # Linux/Mac start script
│   ├── sample-requests.http    # Sample API requests
│   └── sample-data.json        # Sample transaction data
└── docs/
    └── screenshots/            # AI interaction screenshots
```

---

## Technology Stack

- **Runtime**: Node.js
- **Framework**: Express.js
- **Storage**: In-memory (array)

<div align="center">

*This project was completed as part of the AI-Assisted Development course.*

</div>
