# Comprehensive Implementation Prompt for Homework 1

This document contains a detailed, ready-to-use prompt for implementing the Banking Transactions API with AI assistance.

---

## Master Prompt for AI Tools

Copy and use this prompt with Claude Code, GitHub Copilot, or other AI coding assistants:

---

### PROMPT START

I need to build a Banking Transactions REST API as part of a homework assignment. Here are the complete requirements:

## Project Overview
Create a minimal REST API for banking transactions with the following specifications:

## Technology Stack
Use **Node.js with Express.js** (or specify Python/FastAPI or Java/Spring Boot if preferred)

## Required Endpoints

### 1. POST /transactions
Create a new transaction with the following JSON body:
```json
{
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer"
}
```

Response should include auto-generated ID, timestamp, and status (default: "completed").

### 2. GET /transactions
List all transactions with optional filtering:
- Query parameter `accountId`: filter by account (e.g., ?accountId=ACC-12345)
- Query parameter `type`: filter by transaction type (e.g., ?type=transfer)
- Query parameters `from` and `to`: filter by date range (e.g., ?from=2024-01-01&to=2024-01-31)
- Support combining multiple filters

### 3. GET /transactions/:id
Get a specific transaction by ID. Return 404 if not found.

### 4. GET /accounts/:accountId/balance
Calculate and return the current balance for an account based on all transactions:
- Add deposits and incoming transfers
- Subtract withdrawals and outgoing transfers

## Validation Requirements

### Amount Validation
- Must be a positive number
- Maximum 2 decimal places
- Return 400 with error message if invalid

### Account Format Validation
- Must follow pattern: ACC-XXXXX where X is alphanumeric
- Both fromAccount and toAccount must be validated
- Return 400 with error message if invalid

### Currency Validation
- Only accept valid ISO 4217 currency codes: USD, EUR, GBP, JPY, CHF, AUD, CAD
- Return 400 with error message if invalid

### Error Response Format
When validation fails, return this structure:
```json
{
  "error": "Validation failed",
  "details": [
    {"field": "amount", "message": "Amount must be a positive number"},
    {"field": "currency", "message": "Invalid currency code"}
  ]
}
```

## Additional Features (Implement at least ONE)

### Option A: Transaction Summary
Add endpoint: GET /accounts/:accountId/summary

Return:
```json
{
  "accountId": "ACC-12345",
  "totalDeposits": 1500.00,
  "totalWithdrawals": 500.00,
  "transactionCount": 25,
  "mostRecentTransaction": "2024-01-15T10:30:00Z"
}
```

### Option B: Simple Interest Calculation
Add endpoint: GET /accounts/:accountId/interest?rate=0.05&days=30

Calculate simple interest: balance × rate × (days/365)

### Option C: Transaction Export
Add endpoint: GET /transactions/export?format=csv

Export all transactions (or filtered transactions) as CSV format.

### Option D: Rate Limiting
Implement basic rate limiting:
- Maximum 100 requests per minute per IP address
- Return 429 Too Many Requests when exceeded
- Include Retry-After header

## Technical Requirements
- Use **in-memory storage** (array or object) - no database needed
- Return appropriate HTTP status codes:
  - 200 for successful GET
  - 201 for successful POST
  - 400 for validation errors
  - 404 for not found
  - 429 for rate limit exceeded (if implementing Option D)
- Include error handling for all endpoints
- Use proper RESTful conventions

## Project Structure
Organize code into folders:
```
src/
├── index.js (or app.js)        # Main application entry point
├── routes/
│   └── transactions.js         # Route handlers
├── models/
│   └── transaction.js          # Transaction model/schema
├── validators/
│   └── transactionValidator.js # Validation logic
└── utils/
    └── helpers.js              # Helper functions
```

## Deliverables Needed
1. Complete working source code
2. package.json with all dependencies (or equivalent for other languages)
3. README.md explaining the implementation
4. HOWTORUN.md with setup and run instructions

## Implementation Steps
Please help me:
1. Set up the project structure
2. Initialize the project with necessary dependencies
3. Implement all 4 core endpoints
4. Add all validation logic
5. Implement filtering for GET /transactions
6. Add at least one additional feature from Task 4
7. Include comprehensive error handling
8. Add helpful code comments
9. Create a .gitignore file
10. Provide sample test commands (curl or similar)

Please build this step by step, explaining each part as we go.

### PROMPT END

---

## Alternative Prompts for Specific Tasks

### For Initial Setup
```
I need to create a Node.js Express API project for banking transactions.
Please help me:
1. Initialize a new Node.js project
2. Install Express and necessary dependencies
3. Create the basic folder structure: src/routes, src/models, src/validators, src/utils
4. Set up a basic Express server on port 3000
5. Create a .gitignore file for Node.js projects
```

### For Transaction Model
```
Create a Transaction model/schema for a banking API with these fields:
- id: auto-generated UUID
- fromAccount: string (format: ACC-XXXXX)
- toAccount: string (format: ACC-XXXXX)
- amount: number (positive, max 2 decimal places)
- currency: string (ISO 4217 codes: USD, EUR, GBP, etc.)
- type: enum (deposit, withdrawal, transfer)
- timestamp: ISO 8601 datetime
- status: enum (pending, completed, failed)

Include validation functions for:
- Account format validation
- Amount validation (positive, 2 decimals)
- Currency validation (ISO 4217)
```

### For Core Endpoints
```
Implement the following endpoints for a banking transactions API using Express:

1. POST /transactions - Create a new transaction
   - Validate all fields
   - Generate ID and timestamp
   - Store in in-memory array
   - Return 201 with created transaction

2. GET /transactions - List all transactions
   - Support filtering by accountId, type, and date range (from, to)
   - Return 200 with array of transactions

3. GET /transactions/:id - Get specific transaction
   - Return 200 if found
   - Return 404 if not found

4. GET /accounts/:accountId/balance - Calculate balance
   - Sum all deposits and incoming transfers
   - Subtract all withdrawals and outgoing transfers
   - Return 200 with balance object

Use proper error handling and HTTP status codes.
```

### For Validation Logic
```
Create a comprehensive validation module for a banking transactions API.

Validators needed:
1. validateAmount(amount)
   - Must be positive
   - Max 2 decimal places
   - Return error message if invalid

2. validateAccountFormat(account)
   - Must match pattern ACC-XXXXX (X = alphanumeric)
   - Return error message if invalid

3. validateCurrency(currency)
   - Must be one of: USD, EUR, GBP, JPY, CHF, AUD, CAD
   - Return error message if invalid

4. validateTransaction(transactionData)
   - Validate all fields
   - Return array of validation errors
   - Format: [{ field: "amount", message: "..." }]
```

### For Filtering Logic
```
Add filtering functionality to GET /transactions endpoint.

Support these query parameters:
- accountId: Filter transactions where fromAccount OR toAccount matches
- type: Filter by transaction type (deposit, withdrawal, transfer)
- from: Filter transactions after this date (ISO 8601)
- to: Filter transactions before this date (ISO 8601)

Allow combining multiple filters (AND logic).
Return filtered array of transactions.
```

### For Additional Features

#### Summary Endpoint
```
Add a transaction summary endpoint: GET /accounts/:accountId/summary

Calculate and return:
- totalDeposits: sum of all deposits and incoming transfers
- totalWithdrawals: sum of all withdrawals and outgoing transfers
- transactionCount: total number of transactions for this account
- mostRecentTransaction: timestamp of latest transaction

Return 404 if no transactions found for account.
```

#### Interest Calculation
```
Add an interest calculation endpoint: GET /accounts/:accountId/interest

Query parameters:
- rate: annual interest rate (decimal, e.g., 0.05 for 5%)
- days: number of days to calculate interest for

Calculate simple interest: currentBalance × rate × (days/365)

Return:
{
  "accountId": "ACC-12345",
  "currentBalance": 1000.00,
  "interestRate": 0.05,
  "days": 30,
  "interestEarned": 4.11,
  "newBalance": 1004.11
}
```

#### CSV Export
```
Add a transaction export endpoint: GET /transactions/export?format=csv

Convert all transactions (or filtered transactions) to CSV format.

CSV columns: id,fromAccount,toAccount,amount,currency,type,timestamp,status

Support the same filtering parameters as GET /transactions.

Return with headers:
- Content-Type: text/csv
- Content-Disposition: attachment; filename=transactions.csv
```

#### Rate Limiting
```
Implement rate limiting middleware for the API:
- Maximum 100 requests per minute per IP address
- Track request counts in memory (Map with IP as key)
- Reset counts every minute
- Return 429 Too Many Requests when limit exceeded
- Include these headers:
  - X-RateLimit-Limit: 100
  - X-RateLimit-Remaining: (remaining requests)
  - Retry-After: (seconds until reset)
```

### For Documentation
```
Create comprehensive documentation for the Banking Transactions API:

README.md should include:
- Project overview
- Features implemented
- Technology stack used
- API endpoints with examples
- Data model/schema
- Validation rules
- AI tools used and how they helped

HOWTORUN.md should include:
- Prerequisites (Node.js version, etc.)
- Installation steps
- How to start the server
- Example API calls using curl
- How to test all endpoints
```

### For Testing
```
Provide comprehensive test commands for the Banking Transactions API:

Create sample curl commands (or similar) to test:
1. Creating a valid transaction
2. Creating a transaction with validation errors
3. Listing all transactions
4. Filtering transactions by account
5. Filtering transactions by type
6. Filtering transactions by date range
7. Getting a specific transaction by ID
8. Getting a non-existent transaction (404 test)
9. Getting account balance
10. Testing the additional feature (summary/interest/export/rate-limit)

Include expected responses for each test.
```

---

## Prompt Strategies for Different AI Tools

### For Claude Code
- Start with high-level architecture discussion
- Ask for explanation of design decisions
- Request code with detailed comments
- Use iterative refinement approach

### For GitHub Copilot
- Write clear function signatures first
- Use descriptive variable names
- Add inline comments to guide generation
- Review and modify generated code

### For ChatGPT/Claude Web
- Provide complete context in one prompt
- Ask for code blocks that can be copied
- Request explanations alongside code
- Follow up with refinement questions

---

## Example Conversation Flow with AI

**You:** [Use Master Prompt above]

**AI:** [Provides initial setup and structure]

**You:** "Great! Now implement the POST /transactions endpoint with full validation."

**AI:** [Provides endpoint code]

**You:** "Add more detailed error messages for each validation failure."

**AI:** [Refines validation]

**You:** "Now implement the GET /transactions endpoint with filtering support."

**AI:** [Provides filtering code]

**You:** "Excellent! Now add the transaction summary endpoint from Task 4 Option A."

**AI:** [Provides summary endpoint]

**You:** "Can you create sample curl commands to test all the endpoints?"

**AI:** [Provides test commands]

**You:** "Finally, help me write the README.md and HOWTORUN.md files."

**AI:** [Provides documentation]

---

## Tips for Using These Prompts

1. **Be Specific:** The more detailed your prompt, the better the output
2. **Iterate:** Don't expect perfection on first try - refine and improve
3. **Test:** Always test the generated code before accepting it
4. **Understand:** Read and understand the code, don't just copy-paste
5. **Document:** Screenshot your prompts and AI responses for the homework submission
6. **Compare:** Try the same prompt with different AI tools and compare results

---

**Ready to Start?** Copy the Master Prompt above and paste it into your preferred AI coding assistant!
