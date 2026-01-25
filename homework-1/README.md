# 🏦 Homework 1: Banking Transactions API

> **Student Name**: [Your Name]
> **Date Submitted**: [Date]
> **AI Tools Used**: Claude Code (Cursor IDE), GitHub Copilot

---

## 📋 Project Overview

This project implements a RESTful API for managing banking transactions using Node.js, TypeScript, and Express.js. The API supports creating transactions, retrieving transaction history with filtering capabilities, checking account balances, and exporting transaction data to CSV format.

### Key Features

- ✅ **Transaction Management**: Create and retrieve deposit, withdrawal, and transfer transactions
- ✅ **Validation**: Comprehensive input validation for amounts, account formats, and currency codes
- ✅ **Filtering**: Query transactions by account, type, and date range
- ✅ **Account Balances**: Real-time balance calculation based on transaction history
- ✅ **CSV Export**: Export transaction data in CSV format for analysis
- ✅ **Error Handling**: Detailed error messages with field-specific validation feedback
- ✅ **Type Safety**: Full TypeScript implementation with strict typing

---

## 🏗️ Architecture

The application follows a layered architecture pattern:

```
┌─────────────────────────────────────────┐
│         Express Routes Layer            │
│  (transactionRoutes, accountRoutes)     │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Controllers Layer               │
│  (transactionController, accountController) │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Services Layer                  │
│  (transactionService, accountService)   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│    Storage & Validation Layer           │
│  (storageService, transactionValidator) │
└─────────────────────────────────────────┘
```

### Project Structure

```
homework-1/
├── src/
│   ├── index.ts                    # Main application entry point
│   ├── models/
│   │   └── transaction.ts          # TypeScript interfaces and types
│   ├── controllers/
│   │   ├── transactionController.ts # HTTP request handlers
│   │   └── accountController.ts
│   ├── services/
│   │   ├── transactionService.ts   # Business logic
│   │   ├── accountService.ts
│   │   └── storageService.ts       # In-memory data storage
│   ├── validators/
│   │   └── transactionValidator.ts # Input validation
│   ├── routes/
│   │   ├── transactionRoutes.ts    # Route definitions
│   │   └── accountRoutes.ts
│   └── utils/
│       └── csvExporter.ts          # CSV export utility
├── demo/
│   ├── run.sh                      # Start script
│   ├── sample-requests.http        # API test requests
│   └── sample-data.json            # Sample data
├── package.json
├── tsconfig.json
└── README.md
```

---

## 🚀 API Endpoints

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/transactions` | Create a new transaction |
| `GET` | `/transactions` | Get all transactions (with optional filters) |
| `GET` | `/transactions/:id` | Get a specific transaction by ID |
| `GET` | `/transactions/export?format=csv` | Export transactions as CSV |

### Accounts

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/accounts/:accountId/balance` | Get account balance |

### Query Parameters for Filtering

- `accountId`: Filter transactions by account (e.g., `?accountId=ACC-12345`)
- `type`: Filter by transaction type (e.g., `?type=transfer`)
- `from`: Filter by start date (e.g., `?from=2024-01-01`)
- `to`: Filter by end date (e.g., `?to=2024-12-31`)

---

## 📝 Transaction Model

```json
{
  "id": "TXN-00000001",
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer",
  "timestamp": "2024-01-25T10:30:00.000Z",
  "status": "completed"
}
```

### Validation Rules

- **Amount**: Must be positive with maximum 2 decimal places
- **Account Format**: Must follow pattern `ACC-XXXXX` (where X is alphanumeric)
- **Currency**: Must be valid ISO 4217 code (USD, EUR, GBP, JPY, etc.)
- **Type**: Must be one of: `deposit`, `withdrawal`, `transfer`

---

## 🤖 AI Tools Usage

### Claude Code (Cursor IDE)

Used for:
- Initial project structure and architecture planning
- TypeScript interface and type definitions
- Service layer implementation with business logic
- Validation logic and error handling patterns
- CSV export functionality
- Documentation and code comments

### GitHub Copilot

Used for:
- Auto-completion of repetitive code patterns
- Route handler implementations
- Test request generation
- Sample data creation

### AI-Assisted Development Process

1. **Planning Phase**: Used AI to design the layered architecture and file structure
2. **Implementation Phase**: Iteratively developed each layer with AI assistance
3. **Validation Phase**: AI helped create comprehensive validation rules
4. **Testing Phase**: Generated test cases and sample requests with AI
5. **Documentation Phase**: AI assisted in creating clear documentation

---

## 🧪 Testing

The API has been tested with various scenarios including:

- ✅ Valid transactions (deposit, withdrawal, transfer)
- ✅ Invalid amounts (negative, too many decimals)
- ✅ Invalid account formats
- ✅ Invalid currency codes
- ✅ Transaction filtering by account, type, and date
- ✅ Account balance calculations
- ✅ CSV export functionality
- ✅ Error handling for non-existent resources

See `demo/sample-requests.http` for comprehensive test cases.

---

## 💡 Challenges and Solutions

### Challenge 1: Route Ordering
**Problem**: The `/transactions/export` route was being matched by `/transactions/:id`

**Solution**: Placed specific routes before parameterized routes in the router configuration

### Challenge 2: Balance Calculation
**Problem**: Determining correct balance updates for different transaction types

**Solution**: Implemented transaction-type-specific logic in the service layer:
- Deposits: Credit to account
- Withdrawals: Debit from account
- Transfers: Debit from source, credit to destination

### Challenge 3: CSV Special Characters
**Problem**: Transaction data might contain commas or quotes that break CSV format

**Solution**: Implemented proper CSV escaping in the csvExporter utility

---

## 🎯 Completed Tasks

- ✅ **Task 1**: Core API with all required endpoints
- ✅ **Task 2**: Comprehensive validation with detailed error messages
- ✅ **Task 3**: Transaction filtering by account, type, and date range
- ✅ **Task 4**: CSV export functionality (Option C)

---

## 📚 Technologies Used

- **Node.js**: JavaScript runtime
- **TypeScript**: Type-safe JavaScript
- **Express.js**: Web framework
- **dotenv**: Environment variable management

---

## 🔮 Future Enhancements

Potential improvements for future versions:

- Add persistent database storage (PostgreSQL, MongoDB)
- Implement authentication and authorization
- Add transaction reversal/cancellation
- Implement rate limiting
- Add comprehensive unit and integration tests
- Add API documentation with Swagger/OpenAPI
- Implement pagination for large transaction lists
- Add WebSocket support for real-time updates

---

<div align="center">

*This project was completed as part of the AI-Assisted Development course.*

**Built with ❤️ using AI-assisted development tools**

</div>
