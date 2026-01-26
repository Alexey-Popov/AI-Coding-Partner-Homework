# 🏦 Banking Transactions API

> **AI-Assisted Java Spring Boot Application**
> 
> A RESTful API for managing banking transactions with comprehensive validation, filtering, and account management features.

---

## 📋 Project Overview

This is a fully functional REST API for banking transactions built with **Java 17** and **Spring Boot 3.2.1**. The application provides endpoints for creating and managing transactions, querying account balances, and calculating interest. All data is stored in-memory using thread-safe data structures.

### ✨ Key Features Implemented

- ✅ **Complete CRUD Operations** for transactions
- ✅ **Advanced Validation** with custom validators
- ✅ **Transaction Filtering** by account, type, and date range
- ✅ **Account Balance Calculation** from transaction history
- ✅ **Account Summary** with deposits, withdrawals, and statistics
- ✅ **Interest Calculation** using simple interest formula
- ✅ **Global Exception Handling** with detailed error messages
- ✅ **Comprehensive Testing** (Unit & Integration tests)
- ✅ **Thread-Safe In-Memory Storage** using ConcurrentHashMap

---

## 🛠️ Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17 | Programming language |
| **Spring Boot** | 3.2.1 | Application framework |
| **Maven** | 3.6+ | Build tool & dependency management |
| **Lombok** | Latest | Reduce boilerplate code |
| **JUnit 5** | Latest | Unit testing framework |
| **Mockito** | Latest | Mocking framework for tests |
| **Spring MockMvc** | Latest | Integration testing |

---

## 🏗️ Architecture

### Layered Architecture

```
┌─────────────────────────────────────┐
│     Controller Layer (REST API)     │  ← HTTP Endpoints
├─────────────────────────────────────┤
│         Service Layer               │  ← Business Logic
├─────────────────────────────────────┤
│      Repository Layer               │  ← Data Access (In-Memory)
└─────────────────────────────────────┘
```

### Project Structure

```
src/main/java/com/banking/api/
├── BankingApiApplication.java        # Main application class
├── controller/
│   ├── TransactionController.java    # Transaction endpoints
│   └── AccountController.java        # Account endpoints
├── service/
│   ├── TransactionService.java       # Transaction business logic
│   └── AccountService.java           # Account business logic
├── repository/
│   └── TransactionRepository.java    # In-memory data storage
├── model/
│   ├── Transaction.java              # Transaction entity
│   ├── TransactionType.java          # Enum: DEPOSIT, WITHDRAWAL, TRANSFER
│   └── TransactionStatus.java        # Enum: PENDING, COMPLETED, FAILED
├── dto/
│   ├── TransactionRequest.java       # Request DTO
│   ├── TransactionResponse.java      # Response DTO
│   ├── BalanceResponse.java          # Balance response
│   ├── SummaryResponse.java          # Summary response
│   ├── InterestResponse.java         # Interest calculation response
│   └── ErrorResponse.java            # Error response with field details
├── validator/
│   ├── ValidAccountNumber.java       # Custom validation annotation
│   ├── AccountNumberValidator.java   # Account format validator
│   ├── ValidCurrency.java            # Currency validation annotation
│   ├── CurrencyCodeValidator.java    # Currency validator
│   ├── ValidAmount.java              # Amount validation annotation
│   └── AmountValidator.java          # Amount validator
└── exception/
    ├── ValidationException.java       # Custom validation exception
    ├── ResourceNotFoundException.java # 404 exception
    └── GlobalExceptionHandler.java    # Global exception handler
```

---

## 🔌 API Endpoints

### Transaction Endpoints

| Method | Endpoint | Description | Status Code |
|--------|----------|-------------|-------------|
| `POST` | `/api/transactions` | Create a new transaction | 201 Created |
| `GET` | `/api/transactions` | Get all transactions (with filters) | 200 OK |
| `GET` | `/api/transactions/{id}` | Get transaction by ID | 200 OK / 404 Not Found |

### Account Endpoints

| Method | Endpoint | Description | Status Code |
|--------|----------|-------------|-------------|
| `GET` | `/api/accounts/{accountId}/balance` | Get account balance | 200 OK |
| `GET` | `/api/accounts/{accountId}/summary` | Get account summary | 200 OK |
| `GET` | `/api/accounts/{accountId}/interest` | Calculate interest | 200 OK |

### Query Parameters for Filtering

- `accountId` - Filter by account (fromAccount or toAccount)
- `type` - Filter by transaction type (DEPOSIT, WITHDRAWAL, TRANSFER)
- `from` - Filter by start date (ISO 8601 format)
- `to` - Filter by end date (ISO 8601 format)

---

## ✅ Validation Rules

### Transaction Validation

1. **Account Number Format**: Must match `ACC-XXXXX` where X is alphanumeric
   - Example: `ACC-12345`, `ACC-A1B2C`
   
2. **Amount Validation**:
   - Must be positive
   - Maximum 2 decimal places
   - Example: `100.50` ✅, `-50.00` ❌, `100.123` ❌

3. **Currency Validation**:
   - Must be valid ISO 4217 code
   - Supported: USD, EUR, GBP, JPY, CHF, CAD, AUD, CNY, INR, MXN

4. **Transaction Type**:
   - Must be one of: DEPOSIT, WITHDRAWAL, TRANSFER

### Error Response Format

```json
{
  "error": "Validation failed",
  "details": [
    {
      "field": "amount",
      "message": "Amount must be a positive number"
    },
    {
      "field": "currency",
      "message": "Invalid currency code"
    }
  ]
}
```

---

## 📊 Sample Requests

### Create a Transfer Transaction

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccount": "ACC-12345",
    "toAccount": "ACC-67890",
    "amount": 100.50,
    "currency": "USD",
    "type": "TRANSFER"
  }'
```

### Get Account Balance

```bash
curl http://localhost:8080/api/accounts/ACC-12345/balance
```

### Get Filtered Transactions

```bash
curl "http://localhost:8080/api/transactions?accountId=ACC-12345&type=DEPOSIT"
```

### Calculate Interest

```bash
curl "http://localhost:8080/api/accounts/ACC-12345/interest?rate=0.05&days=30"
```

---

## 🧪 Testing

The application includes comprehensive test coverage:

### Unit Tests
- **TransactionServiceTest** - Service layer logic
- **AccountServiceTest** - Balance and summary calculations

### Integration Tests
- **TransactionControllerIntegrationTest** - API endpoint testing
- **AccountControllerIntegrationTest** - Account endpoint testing

### Running Tests

```bash
# Run all tests
./demo/test.sh        # Unix/Mac
demo\test.bat         # Windows

# Or use Maven directly
mvn clean test
```

---

## 🎯 Architecture Decisions

### 1. **In-Memory Storage with ConcurrentHashMap**
   - **Rationale**: Thread-safe, fast access, no database setup required
   - **Trade-off**: Data is lost on application restart

### 2. **BigDecimal for Monetary Amounts**
   - **Rationale**: Precise decimal arithmetic, no floating-point errors
   - **Use Case**: Critical for financial calculations

### 3. **Lombok for Boilerplate Reduction**
   - **Rationale**: Reduces getter/setter/constructor code
   - **Benefit**: Cleaner, more maintainable code

### 4. **Custom Validation Annotations**
   - **Rationale**: Declarative, reusable validation logic
   - **Benefit**: Clear separation of concerns

### 5. **Layered Architecture**
   - **Rationale**: Separation of concerns, testability, maintainability
   - **Structure**: Controller → Service → Repository

### 6. **Global Exception Handler**
   - **Rationale**: Centralized error handling, consistent error responses
   - **Benefit**: Clean controller code, better user experience

---

## 🚀 Quick Start

See [HOWTORUN.md](HOWTORUN.md) for detailed instructions.

```bash
# Clone and navigate to project
cd homework-1

# Run the application (builds automatically)
./demo/run.sh        # Unix/Mac
demo\run.bat         # Windows

# Access the API
open http://localhost:8080/api/transactions
```

---

## 📁 Project Files

- `pom.xml` - Maven dependencies and build configuration
- `src/main/` - Application source code
- `src/test/` - Test files
- `demo/` - Scripts and sample files
  - `run.sh` / `run.bat` - Launch scripts
  - `test.sh` / `test.bat` - Test scripts
  - `sample-requests.http` - VS Code REST Client samples
  - `sample-requests.sh` - curl command samples
  - `sample-data.json` - Sample transaction data

---

## 📝 Homework Requirements Completed

### Task 1: Core API Implementation ✅
- ✅ POST /transactions - Create transaction
- ✅ GET /transactions - List all transactions
- ✅ GET /transactions/:id - Get by ID
- ✅ GET /accounts/:accountId/balance - Get balance

### Task 2: Transaction Validation ✅
- ✅ Amount validation (positive, 2 decimals)
- ✅ Account number format validation (ACC-XXXXX)
- ✅ Currency validation (ISO 4217)
- ✅ Detailed error messages

### Task 3: Transaction History ✅
- ✅ Filter by accountId
- ✅ Filter by type
- ✅ Filter by date range
- ✅ Multiple filters combined

### Task 4: Additional Features ✅
- ✅ **Option A**: Transaction Summary Endpoint
- ✅ **Option B**: Interest Calculation

---

<div align="center">

### ✨ Built with AI-Assisted Development

*This project demonstrates effective use of AI coding assistants for rapid, high-quality application development.*

</div>
