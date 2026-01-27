# 🏦 Homework 1: Banking Transactions API

> **Project Name**: Banking Transactions API  
> **Date Completed**: January 27, 2026  
> **AI Tools Used**: GitHub Copilot (Claude Sonnet 4.5)  
> **Technology Stack**: Node.js + Express.js

---

## 📋 Project Overview

A fully functional REST API for managing banking transactions built with Node.js and Express.js. The API supports creating and retrieving transactions (deposits, withdrawals, transfers), checking account balances, and generating account summaries with comprehensive validation and filtering capabilities.

### Key Features

✨ **Transaction Management**
- Create deposits, withdrawals, and transfer transactions
- Auto-generated unique transaction IDs (UUID)
- ISO 8601 timestamps
- Transaction status tracking

✨ **Account Operations**
- Real-time balance calculation across all transaction types
- Transaction history by account
- Detailed account summaries with statistics

✨ **Data Validation**
- Strict input validation with field-specific error messages
- ISO 4217 currency code validation (USD, EUR, GBP, JPY, etc.)
- Account number format enforcement (ACC-XXXXX)
- Amount precision control (maximum 2 decimal places)

✨ **Query & Filtering**
- Filter transactions by account ID
- Filter by transaction type
- Filter by date range
- Combine multiple filters for complex queries

---

## 🏗️ Architecture

### Project Structure

```
homework-1/
├── src/
│   ├── index.js                    # Main application server
│   ├── models/
│   │   └── transaction.js          # Transaction data model
│   ├── routes/
│   │   ├── transactions.js         # Transaction endpoints
│   │   └── accounts.js             # Account endpoints
│   ├── validators/
│   │   └── transactionValidator.js # Input validation logic
│   └── utils/
│       └── helpers.js              # Utility functions
├── demo/
│   ├── run.sh                      # Quick start script
│   ├── sample-requests.http        # REST Client test cases
│   ├── sample-requests.sh          # Automated test script
│   ├── sample-data.json            # Sample transaction data
│   └── TEST_REPORT.md              # Test results documentation
└── docs/
    └── screenshots/                # Screenshots directory
```

### Architecture Decisions

**1. Modular Design**
- Separated concerns into routes, models, validators, and utilities
- Each module has a single responsibility
- Easy to test and maintain

**2. In-Memory Storage**
- Using JavaScript arrays for transaction storage
- No database required as per assignment specifications
- Simple and fast for prototype/demonstration purposes

**3. Validation Strategy**
- Centralized validation logic in dedicated validator module
- Returns detailed error messages with field-specific information
- Validates all inputs before processing

**4. RESTful API Design**
- Following REST principles for endpoint design
- Proper HTTP methods (GET, POST)
- Appropriate status codes (200, 201, 400, 404, 500)
- JSON request/response format

---

## 🚀 API Endpoints

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/transactions` | Create a new transaction |
| `GET` | `/transactions` | List all transactions (with filtering) |
| `GET` | `/transactions/:id` | Get specific transaction by ID |

### Accounts

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/accounts/:accountId/balance` | Get account balance |
| `GET` | `/accounts/:accountId/summary` | Get account summary with statistics |

### Query Parameters (for GET /transactions)

- `accountId` - Filter by account ID
- `type` - Filter by transaction type (deposit, withdrawal, transfer)
- `from` - Filter by start date (YYYY-MM-DD)
- `to` - Filter by end date (YYYY-MM-DD)

---

## 📊 Data Models

### Transaction Model

```json
{
  "id": "string (UUID, auto-generated)",
  "fromAccount": "string (ACC-XXXXX format, required for withdrawal/transfer)",
  "toAccount": "string (ACC-XXXXX format, required for deposit/transfer)",
  "amount": "number (positive, max 2 decimal places)",
  "currency": "string (ISO 4217 code: USD, EUR, GBP, etc.)",
  "type": "string (deposit | withdrawal | transfer)",
  "timestamp": "string (ISO 8601 datetime, auto-generated)",
  "status": "string (pending | completed | failed, default: completed)"
}
```

---

## ✅ Implemented Tasks

### Task 1: Core API Implementation ⭐
- ✅ POST /transactions endpoint
- ✅ GET /transactions endpoint
- ✅ GET /transactions/:id endpoint
- ✅ GET /accounts/:accountId/balance endpoint
- ✅ In-memory storage
- ✅ Proper HTTP status codes
- ✅ Error handling

### Task 2: Transaction Validation ✅
- ✅ Amount validation (positive, 2 decimal places max)
- ✅ Account number validation (ACC-XXXXX format)
- ✅ Currency validation (ISO 4217 codes)
- ✅ Transaction type validation
- ✅ Detailed error messages

### Task 3: Transaction History 📜
- ✅ Filter by account ID
- ✅ Filter by transaction type
- ✅ Filter by date range
- ✅ Combine multiple filters

### Task 4: Transaction Summary Endpoint 📈
- ✅ GET /accounts/:accountId/summary
- ✅ Total deposits calculation
- ✅ Total withdrawals calculation
- ✅ Transaction count
- ✅ Most recent transaction date

---

## 🧪 Testing

Comprehensive testing was performed on all endpoints. See [demo/TEST_REPORT.md](demo/TEST_REPORT.md) for detailed test results.

**Test Results:**
- ✅ All core endpoints functional
- ✅ Validation working correctly
- ✅ Filtering operational
- ✅ Balance calculations accurate
- ✅ Error handling proper

---

## 💡 How AI Assisted This Project

### AI Tool Used
**GitHub Copilot** powered by Claude Sonnet 4.5

### AI Contributions

1. **Project Structure Setup**
   - Generated package.json with appropriate dependencies
   - Created .gitignore with Node.js best practices
   - Organized folder structure following MVC pattern

2. **Code Generation**
   - Implemented Express.js routing structure
   - Created transaction model with UUID generation
   - Developed comprehensive validation logic
   - Built helper functions for balance calculation and filtering

3. **Validation Logic**
   - Generated regex patterns for account number validation
   - Implemented ISO 4217 currency code checking
   - Created detailed error message formatting

4. **Testing & Documentation**
   - Created sample API requests in multiple formats
   - Generated automated test scripts
   - Wrote comprehensive test reports

### Effective Prompting Strategies

- Clearly specified requirements from TASKS.md
- Requested modular architecture with separation of concerns
- Asked for comprehensive validation with detailed error messages
- Specified REST API best practices

---

## 🛠️ Technologies Used

- **Node.js** - JavaScript runtime
- **Express.js** - Web framework
- **uuid** - UUID generation for transaction IDs
- **JavaScript ES6+** - Modern JavaScript features

---

## 📖 Additional Documentation

- **[HOWTORUN.md](HOWTORUN.md)** - Step-by-step setup and running instructions
- **[demo/TEST_REPORT.md](demo/TEST_REPORT.md)** - Comprehensive test results
- **[demo/sample-requests.http](demo/sample-requests.http)** - REST Client format examples
- **[IMPLEMENTATION_COMPLETE.md](IMPLEMENTATION_COMPLETE.md)** - Implementation summary

---

<div align="center">

*This project was completed as part of the AI-Assisted Development course.*

**Status:** ✅ All tasks completed successfully

</div>
