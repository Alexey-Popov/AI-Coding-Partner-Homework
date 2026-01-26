# 🚀 Implementation Plan: Banking Transactions API (Java Spring Boot)

## 📋 Project Overview
Create a REST API for banking transactions using Spring Boot with comprehensive testing and deployment scripts.

---

## 🎯 Phase 1: Project Setup & Structure

### 1.1 Initialize Spring Boot Project
- [ ] Create Maven-based Spring Boot project structure
- [ ] Configure `pom.xml` with dependencies:
  - Spring Boot Starter Web
  - Spring Boot Starter Validation
  - Spring Boot Starter Test
  - Lombok (for reducing boilerplate code)
  - JUnit 5
  - MockMvc for integration tests
  - H2 Database (for potential future use, but using in-memory storage)
- [ ] Set up project package structure:
  ```
  src/main/java/com/banking/api/
  ├── BankingApiApplication.java (Main application class)
  ├── controller/
  │   ├── TransactionController.java
  │   └── AccountController.java
  ├── model/
  │   ├── Transaction.java
  │   ├── TransactionType.java (enum)
  │   └── TransactionStatus.java (enum)
  ├── service/
  │   ├── TransactionService.java
  │   └── AccountService.java
  ├── repository/
  │   ├── TransactionRepository.java (in-memory implementation)
  │   └── AccountRepository.java (in-memory implementation)
  ├── dto/
  │   ├── TransactionRequest.java
  │   ├── TransactionResponse.java
  │   ├── BalanceResponse.java
  │   ├── SummaryResponse.java
  │   └── ErrorResponse.java
  ├── validator/
  │   ├── TransactionValidator.java
  │   └── AccountValidator.java
  ├── exception/
  │   ├── ValidationException.java
  │   ├── ResourceNotFoundException.java
  │   └── GlobalExceptionHandler.java
  └── util/
      └── CurrencyValidator.java
  ```

### 1.2 Configuration Files
- [ ] Create `application.properties` with:
  - Server port configuration (8080)
  - Logging configuration
  - Application name
- [ ] Create `application-test.properties` for test configurations
- [ ] Set up `.gitignore` for Maven/Java projects

---

## 🎯 Phase 2: Core Domain Models & DTOs

### 2.1 Create Transaction Model
- [ ] `Transaction.java` with fields:
  - id (UUID, auto-generated)
  - fromAccount (String)
  - toAccount (String)
  - amount (BigDecimal)
  - currency (String)
  - type (TransactionType enum: DEPOSIT, WITHDRAWAL, TRANSFER)
  - timestamp (LocalDateTime)
  - status (TransactionStatus enum: PENDING, COMPLETED, FAILED)
- [ ] Use Lombok annotations (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- [ ] Add validation annotations (@NotNull, @Positive, etc.)

### 2.2 Create DTOs
- [ ] `TransactionRequest.java` - for creating transactions
- [ ] `TransactionResponse.java` - for returning transaction data
- [ ] `BalanceResponse.java` - for account balance
- [ ] `SummaryResponse.java` - for account summary (Task 4, Option A)
- [ ] `ErrorResponse.java` - for error responses with field-level details

---

## 🎯 Phase 3: Repository Layer (In-Memory Storage)

### 3.1 Transaction Repository
- [ ] Create `TransactionRepository.java` interface
- [ ] Implement in-memory storage using `ConcurrentHashMap<String, Transaction>`
- [ ] Methods:
  - `save(Transaction)` - add new transaction
  - `findById(String id)` - get transaction by ID
  - `findAll()` - get all transactions
  - `findByAccountId(String accountId)` - filter by account
  - `findByType(TransactionType type)` - filter by type
  - `findByDateRange(LocalDateTime from, LocalDateTime to)` - filter by date
  - `findWithFilters(...)` - combined filtering

### 3.2 Account Repository (for balance tracking)
- [ ] Create `AccountRepository.java` interface
- [ ] Implement in-memory storage using `ConcurrentHashMap<String, BigDecimal>`
- [ ] Methods:
  - `getBalance(String accountId)` - get current balance
  - `updateBalance(String accountId, BigDecimal amount)` - update balance
  - `initializeAccount(String accountId, BigDecimal initialBalance)` - create account

---

## 🎯 Phase 4: Validation Layer

### 4.1 Custom Validators
- [ ] `AccountValidator.java`:
  - Validate account number format: `ACC-XXXXX` (alphanumeric)
  - Regex pattern: `^ACC-[A-Z0-9]{5}$`
- [ ] `CurrencyValidator.java`:
  - Validate ISO 4217 currency codes (USD, EUR, GBP, JPY, etc.)
  - Use Java Currency class or maintain whitelist
- [ ] `TransactionValidator.java`:
  - Amount validation: positive, max 2 decimal places
  - Combined validation logic

### 4.2 Bean Validation Annotations
- [ ] Create custom annotations:
  - `@ValidAccountNumber`
  - `@ValidCurrency`
  - `@ValidAmount`
- [ ] Implement ConstraintValidator for each custom annotation

---

## 🎯 Phase 5: Service Layer

### 5.1 Transaction Service
- [ ] Create `TransactionService.java`
- [ ] Implement business logic:
  - `createTransaction(TransactionRequest)` - validate and create
  - `getTransactionById(String id)` - retrieve by ID
  - `getAllTransactions()` - retrieve all
  - `getFilteredTransactions(...)` - with filters (accountId, type, dateRange)
  - Generate UUID for transaction ID
  - Set timestamp automatically
  - Initial status as PENDING, then update to COMPLETED

### 5.2 Account Service
- [ ] Create `AccountService.java`
- [ ] Implement:
  - `getBalance(String accountId)` - calculate from transactions
  - `calculateBalance(String accountId)` - sum all transactions
  - `getAccountSummary(String accountId)` - Task 4, Option A implementation
  - `calculateInterest(String accountId, double rate, int days)` - Task 4, Option B

---

## 🎯 Phase 6: Controller Layer (REST API Endpoints)

### 6.1 Transaction Controller
- [ ] Create `TransactionController.java`
- [ ] Implement endpoints:
  - **POST** `/api/transactions` - Create transaction
    - Request body: TransactionRequest
    - Response: TransactionResponse with 201 CREATED
  - **GET** `/api/transactions` - List all transactions
    - Query params: accountId, type, from, to
    - Response: List<TransactionResponse> with 200 OK
  - **GET** `/api/transactions/{id}` - Get by ID
    - Path variable: id
    - Response: TransactionResponse with 200 OK
    - Error: 404 NOT FOUND if not exists

### 6.2 Account Controller
- [ ] Create `AccountController.java`
- [ ] Implement endpoints:
  - **GET** `/api/accounts/{accountId}/balance` - Get balance
    - Response: BalanceResponse with 200 OK
  - **GET** `/api/accounts/{accountId}/summary` - Get summary (Task 4A)
    - Response: SummaryResponse with 200 OK
  - **GET** `/api/accounts/{accountId}/interest` - Calculate interest (Task 4B)
    - Query params: rate, days
    - Response: Interest calculation result

### 6.3 Response Status Codes
- [ ] 200 OK - Successful GET requests
- [ ] 201 CREATED - Successful POST requests
- [ ] 400 BAD REQUEST - Validation errors
- [ ] 404 NOT FOUND - Resource not found
- [ ] 500 INTERNAL SERVER ERROR - Server errors

---

## 🎯 Phase 7: Exception Handling

### 7.1 Custom Exceptions
- [ ] `ValidationException.java` - for validation errors
- [ ] `ResourceNotFoundException.java` - for 404 errors

### 7.2 Global Exception Handler
- [ ] Create `GlobalExceptionHandler.java` with @ControllerAdvice
- [ ] Handle:
  - `ValidationException` → 400 with ErrorResponse
  - `ResourceNotFoundException` → 404 with ErrorResponse
  - `MethodArgumentNotValidException` → 400 with field errors
  - Generic exceptions → 500

---

## 🎯 Phase 8: Testing

### 8.1 Unit Tests
- [ ] **Service Layer Tests** (using JUnit 5 & Mockito):
  - `TransactionServiceTest.java`:
    - Test create transaction
    - Test get by ID (success & not found)
    - Test filtering (by account, type, date)
    - Test validation scenarios
  - `AccountServiceTest.java`:
    - Test balance calculation
    - Test summary generation
    - Test interest calculation

### 8.2 Integration Tests
- [ ] **Controller Tests** (using MockMvc):
  - `TransactionControllerTest.java`:
    - Test POST /api/transactions (valid & invalid)
    - Test GET /api/transactions (all & filtered)
    - Test GET /api/transactions/{id}
  - `AccountControllerTest.java`:
    - Test GET /api/accounts/{accountId}/balance
    - Test GET /api/accounts/{accountId}/summary
    - Test GET /api/accounts/{accountId}/interest

### 8.3 Validation Tests
- [ ] Test account number format validation
- [ ] Test currency code validation
- [ ] Test amount validation (positive, 2 decimals)
- [ ] Test error response format

### 8.4 Test Coverage
- [ ] Aim for >80% code coverage
- [ ] Test happy paths and error scenarios
- [ ] Test edge cases (null values, empty strings, etc.)

---

## 🎯 Phase 9: Scripts & Documentation

### 9.1 Launch Scripts (in `/demo` folder)
- [ ] **Unix/Mac**: `run.sh`
  ```bash
  #!/bin/bash
  mvn clean install
  mvn spring-boot:run
  ```
- [ ] **Windows**: `run.bat`
  ```batch
  @echo off
  mvn clean install
  mvn spring-boot:run
  ```
- [ ] Make scripts executable: `chmod +x run.sh`

### 9.2 Test Scripts
- [ ] **Unix/Mac**: `test.sh`
  ```bash
  #!/bin/bash
  mvn clean test
  ```
- [ ] **Windows**: `test.bat`
  ```batch
  @echo off
  mvn clean test
  ```

### 9.3 Sample Requests
- [ ] Create `sample-requests.http` (VS Code REST Client format):
  - Create transaction examples
  - Get all transactions
  - Get by ID
  - Get with filters
  - Get account balance
  - Get account summary
  - Error scenarios
- [ ] Create `sample-requests.sh` (curl commands):
  - Same examples using curl

### 9.4 Sample Data
- [ ] Create `sample-data.json` with pre-populated transactions
- [ ] Optional: Add data initialization in application startup

---

## 🎯 Phase 10: Documentation

### 10.1 README.md
- [ ] Project title and description
- [ ] Features implemented (all 3 required tasks + 1 additional)
- [ ] Technology stack:
  - Java 17+
  - Spring Boot 3.x
  - Maven
  - JUnit 5
- [ ] Architecture overview:
  - Layered architecture (Controller → Service → Repository)
  - In-memory storage design
  - Validation approach
- [ ] API endpoints table
- [ ] Project structure
- [ ] Architecture decisions and rationale

### 10.2 HOWTORUN.md
- [ ] Prerequisites:
  - Java 17 or higher
  - Maven 3.6+
- [ ] Step-by-step instructions:
  1. Clone/download repository
  2. Navigate to homework-1 folder
  3. Run `./demo/run.sh` or `demo\run.bat`
  4. Access API at http://localhost:8080
- [ ] Running tests: `./demo/test.sh` or `demo\test.bat`
- [ ] Testing API with sample requests
- [ ] Troubleshooting section

---

## 🎯 Phase 11: Additional Features (Task 4)

### 11.1 Transaction Summary Endpoint (Option A) ✅
- [ ] Implement GET `/api/accounts/{accountId}/summary`
- [ ] Return:
  - Total deposits
  - Total withdrawals
  - Number of transactions
  - Most recent transaction date
- [ ] Add tests for summary endpoint

### 11.2 Interest Calculation (Option B) ✅
- [ ] Implement GET `/api/accounts/{accountId}/interest?rate=0.05&days=30`
- [ ] Simple interest formula: `Principal × Rate × Time`
- [ ] Validate rate (0-1) and days (positive)
- [ ] Add tests for interest calculation

---

## 🎯 Phase 12: Final Polish

### 12.1 Code Quality
- [ ] Run code formatter
- [ ] Remove unused imports
- [ ] Add JavaDoc comments to public methods
- [ ] Ensure consistent naming conventions
- [ ] Review and refactor complex methods

### 12.2 Configuration
- [ ] Review all configuration files
- [ ] Ensure proper logging levels
- [ ] Add application banner (optional)

### 12.3 Git & Version Control
- [ ] Ensure `.gitignore` excludes:
  - target/
  - .idea/
  - *.iml
  - .DS_Store
  - .vscode/
- [ ] Clean commit history
- [ ] Meaningful commit messages

---

## 📊 Success Criteria Checklist

### Core Requirements ✅
- [x] All 4 endpoints from Task 1 working
- [x] Validation logic from Task 2 implemented
- [x] Transaction filtering from Task 3 working
- [x] At least 1 additional feature from Task 4
- [x] In-memory storage (no database)
- [x] Proper HTTP status codes
- [x] Error handling with meaningful messages

### Testing ✅
- [x] Unit tests for services
- [x] Integration tests for controllers
- [x] Validation tests
- [x] >80% code coverage

### Documentation ✅
- [x] README.md with architecture decisions
- [x] HOWTORUN.md with clear instructions
- [x] Sample requests (HTTP & shell scripts)

### Scripts ✅
- [x] Launch scripts (run.sh, run.bat)
- [x] Test scripts (test.sh, test.bat)
- [x] All scripts tested and working

---

## 🚀 Execution Order

1. **Day 1**: Phase 1-2 (Setup & Models)
2. **Day 2**: Phase 3-4 (Repository & Validation)
3. **Day 3**: Phase 5-6 (Service & Controllers)
4. **Day 4**: Phase 7-8 (Exception Handling & Testing)
5. **Day 5**: Phase 9-12 (Scripts, Documentation & Polish)

---

## 📝 Notes & Assumptions

- Using **Java 17** for modern language features
- Using **Spring Boot 3.2.x** (latest stable)
- Using **Maven** as build tool
- Using **Lombok** to reduce boilerplate
- Using **JUnit 5** and **Mockito** for testing
- Using **BigDecimal** for monetary amounts (precision)
- Transaction IDs generated as UUIDs
- Account balances calculated from transaction history
- Initial account balance assumed as 0 if not specified
- All timestamps in ISO 8601 format (via LocalDateTime)
- Currency validation against ISO 4217 standard
- Thread-safe in-memory storage using ConcurrentHashMap
- RESTful API design principles
- JSON as request/response format

---

## 🔧 Technology Decisions Rationale

| Decision | Rationale |
|----------|-----------|
| **Spring Boot** | Industry-standard, comprehensive ecosystem, built-in features |
| **BigDecimal** | Precise decimal arithmetic for financial calculations |
| **Lombok** | Reduces boilerplate, cleaner code |
| **ConcurrentHashMap** | Thread-safe in-memory storage |
| **Bean Validation** | Declarative validation, standard approach |
| **MockMvc** | Integration testing without starting full server |
| **Maven** | Standard Java build tool, dependency management |

---

<div align="center">

### ✅ Ready for Implementation

**Please review this plan and approve or suggest modifications before proceeding.**

</div>
