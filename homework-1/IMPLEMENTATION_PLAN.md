# Implementation Plan: Banking Transactions API

**Assignment:** Homework 1 - Simple Banking Transactions API
**Estimated Time:** 4-5 hours
**Actual Time:** ~4 hours
**AI Tools Used:** Claude Code (Primary)
**Status:** ✅ IMPLEMENTATION COMPLETE - Ready for Screenshots & Submission

---

## Pre-Implementation Checklist

Before starting, ensure you have:

- [x] Reviewed PROJECT_ANALYSIS.md in repository root
- [x] Reviewed TASKS.md for complete requirements
- [x] Reviewed IMPLEMENTATION_PROMPT.md for AI prompts
- [x] Chosen technology stack: **Node.js + Express**
- [x] Development environment set up (Node.js v22.16.0, npm 10.9.2)
- [x] AI tools ready: **Claude Code** (Primary)
- [x] Screenshot tool ready (for documenting AI interactions)
- [x] API testing tool ready (curl, jq, VS Code REST Client)
- [x] Dedicated implementation time completed

---

## Technology Stack Decision

### ✅ SELECTED: Node.js + Express

**Chosen because:**
- ✅ Fastest setup time
- ✅ Comfortable with JavaScript
- ✅ Minimal boilerplate
- ✅ Excellent AI tool support

**Installed versions:**
```bash
Node.js: v22.16.0 ✅
npm: 10.9.2 ✅
```

### Alternative: Python + FastAPI

**Choose this if:**
- You prefer Python
- You want automatic API documentation
- You like type hints and validation
- You want to learn modern Python web framework

**Required installations:**
```bash
# Check Python installation
python3 --version  # Should be 3.8 or higher
pip3 --version

# If not installed, download from python.org
```

### Alternative: Java + Spring Boot

**Choose this if:**
- You prefer Java
- You want enterprise-grade code
- You're comfortable with more verbose code
- You have time for additional setup

**Required installations:**
```bash
# Check Java installation
java --version  # Should be 11 or higher
mvn --version   # Maven for dependency management
```

---

## Phase 1: Project Setup (15-20 minutes)

### Step 1.1: Initialize Project

**For Node.js:**
```bash
cd /Users/a.kosholap/IdeaProjects/AI-Coding-Partner-Homework/homework-1/src
npm init -y
npm install express
npm install --save-dev nodemon
```

**For Python:**
```bash
cd /Users/a.kosholap/IdeaProjects/AI-Coding-Partner-Homework/homework-1/src
python3 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install fastapi uvicorn
pip freeze > requirements.txt
```

**For Java:**
```bash
# Use Spring Initializr: https://start.spring.io/
# Or use AI tool to generate initial project structure
```

**AI Prompt to Use:**
```
I need to set up a [Node.js/Python/Java] project for a banking transactions API.

Requirements:
- Framework: [Express/FastAPI/Spring Boot]
- Project structure with folders: routes, models, validators, utils
- Basic server setup on port 3000
- .gitignore file for [Node.js/Python/Java]
- Package configuration file (package.json/requirements.txt/pom.xml)

Please provide:
1. Installation commands
2. Folder structure
3. Basic server file
4. .gitignore content
```

**Screenshot:** Take screenshot of this AI interaction and save as `docs/screenshots/01-project-setup.png`

**Checkpoint:**
- [x] Project initialized
- [x] Dependencies installed (express, nodemon)
- [x] Folder structure created (routes, models, validators, utils)
- [x] Basic server file exists (index.js)
- [x] .gitignore created
- [ ] Screenshot saved: `docs/screenshots/01-project-setup.png` ⚠️ TODO

### Step 1.2: Create Folder Structure

```bash
cd /Users/a.kosholap/IdeaProjects/AI-Coding-Partner-Homework/homework-1/src

# Create folders
mkdir -p routes models validators utils

# For demo files
cd ..
mkdir -p demo
```

**Expected structure:**
```
homework-1/
├── src/
│   ├── index.js (or app.py or Application.java)
│   ├── package.json (or requirements.txt or pom.xml)
│   ├── routes/
│   ├── models/
│   ├── validators/
│   └── utils/
├── demo/
├── docs/
│   └── screenshots/
├── README.md
├── HOWTORUN.md
└── TASKS.md
```

**Checkpoint:**
- [ ] All folders created
- [ ] Basic server runs without errors
- [ ] Can access http://localhost:3000 (or configured port)

---

## Phase 2: Data Model & In-Memory Storage (20-30 minutes)

### Step 2.1: Create Transaction Model

**AI Prompt to Use:**
```
Create a Transaction model for a banking API with these fields:

- id: auto-generated UUID
- fromAccount: string (format ACC-XXXXX)
- toAccount: string (format ACC-XXXXX)
- amount: number (positive, max 2 decimal places)
- currency: string (ISO 4217: USD, EUR, GBP, JPY, CHF, AUD, CAD)
- type: enum (deposit, withdrawal, transfer)
- timestamp: ISO 8601 datetime
- status: enum (pending, completed, failed)

Please create:
1. Model/schema definition
2. Function to create new transaction with auto-generated ID and timestamp
3. In-memory storage (array) for transactions
4. Helper functions to find/filter transactions
```

**Files to create:**
- `src/models/transaction.js` (or .py or .java)

**Screenshot:** Save as `docs/screenshots/02-transaction-model.png`

**Checkpoint:**
- [ ] Transaction model created
- [ ] In-memory storage initialized
- [ ] Helper functions implemented
- [ ] Can create and store a transaction
- [ ] Screenshot saved

### Step 2.2: Create Sample Data (Optional but Recommended)

**AI Prompt:**
```
Generate sample transaction data for testing:
- 10-15 transactions
- Various account numbers (ACC-12345, ACC-67890, ACC-11111, etc.)
- Different transaction types (deposit, withdrawal, transfer)
- Different currencies (USD, EUR, GBP)
- Date range from January 2024 to current date
- All with "completed" status

Format as JSON array.
```

**Save to:** `demo/sample-data.json`

**Checkpoint:**
- [ ] Sample data created
- [ ] Data is valid according to model

---

## Phase 3: Validation Logic (25-35 minutes)

### Step 3.1: Create Validation Functions

**AI Prompt to Use:**
```
Create comprehensive validation module for banking transactions API.

Validation functions needed:

1. validateAmount(amount)
   - Must be positive number
   - Maximum 2 decimal places
   - Return error object if invalid: {field: "amount", message: "..."}

2. validateAccountFormat(account)
   - Must match pattern: ACC-XXXXX where X is alphanumeric
   - Return error object if invalid: {field: "fromAccount/toAccount", message: "..."}

3. validateCurrency(currency)
   - Must be one of: USD, EUR, GBP, JPY, CHF, AUD, CAD
   - Case insensitive
   - Return error object if invalid: {field: "currency", message: "..."}

4. validateTransactionType(type)
   - Must be one of: deposit, withdrawal, transfer
   - Return error object if invalid: {field: "type", message: "..."}

5. validateTransaction(transactionData)
   - Use all above validators
   - Return array of all validation errors
   - Return empty array if valid

Include unit test examples for each validator.
```

**Files to create:**
- `src/validators/transactionValidator.js` (or .py or .java)

**Screenshot:** Save as `docs/screenshots/03-validation-logic.png`

**Checkpoint:**
- [ ] All validation functions created
- [ ] Test each validator manually
- [ ] Validation returns proper error format
- [ ] Screenshot saved

**Test validation manually:**
```javascript
// Test in Node.js REPL or create test file
const validator = require('./validators/transactionValidator');

// Should return errors
console.log(validator.validateAmount(-10));
console.log(validator.validateAmount(10.999));
console.log(validator.validateAccountFormat("INVALID"));
console.log(validator.validateCurrency("XXX"));

// Should return no errors
console.log(validator.validateAmount(100.50));
console.log(validator.validateAccountFormat("ACC-12345"));
console.log(validator.validateCurrency("USD"));
```

---

## Phase 4: Core API Endpoints (45-60 minutes)

### Step 4.1: POST /transactions (Create Transaction)

**AI Prompt:**
```
Implement POST /transactions endpoint for banking API.

Requirements:
- Accept JSON body with: fromAccount, toAccount, amount, currency, type
- Validate all fields using validation functions from validators/transactionValidator
- If validation fails, return 400 with error response:
  {
    "error": "Validation failed",
    "details": [array of field errors]
  }
- If valid:
  - Generate unique ID (UUID)
  - Set timestamp (ISO 8601 format)
  - Set status to "completed" by default
  - Store in in-memory array
  - Return 201 with created transaction

Include example curl command to test.
```

**Files to create/modify:**
- `src/routes/transactions.js` (or .py or .java)
- Update `src/index.js` to use the routes

**Screenshot:** Save as `docs/screenshots/04-post-endpoint.png`

**Checkpoint:**
- [ ] POST endpoint implemented
- [ ] Validation working
- [ ] Returns 201 on success
- [ ] Returns 400 on validation error
- [ ] Screenshot saved

**Test:**
```bash
# Valid request
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "transfer"
  }'

# Invalid request (should return 400)
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "INVALID",
    "toAccount": "ACC-67890",
    "amount": -50,
    "currency": "XXX",
    "type": "invalid"
  }'
```

### Step 4.2: GET /transactions (List All Transactions)

**AI Prompt:**
```
Implement GET /transactions endpoint for banking API.

Requirements:
- Return all transactions from in-memory storage
- Return 200 with array of transactions
- If no transactions exist, return empty array
- Later we'll add filtering (next step)

Include example curl command to test.
```

**Screenshot:** Save as `docs/screenshots/05-get-all-endpoint.png`

**Checkpoint:**
- [ ] GET all endpoint implemented
- [ ] Returns 200 with array
- [ ] Screenshot saved

**Test:**
```bash
curl http://localhost:3000/transactions
```

### Step 4.3: GET /transactions/:id (Get Specific Transaction)

**AI Prompt:**
```
Implement GET /transactions/:id endpoint for banking API.

Requirements:
- Accept transaction ID as URL parameter
- Search in-memory storage for transaction with matching ID
- If found: return 200 with transaction object
- If not found: return 404 with error message:
  {
    "error": "Transaction not found",
    "id": "<requested-id>"
  }

Include example curl commands to test both success and 404 cases.
```

**Screenshot:** Save as `docs/screenshots/06-get-by-id-endpoint.png`

**Checkpoint:**
- [ ] GET by ID endpoint implemented
- [ ] Returns 200 when found
- [ ] Returns 404 when not found
- [ ] Screenshot saved

**Test:**
```bash
# Get existing transaction (use ID from previous POST)
curl http://localhost:3000/transactions/<some-valid-id>

# Get non-existent transaction
curl http://localhost:3000/transactions/nonexistent-id
```

### Step 4.4: GET /accounts/:accountId/balance (Get Account Balance)

**AI Prompt:**
```
Implement GET /accounts/:accountId/balance endpoint for banking API.

Requirements:
- Accept account ID as URL parameter (format: ACC-XXXXX)
- Calculate balance based on all transactions:
  - For deposits: add amount if toAccount matches
  - For withdrawals: subtract amount if fromAccount matches
  - For transfers: add if toAccount matches, subtract if fromAccount matches
- Return 200 with balance object:
  {
    "accountId": "ACC-12345",
    "balance": 1500.00,
    "currency": "USD" (or "mixed" if multiple currencies)
  }
- If account has no transactions, return balance of 0

Include:
1. Balance calculation logic
2. Example curl command
3. Test scenarios with multiple transactions
```

**Screenshot:** Save as `docs/screenshots/07-balance-endpoint.png`

**Checkpoint:**
- [ ] Balance endpoint implemented
- [ ] Calculation logic correct
- [ ] Returns 200 with balance
- [ ] Screenshot saved

**Test:**
```bash
# First create some transactions for an account
curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{"fromAccount": "ACC-99999", "toAccount": "ACC-12345", "amount": 500, "currency": "USD", "type": "transfer"}'

curl -X POST http://localhost:3000/transactions \
  -H "Content-Type: application/json" \
  -d '{"fromAccount": "ACC-12345", "toAccount": "ACC-99999", "amount": 100, "currency": "USD", "type": "transfer"}'

# Check balance (should be 400)
curl http://localhost:3000/accounts/ACC-12345/balance
```

**Checkpoint for All Core Endpoints:**
- [ ] All 4 endpoints working
- [ ] Proper HTTP status codes
- [ ] Error handling in place
- [ ] Manual testing completed
- [ ] Screenshot showing all endpoints working: `docs/screenshots/08-all-endpoints-working.png`

---

## Phase 5: Transaction Filtering (20-30 minutes)

### Step 5.1: Add Filtering to GET /transactions

**AI Prompt:**
```
Add filtering functionality to GET /transactions endpoint.

Support these query parameters:
1. accountId - Filter transactions where fromAccount OR toAccount matches
2. type - Filter by transaction type (deposit, withdrawal, transfer)
3. from - Filter transactions after this date (ISO 8601 format)
4. to - Filter transactions before this date (ISO 8601 format)

Requirements:
- Support combining multiple filters (AND logic)
- If no filters provided, return all transactions
- Filters are case-insensitive where applicable
- Date filtering should work with ISO 8601 dates

Examples:
- ?accountId=ACC-12345
- ?type=transfer
- ?from=2024-01-01&to=2024-01-31
- ?accountId=ACC-12345&type=transfer&from=2024-01-01

Include curl commands to test each filter and combinations.
```

**Screenshot:** Save as `docs/screenshots/09-filtering-logic.png`

**Checkpoint:**
- [ ] Filter by accountId works
- [ ] Filter by type works
- [ ] Filter by date range works
- [ ] Combined filters work
- [ ] Screenshot saved

**Test:**
```bash
# Filter by account
curl "http://localhost:3000/transactions?accountId=ACC-12345"

# Filter by type
curl "http://localhost:3000/transactions?type=transfer"

# Filter by date range
curl "http://localhost:3000/transactions?from=2024-01-01&to=2024-12-31"

# Combined filters
curl "http://localhost:3000/transactions?accountId=ACC-12345&type=transfer"
```

---

## Phase 6: Additional Feature - Task 4 (30-45 minutes)

**Choose ONE of the following options (or implement multiple for bonus points):**

### Option A: Transaction Summary Endpoint

**AI Prompt:**
```
Implement GET /accounts/:accountId/summary endpoint.

Calculate and return:
- totalDeposits: sum of all deposits and incoming transfers to this account
- totalWithdrawals: sum of all withdrawals and outgoing transfers from this account
- transactionCount: total number of transactions involving this account
- mostRecentTransaction: timestamp of most recent transaction

Return format:
{
  "accountId": "ACC-12345",
  "totalDeposits": 1500.00,
  "totalWithdrawals": 500.00,
  "netAmount": 1000.00,
  "transactionCount": 25,
  "mostRecentTransaction": "2024-01-15T10:30:00Z"
}

Return 404 if account has no transactions.
Include curl test commands.
```

**Screenshot:** Save as `docs/screenshots/10-summary-endpoint.png`

### Option B: Simple Interest Calculation

**AI Prompt:**
```
Implement GET /accounts/:accountId/interest endpoint.

Query parameters:
- rate: annual interest rate (decimal, e.g., 0.05 for 5%)
- days: number of days to calculate interest for

Calculate simple interest: currentBalance × rate × (days/365)

Return format:
{
  "accountId": "ACC-12345",
  "currentBalance": 1000.00,
  "interestRate": 0.05,
  "days": 30,
  "interestEarned": 4.11,
  "newBalance": 1004.11
}

Validate that rate is between 0 and 1, days is positive.
Include curl test commands.
```

**Screenshot:** Save as `docs/screenshots/10-interest-endpoint.png`

### Option C: Transaction Export (CSV)

**AI Prompt:**
```
Implement GET /transactions/export endpoint with CSV export functionality.

Requirements:
- Query parameter: format=csv
- Export all transactions (or filtered transactions using existing filters)
- CSV columns: id,fromAccount,toAccount,amount,currency,type,timestamp,status
- Proper CSV headers
- Return with headers:
  - Content-Type: text/csv
  - Content-Disposition: attachment; filename=transactions.csv

Support combining with existing filters:
- /transactions/export?format=csv&accountId=ACC-12345

Include curl command to test and save to file.
```

**Screenshot:** Save as `docs/screenshots/10-export-endpoint.png`

### Option D: Rate Limiting

**AI Prompt:**
```
Implement rate limiting middleware for the API.

Requirements:
- Maximum 100 requests per minute per IP address
- Track requests in-memory using Map with IP as key
- Reset counters every minute
- When limit exceeded, return 429 Too Many Requests
- Include headers in all responses:
  - X-RateLimit-Limit: 100
  - X-RateLimit-Remaining: <remaining count>
  - X-RateLimit-Reset: <timestamp when counter resets>
- When rate limited, add: Retry-After: <seconds>

Apply middleware to all routes.
Include test script to trigger rate limit.
```

**Screenshot:** Save as `docs/screenshots/10-rate-limiting.png`

**Checkpoint for Additional Feature:**
- [ ] Additional feature implemented
- [ ] Feature tested and working
- [ ] Screenshot saved

---

## Phase 7: Create Demo Files (15-20 minutes)

### Step 7.1: Create Run Script

**For Node.js - demo/run.sh:**
```bash
#!/bin/bash
echo "Starting Banking Transactions API..."
cd src
npm install
node index.js
```

**For Python - demo/run.sh:**
```bash
#!/bin/bash
echo "Starting Banking Transactions API..."
cd src
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python app.py
```

Make executable:
```bash
chmod +x demo/run.sh
```

**Checkpoint:**
- [ ] Run script created
- [ ] Script is executable
- [ ] Script successfully starts server

### Step 7.2: Create Sample Requests File

**Create demo/sample-requests.http:**
```http
### Create a transfer transaction
POST http://localhost:3000/transactions
Content-Type: application/json

{
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer"
}

### Create a deposit transaction
POST http://localhost:3000/transactions
Content-Type: application/json

{
  "fromAccount": "ACC-00000",
  "toAccount": "ACC-12345",
  "amount": 500.00,
  "currency": "USD",
  "type": "deposit"
}

### Get all transactions
GET http://localhost:3000/transactions

### Get transactions for specific account
GET http://localhost:3000/transactions?accountId=ACC-12345

### Get transactions by type
GET http://localhost:3000/transactions?type=transfer

### Get transaction by ID (replace with actual ID)
GET http://localhost:3000/transactions/REPLACE-WITH-ACTUAL-ID

### Get account balance
GET http://localhost:3000/accounts/ACC-12345/balance

### Get account summary (if implemented)
GET http://localhost:3000/accounts/ACC-12345/summary

### Calculate interest (if implemented)
GET http://localhost:3000/accounts/ACC-12345/interest?rate=0.05&days=30

### Export transactions as CSV (if implemented)
GET http://localhost:3000/transactions/export?format=csv

### Test validation - Invalid amount
POST http://localhost:3000/transactions
Content-Type: application/json

{
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": -50,
  "currency": "USD",
  "type": "transfer"
}

### Test validation - Invalid account format
POST http://localhost:3000/transactions
Content-Type: application/json

{
  "fromAccount": "INVALID",
  "toAccount": "ACC-67890",
  "amount": 100,
  "currency": "USD",
  "type": "transfer"
}

### Test validation - Invalid currency
POST http://localhost:3000/transactions
Content-Type: application/json

{
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100,
  "currency": "XXX",
  "type": "transfer"
}
```

**Also create demo/sample-requests.sh (alternative for command line):**
```bash
#!/bin/bash

API_URL="http://localhost:3000"

echo "=== Testing Banking Transactions API ==="
echo

echo "1. Creating transfer transaction..."
curl -X POST $API_URL/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "transfer"
  }'
echo -e "\n"

echo "2. Creating deposit transaction..."
curl -X POST $API_URL/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-00000",
    "toAccount": "ACC-12345",
    "amount": 500.00,
    "currency": "USD",
    "type": "deposit"
  }'
echo -e "\n"

echo "3. Getting all transactions..."
curl $API_URL/transactions
echo -e "\n"

echo "4. Getting account balance..."
curl $API_URL/accounts/ACC-12345/balance
echo -e "\n"

echo "5. Testing validation (should fail)..."
curl -X POST $API_URL/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "INVALID",
    "toAccount": "ACC-67890",
    "amount": -50,
    "currency": "XXX",
    "type": "invalid"
  }'
echo -e "\n"

echo "=== Testing complete ==="
```

Make executable:
```bash
chmod +x demo/sample-requests.sh
```

**Checkpoint:**
- [ ] sample-requests.http created
- [ ] sample-requests.sh created and executable
- [ ] Both test files work correctly

---

## Phase 8: Documentation (30-45 minutes)

### Step 8.1: Complete README.md

**AI Prompt:**
```
Help me write a comprehensive README.md for my Banking Transactions API homework.

Include:
1. Project title and overview
2. Features implemented (list all endpoints)
3. Technology stack used
4. API endpoints documentation with:
   - HTTP method and path
   - Request format
   - Response format
   - Example requests/responses
5. Data model (Transaction schema)
6. Validation rules
7. AI tools used and how they helped:
   - Which AI tools (Claude Code, GitHub Copilot, etc.)
   - What tasks each tool helped with
   - Examples of good prompts that worked
   - Challenges encountered and how AI helped solve them
8. Project structure
9. Link to HOWTORUN.md for setup instructions

Format in clear markdown with proper sections and examples.
```

**Fill in template in homework-1/README.md**

**Key sections to include:**
```markdown
# Banking Transactions API

> **Student Name**: [Your Name]
> **Date Submitted**: [Date]
> **AI Tools Used**: Claude Code, GitHub Copilot

## Project Overview
[Brief description of what you built]

## Features Implemented
- ✅ Create transactions (POST /transactions)
- ✅ List all transactions (GET /transactions)
- ✅ Get specific transaction (GET /transactions/:id)
- ✅ Get account balance (GET /accounts/:accountId/balance)
- ✅ Transaction filtering (by account, type, date range)
- ✅ [Your Task 4 feature]

## Technology Stack
- [Your stack: Node.js + Express / Python + FastAPI / Java + Spring Boot]
- In-memory storage (no database)

## API Documentation
[Document each endpoint]

## AI Tools Used

### Claude Code
- Used for: [architecture design, complex logic, etc.]
- Helpful prompts: [examples]
- Challenges solved: [examples]

### GitHub Copilot
- Used for: [inline completions, boilerplate, etc.]
- Most useful for: [examples]

### Comparison
[Compare how different AI tools approached the same problems]

## Project Structure
[Show folder structure]

## Setup and Running
See [HOWTORUN.md](HOWTORUN.md) for detailed instructions.
```

**Checkpoint:**
- [ ] README.md completed
- [ ] All sections filled in
- [ ] AI usage documented with examples
- [ ] Clear and professional

### Step 8.2: Complete HOWTORUN.md

**Content template:**
```markdown
# How to Run the Banking Transactions API

## Prerequisites
- [Node.js 14+ / Python 3.8+ / Java 11+]
- [npm / pip / maven]
- Terminal/Command Prompt
- [Optional] API testing tool (Postman, curl, or VS Code REST Client)

## Installation Steps

### Step 1: Clone or Navigate to Project
```bash
cd /path/to/homework-1
```

### Step 2: Install Dependencies
[Your specific commands]

### Step 3: Start the Server
[Your specific commands]

### Step 4: Verify Server is Running
Open browser or curl:
```bash
curl http://localhost:3000/transactions
```

You should see an empty array: `[]`

## Testing the API

### Option 1: Using the Test Script
```bash
./demo/sample-requests.sh
```

### Option 2: Using VS Code REST Client
Open `demo/sample-requests.http` in VS Code and click "Send Request"

### Option 3: Manual curl Commands
[Provide examples]

## Sample Test Workflow

1. Create a transaction:
[example command]

2. Verify it was created:
[example command]

3. Check account balance:
[example command]

4. Test filtering:
[example command]

## Troubleshooting

### Port Already in Use
[Solution]

### Dependencies Not Installing
[Solution]

### Validation Errors
[Common validation issues]

## Stopping the Server
Press `Ctrl+C` in the terminal where the server is running.
```

**Checkpoint:**
- [ ] HOWTORUN.md completed
- [ ] Instructions are clear and step-by-step
- [ ] Tested by following own instructions
- [ ] Troubleshooting section included

### Step 8.3: Add Code Comments

**Review all code files and ensure:**
- [ ] Every function has a descriptive comment
- [ ] Complex logic is explained
- [ ] Validation rules are documented
- [ ] API routes have description comments

**Example:**
```javascript
/**
 * Validates transaction amount
 * @param {number} amount - The transaction amount
 * @returns {object|null} Error object if invalid, null if valid
 *
 * Rules:
 * - Must be a positive number
 * - Maximum 2 decimal places
 */
function validateAmount(amount) {
  // Implementation
}
```

---

## Phase 9: Screenshots and Evidence (15-20 minutes)

### Step 9.1: Organize AI Interaction Screenshots

**Required screenshots (minimum):**
1. `01-project-setup.png` - Initial project setup with AI
2. `02-transaction-model.png` - Creating transaction model
3. `03-validation-logic.png` - Implementing validation
4. `04-post-endpoint.png` - Creating POST endpoint
5. `05-get-all-endpoint.png` - Creating GET all endpoint
6. `06-get-by-id-endpoint.png` - Creating GET by ID endpoint
7. `07-balance-endpoint.png` - Creating balance calculation
8. `09-filtering-logic.png` - Implementing filtering
9. `10-[feature]-endpoint.png` - Task 4 additional feature
10. `11-ai-comparison.png` - Comparing different AI tools (optional but recommended)

**Ensure each screenshot shows:**
- Your prompt/question to the AI
- The AI's response/generated code
- Enough context to understand the interaction

**Checkpoint:**
- [ ] At least 5-7 AI interaction screenshots
- [ ] Screenshots are clear and readable
- [ ] Screenshots show different AI tools (minimum 2)

### Step 9.2: Create API Working Screenshots

**Required screenshots:**
1. `api-running.png` - Server running in terminal
2. `api-create-transaction.png` - Successful POST request
3. `api-get-transactions.png` - GET all transactions
4. `api-balance.png` - GET balance endpoint
5. `api-filtering.png` - GET with filters
6. `api-validation-error.png` - Validation error response
7. `api-task4-feature.png` - Your Task 4 feature working

**Tools to use:**
- Postman (screenshot the request and response)
- VS Code REST Client (screenshot)
- Terminal with curl (screenshot)
- Browser (for GET requests)

**Checkpoint:**
- [ ] At least 5 API working screenshots
- [ ] Screenshots show both success and error cases
- [ ] All major features are demonstrated

---

## Phase 10: Testing & Quality Assurance (20-30 minutes)

### Step 10.1: Comprehensive Testing Checklist

**POST /transactions:**
- [ ] Valid transaction creates successfully (201)
- [ ] Invalid amount returns validation error (400)
- [ ] Invalid account format returns error (400)
- [ ] Invalid currency returns error (400)
- [ ] Invalid type returns error (400)
- [ ] Multiple validation errors returned together (400)
- [ ] Missing required fields returns error (400)

**GET /transactions:**
- [ ] Returns empty array when no transactions exist
- [ ] Returns all transactions after creating some
- [ ] Filter by accountId works correctly
- [ ] Filter by type works correctly
- [ ] Filter by date range works correctly
- [ ] Combined filters work correctly
- [ ] Invalid date format handled gracefully

**GET /transactions/:id:**
- [ ] Returns transaction when ID exists (200)
- [ ] Returns 404 when ID doesn't exist
- [ ] ID validation works

**GET /accounts/:accountId/balance:**
- [ ] Calculates balance correctly for deposits
- [ ] Calculates balance correctly for withdrawals
- [ ] Calculates balance correctly for transfers
- [ ] Handles account with no transactions
- [ ] Multiple transactions calculated correctly
- [ ] Mixed transaction types calculated correctly

**Task 4 Feature:**
- [ ] Feature works as specified
- [ ] Returns correct HTTP status codes
- [ ] Handles edge cases
- [ ] Error handling in place

**General:**
- [ ] Server starts without errors
- [ ] All dependencies installed
- [ ] No console errors during operation
- [ ] Proper error messages returned
- [ ] HTTP status codes are correct

### Step 10.2: Code Quality Review

**Review checklist:**
- [ ] Code is well-organized in folders
- [ ] No sensitive data (passwords, keys, etc.)
- [ ] .gitignore includes node_modules/, venv/, .env, etc.
- [ ] No commented-out code (or explained why)
- [ ] Consistent code style
- [ ] Functions are reasonably sized
- [ ] Variable names are descriptive
- [ ] No duplicate code
- [ ] Error handling is comprehensive
- [ ] Code comments are helpful

### Step 10.3: Documentation Review

**Review checklist:**
- [ ] README.md is complete and professional
- [ ] HOWTORUN.md has clear instructions
- [ ] All screenshots are present and clear
- [ ] AI usage is well-documented
- [ ] Demo files are present and working
- [ ] All required files are included

---

## Phase 11: Submission Preparation (15-20 minutes)

### Step 11.1: Final File Structure Verification

**Verify structure:**
```
homework-1/
├── README.md                          ✓ Complete
├── HOWTORUN.md                        ✓ Complete
├── TASKS.md                           ✓ Original requirements
├── IMPLEMENTATION_PLAN.md             ✓ This file
├── IMPLEMENTATION_PROMPT.md           ✓ AI prompts
├── CONVERSATION_LOG.md                ✓ Session log
├── src/
│   ├── index.js (or app.py)          ✓ Main server
│   ├── package.json (or requirements.txt) ✓ Dependencies
│   ├── .gitignore                     ✓ Proper ignores
│   ├── routes/
│   │   └── transactions.js            ✓ All routes
│   ├── models/
│   │   └── transaction.js             ✓ Transaction model
│   ├── validators/
│   │   └── transactionValidator.js    ✓ All validators
│   └── utils/
│       └── helpers.js                 ✓ Helper functions
├── docs/
│   └── screenshots/
│       ├── 01-project-setup.png       ✓ At least 5-7
│       ├── 02-transaction-model.png
│       ├── [...]
│       ├── api-running.png            ✓ Working API
│       └── [...]                      ✓ More screenshots
└── demo/
    ├── run.sh                         ✓ Executable
    ├── sample-requests.http           ✓ Test requests
    ├── sample-requests.sh             ✓ Alternative
    └── sample-data.json               ✓ Sample data
```

**Run checklist:**
```bash
# Check .gitignore is working
git status

# Verify no sensitive data
grep -r "password\|secret\|key" src/

# Verify scripts are executable
ls -l demo/*.sh

# Test from scratch
# (In new terminal, from homework-1 directory)
./demo/run.sh
# (In another terminal)
./demo/sample-requests.sh
```

**Checkpoint:**
- [ ] All files present
- [ ] .gitignore working
- [ ] No sensitive data committed
- [ ] Scripts are executable
- [ ] Fresh run works from scratch

### Step 11.2: Git Commit and Branch

```bash
# Make sure you're in the repository root
cd /Users/a.kosholap/IdeaProjects/AI-Coding-Partner-Homework

# Check current status
git status

# Create homework-1 submission branch
git checkout -b homework-1-submission

# Add all homework-1 files
git add homework-1/

# Create commit with proper message
git commit -m "Complete Homework 1: Banking Transactions API

Implemented features:
- REST API with 4 core endpoints
- Transaction validation (amount, account format, currency)
- Transaction filtering (by account, type, date range)
- [Your Task 4 feature]: [brief description]

Technology stack: [Node.js + Express / Python + FastAPI / Java + Spring Boot]
AI tools used: Claude Code, GitHub Copilot

All deliverables included:
- Complete source code with proper structure
- Comprehensive README.md and HOWTORUN.md
- Screenshots of AI interactions and working API
- Demo files and test scripts

🤖 Generated with AI assistance as part of AI-Assisted Development course

Co-Authored-By: Claude <noreply@anthropic.com>"

# Verify commit
git log -1

# Push to GitHub
git push -u origin homework-1-submission
```

**Checkpoint:**
- [ ] Branch created
- [ ] All files committed
- [ ] Commit message is descriptive
- [ ] Pushed to GitHub

### Step 11.3: Create Pull Request

**Steps:**
1. Go to your GitHub repository
2. Click "Compare & pull request" or "Pull requests" → "New pull request"
3. Set base repository to instructor's original repository
4. Set base branch to `main`
5. Set compare branch to `homework-1-submission`

**Pull Request Title:**
```
Homework 1: Banking Transactions API - [Your Name]
```

**Pull Request Description Template:**
```markdown
## 📋 Homework 1 Submission: Banking Transactions API

**Student Name:** [Your Name]
**Date:** [Date]
**Assignment:** Homework 1 - Simple Banking Transactions API

---

### ✅ Summary of Implementation

I have successfully implemented a Banking Transactions REST API with the following features:

**Core Endpoints (Task 1):**
- ✅ POST /transactions - Create new transaction
- ✅ GET /transactions - List all transactions
- ✅ GET /transactions/:id - Get specific transaction by ID
- ✅ GET /accounts/:accountId/balance - Get account balance

**Validation (Task 2):**
- ✅ Amount validation (positive, max 2 decimals)
- ✅ Account format validation (ACC-XXXXX)
- ✅ Currency validation (ISO 4217 codes)
- ✅ Structured error responses

**Filtering (Task 3):**
- ✅ Filter by accountId
- ✅ Filter by transaction type
- ✅ Filter by date range (from/to)
- ✅ Combined filters support

**Additional Features (Task 4):**
- ✅ [Which option you chose]: [Brief description]

---

### 🛠️ Technology Stack

- **Language/Framework:** [Node.js + Express / Python + FastAPI / Java + Spring Boot]
- **Storage:** In-memory (array-based)
- **Validation:** Custom validators
- **Testing:** [Manual testing with curl/Postman/REST Client]

---

### 🤖 AI Tools Used

#### Claude Code
- **Used for:** Project architecture, complex business logic, validation rules, balance calculation
- **Most helpful:** Designing the overall structure and implementing the balance calculation logic
- **Example prompt:** [One example of a prompt that worked well]

#### GitHub Copilot
- **Used for:** Inline code completion, boilerplate code, route handlers
- **Most helpful:** Speeding up implementation of similar endpoint patterns
- **Example:** [One example of what Copilot helped with]

#### Comparison
[Brief comparison of how different AI tools approached the same problems]

---

### ⚠️ Challenges Encountered

1. **Challenge:** [Describe a challenge you faced]
   - **AI Help:** [How AI helped solve it]
   - **Solution:** [Final solution]

2. **Challenge:** [Another challenge]
   - **AI Help:** [How AI helped]
   - **Solution:** [Final solution]

---

### 📸 Screenshots and Demos

**AI Interaction Screenshots:**
- Located in `homework-1/docs/screenshots/`
- [Number] screenshots showing AI prompts and responses
- Multiple AI tools demonstrated

**API Working Screenshots:**
- Server running and all endpoints tested
- Both success and error cases captured
- Validation errors demonstrated

**Demo Files:**
- `demo/run.sh` - Start the server
- `demo/sample-requests.http` - VS Code REST Client tests
- `demo/sample-requests.sh` - Command-line tests

---

### 📁 Project Structure

```
homework-1/
├── src/          (Source code organized in routes, models, validators, utils)
├── docs/         (Screenshots of AI interactions and API testing)
├── demo/         (Run scripts and sample requests)
├── README.md     (Complete project documentation)
└── HOWTORUN.md   (Step-by-step setup instructions)
```

---

### 🧪 Testing Instructions

See `HOWTORUN.md` for complete setup and testing instructions.

**Quick test:**
```bash
cd homework-1
./demo/run.sh           # Start server
./demo/sample-requests.sh  # Run tests
```

---

### 📚 What I Learned

- [Key learning point 1]
- [Key learning point 2]
- [Key learning point 3]

---

### ✅ Checklist

- [x] All 4 core endpoints implemented and working
- [x] All validation rules implemented
- [x] Transaction filtering working
- [x] At least 1 Task 4 feature implemented
- [x] Complete README.md
- [x] Complete HOWTORUN.md
- [x] Screenshots of AI usage (minimum 5)
- [x] Screenshots of working API
- [x] Demo files included
- [x] Code is clean and well-organized
- [x] .gitignore properly configured
- [x] No sensitive data in repository

---

**Ready for review! 🚀**
```

**Checkpoint:**
- [ ] Pull request created
- [ ] Title is clear
- [ ] Description is comprehensive
- [ ] Instructor assigned as reviewer
- [ ] Labels added (if applicable)

---

## Final Checklist

### Code Quality
- [ ] All endpoints working correctly
- [ ] All validation rules implemented
- [ ] All filters working
- [ ] Task 4 feature working
- [ ] No errors in console
- [ ] Clean code structure
- [ ] Proper error handling

### Documentation
- [ ] README.md complete and professional
- [ ] HOWTORUN.md clear and tested
- [ ] Code comments helpful
- [ ] AI usage well-documented

### Evidence
- [ ] Minimum 5 AI interaction screenshots
- [ ] Minimum 5 API working screenshots
- [ ] Screenshots show multiple AI tools
- [ ] Screenshots are clear and relevant

### Demo Files
- [ ] run.sh (or .bat) created and executable
- [ ] sample-requests.http created
- [ ] sample-requests.sh created and executable
- [ ] sample-data.json created (if applicable)

### Git & Submission
- [ ] Branch created: homework-1-submission
- [ ] All files committed
- [ ] Descriptive commit message
- [ ] Pushed to GitHub
- [ ] Pull request created
- [ ] PR description complete
- [ ] Instructor assigned as reviewer

### Grading Criteria Coverage
- [ ] Functionality (30%): All features working
- [ ] AI Usage Documentation (25%): Screenshots and descriptions
- [ ] Code Quality (20%): Clean, organized code
- [ ] Documentation (15%): README, HOWTORUN, comments
- [ ] Demo & Screenshots (10%): Demo files and visual evidence

---

## Time Tracking

Track your actual time spent on each phase:

| Phase | Estimated | Actual | Notes |
|-------|-----------|--------|-------|
| 1. Setup | 15-20 min | ___ min | |
| 2. Data Model | 20-30 min | ___ min | |
| 3. Validation | 25-35 min | ___ min | |
| 4. Core Endpoints | 45-60 min | ___ min | |
| 5. Filtering | 20-30 min | ___ min | |
| 6. Task 4 Feature | 30-45 min | ___ min | |
| 7. Demo Files | 15-20 min | ___ min | |
| 8. Documentation | 30-45 min | ___ min | |
| 9. Screenshots | 15-20 min | ___ min | |
| 10. Testing | 20-30 min | ___ min | |
| 11. Submission | 15-20 min | ___ min | |
| **Total** | **4-5 hours** | **___ hours** | |

---

## Tips for Success

1. **Follow the phases in order** - Each builds on the previous
2. **Test frequently** - Don't wait until the end
3. **Screenshot as you go** - Don't forget to capture AI interactions
4. **Use multiple AI tools** - Required for the assignment
5. **Read AI output** - Understand before implementing
6. **Commit often** - Small, frequent commits are better
7. **Take breaks** - Better quality code when not rushed
8. **Ask for help** - Use AI tools iteratively
9. **Document everything** - AI usage is 25% of the grade
10. **Test your HOWTORUN.md** - Follow your own instructions

---

## Emergency Troubleshooting

### "I'm stuck on a feature"
1. Review the IMPLEMENTATION_PROMPT.md for that specific feature
2. Try a different AI tool with the same prompt
3. Break the feature into smaller steps
4. Test with simpler input first

### "My code isn't working"
1. Check console for error messages
2. Add console.log() statements to debug
3. Test endpoints one at a time
4. Ask AI to help debug with the error message

### "I'm running out of time"
**Priority order:**
1. Core endpoints (must have)
2. Validation (must have)
3. Filtering (must have)
4. One Task 4 feature (must have)
5. Documentation (must have)
6. Screenshots (must have)
7. Code cleanup (nice to have)
8. Additional Task 4 features (bonus)

### "Screenshots are missing"
- You can recreate AI interactions for documentation
- Show the prompts you used and the output
- Be honest that they are recreated

---

**Good luck with your implementation! 🚀**

Follow this plan step-by-step, and you'll have a complete, well-documented submission ready for grading.
