# 🏦 Homework 1: Banking Transactions API

> **Student Name**: Mykhailo Bestiuk
> **Date Submitted**: 2024-01-24
> **AI Tools Used**: Claude Code, GitHub Copilot

---

## 📋 Project Overview

A comprehensive RESTful API for managing banking transactions built with **Python** and **FastAPI**. This project demonstrates the power of AI-assisted development by implementing a production-ready banking API with features including transaction management, advanced filtering, balance calculation, and account summaries.

### Key Features

✅ **Task 1: Core API** - Complete CRUD operations for banking transactions
✅ **Task 2: Validation** - Comprehensive input validation for amounts, accounts, and currencies
✅ **Task 3: Filtering** - Advanced filtering by account, type, and date range
✅ **Task 4: Additional Feature** - Transaction summary with aggregated statistics

---

## 🚀 Features Implemented

### 1. Transaction Management (Task 1)
- **Create Transactions**: Support for deposits, withdrawals, and transfers
- **Retrieve Transactions**: Get all transactions or specific transaction by ID
- **Auto-generated IDs**: UUID-based unique identifiers
- **Timestamps**: Automatic timestamp generation for each transaction
- **Account Balance**: Calculate current balance for any account

### 2. Input Validation (Task 2)
- **Amount Validation**:
  - Must be positive (> 0)
  - Maximum 2 decimal places
- **Account Format Validation**:
  - Must match pattern: `ACC-XXXXX` (5 alphanumeric characters)
  - Example: `ACC-12345`, `ACC-ABCDE`
- **Currency Validation**:
  - Must be valid ISO 4217 currency code
  - Supports 50+ currencies (USD, EUR, GBP, JPY, CHF, etc.)
- **Structured Error Responses**:
  ```json
  {
    "error": "Validation failed",
    "details": [
      {
        "field": "amount",
        "message": "Amount must be a positive number",
        "type": "value_error"
      }
    ]
  }
  ```

### 3. Advanced Filtering (Task 3)
- **Filter by Account**: Get all transactions involving a specific account
- **Filter by Type**: Filter by deposit, withdrawal, or transfer
- **Filter by Date Range**: Filter transactions between start and end dates
- **Combined Filters**: Apply multiple filters simultaneously

### 4. Transaction Summary (Task 4 - Additional Feature)
Comprehensive account statistics including:
- Total deposits (money coming in)
- Total withdrawals (money going out)
- Transaction count
- Most recent transaction date

---

## 🏗️ Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Python | 3.8+ |
| **Framework** | FastAPI | 0.104.1 |
| **Server** | Uvicorn | 0.24.0 |
| **Validation** | Pydantic | 2.5.0 |
| **Date Parsing** | python-dateutil | 2.8.2 |
| **Testing** | pytest | 7.4.3 |

---

## 📐 Architecture Overview

### Project Structure
```
homework-1/
├── src/                           # Source code
│   ├── main.py                   # FastAPI application entry point
│   ├── models/                   # Data models
│   │   └── transaction.py        # Transaction Pydantic model
│   ├── routes/                   # API routes
│   │   └── transactions.py       # Transaction endpoints
│   ├── services/                 # Business logic
│   │   └── transaction_service.py # Transaction CRUD operations
│   ├── validators/               # Validation utilities
│   │   └── transaction_validator.py
│   └── utils/                    # Helper functions
│       └── helpers.py            # Filtering utilities
├── demo/                         # Demo files
│   ├── run.sh                    # Linux/Mac startup script
│   ├── run.bat                   # Windows startup script
│   ├── sample-requests.http      # HTTP request examples
│   └── sample-data.json          # Sample transaction data
├── docs/                         # Documentation
│   └── screenshots/              # Screenshots (to be added)
├── requirements.txt              # Python dependencies
├── .gitignore                    # Git ignore rules
├── README.md                     # This file
├── HOWTORUN.md                   # Setup and run instructions
├── AI-PLAN.md                    # Detailed implementation plan
└── TASKS.md                      # Task requirements
```

### Architecture Layers

1. **Presentation Layer** (`main.py`):
   - FastAPI application setup
   - CORS middleware configuration
   - Global exception handlers
   - API documentation configuration

2. **Routing Layer** (`routes/`):
   - RESTful endpoint definitions
   - Request/response models
   - Query parameter handling

3. **Service Layer** (`services/`):
   - Business logic implementation
   - In-memory data storage
   - Balance calculations
   - Summary generation

4. **Model Layer** (`models/`):
   - Pydantic models with validation
   - Type definitions
   - Enumerations (TransactionType, TransactionStatus)

5. **Validation Layer** (`validators/`):
   - Standalone validation functions
   - Reusable validation logic

6. **Utility Layer** (`utils/`):
   - Helper functions
   - Filtering operations
   - Common utilities

---

## 🔗 API Endpoints

### Base URL
```
http://localhost:8000
```

### Endpoints Table

| Method | Endpoint | Description | Task |
|--------|----------|-------------|------|
| **GET** | `/` | API information | - |
| **GET** | `/health` | Health check | - |
| **POST** | `/api/transactions` | Create new transaction | Task 1 |
| **GET** | `/api/transactions` | Get all transactions (with filters) | Task 1 & 3 |
| **GET** | `/api/transactions/{id}` | Get transaction by ID | Task 1 |
| **GET** | `/api/accounts/{accountId}/balance` | Get account balance | Task 1 |
| **GET** | `/api/accounts/{accountId}/summary` | Get account summary | Task 4 |

### Example Requests

#### Create a Deposit
```bash
curl -X POST http://localhost:8000/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "toAccount": "ACC-12345",
    "amount": 1000.00,
    "currency": "USD",
    "type": "deposit",
    "status": "completed"
  }'
```

#### Get All Transactions
```bash
curl http://localhost:8000/api/transactions
```

#### Filter by Account
```bash
curl "http://localhost:8000/api/transactions?accountId=ACC-12345"
```

#### Get Account Balance
```bash
curl http://localhost:8000/api/accounts/ACC-12345/balance
```

#### Get Account Summary
```bash
curl http://localhost:8000/api/accounts/ACC-12345/summary
```

---

## 🤖 AI Tools Used

This project was developed with the assistance of two AI tools:

### 1. **Claude Code**
- **Usage**: Architecture design, implementation planning, complex logic
- **Contributions**:
  - Designed the overall project structure
  - Created comprehensive validation logic
  - Implemented filtering and summary features
  - Generated detailed documentation

### 2. **GitHub Copilot**
- **Usage**: Code completion, inline suggestions, boilerplate code
- **Contributions**:
  - Rapid code generation for models and routes
  - Autocomplete for Pydantic validators
  - Helper function implementations
  - Test data generation

### AI Tool Comparison

| Aspect | Claude Code | GitHub Copilot |
|--------|-------------|----------------|
| **Planning** | Excellent - detailed plans | Limited |
| **Code Generation** | Complete functions | Inline suggestions |
| **Documentation** | Comprehensive docstrings | Brief comments |
| **Explanations** | Detailed explanations | Minimal |
| **Best For** | Architecture, complex logic | Boilerplate, completions |

---

## 📚 Interactive API Documentation

FastAPI automatically generates interactive API documentation:

### Swagger UI (Recommended)
```
http://localhost:8000/docs
```
- Interactive API explorer
- Try out endpoints directly in the browser
- View request/response schemas
- Test all functionality without Postman

### ReDoc (Alternative)
```
http://localhost:8000/redoc
```
- Clean, organized documentation
- Easy to read and navigate
- Perfect for sharing with team members

---

## ✅ Task Completion Checklist

### Task 1: Core API ✅
- ✅ POST /transactions - Create transaction with auto-generated ID
- ✅ GET /transactions - Retrieve all transactions
- ✅ GET /transactions/{id} - Get specific transaction
- ✅ GET /accounts/{accountId}/balance - Calculate account balance
- ✅ In-memory storage with TransactionService
- ✅ Proper HTTP status codes (200, 201, 404)

### Task 2: Validation ✅
- ✅ Amount validation (positive, max 2 decimals)
- ✅ Account format validation (ACC-XXXXX pattern)
- ✅ Currency validation (ISO 4217 codes)
- ✅ Pydantic field validators
- ✅ Custom exception handlers
- ✅ Structured error responses

### Task 3: Filtering ✅
- ✅ Filter by accountId
- ✅ Filter by transaction type
- ✅ Filter by date range (from/to)
- ✅ Multiple simultaneous filters
- ✅ Helper functions for filtering

### Task 4: Additional Feature ✅
- ✅ Transaction Summary endpoint (Option A)
- ✅ Calculate total deposits
- ✅ Calculate total withdrawals
- ✅ Count transactions
- ✅ Track most recent transaction date

---

## 🧪 Testing

### Manual Testing
1. Start the server: `./demo/run.sh` or `demo\run.bat`
2. Visit: `http://localhost:8000/docs`
3. Use the interactive Swagger UI to test all endpoints
4. Refer to `demo/sample-requests.http` for example requests

### Test Scenarios
- ✅ Valid transaction creation (deposits, withdrawals, transfers)
- ✅ Validation errors (negative amounts, invalid formats, bad currency codes)
- ✅ Transaction retrieval and filtering
- ✅ Balance calculation with multiple transactions
- ✅ Account summary generation
- ✅ Error handling (404 for non-existent transactions)

---

## 🎓 Learning Outcomes

Through this project, I learned:
- **FastAPI Framework**: Building RESTful APIs with automatic documentation
- **Pydantic Validation**: Creating robust data models with field validators
- **AI-Assisted Development**: Leveraging AI tools for architecture and implementation
- **API Design**: RESTful principles and proper endpoint structure
- **Error Handling**: Comprehensive validation and error responses
- **Python Best Practices**: Type hints, docstrings, code organization

---

## 🔄 Future Enhancements

Potential improvements for production use:
- 🔲 Persistent database storage (PostgreSQL, MongoDB)
- 🔲 User authentication and authorization (JWT tokens)
- 🔲 Rate limiting for API endpoints
- 🔲 Transaction rollback and cancellation
- 🔲 Multi-currency conversion
- 🔲 Transaction history export (CSV, PDF)
- 🔲 Comprehensive test suite (unit tests, integration tests)
- 🔲 Docker containerization
- 🔲 CI/CD pipeline
- 🔲 Logging and monitoring

---

## 📞 Support

For questions or issues:
- Check `HOWTORUN.md` for setup instructions
- Review `demo/sample-requests.http` for API examples
- Examine `AI-PLAN.md` for implementation details

---

<div align="center">

### ⭐ Project Statistics

**Lines of Code**: ~1,500+
**Files Created**: 15+
**API Endpoints**: 7
**Tasks Completed**: 4/4
**Development Time**: ~8 hours

---

*This project was completed as part of the AI-Assisted Development course.*

**Built with ❤️ using FastAPI and AI assistance**

</div>
