# 🏦 Banking Transactions API

> **Student Name**: Denys Usenko
> **Date Submitted**: January 24, 2026
> **AI Tools Used**: GitHub Copilot, Claude Code

---

## 📋 Project Overview

A production-ready REST API for managing banking transactions built with **Node.js**, **Express.js**, and **TypeScript**. This API provides a complete suite of endpoints for creating transactions, filtering transaction history, calculating account balances, generating summaries, and exporting data—all with comprehensive type safety, strict validation, and rate limiting.

### Key Highlights

- 🔒 **Type-Safe**: Built with TypeScript in strict mode with zero `any` types
- ✅ **Fully Validated**: Multi-layered validation for amounts, currencies, accounts, and transaction types
- 📊 **Feature-Rich**: Advanced filtering, summaries, CSV export, and rate limiting
- 🚀 **Production-Ready**: Proper error handling, logging, and HTTP status codes
- 📚 **Well-Documented**: Comprehensive API documentation and examples

## 🚀 Features Implemented

### Task 1: Core API Implementation ✅

All required endpoints are fully functional:

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| `POST` | `/transactions` | Create a new transaction | 201, 400 |
| `GET` | `/transactions` | List all transactions (with optional filters) | 200 |
| `GET` | `/transactions/:id` | Get a specific transaction by ID | 200, 404 |
| `GET` | `/accounts/:accountId/balance` | Get account balance | 200 |

### Task 2: Transaction Validation ✅

Comprehensive validation system with detailed error messages:

- ✅ **Amount Validation**: Positive numbers only, maximum 2 decimal places
- ✅ **Account Format**: Strict `ACC-XXXXX` pattern (5 alphanumeric characters)
- ✅ **Currency Validation**: ISO 4217 codes (USD, EUR, GBP, JPY, CHF, CAD, AUD, CNY, etc.)
- ✅ **Type Validation**: Enforces required accounts based on transaction type
- ✅ **Decimal Precision**: Validates proper decimal formatting

### Task 3: Transaction History Filtering ✅

Advanced filtering capabilities on `GET /transactions`:

- 🔍 **Account Filter**: `?accountId=ACC-12345` - Returns all transactions involving the account
- 📝 **Type Filter**: `?type=transfer` - Filter by transaction type (case-insensitive)
- 📅 **Date Range**: `?from=2024-01-01&to=2024-01-31` - Filter by date range
- 🔗 **Combined Filters**: Chain multiple filters together

### Task 4: Additional Features ✅

Multiple bonus features implemented:

#### ✅ Transaction Summary (Option A)
```
GET /accounts/:accountId/summary
```
Returns detailed account analytics:
- Total deposits, withdrawals, and transfers
- Count of each transaction type
- Current balance
- Most recent transaction timestamp

#### ✅ Transaction Export (Option C)
```
GET /transactions/export?format=csv
```
Export transactions to CSV with:
- Automatic date-stamped filenames
- Proper CSV formatting with headers
- Support for all filtering parameters
- Browser-friendly download response

#### ✅ Rate Limiting (Option D)
```
Rate Limit: 100 requests per minute per IP
```
Protection against abuse:
- Returns `429 Too Many Requests` when exceeded
- Standard `RateLimit-*` headers for client transparency
- Per-IP address tracking

## 🛠️ Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Runtime** | Node.js | 18.0+ |
| **Framework** | Express.js | 5.x |
| **Language** | TypeScript | 5.x |
| **Validation** | currency-codes library | 2.x |
| **Rate Limiting** | express-rate-limit | 8.x |
| **Storage** | In-memory array | N/A |

## 🏗️ Architecture Overview

### Design Principles

1. **Separation of Concerns**: Clear separation between routes, controllers, validators, and data storage
2. **Type Safety First**: Comprehensive TypeScript interfaces and enums
3. **Validation at Boundaries**: Input validation before business logic
4. **Idempotent Operations**: Safe GET operations with predictable outcomes
5. **RESTful Design**: Standard HTTP methods and status codes

### Transaction Model

```typescript
interface Transaction {
  id: string;                    // Auto-generated UUID v4
  fromAccount?: string;          // Required for withdrawal and transfer
  toAccount?: string;            // Required for deposit and transfer
  amount: number;                // Positive number, max 2 decimals
  currency: Currency;            // ISO 4217 code (USD, EUR, GBP, JPY, etc.)
  type: TransactionType;         // deposit | withdrawal | transfer
  timestamp: string;             // ISO 8601 datetime
  status: TransactionStatus;     // completed (extensible for future states)
}
```

### Balance Calculation Logic

The system calculates balances by aggregating all transactions for an account:

- **Deposits**: Add `amount` to `toAccount`
- **Withdrawals**: Subtract `amount` from `fromAccount`
- **Transfers**: Subtract from `fromAccount`, add to `toAccount`
- All balances start at 0 and are rounded to 2 decimal places

## 📁 Project Structure

```
homework-1/
├── src/
│   ├── index.ts                          # Main Express server and middleware
│   ├── controllers/
│   │   ├── accountController.ts          # Account balance & summary logic
│   │   └── transactionController.ts      # Transaction business logic & filtering
│   ├── models/
│   │   ├── transaction.ts                # Transaction types and enums
│   │   ├── balance.ts                    # Balance and summary response types
│   │   └── common.ts                     # Shared types (ErrorResponse, etc.)
│   ├── routes/
│   │   ├── transactions.ts               # Transaction API routes
│   │   └── accounts.ts                   # Account API routes
│   ├── validators/
│   │   ├── transactionValidator.ts       # Main transaction validation orchestrator
│   │   ├── accountValidator.ts           # Account format validation
│   │   ├── amountValidator.ts            # Amount and decimal validation
│   │   ├── currencyValidator.ts          # ISO 4217 currency validation
│   │   ├── queryValidator.ts             # Query parameter validation
│   │   └── accountConstants.ts           # Account validation regex patterns
│   └── utils/
│       └── dataStore.ts                  # In-memory storage and helper functions
├── demo/
│   ├── run.sh                            # Script to start the server
│   ├── sample-requests.http              # Sample API requests for testing
│   └── sample-data.json                  # Sample transaction data
├── docs/
│   └── screenshots/                      # AI interaction screenshots
├── package.json                          # Dependencies and scripts
├── tsconfig.json                         # TypeScript configuration (strict mode)
├── README.md                             # Project documentation (this file)
├── HOWTORUN.md                           # Setup and usage instructions
└── TASKS.md                              # Assignment requirements
```

## ✨ Key Implementation Details

### Type Safety & Code Quality

- **Strict TypeScript**: Zero tolerance for `any` types, all functions fully typed
- **Enums for Constants**: `TransactionType`, `TransactionStatus` prevent magic strings
- **Interface Segregation**: Separate DTOs for creation vs. complete transaction objects
- **Immutable Patterns**: Data transformations return new objects
- **No External State**: Controllers are stateless, data isolated in dataStore

### Validation Architecture

The validation system uses a modular, composable approach:

```typescript
// Layered validation with specific error messages
validateTransaction(dto) {
  1. validateAmount()      // Positive, max 2 decimals
  2. validateCurrency()    // ISO 4217 codes via library
  3. validateType()        // Enum-based validation
  4. validateAccounts()    // Format + required field logic
}
```

**Account Rules by Transaction Type:**
- **Deposit**: `toAccount` required, `fromAccount` forbidden
- **Withdrawal**: `fromAccount` required, `toAccount` forbidden  
- **Transfer**: Both `fromAccount` and `toAccount` required

### Filtering Implementation

Multi-stage filtering pipeline:

```typescript
getAllTransactions()
  → filterByAccount(accountId)    // Match fromAccount OR toAccount
  → filterByType(type)            // Case-insensitive exact match
  → filterByDateRange(from, to)   // Inclusive date filtering
```

**Date Handling:**
- `from` parameter: Sets time to 00:00:00.000 (start of day)
- `to` parameter: Sets time to 23:59:59.999 (end of day)
- Supports partial dates (from only, to only, or both)

### CSV Export

CSV generation with proper formatting:

- Header row with column names
- Quoted fields for safety
- RFC 4180 compliant formatting
- Applies all filter parameters (accountId, type, date range)
- Generates timestamped filenames: `transactions-YYYY-MM-DD.csv`

### Error Handling

Consistent error response structure:

```typescript
interface ErrorResponse {
  error: string;           // Human-readable error message
  details?: unknown;       // Optional validation details or stack trace
}
```

**HTTP Status Code Usage:**
- `200` OK: Successful GET request
- `201` Created: Transaction successfully created
- `400` Bad Request: Validation errors
- `404` Not Found: Transaction ID doesn't exist
- `429` Too Many Requests: Rate limit exceeded
- `500` Internal Server Error: Unexpected server errors

## 🎯 Architecture Decisions

### Why In-Memory Storage?

For this educational project, in-memory storage provides:
- ✅ Zero setup complexity (no database installation)
- ✅ Fast development iteration
- ✅ Simple data access patterns for learning
- ✅ Easy to understand and debug
- ⚠️ **Note**: Data resets on server restart (not production-suitable)

### Why TypeScript Strict Mode?

Benefits of strict type checking:
- Catch errors at compile time, not runtime
- Better IDE autocomplete and refactoring
- Self-documenting code through type annotations
- Prevents common JavaScript pitfalls (`null`, `undefined` handling)

### Why Modular Validators?

Separating validators by concern:
- Single Responsibility Principle
- Reusable validation logic
- Easier to test individual validators
- Clear error messages traced to specific validators
- Extensible for future validation rules

## 📊 API Usage Examples

### Creating Transactions

```bash
# Deposit money into an account
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": 1000,
    "currency": "USD",
    "type": "deposit"
  }'

# Withdraw money from an account
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "amount": 200,
    "currency": "USD",
    "type": "withdrawal"
  }'

# Transfer between accounts
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 150.50,
    "currency": "EUR",
    "type": "transfer"
  }'
```

### Querying Transactions

```bash
# Get all transactions
curl http://localhost:3000/transactions

# Get transactions for a specific account
curl "http://localhost:3000/transactions?accountId=ACC-12345"

# Get only transfers
curl "http://localhost:3000/transactions?type=transfer"

# Get transactions in date range
curl "http://localhost:3000/transactions?from=2026-01-01&to=2026-01-31"

# Combine multiple filters
curl "http://localhost:3000/transactions?accountId=ACC-12345&type=deposit&from=2026-01-01"
```

### Account Information

```bash
# Get account balance
curl http://localhost:3000/accounts/ACC-12345/balance

# Get account summary with statistics
curl http://localhost:3000/accounts/ACC-12345/summary

# Export transactions to CSV
curl "http://localhost:3000/transactions/export?format=csv" -O -J
```

## 🧪 Testing Recommendations

### Manual Testing Checklist

- [ ] Create deposit, withdrawal, and transfer transactions
- [ ] Test validation errors (negative amounts, invalid currencies, wrong account formats)
- [ ] Verify balance calculation across multiple transactions
- [ ] Test each filter parameter independently
- [ ] Test combined filters
- [ ] Verify CSV export downloads correctly
- [ ] Test rate limiting by making 100+ requests rapidly
- [ ] Test 404 responses for non-existent transaction IDs

### Sample Test Scenarios

**Scenario 1: Basic Flow**
1. Deposit $1000 to ACC-12345
2. Withdraw $200 from ACC-12345
3. Check balance (should be $800)

**Scenario 2: Transfers**
1. Deposit $500 to ACC-AAAA1
2. Transfer $200 from ACC-AAAA1 to ACC-BBBB2
3. Check ACC-AAAA1 balance (should be $300)
4. Check ACC-BBBB2 balance (should be $200)

**Scenario 3: Validation**
1. Try to create deposit without `toAccount` (should fail)
2. Try negative amount (should fail)
3. Try invalid currency "XYZ" (should fail)
4. Try invalid account format "INVALID" (should fail)

## 🚀 Future Enhancements

Potential improvements for production use:

1. **Persistence**: Replace in-memory storage with PostgreSQL or MongoDB
2. **Authentication**: Add JWT-based user authentication
3. **Authorization**: Implement role-based access control (RBAC)
4. **Testing**: Add unit tests (Jest) and integration tests (Supertest)
5. **Logging**: Implement structured logging (Winston, Pino)
6. **Monitoring**: Add health checks and metrics (Prometheus)
7. **Validation**: Enhance with schema validation library (Zod, Joi)
8. **Documentation**: Generate OpenAPI/Swagger documentation
9. **Multi-Currency**: Support currency conversion
10. **Transactions**: Add pending/failed status and rollback support

## 📝 API Documentation

For detailed setup instructions, API endpoint documentation, and troubleshooting guide, see [HOWTORUN.md](HOWTORUN.md).

## 🤖 Development Process

This project was developed using GitHub Copilot for AI-assisted coding. Screenshots of the AI interaction process are available in `docs/screenshots/`.

---

<div align="center">

**Banking Transactions API v1.0.0**

*Built with TypeScript, Express.js, and AI assistance*

*This project was completed as part of the AI-Assisted Development course.*

</div>
