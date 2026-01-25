# Banking Transactions API

> **AI Tools Used**: Claude Code (Claude Opus 4.5)

---

## Project Overview

A minimal REST API for banking transactions built with Node.js and Express.js. This project demonstrates AI-assisted development practices, showcasing how AI coding tools can help create clean, well-structured code with proper validation and error handling.

### Features Implemented

- **Core API (Task 1)**: Full CRUD operations for transactions
- **Validation (Task 2)**: Comprehensive input validation with meaningful error messages
- **Filtering (Task 3)**: Transaction filtering by account, type, and date range
- **CSV Export (Task 4 - Option C)**: Export transactions to CSV format

---

## Architecture

```
src/
├── index.js                    # Express app entry point
├── routes/
│   ├── transactions.js         # Transaction endpoints
│   └── accounts.js             # Account endpoints
├── models/
│   └── transaction.js          # Transaction model & in-memory storage
├── validators/
│   └── transactionValidator.js # Input validation logic
└── utils/
    └── helpers.js              # Utility functions (CSV, formatting)
```

### Design Decisions

1. **In-Memory Storage**: Uses a simple array for storing transactions, as specified in requirements. Easy to replace with a database later.

2. **Separation of Concerns**:
   - Routes handle HTTP request/response
   - Models manage data and business logic
   - Validators handle input validation
   - Utils provide reusable helper functions

3. **Validation Strategy**: Validates all inputs before processing, returning detailed error messages for each invalid field.

4. **Balance Calculation**: Dynamically calculates balance from completed transactions rather than storing a separate balance value.

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | API information and available endpoints |
| `GET` | `/health` | Health check |
| `POST` | `/transactions` | Create a new transaction |
| `GET` | `/transactions` | List all transactions (with filtering) |
| `GET` | `/transactions/:id` | Get a specific transaction |
| `GET` | `/transactions/export?format=csv` | Export transactions as CSV |
| `GET` | `/accounts/:accountId/balance` | Get account balance |

### Query Parameters for Filtering

- `accountId` - Filter by account (matches from or to)
- `type` - Filter by transaction type (deposit, withdrawal, transfer)
- `from` - Filter by start date (ISO 8601)
- `to` - Filter by end date (ISO 8601)

---

## Transaction Model

```json
{
  "id": "uuid",
  "fromAccount": "ACC-XXXXX",
  "toAccount": "ACC-XXXXX",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer",
  "timestamp": "2024-01-15T10:30:00.000Z",
  "status": "completed"
}
```

### Validation Rules

- **Amount**: Positive number, maximum 2 decimal places
- **Account**: Format `ACC-XXXXX` (5 alphanumeric characters)
- **Currency**: Valid ISO 4217 codes (USD, EUR, GBP, JPY, CHF, CAD, AUD, CNY, INR, PLN)
- **Type**: `deposit`, `withdrawal`, or `transfer`

---

## Dependencies

| Package | Version | Purpose |
|---------|---------|---------|
| express | ^4.18.2 | Web framework |
| uuid | ^9.0.0 | Generate unique transaction IDs |

---

## Quick Start

```bash
# Install dependencies
npm install

# Start the server
npm start

# Or use the demo script
./demo/run.sh
```

The API will be available at `http://localhost:3000`

---

## Example Requests

```bash
# Create a deposit
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{"toAccount": "ACC-12345", "amount": 1000, "currency": "USD", "type": "deposit"}'

# Create a transfer
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{"fromAccount": "ACC-12345", "toAccount": "ACC-67890", "amount": 100.50, "currency": "USD", "type": "transfer"}'

# Get all transactions
curl http://localhost:3000/transactions

# Get account balance
curl http://localhost:3000/accounts/ACC-12345/balance

# Export as CSV
curl http://localhost:3000/transactions/export?format=csv
```

---

## AI Contribution

This project was developed with assistance from Claude Code (Claude Opus 4.5). See [AI-CONTRIBUTIONS.md](./AI-CONTRIBUTIONS.md) for detailed documentation of how AI tools contributed to the development process.

---

*This project was completed as part of the AI-Assisted Development course.*
