# Banking Transactions API

> **Student Name**: Yaroslav Brahinets
> **Date Submitted**: 2026-01-25
> **AI Tools Used**: Claude Code, GitHub Copilot, Claude Web

---

## Project Overview

A REST API for managing banking transactions built with Node.js, Express, and TypeScript. The API supports creating transactions, retrieving transaction history, and checking account balances with built-in validation and rate limiting.

---

## Technology Stack

- **Runtime**: Node.js
- **Framework**: Express.js
- **Language**: TypeScript
- **Testing**: Jest + Supertest
- **Storage**: In-memory (Map/Array)

---

## Project Structure

```
homework-1/
├── src/
│   ├── index.ts              # Entry point
│   ├── app.ts                # Express app configuration
│   ├── types/
│   │   └── index.ts          # TypeScript interfaces
│   ├── routes/
│   │   ├── transactions.ts   # Transaction endpoints
│   │   └── accounts.ts       # Account endpoints
│   ├── services/
│   │   ├── transaction.service.ts
│   │   └── account.service.ts
│   ├── validators/
│   │   └── transaction.validator.ts
│   ├── middleware/
│   │   └── rateLimiter.ts    # Rate limiting middleware
│   ├── errors/
│   │   └── index.ts          # Custom error classes
│   └── utils/
│       └── storage.ts        # In-memory data storage
├── demo/
│   ├── run.sh                # Start script (Linux/Mac)
│   ├── run.bat               # Start script (Windows)
│   └── sample-data.json      # Sample transaction data
├── package.json
├── tsconfig.json
├── HOWTORUN.md               # Detailed run instructions
└── TASKS.md                  # Assignment requirements
```

---

## API Usage Examples

### Create a Transaction

```bash
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "transfer"
  }'
```

### Get All Transactions

```bash
curl http://localhost:3000/transactions
```

### Get Account Balance

```bash
curl http://localhost:3000/accounts/ACC-12345/balance
```

---

## Error Handling

The API returns structured error responses:

```json
{
  "error": "Validation failed",
  "details": [
    { "field": "amount", "message": "Amount must be a positive number" }
  ]
}
```

HTTP status codes:
- `200` - Success
- `201` - Created
- `400` - Validation error
- `404` - Not found
- `429` - Rate limit exceeded

---

## Running Tests

```bash
npm test              # Run all tests
npm run test:coverage # Run tests with coverage report
```

---

<div align="center">

*This project was completed as part of the AI-Assisted Development course.*

</div>
