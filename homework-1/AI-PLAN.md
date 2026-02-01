# 📋 Detailed Step-by-Step Plan: Banking Transactions API (Python)

## 🎯 Project Overview
**Technology Stack:** Python with FastAPI  
**AI Tools:** GitHub Copilot + Claude Code (minimum 2 required)  
**Total Estimated Time:** ~8 hours

---

## 📐 Phase 1: Project Setup & Structure

### Step 1: Initialize Project Directory
```bash
# Create project structure
mkdir homework-1
cd homework-1
```

### Step 2: Set Up Python Environment
```bash
# Create virtual environment
python -m venv venv

# Activate virtual environment
# Windows: venv\Scripts\activate
# Linux/Mac: source venv/bin/activate

# Create requirements.txt
```

### Step 3: Install Dependencies
**Required packages:**
- `fastapi` - Web framework
- `uvicorn` - ASGI server
- `pydantic` - Data validation
- `python-dateutil` - Date parsing
- `pytest` (optional) - Testing

**Create `requirements.txt`:**
```
fastapi==0.104.1
uvicorn==0.24.0
pydantic==2.5.0
python-dateutil==2.8.2
```

### Step 4: Create Project Structure
```
homework-1/
├── README.md
├── HOWTORUN.md
├── requirements.txt
├── .gitignore
├── src/
│   ├── __init__.py
│   ├── main.py
│   ├── models/
│   │   ├── __init__.py
│   │   └── transaction.py
│   ├── routes/
│   │   ├── __init__.py
│   │   └── transactions.py
│   ├── validators/
│   │   ├── __init__.py
│   │   └── transaction_validator.py
│   ├── services/
│   │   ├── __init__.py
│   │   └── transaction_service.py
│   └── utils/
│       ├── __init__.py
│       └── helpers.py
├── docs/
│   └── screenshots/
├── demo/
│   ├── run.sh
│   ├── run.bat
│   ├── sample-requests.http
│   └── sample-data.json
└── tests/
    ├── __init__.py
    └── test_transactions.py
```

---

## 🔨 Phase 2: Core Implementation (Task 1)

### Step 5: Create Transaction Model (`src/models/transaction.py`)
**AI Prompt for Copilot/Claude:**
> "Create a Pydantic model for a banking transaction with fields: id, fromAccount, toAccount, amount, currency, type (deposit/withdrawal/transfer), timestamp, and status (pending/completed/failed). Use proper type hints and validation."

**Expected Output:**
- Pydantic BaseModel class
- Proper field types (str, float, datetime)
- Enums for transaction type and status

### Step 6: Create In-Memory Storage (`src/services/transaction_service.py`)
**AI Prompt:**
> "Create a TransactionService class in Python that manages transactions in-memory using a list. Include methods for: create_transaction, get_all_transactions, get_transaction_by_id, and calculate_account_balance."

**Expected functionality:**
- Dictionary/list for storing transactions
- UUID generation for transaction IDs
- CRUD operations
- Balance calculation logic

### Step 7: Implement Core API Endpoints (`src/routes/transactions.py`)
**AI Prompt:**
> "Create FastAPI router with endpoints: POST /transactions, GET /transactions, GET /transactions/{id}, GET /accounts/{accountId}/balance. Use dependency injection for TransactionService."

**Endpoints to implement:**

| Endpoint | Method | Function |
|----------|--------|----------|
| `/transactions` | POST | Create new transaction |
| `/transactions` | GET | List all transactions |
| `/transactions/{id}` | GET | Get transaction by ID |
| `/accounts/{accountId}/balance` | GET | Get account balance |

### Step 8: Create Main Application (`src/main.py`)
**AI Prompt:**
> "Create a FastAPI main application file that includes the transactions router, CORS middleware, and proper error handling. Add a root endpoint that returns API information."

**Include:**
- FastAPI app initialization
- Router inclusion
- CORS configuration
- Global exception handlers
- Health check endpoint

**💡 Bonus:** FastAPI automatically generates interactive API documentation at `/docs` (Swagger UI) and `/redoc` - mention this in your README!

---

## ✅ Phase 3: Validation (Task 2)

### Step 9: Create Validation Utilities (`src/validators/transaction_validator.py`)
**AI Prompt:**
> "Create validation functions in Python for: 1) amount validation (positive, max 2 decimals), 2) account format validation (ACC-XXXXX pattern), 3) ISO 4217 currency code validation (USD, EUR, GBP, JPY, etc.)."

**Validation rules:**
- Amount: positive, max 2 decimal places
- Account: regex pattern `^ACC-[A-Z0-9]{5}$`
- Currency: ISO 4217 codes (create list of valid codes)

### Step 10: Integrate Validation in Transaction Model
**AI Prompt:**
> "Add Pydantic validators to the Transaction model using @field_validator for amount, account numbers, and currency code validation. Return detailed error messages."

### Step 11: Create Custom Exception Handler
**AI Prompt:**
> "Create a FastAPI exception handler for validation errors that returns a structured JSON response with error field and details list."

**Error response format:**
```json
{
  "error": "Validation failed",
  "details": [
    {"field": "amount", "message": "Amount must be a positive number"}
  ]
}
```

---

## 📜 Phase 4: Transaction History & Filtering (Task 3)

### Step 12: Implement Query Filters (`src/routes/transactions.py`)
**AI Prompt:**
> "Modify the GET /transactions endpoint to accept optional query parameters: accountId, type, from (date), to (date). Implement filtering logic in the TransactionService to filter transactions based on these parameters."

**Query parameters:**
- `accountId`: Filter by account
- `type`: Filter by transaction type
- `from`: Start date (ISO format)
- `to`: End date (ISO format)

### Step 13: Create Filter Helper Functions (`src/utils/helpers.py`)
**AI Prompt:**
> "Create helper functions for filtering transactions: filter_by_account, filter_by_type, filter_by_date_range. Each function should take a list of transactions and return filtered results."

---

## 🌟 Phase 5: Additional Feature (Choose 1 - Recommended: Option A)

### Option A: Transaction Summary (Recommended)

#### Step 14: Implement Summary Endpoint
**AI Prompt:**
> "Create a GET /accounts/{accountId}/summary endpoint that returns: total deposits, total withdrawals, number of transactions, and most recent transaction date for the specified account."

**Response format:**
```json
{
  "accountId": "ACC-12345",
  "totalDeposits": 1500.00,
  "totalWithdrawals": 500.00,
  "transactionCount": 25,
  "mostRecentTransaction": "2024-01-15T10:30:00Z"
}
```

### Alternative Options (if not choosing Option A):

#### Option B: Simple Interest Calculation
```
GET /accounts/{accountId}/interest?rate=0.05&days=30
```

#### Option C: Transaction Export to CSV
```
GET /transactions/export?format=csv
```

#### Option D: Rate Limiting
Implement basic rate limiting with 100 requests/minute per IP

---

## 📝 Phase 6: Documentation

### Step 15: Create README.md
**Sections to include:**
- Project title and description
- Features implemented (list all 4 tasks completed)
- Technology stack (Python, FastAPI, Pydantic, Uvicorn)
- Architecture overview (models, routes, services, validators)
- API endpoints documentation
- AI tools used and their contributions
- How to access FastAPI's automatic Swagger documentation at `/docs`

**AI Prompt:**
> "Create a comprehensive README.md for a Python FastAPI banking transactions API. Include project description, features, tech stack, API endpoints table, and instructions for viewing the interactive API docs."

### Step 16: Create HOWTORUN.md
**Include:**
1. Prerequisites (Python 3.8+)
2. Installation steps (clone, venv, install dependencies)
3. Running the application (activate venv, start uvicorn)
4. Testing endpoints (using curl, Postman, or `/docs`)
5. Troubleshooting tips

**AI Prompt:**
> "Create a HOWTORUN.md file with step-by-step instructions to set up and run a Python FastAPI application. Include commands for Windows and Linux/Mac."

### Step 17: Document AI Assistance (Optional but Recommended)
**Create `docs/AI_ASSISTANCE.md`:**
- List of prompts used for each component
- Which AI tool was used for each task
- Comparison of AI tools' outputs (GitHub Copilot vs Claude Code)
- Modifications made to AI-generated code
- Lessons learned about prompting strategies

**Sections:**
1. **AI Tools Used**: GitHub Copilot, Claude Code
2. **Prompt Examples**: Copy actual prompts used
3. **AI Tool Comparison**: Which tool was better for what task
4. **Code Modifications**: What needed to be changed
5. **Best Practices Learned**: Effective prompting techniques

---

## 🎬 Phase 7: Demo Files

### Step 18: Create Run Scripts

**`demo/run.sh` (Linux/Mac):**
```bash
#!/bin/bash
echo "Starting Banking Transactions API..."
source venv/bin/activate
cd src
uvicorn main:app --reload --port 8000
```

**`demo/run.bat` (Windows):**
```batch
@echo off
echo Starting Banking Transactions API...
call venv\Scripts\activate
cd src
uvicorn main:app --reload --port 8000
```

### Step 19: Create Sample Requests (`demo/sample-requests.http`)
**AI Prompt:**
> "Create an HTTP file with sample requests for all API endpoints including: creating transactions, getting all transactions, filtering by account, getting account balance, and the summary endpoint. Use REST Client format."

**Include examples for:**
- POST valid transaction
- POST invalid transaction (for testing validation)
- GET all transactions
- GET with filters (accountId, type, date range)
- GET account balance
- GET account summary

### Step 20: Create Sample Data (`demo/sample-data.json`)
Create JSON file with 5-10 sample transactions for testing.

**AI Prompt:**
> "Generate 10 sample banking transactions in JSON format with various accounts (ACC-12345, ACC-67890, ACC-ABCDE), amounts, currencies (USD, EUR, GBP), types (deposit, withdrawal, transfer), and timestamps."

---

## 📸 Phase 8: Screenshots & Testing

### Step 21: Capture AI Interaction Screenshots
**Required screenshots in `docs/screenshots/`:**
1. `ai-prompt-core-api.png` - Creating core API endpoints
2. `ai-prompt-validation.png` - Implementing validation logic
3. `ai-prompt-filtering.png` - Adding filter functionality
4. `ai-comparison.png` - Comparing outputs from 2 different AI tools
5. `ai-copilot-example.png` - GitHub Copilot in action
6. `ai-claude-example.png` - Claude Code in action

**Tips:**
- Capture the prompt AND the generated code
- Show inline suggestions from Copilot
- Show chat interface from Claude Code
- Highlight differences in AI approaches

### Step 22: Test API and Capture Results
**Test scenarios:**
1. ✅ Create valid transaction
2. ❌ Create invalid transaction (validation error)
3. ✅ Get all transactions
4. ✅ Filter by account
5. ✅ Filter by type and date range
6. ✅ Get account balance
7. ✅ Get account summary

**Capture screenshots:**
- `api-running.png` - Terminal showing Uvicorn server running
- `api-docs-swagger.png` - FastAPI's automatic /docs page
- `postman-test-1.png` - Successful POST request
- `postman-test-2.png` - Validation error response
- `postman-test-3.png` - GET filtered results
- `postman-test-4.png` - Account summary response

**Testing tools:**
- Postman
- curl commands
- VS Code REST Client extension
- FastAPI's built-in `/docs` Swagger UI

### Step 23: Create `.gitignore`
```gitignore
# Python
__pycache__/
*.py[cod]
*$py.class
*.so
.Python
venv/
env/
ENV/
build/
develop-eggs/
dist/
downloads/
eggs/
.eggs/
lib/
lib64/
parts/
sdist/
var/
wheels/
*.egg-info/
.installed.cfg
*.egg

# IDE
.vscode/
.idea/
*.swp
*.swo
*~

# OS
.DS_Store
Thumbs.db

# Environment
.env
.env.local

# Testing
.pytest_cache/
.coverage
htmlcov/

# Logs
*.log
```

---

## ✅ Phase 9: Final Review & Submission

### Step 24: Code Quality Check
- [ ] All endpoints return correct HTTP status codes (200, 201, 400, 404)
- [ ] Error handling is comprehensive
- [ ] Code is properly commented
- [ ] Pydantic models validate correctly
- [ ] All validation rules work (amount, account format, currency)
- [ ] No hardcoded values (use constants/enums)
- [ ] Code follows Python PEP 8 style guidelines
- [ ] Type hints are used throughout

### Step 25: Documentation Review
- [ ] README.md is complete and clear
- [ ] HOWTORUN.md has step-by-step instructions
- [ ] AI_ASSISTANCE.md documents all AI usage
- [ ] Screenshots are clear, labeled, and properly organized
- [ ] Code comments explain complex logic
- [ ] API documentation is accurate

### Step 26: Testing Checklist

**Task 1: Core API**
- [ ] POST /transactions creates transaction with auto-generated ID
- [ ] GET /transactions returns all transactions
- [ ] GET /transactions/{id} returns specific transaction
- [ ] GET /transactions/{id} returns 404 for non-existent ID
- [ ] GET /accounts/{accountId}/balance calculates correctly

**Task 2: Validation**
- [ ] Amount validation: rejects negative numbers
- [ ] Amount validation: rejects more than 2 decimal places
- [ ] Account validation: accepts ACC-XXXXX format
- [ ] Account validation: rejects invalid formats
- [ ] Currency validation: accepts valid ISO 4217 codes
- [ ] Currency validation: rejects invalid codes
- [ ] Error responses include detailed field-level messages

**Task 3: Filtering**
- [ ] Filter by accountId works
- [ ] Filter by type works (deposit/withdrawal/transfer)
- [ ] Filter by date range works (from & to)
- [ ] Multiple filters work together
- [ ] Empty results return empty array (not error)

**Task 4: Additional Feature**
- [ ] Summary endpoint returns correct totals
- [ ] Summary calculates deposits correctly
- [ ] Summary calculates withdrawals correctly
- [ ] Summary counts transactions correctly
- [ ] Summary shows most recent transaction date

**General**
- [ ] Demo scripts run successfully
- [ ] All sample requests work
- [ ] FastAPI /docs page loads correctly
- [ ] No Python errors or warnings

### Step 27: Final Submission Checklist
**Verify submission includes:**
1. ✅ Complete source code in `src/` folder
2. ✅ `requirements.txt` with all dependencies
3. ✅ `README.md` with comprehensive documentation
4. ✅ `HOWTORUN.md` with setup instructions
5. ✅ `.gitignore` file
6. ✅ Screenshots in `docs/screenshots/` (minimum 6-8 images)
7. ✅ Demo files in `demo/` (run.sh, run.bat, sample-requests.http, sample-data.json)
8. ✅ AI assistance documentation (optional but recommended)
9. ✅ All 4 tasks completed (Core API, Validation, Filtering, Additional Feature)
10. ✅ Used at least 2 different AI tools and documented their use

---

## 🎯 Specific AI Prompts Checklist

| Phase | Component | AI Tool | Prompt Template |
|-------|-----------|---------|-----------------|
| Setup | Project Structure | Copilot | "Create a Python FastAPI project structure for a banking transactions API with folders for models, routes, services, validators, and utils" |
| Core | Transaction Model | Claude | "Create a Pydantic model for banking transactions with validation" |
| Core | Service Layer | Copilot | "Create a TransactionService class with in-memory storage for CRUD operations" |
| Core | API Routes | Claude | "Create FastAPI router with POST, GET endpoints for transactions" |
| Core | Main App | Copilot | "Create FastAPI main application with CORS and error handling" |
| Validation | Validators | Claude | "Create validation functions for amount, account format, and currency codes" |
| Validation | Model Validators | Copilot | "Add Pydantic field validators to Transaction model" |
| Validation | Exception Handler | Claude | "Create FastAPI exception handler for validation errors" |
| Filtering | Query Params | Copilot | "Add query parameter filtering to GET /transactions endpoint" |
| Filtering | Helper Functions | Claude | "Create helper functions for filtering transactions by account, type, and date" |
| Feature | Summary Endpoint | Copilot | "Create account summary endpoint with aggregated statistics" |
| Demo | Sample Requests | Claude | "Create .http file with sample API requests for testing" |
| Demo | Sample Data | Copilot | "Generate 10 sample banking transactions in JSON format" |
| Docs | README | Claude | "Create comprehensive README.md for FastAPI banking API" |
| Docs | HOWTORUN | Copilot | "Create HOWTORUN.md with setup and run instructions" |

**Strategy for comparing AI tools:**
- Use **GitHub Copilot** for inline code completions and quick implementations
- Use **Claude Code** for architectural decisions and complex logic explanations
- Document which tool performed better for each task type
- Note any differences in code style, structure, or approach

---

## ⏱️ Estimated Timeline

| Phase | Duration | Tasks | Priority |
|-------|----------|-------|----------|
| Phase 1: Setup | 30 min | Steps 1-4 | 🔴 Critical |
| Phase 2: Core API | 2 hours | Steps 5-8 | 🔴 Critical |
| Phase 3: Validation | 1 hour | Steps 9-11 | 🔴 Critical |
| Phase 4: Filtering | 1 hour | Steps 12-13 | 🔴 Critical |
| Phase 5: Additional Feature | 1 hour | Step 14 | 🔴 Critical |
| Phase 6: Documentation | 1 hour | Steps 15-17 | 🟡 Important |
| Phase 7: Demo Files | 30 min | Steps 18-20 | 🟡 Important |
| Phase 8: Screenshots | 30 min | Steps 21-23 | 🟡 Important |
| Phase 9: Review | 30 min | Steps 24-27 | 🟢 Final |
| **Total** | **~8 hours** | 27 steps | |

**Recommended execution order:**
1. **Day 1** (3-4 hours): Phases 1-3 (Setup, Core API, Validation)
2. **Day 2** (2-3 hours): Phases 4-5 (Filtering, Additional Feature)
3. **Day 3** (2 hours): Phases 6-9 (Documentation, Demo, Screenshots, Review)

---

## 💡 Tips for Success with AI Tools

### Effective Prompting Strategies
1. **Be specific**: Include expected input/output in prompts
2. **Provide context**: Mention the framework (FastAPI) and language (Python)
3. **Iterate**: Refine AI responses with follow-up questions
4. **Request examples**: Ask for sample data or test cases
5. **Specify style**: Request PEP 8 compliance, type hints, docstrings

### Comparing AI Tools
1. **Use different tools for same task** - implement one endpoint with Copilot, another with Claude
2. **Document differences** - which tool gave better initial output?
3. **Note strengths** - Copilot for code completion, Claude for explanations
4. **Capture screenshots** - show side-by-side comparisons

### Code Review Best Practices
1. **Don't just copy-paste** - understand what the AI generated
2. **Test immediately** - run code after each generation
3. **Refine incrementally** - improve AI output step by step
4. **Add comments** - explain complex logic even if AI didn't
5. **Handle edge cases** - AI might miss error scenarios

### Common Pitfalls to Avoid
- ❌ Not testing AI-generated code before moving on
- ❌ Accepting incomplete validation logic
- ❌ Forgetting to handle edge cases (empty lists, None values)
- ❌ Not documenting which AI tool generated which code
- ❌ Skipping error handling in routes
- ❌ Not capturing screenshots during development

---

## 🚀 Python/FastAPI Specific Tips

### FastAPI Best Practices
1. **Use Pydantic models** for request/response validation
2. **Dependency injection** for services (TransactionService)
3. **Router organization** - separate file for transaction routes
4. **Exception handlers** - global error handling
5. **Automatic docs** - FastAPI generates /docs and /redoc

### Python Code Quality
1. **Type hints** - use them everywhere for clarity
2. **Docstrings** - document classes and functions
3. **Enums** - use for transaction type and status
4. **List comprehensions** - for filtering operations
5. **f-strings** - for string formatting

### Testing Tips
1. Use FastAPI's TestClient for unit tests
2. Test validation errors explicitly
3. Test filtering with various combinations
4. Check HTTP status codes in tests
5. Use pytest fixtures for sample data

### Useful FastAPI Features
```python
# Automatic API documentation
# Visit: http://localhost:8000/docs

# Dependency injection
from fastapi import Depends

# Request validation
from pydantic import BaseModel, Field, field_validator

# Response models
@app.get("/transactions", response_model=List[Transaction])

# Status codes
from fastapi import status
return JSONResponse(status_code=status.HTTP_201_CREATED)
```

---

## 📚 Additional Resources

### FastAPI Documentation
- [FastAPI Official Docs](https://fastapi.tiangolo.com/)
- [Pydantic Validation](https://docs.pydantic.dev/)
- [Uvicorn Server](https://www.uvicorn.org/)

### Python Best Practices
- [PEP 8 Style Guide](https://pep8.org/)
- [Python Type Hints](https://docs.python.org/3/library/typing.html)
- [ISO 4217 Currency Codes](https://en.wikipedia.org/wiki/ISO_4217)

### Testing and Tools
- [Postman](https://www.postman.com/)
- [VS Code REST Client](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)
- [curl documentation](https://curl.se/docs/)

---

## 🎓 Learning Outcomes

By completing this plan, you will:
- ✅ Understand FastAPI's request/response handling
- ✅ Master Pydantic validation patterns
- ✅ Learn effective AI prompting techniques
- ✅ Compare different AI coding assistants
- ✅ Practice API design and error handling
- ✅ Gain experience with Python web development
- ✅ Document AI-assisted development workflows

---

<div align="center">

### ✨ Ready to start coding with AI assistance! ✨

**Total Steps:** 27  
**Total Time:** ~8 hours  
**Difficulty:** Intermediate  

</div>
