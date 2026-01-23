# 🏦 Homework 1: Banking Transactions API

> **Student Name**: [Your Name]
> **Date Submitted**: January 21, 2026
> **AI Tools Used**: Claude Code (Primary), [Add second tool if used]

---

## 📋 Project Overview

A RESTful API for managing banking transactions built with **Node.js and Express**, developed entirely with AI assistance. This project demonstrates effective use of AI coding tools for rapid API development, implementing CRUD operations, data validation, filtering capabilities, and advanced rate limiting.

### Key Features

- ✅ **Complete REST API** with 4 core endpoints
- ✅ **Comprehensive Validation** (amount, account format, currency codes)
- ✅ **Advanced Filtering** (by account, type, and date range)
- ✅ **Rate Limiting** (100 requests/minute per IP)
- ✅ **Account Summary** endpoint (bonus feature)
- ✅ **In-memory storage** (no database required)
- ✅ **Detailed error handling** with meaningful messages
- ✅ **Transaction balance calculation**

---

## 🚀 Features Implemented

### ✅ Task 1: Core API Implementation (25 points)

| Endpoint | Method | Description | Status |
|----------|--------|-------------|--------|
| `/transactions` | POST | Create a new transaction | ✅ Complete |
| `/transactions` | GET | List all transactions (with filtering) | ✅ Complete |
| `/transactions/:id` | GET | Get specific transaction by ID | ✅ Complete |
| `/accounts/:accountId/balance` | GET | Calculate account balance | ✅ Complete |

### ✅ Task 2: Transaction Validation (15 points)

- **Amount Validation**: Positive numbers only, maximum 2 decimal places ✅
- **Account Format**: Must match `ACC-XXXXX` pattern (alphanumeric) ✅
- **Currency Validation**: ISO 4217 codes (USD, EUR, GBP, JPY, CHF, AUD, CAD) ✅
- **Type Validation**: deposit, withdrawal, or transfer only ✅
- **Structured Error Responses**: Field-level validation errors ✅

### ✅ Task 3: Transaction Filtering (15 points)

- Filter by `accountId` (transactions involving this account) ✅
- Filter by `type` (deposit, withdrawal, transfer) ✅
- Filter by date range (`from` and `to` parameters) ✅
- Combined filters support (all filters can work together) ✅

### ✅ Task 4: Rate Limiting (Additional Feature)

Implemented **Option D: Rate Limiting**

- 100 requests per minute per IP address ✅
- Returns `429 Too Many Requests` when exceeded ✅
- Includes rate limit headers:
  - `X-RateLimit-Limit`: Maximum requests allowed
  - `X-RateLimit-Remaining`: Requests remaining in current window
  - `X-RateLimit-Reset`: Timestamp when limit resets
  - `Retry-After`: Seconds until retry is allowed
- Automatic cleanup of expired rate limit entries ✅

### 🌟 Bonus Features

- **Account Summary Endpoint**: `GET /accounts/:accountId/summary`
  - Total deposits and withdrawals
  - Net amount and transaction count
  - Most recent transaction timestamp

---

## 🛠️ Technology Stack

- **Runtime**: Node.js v22.16.0
- **Framework**: Express.js v5.2.1
- **Development**: nodemon v3.1.11 (auto-restart)
- **Storage**: In-memory (JavaScript arrays and objects)
- **Lifecycle**: Graceful shutdown with signal handling
- **Testing**: Manual testing with curl, VS Code REST Client
- **AI Tools**: Claude Code (architecture, implementation, debugging)

---

## 📚 API Documentation

### Base URL

```
http://localhost:3000
```

### Endpoints

#### 1. Create Transaction

```http
POST /transactions
Content-Type: application/json
```

**Request Body:**
```json
{
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer"
}
```

**Success Response (201):**
```json
{
  "id": "762bb12a-6532-447a-9189-0f6b01fcf048",
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100.5,
  "currency": "USD",
  "type": "transfer",
  "timestamp": "2026-01-21T13:49:29.695Z",
  "status": "completed"
}
```

**Validation Error Response (400):**
```json
{
  "error": "Validation failed",
  "details": [
    {
      "field": "amount",
      "message": "Amount must be a positive number"
    }
  ]
}
```

#### 2. List Transactions

```http
GET /transactions
GET /transactions?accountId=ACC-12345
GET /transactions?type=deposit
GET /transactions?from=2024-01-01&to=2024-12-31
GET /transactions?accountId=ACC-12345&type=transfer
```

**Success Response (200):**
```json
[
  {
    "id": "...",
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.5,
    "currency": "USD",
    "type": "transfer",
    "timestamp": "2026-01-21T13:49:29.695Z",
    "status": "completed"
  }
]
```

#### 3. Get Transaction by ID

```http
GET /transactions/:id
```

**Success Response (200):** Single transaction object
**Not Found Response (404):**
```json
{
  "error": "Transaction not found",
  "id": "nonexistent-id"
}
```

#### 4. Get Account Balance

```http
GET /accounts/:accountId/balance
```

**Success Response (200):**
```json
{
  "accountId": "ACC-12345",
  "balance": 349.25,
  "currency": "USD",
  "transactionCount": 3
}
```

#### 5. Get Account Summary (Bonus)

```http
GET /accounts/:accountId/summary
```

**Success Response (200):**
```json
{
  "accountId": "ACC-12345",
  "totalDeposits": 500.00,
  "totalWithdrawals": 150.75,
  "netAmount": 349.25,
  "transactionCount": 3,
  "mostRecentTransaction": "2026-01-21T13:49:29.695Z"
}
```

---

## 📐 Data Model

### Transaction Schema

```javascript
{
  id: String (UUID),           // Auto-generated
  fromAccount: String,         // Format: ACC-XXXXX
  toAccount: String,           // Format: ACC-XXXXX
  amount: Number,              // Positive, max 2 decimals
  currency: String,            // ISO 4217 code
  type: String,                // deposit | withdrawal | transfer
  timestamp: String,           // ISO 8601 datetime
  status: String               // pending | completed | failed
}
```

### Validation Rules

| Field | Rules |
|-------|-------|
| `amount` | Positive number, maximum 2 decimal places |
| `fromAccount` | Pattern: `ACC-[A-Z0-9]{5}` (case-insensitive) |
| `toAccount` | Pattern: `ACC-[A-Z0-9]{5}` (case-insensitive) |
| `currency` | One of: USD, EUR, GBP, JPY, CHF, AUD, CAD |
| `type` | One of: deposit, withdrawal, transfer |

---

## 🤖 AI Tools Used

### Primary Tool: Claude Code

**Usage:** 95% of development (architecture, implementation, testing, documentation)

**Most Helpful For:**
- **Project Architecture**: Designed folder structure (models, routes, validators, utils)
- **Validation Logic**: Created comprehensive validators with detailed error messages
- **Balance Calculation**: Implemented correct logic for deposits, withdrawals, and transfers
- **Rate Limiting**: Built sophisticated in-memory rate limiter with cleanup
- **Documentation**: Generated API docs, README, and test scripts

**Example Prompts That Worked Well:**

1. **Initial Setup:**
   ```
   "I need to set up a Node.js Express project for a banking transactions API.
   Requirements: in-memory storage, 4 core endpoints, validation, filtering.
   Please provide folder structure and initial setup."
   ```
   Result: Complete project structure with package.json, .gitignore, and basic server

2. **Validation:**
   ```
   "Create comprehensive validation module with:
   - Amount validation (positive, max 2 decimals)
   - Account format ACC-XXXXX validation
   - Currency validation (ISO 4217)
   Return detailed field-level errors in array format"
   ```
   Result: Perfect validation module with all edge cases handled

3. **Rate Limiting:**
   ```
   "Implement rate limiting middleware:
   - 100 requests per minute per IP
   - Track in memory with Map
   - Return 429 with Retry-After header
   - Include X-RateLimit-* headers on all responses"
   ```
   Result: Complete rate limiter with automatic cleanup and proper headers

**Challenges Encountered:**

1. **Challenge**: Balance calculation logic was initially unclear
   - **AI Help**: Asked Claude to explain the logic step-by-step
   - **Solution**: Understood that deposits/incoming transfers add, withdrawals/outgoing transfers subtract

2. **Challenge**: Rate limiting needed to handle IP addresses correctly
   - **AI Help**: Requested handling of various IP header formats (x-forwarded-for, x-real-ip)
   - **Solution**: Implemented fallback chain for different proxy configurations

**Iteration Examples:**

- **First attempt**: Basic validation returned simple boolean
- **Refinement**: Asked for detailed error objects with field names
- **Final version**: Structured error response with array of field-level errors

### Secondary Tool: [Add if you used another tool]

**Note**: Assignment requires at least 2 AI tools.

⚠️ **TODO**: To meet the requirement fully:
1. Use GitHub Copilot or ChatGPT/Claude Web to implement one small feature or refactor
2. Document the comparison (which tool was better for what)
3. Take screenshots of both tools in action
4. Add comparison notes here

This will complete the AI tools requirement and provide valuable learning insights.

---

## 📁 Project Structure

```
homework-1/
├── src/
│   ├── index.js                      # Main server file
│   ├── package.json                  # Dependencies and scripts
│   ├── .gitignore                    # Git ignore rules
│   ├── models/
│   │   └── transaction.js            # Transaction model and storage
│   ├── routes/
│   │   ├── transactions.js           # Transaction endpoints
│   │   └── accounts.js               # Account endpoints
│   ├── validators/
│   │   └── transactionValidator.js   # Validation logic
│   └── utils/
│       └── rateLimiter.js            # Rate limiting middleware
├── demo/
│   ├── run.sh                        # Server startup script
│   ├── sample-requests.http          # VS Code REST Client tests
│   ├── sample-requests.sh            # Command-line test script
│   └── sample-data.json              # Sample transaction data
├── docs/
│   └── screenshots/                  # AI interaction screenshots
├── README.md                         # This file
├── HOWTORUN.md                       # Setup and run instructions
└── TASKS.md                          # Original assignment requirements
```

---

## 🎯 How to Run

See [HOWTORUN.md](HOWTORUN.md) for detailed setup and testing instructions.

**Quick start:**
```bash
cd homework-1
./demo/run.sh              # Start the server
./demo/sample-requests.sh  # Run all tests (in another terminal)
```

---

## 🧪 Testing

### Manual Testing

All endpoints have been thoroughly tested with:
- ✅ Valid data scenarios
- ✅ Invalid data (validation errors)
- ✅ Edge cases (empty results, non-existent IDs)
- ✅ Filtering combinations
- ✅ Rate limiting (100+ requests)

### Test Results

- **Balance Calculation**: Verified with manual calculations
  - Example: ACC-12345 received 500, sent 100.50 and 50.25 = 349.25 ✅
- **Validation**: All validation rules enforced correctly ✅
- **Rate Limiting**: 429 response triggered at 101st request ✅
- **Filtering**: All filter combinations working ✅

---

## 📝 Lessons Learned

### About AI-Assisted Development

1. **Start with Clear Architecture**: AI works best when you define the structure first
2. **Iterate on Prompts**: First response is good, refinements make it great
3. **Understand Generated Code**: Don't just copy-paste, read and comprehend
4. **Test Incrementally**: Build and test one feature at a time
5. **Ask for Explanations**: When logic is complex, ask AI to explain it

### Technical Insights

1. **In-Memory Storage**: Simple for learning, but wouldn't scale in production
2. **Rate Limiting**: More complex than expected, needs cleanup strategy
3. **Validation**: Field-level errors much better than boolean responses
4. **Express Middleware**: Order matters (rate limit → parsing → routes)

---

## 🚀 Future Enhancements

If I had more time, I would add:

- [ ] Unit tests with Jest or Mocha
- [ ] Persistent storage (SQLite or PostgreSQL)
- [ ] Authentication and authorization
- [ ] Transaction status updates (pending → completed)
- [ ] Pagination for transaction lists
- [ ] WebSocket support for real-time updates
- [ ] Docker containerization
- [ ] API versioning
- [ ] Swagger/OpenAPI documentation

---

<div align="center">

*This project was completed as part of the AI-Assisted Development course.*

**Total Development Time**: ~4 hours
**AI Tools**: Claude Code
**Lines of Code**: ~1000+

</div>
