# Homework 1: Banking Transaction REST API

> **Student Name**: Serhii Kindyk  
> **Date Submitted**: 01/23/2026  
> **AI Tools Used**: GitHub Copilot (mainly), Claude Code (for cli training)

---

## 🎯 Project Overview

This project implements a **Banking Transaction REST API** using Node.js and Express. The API allows users to manage financial transactions, track account balances, and retrieve transaction histories with advanced filtering capabilities.

### ✅ Features Implemented

#### **Core Functionality (Task 1)**
- ✅ `POST /transactions` — Create new transactions
- ✅ `GET /transactions` — List all transactions
- ✅ `GET /transactions/:id` — Retrieve specific transaction by ID
- ✅ `GET /accounts/:accountId/balance` — Get account balance
- ✅ Proper HTTP status codes (201, 200, 400, 404)
- ✅ In-memory transaction storage

#### **Validation (Task 2)**
- ✅ Amount validation (positive numbers, max 2 decimal places)
- ✅ Account format validation (`ACC-XXXXX` alphanumeric)
- ✅ ISO 4217 currency code validation
- ✅ Transaction type validation (deposit, withdrawal, transfer)
- ✅ Type-specific account validation (deposits require toAccount only, etc.)
- ✅ Date format validation for query parameters
- ✅ Field-level error messages with detailed context

#### **Filtering (Task 3)**
- ✅ Filter by account ID: `?accountId=ACC-12345`
- ✅ Filter by transaction type: `?type=deposit`
- ✅ Filter by date range: `?from=2024-01-01&to=2024-12-31`
- ✅ Support combining multiple filters

#### **Optional Feature (Task 4 - Option A)**
- ✅ **Transaction Summary Endpoint**: `GET /summary`
  - Returns **currency-grouped** statistics (prevents mixing USD + EUR + GBP)
  - Shows total deposits, withdrawals, and transfers per currency
  - Transaction count and breakdown per currency
  - Most recent transaction
  - Supports filtering by account ID

---

## 📊 Transaction Model

```json
{
  "id": "string (auto-generated unique ID)",
  "fromAccount": "string (ACC-XXXXX format, required for withdrawal/transfer)",
  "toAccount": "string (ACC-XXXXX format, required for deposit/transfer)",
  "amount": "number (positive, max 2 decimals)",
  "currency": "string (ISO 4217 code)",
  "type": "string (deposit | withdrawal | transfer)",
  "timestamp": "string (ISO 8601 format)",
  "status": "string (completed)"
}
```

**Transaction Type Rules**:
- **Deposit**: Only `toAccount` required (money coming in)
- **Withdrawal**: Only `fromAccount` required (money going out)
- **Transfer**: Both `fromAccount` and `toAccount` required (money moving between accounts)

---

## 🏗️ Architecture

### Folder Structure

```
src/
├── index.js                          # Main application entry point
├── models/
│   └── Transaction.js                # Transaction data model and storage
├── validators/
│   └── validators.js                 # Validation functions
├── middleware/
│   └── errorHandler.js              # Global error handling middleware
├── routes/
│   ├── transactions.js              # Transaction endpoints
│   ├── accounts.js                  # Account endpoints
│   └── summary.js                   # Summary endpoint (Optional Feature A)
└── utils/
    └── response.js                  # Response helper functions
```

### Key Design Decisions

1. **Modular Architecture**: Routes, validators, and middleware are separated for maintainability
2. **In-Memory Storage**: Uses JavaScript arrays for simplicity (no database required)
3. **Consistent Response Format**: All endpoints return a standardized JSON response structure
4. **Field-Level Validation**: Provides detailed error messages for each invalid field
5. **Filter Composition**: Supports combining multiple filters for flexible querying
6. **Type-Safe Transactions**: Enforces transaction type rules (deposit/withdrawal/transfer account requirements)
7. **Currency Separation**: Summary statistics grouped by currency to prevent mixing different currencies
8. **Comprehensive JSDoc**: All functions documented with type annotations for better developer experience

---

## 🚀 Quick Start

**For detailed setup and testing instructions, see [HOWTORUN.md](HOWTORUN.md)**

### Installation & Running
```bash
cd homework-1
npm install
npm start
```

The API will start on `http://localhost:3000`

### Web Interface
Open `http://localhost:3000` in your browser for the interactive UI

### Quick Test Data
```bash
cd demo
.\populate-test-data.bat
```

---

## 📡 API Endpoints

### Transactions
- `POST /transactions` — Create new transaction
- `GET /transactions` — List all transactions (with filters)
- `GET /transactions/:id` — Get specific transaction

### Accounts
- `GET /accounts/:accountId/balance` — Get account balance

### Summary
- `GET /summary` — Get transaction summary (currency-grouped)

### Health
- `GET /health` — Health check

**For detailed API examples and testing methods, see [HOWTORUN.md](HOWTORUN.md)**

---

## 📝 Error Handling

All errors return a standardized JSON response:

```json
{
  "success": false,
  "error": {
    "message": "Validation failed",
    "details": {
      "amount": "Amount must be positive",
      "currency": "Currency must be a valid ISO 4217 code"
    }
  }
}
```

## 📸 Screenshots & Demo

**10 comprehensive screenshots** demonstrating all features are located in `docs/screenshots/`

Includes:
- Homepage overview with loaded transactions
- Smart transaction forms (deposit/withdrawal/transfer)
- Account balance calculations
- Currency-grouped summary statistics
- Transaction filtering (by type and account)
- Validation error handling

See detailed descriptions: [DEMO_SCREENSHOTS.md](docs/screenshots/DEMO_SCREENSHOTS.md)

**Automated Demo**: Screenshots captured using Selenium WebDriver in fullscreen (1920x1080)

---

## 📚 Documentation

Comprehensive documentation is available in the `docs/` folder:

- **[HOWTORUN.md](HOWTORUN.md)** - **Complete setup, running, and testing guide**
- **[docs/HOW_TO_RUN.md](docs/HOW_TO_RUN.md)** - Detailed setup and running instructions
- **[docs/IMPLEMENTATION_PLAN.md](docs/IMPLEMENTATION_PLAN.md)** - Development planning and approach
- **[docs/SUBMISSION_ANALYSIS.md](docs/SUBMISSION_ANALYSIS.md)** - Analysis of submission completeness
- **[docs/screenshots/DEMO_SCREENSHOTS.md](docs/screenshots/DEMO_SCREENSHOTS.md)** - Screenshot gallery with descriptions

---

## 📋 Demo Files

- `demo/sample-requests.http` — VS Code / IDEA REST Client requests
- `demo/sample-requests.sh` — cURL commands
- `demo/test-api.bat` — Windows batch test script
- `demo/populate-test-data.bat` — **NEW**: Creates 10 diverse test transactions
- `demo/README.md` — Comprehensive demo documentation

---

<div align="center">

*This project was completed as part of the AI-Assisted Development course.*

</div>
