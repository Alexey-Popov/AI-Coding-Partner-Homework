# Banking Transactions API

A REST API for banking transactions built with Node.js and Express.js.

## Features

- Create, list, and retrieve transactions
- Filter transactions by account, type, and date range
- Get account balances
- Calculate simple interest on account balances
- Input validation with detailed error messages
- In-memory data storage

## Architecture

```
homework-1/
├── src/
│   ├── index.js              # Express app entry point
│   ├── routes/
│   │   ├── transactions.js   # Transaction endpoints
│   │   └── accounts.js       # Account endpoints
│   ├── models/
│   │   └── transaction.js    # Data model & storage
│   ├── validators/
│   │   └── transactionValidator.js
│   └── utils/
│       └── helpers.js        # Utility functions
├── demo/
│   ├── run.sh               # Startup script
│   ├── sample-requests.http # API test requests
│   └── sample-data.json     # Example data
└── docs/
    └── screenshots/         # AI interaction screenshots
```

## API Endpoints

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/transactions` | Create a new transaction |
| GET | `/transactions` | List all transactions |
| GET | `/transactions/:id` | Get transaction by ID |

### Accounts

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/accounts/:accountId/balance` | Get account balance |
| GET | `/accounts/:accountId/interest` | Calculate simple interest |

## Transaction Types

- **deposit**: Add funds to an account (requires `toAccount`)
- **withdrawal**: Remove funds from an account (requires `fromAccount`)
- **transfer**: Move funds between accounts (requires both `fromAccount` and `toAccount`)

## Supported Currencies

USD, EUR, GBP, JPY, CHF, CAD, AUD, CNY

## Account ID Format

`ACC-XXXXX` where X is an uppercase alphanumeric character (e.g., `ACC-12345`, `ACC-ABCDE`)

## Quick Start

See [HOWTORUN.md](./HOWTORUN.md) for detailed instructions.

```bash
npm install
npm start
```

The API will be available at `http://localhost:3000`
