# 🏗️ Banking Transactions REST API - Architecture Document

**Project:** Banking Transactions REST API  
**Technology Stack:** Java 17+ with Spring Boot 3.x  
**Date:** January 22, 2026  
**Version:** 1.0

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Project Structure](#project-structure)
3. [Data Model](#data-model)
4. [API Endpoints Specification](#api-endpoints-specification)
5. [Validation Architecture](#validation-architecture)
6. [Data Storage Strategy](#data-storage-strategy)
7. [Error Handling Architecture](#error-handling-architecture)
8. [Middleware & Interceptors](#middleware--interceptors)
9. [Security Considerations](#security-considerations)
10. [Testing Strategy](#testing-strategy)
11. [Architecture Decisions & Rationale](#architecture-decisions--rationale)

---

## 1. Overview

This document outlines the complete architecture for a RESTful Banking Transactions API built with Spring Boot. The system supports transaction management, account balance tracking, transaction filtering, and advanced features including transaction summaries, interest calculations, CSV exports, and rate limiting.

### Key Features
- ✅ Core transaction CRUD operations
- ✅ Advanced transaction filtering and history
- ✅ Account balance calculation
- ✅ Transaction summary analytics
- ✅ Simple interest calculation
- ✅ CSV export functionality
- ✅ IP-based rate limiting
- ✅ Comprehensive validation
- ✅ In-memory data storage

---

## 2. Project Structure

```
homework-1/
├── 📄 pom.xml                              # Maven configuration
├── 📄 README.md                            # Project overview
├── 📄 HOWTORUN.md                          # Setup and run instructions
├── 📄 TASKS.md                             # Requirements document
├── 📄 architecture.md                      # This document
├── 📄 .gitignore                           # Git ignore rules
│
├── 📂 src/
│   ├── 📂 main/
│   │   ├── 📂 java/
│   │   │   └── 📂 com/banking/api/
│   │   │       ├── 📄 BankingApiApplication.java          # Main Spring Boot application
│   │   │       │
│   │   │       ├── 📂 controller/                         # REST Controllers
│   │   │       │   ├── TransactionController.java        # Transaction endpoints
│   │   │       │   ├── AccountController.java            # Account endpoints
│   │   │       │   └── ExportController.java             # Export endpoints
│   │   │       │
│   │   │       ├── 📂 model/                              # Domain models
│   │   │       │   ├── Transaction.java                  # Transaction entity
│   │   │       │   ├── TransactionType.java              # Enum: deposit/withdrawal/transfer
│   │   │       │   ├── TransactionStatus.java            # Enum: pending/completed/failed
│   │   │       │   ├── TransactionSummary.java           # Summary response DTO
│   │   │       │   └── InterestCalculation.java          # Interest response DTO
│   │   │       │
│   │   │       ├── 📂 dto/                                # Data Transfer Objects
│   │   │       │   ├── TransactionRequest.java           # Create transaction request
│   │   │       │   ├── TransactionResponse.java          # Transaction response
│   │   │       │   ├── BalanceResponse.java              # Balance response
│   │   │       │   ├── ErrorResponse.java                # Error response
│   │   │       │   └── ValidationError.java              # Validation error detail
│   │   │       │
│   │   │       ├── 📂 service/                            # Business logic layer
│   │   │       │   ├── TransactionService.java           # Transaction business logic
│   │   │       │   ├── AccountService.java               # Account operations
│   │   │       │   ├── ValidationService.java            # Validation logic
│   │   │       │   ├── ExportService.java                # CSV export logic
│   │   │       │   └── InterestService.java              # Interest calculation
│   │   │       │
│   │   │       ├── 📂 repository/                         # Data access layer
│   │   │       │   ├── TransactionRepository.java        # In-memory transaction storage
│   │   │       │   └── AccountBalanceRepository.java     # In-memory balance tracking
│   │   │       │
│   │   │       ├── 📂 validator/                          # Custom validators
│   │   │       │   ├── AccountNumberValidator.java       # Account format validation
│   │   │       │   ├── CurrencyCodeValidator.java        # ISO 4217 validation
│   │   │       │   ├── AmountValidator.java              # Amount validation
│   │   │       │   └── TransactionValidator.java         # Transaction validation
│   │   │       │
│   │   │       ├── 📂 interceptor/                        # Request interceptors
│   │   │       │   └── RateLimitInterceptor.java         # Rate limiting logic
│   │   │       │
│   │   │       ├── 📂 exception/                          # Custom exceptions
│   │   │       │   ├── GlobalExceptionHandler.java       # Global exception handling
│   │   │       │   ├── ValidationException.java          # Validation exception
│   │   │       │   ├── ResourceNotFoundException.java    # 404 exception
│   │   │       │   └── RateLimitExceededException.java   # 429 exception
│   │   │       │
│   │   │       ├── 📂 config/                             # Configuration classes
│   │   │       │   ├── WebConfig.java                    # Web MVC configuration
│   │   │       │   ├── CorsConfig.java                   # CORS configuration
│   │   │       │   └── AppConfig.java                    # General app configuration
│   │   │       │
│   │   │       └── 📂 util/                               # Utility classes
│   │   │           ├── DateTimeUtil.java                 # Date/time helpers
│   │   │           ├── CurrencyUtil.java                 # Currency utilities
│   │   │           └── CsvUtil.java                      # CSV generation helpers
│   │   │
│   │   └── 📂 resources/
│   │       ├── 📄 application.properties                  # Spring Boot configuration
│   │       ├── 📄 application-dev.properties              # Development profile
│   │       ├── 📄 application-prod.properties             # Production profile
│   │       └── 📂 static/                                 # Static resources (if needed)
│   │
│   └── 📂 test/
│       └── 📂 java/
│           └── 📂 com/banking/api/
│               ├── 📂 controller/                          # Controller tests
│               │   ├── TransactionControllerTest.java
│               │   └── AccountControllerTest.java
│               ├── 📂 service/                             # Service tests
│               │   ├── TransactionServiceTest.java
│               │   └── ValidationServiceTest.java
│               ├── 📂 validator/                           # Validator tests
│               │   └── TransactionValidatorTest.java
│               └── 📂 integration/                         # Integration tests
│                   └── ApiIntegrationTest.java
│
├── 📂 docs/
│   └── 📂 screenshots/
│       ├── ai-prompt-1.png
│       ├── ai-prompt-2.png
│       ├── api-running.png
│       └── postman-requests.png
│
└── 📂 demo/
    ├── 📄 run.sh                                          # Startup script (Unix/Mac)
    ├── 📄 run.bat                                         # Startup script (Windows)
    ├── 📄 sample-requests.http                            # REST Client requests
    └── 📄 sample-data.json                                # Sample transaction data
```

---

## 3. Data Model

### 3.1 Transaction Entity

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private String id;                    // UUID auto-generated
    private String fromAccount;           // Format: ACC-XXXXX
    private String toAccount;             // Format: ACC-XXXXX
    private BigDecimal amount;            // Positive, max 2 decimal places
    private String currency;              // ISO 4217 code (USD, EUR, GBP, JPY, etc.)
    private TransactionType type;         // DEPOSIT, WITHDRAWAL, TRANSFER
    private LocalDateTime timestamp;      // ISO 8601 format
    private TransactionStatus status;     // PENDING, COMPLETED, FAILED
}
```

### 3.2 Enumerations

```java
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER
}

public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED
}
```

### 3.3 Supporting Models

#### TransactionSummary (Response DTO)
```java
@Data
@Builder
public class TransactionSummary {
    private String accountId;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private int numberOfTransactions;
    private LocalDateTime mostRecentTransactionDate;
    private BigDecimal currentBalance;
}
```

#### InterestCalculation (Response DTO)
```java
@Data
@Builder
public class InterestCalculation {
    private String accountId;
    private BigDecimal currentBalance;
    private BigDecimal interestRate;
    private int days;
    private BigDecimal interestAmount;
    private BigDecimal projectedBalance;
    private String formula;                // "Principal × Rate × Time"
}
```

#### BalanceResponse (Response DTO)
```java
@Data
@Builder
public class BalanceResponse {
    private String accountId;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime calculatedAt;
}
```

---

## 4. API Endpoints Specification

### 4.1 Core Transaction Endpoints (Task 1)

#### **POST /api/v1/transactions**
Create a new transaction.

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

**Response:** `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer",
  "timestamp": "2026-01-22T10:30:00Z",
  "status": "completed"
}
```

**Error Response:** `400 Bad Request`
```json
{
  "error": "Validation failed",
  "timestamp": "2026-01-22T10:30:00Z",
  "path": "/api/v1/transactions",
  "details": [
    {
      "field": "amount",
      "message": "Amount must be a positive number"
    },
    {
      "field": "currency",
      "message": "Invalid currency code. Must be ISO 4217 format"
    }
  ]
}
```

---

#### **GET /api/v1/transactions**
List all transactions with optional filtering.

**Query Parameters:**
- `accountId` (optional) - Filter by account ID
- `type` (optional) - Filter by transaction type (deposit|withdrawal|transfer)
- `from` (optional) - Start date (ISO 8601: 2026-01-01)
- `to` (optional) - End date (ISO 8601: 2026-01-31)
- `status` (optional) - Filter by status (pending|completed|failed)

**Example Request:**
```
GET /api/v1/transactions?accountId=ACC-12345&type=transfer&from=2026-01-01&to=2026-01-31
```

**Response:** `200 OK`
```json
{
  "transactions": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "fromAccount": "ACC-12345",
      "toAccount": "ACC-67890",
      "amount": 100.50,
      "currency": "USD",
      "type": "transfer",
      "timestamp": "2026-01-22T10:30:00Z",
      "status": "completed"
    }
  ],
  "count": 1,
  "filters": {
    "accountId": "ACC-12345",
    "type": "transfer",
    "from": "2026-01-01",
    "to": "2026-01-31"
  }
}
```

---

#### **GET /api/v1/transactions/{id}**
Get a specific transaction by ID.

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fromAccount": "ACC-12345",
  "toAccount": "ACC-67890",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer",
  "timestamp": "2026-01-22T10:30:00Z",
  "status": "completed"
}
```

**Error Response:** `404 Not Found`
```json
{
  "error": "Transaction not found",
  "timestamp": "2026-01-22T10:30:00Z",
  "path": "/api/v1/transactions/invalid-id",
  "details": []
}
```

---

#### **GET /api/v1/accounts/{accountId}/balance**
Get current account balance.

**Response:** `200 OK`
```json
{
  "accountId": "ACC-12345",
  "balance": 5420.75,
  "currency": "USD",
  "calculatedAt": "2026-01-22T10:30:00Z"
}
```

**Error Response:** `404 Not Found`
```json
{
  "error": "Account not found",
  "timestamp": "2026-01-22T10:30:00Z",
  "path": "/api/v1/accounts/ACC-99999/balance",
  "details": []
}
```

---

### 4.2 Task 4 Feature A: Transaction Summary

#### **GET /api/v1/accounts/{accountId}/summary**
Get transaction summary for an account.

**Response:** `200 OK`
```json
{
  "accountId": "ACC-12345",
  "totalDeposits": 2500.00,
  "totalWithdrawals": 850.50,
  "numberOfTransactions": 15,
  "mostRecentTransactionDate": "2026-01-22T10:30:00Z",
  "currentBalance": 1649.50
}
```

---

### 4.3 Task 4 Feature B: Interest Calculation

#### **GET /api/v1/accounts/{accountId}/interest**
Calculate simple interest on account balance.

**Query Parameters:**
- `rate` (required) - Annual interest rate as decimal (e.g., 0.05 for 5%)
- `days` (required) - Number of days for calculation

**Example Request:**
```
GET /api/v1/accounts/ACC-12345/interest?rate=0.05&days=30
```

**Response:** `200 OK`
```json
{
  "accountId": "ACC-12345",
  "currentBalance": 1000.00,
  "interestRate": 0.05,
  "days": 30,
  "interestAmount": 4.11,
  "projectedBalance": 1004.11,
  "formula": "Interest = Principal × Rate × (Days/365) = 1000.00 × 0.05 × (30/365)"
}
```

**Validation:**
- `rate`: Must be between 0.0 and 1.0
- `days`: Must be positive integer

---

### 4.4 Task 4 Feature C: CSV Export

#### **GET /api/v1/transactions/export**
Export transactions as CSV.

**Query Parameters:** (Same as GET /transactions)
- `accountId` (optional)
- `type` (optional)
- `from` (optional)
- `to` (optional)
- `format` (required) - Must be "csv"

**Example Request:**
```
GET /api/v1/transactions/export?format=csv&accountId=ACC-12345
```

**Response:** `200 OK`
```
Content-Type: text/csv
Content-Disposition: attachment; filename="transactions_2026-01-22.csv"

id,fromAccount,toAccount,amount,currency,type,timestamp,status
550e8400-e29b-41d4-a716-446655440000,ACC-12345,ACC-67890,100.50,USD,transfer,2026-01-22T10:30:00Z,completed
...
```

---

### 4.5 Task 4 Feature D: Rate Limiting

**Implementation:** IP-based rate limiting

**Limits:**
- **100 requests per minute per IP address**
- Sliding window algorithm

**Response when exceeded:** `429 Too Many Requests`
```json
{
  "error": "Rate limit exceeded",
  "timestamp": "2026-01-22T10:30:00Z",
  "path": "/api/v1/transactions",
  "details": [
    {
      "field": "rate_limit",
      "message": "Maximum 100 requests per minute exceeded. Please try again later."
    }
  ]
}
```

**Headers:**
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1706788260
Retry-After: 45
```

---

## 5. Validation Architecture

### 5.1 Validation Layers

```
┌─────────────────────────────────────┐
│   Controller Layer                  │
│   - @Valid annotation               │
│   - Basic parameter validation      │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│   Custom Validators                 │
│   - AccountNumberValidator          │
│   - CurrencyCodeValidator           │
│   - AmountValidator                 │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│   Service Layer                     │
│   - Business logic validation       │
│   - Cross-field validation          │
└─────────────────────────────────────┘
```

### 5.2 Validation Rules

#### Amount Validation
```java
@Constraint(validatedBy = AmountValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface ValidAmount {
    String message() default "Amount must be positive with max 2 decimal places";
}
```

**Rules:**
- Must be positive (> 0)
- Maximum 2 decimal places
- Cannot exceed practical limits (e.g., < 1,000,000,000)

#### Account Number Validation
```java
@Constraint(validatedBy = AccountNumberValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface ValidAccountNumber {
    String message() default "Account number must follow format ACC-XXXXX";
}
```

**Rules:**
- Format: `ACC-` followed by exactly 5 alphanumeric characters
- Pattern: `^ACC-[A-Z0-9]{5}$`
- Case-insensitive matching, stored uppercase

#### Currency Code Validation
```java
@Constraint(validatedBy = CurrencyCodeValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface ValidCurrency {
    String message() default "Invalid currency code. Must be ISO 4217 format";
}
```

**Supported Currencies:**
```java
public class CurrencyCodeValidator {
    private static final Set<String> VALID_CURRENCIES = Set.of(
        "USD", "EUR", "GBP", "JPY", "AUD", "CAD", 
        "CHF", "CNY", "SEK", "NZD", "INR", "BRL"
    );
    
    // Uses Java Currency.getInstance() for validation
}
```

### 5.3 TransactionRequest DTO with Validation

```java
@Data
public class TransactionRequest {
    
    @NotNull(message = "From account is required")
    @ValidAccountNumber
    private String fromAccount;
    
    @NotNull(message = "To account is required")
    @ValidAccountNumber
    private String toAccount;
    
    @NotNull(message = "Amount is required")
    @ValidAmount
    private BigDecimal amount;
    
    @NotNull(message = "Currency is required")
    @ValidCurrency
    private String currency;
    
    @NotNull(message = "Transaction type is required")
    @ValidTransactionType
    private String type;
}
```

### 5.4 Error Response Format

All validation errors follow a consistent structure:

```java
@Data
@Builder
public class ErrorResponse {
    private String error;                    // Error message summary
    private LocalDateTime timestamp;         // When error occurred
    private String path;                     // Request path
    private List<ValidationError> details;   // Detailed error list
}

@Data
@Builder
public class ValidationError {
    private String field;                    // Field name
    private String message;                  // Error message
    private Object rejectedValue;            // (Optional) Invalid value
}
```

---

## 6. Data Storage Strategy

### 6.1 In-Memory Storage Architecture

```
┌──────────────────────────────────────────────────┐
│           Repository Layer                        │
├──────────────────────────────────────────────────┤
│                                                   │
│  TransactionRepository                            │
│  ┌─────────────────────────────────────────┐    │
│  │ ConcurrentHashMap<String, Transaction>  │    │
│  │   Key: Transaction ID (UUID)            │    │
│  │   Value: Transaction object             │    │
│  └─────────────────────────────────────────┘    │
│                                                   │
│  AccountBalanceRepository                         │
│  ┌─────────────────────────────────────────┐    │
│  │ ConcurrentHashMap<String, BigDecimal>   │    │
│  │   Key: Account ID                        │    │
│  │   Value: Current balance                │    │
│  └─────────────────────────────────────────┘    │
│                                                   │
│  RateLimitRepository                              │
│  ┌─────────────────────────────────────────┐    │
│  │ ConcurrentHashMap<String, RequestLog>   │    │
│  │   Key: IP Address                        │    │
│  │   Value: Request timestamps + count     │    │
│  └─────────────────────────────────────────┘    │
│                                                   │
└──────────────────────────────────────────────────┘
```

### 6.2 Repository Implementations

#### TransactionRepository Interface
```java
@Repository
public class TransactionRepository {
    private final ConcurrentHashMap<String, Transaction> transactions = 
        new ConcurrentHashMap<>();
    
    public Transaction save(Transaction transaction);
    public Optional<Transaction> findById(String id);
    public List<Transaction> findAll();
    public List<Transaction> findByAccount(String accountId);
    public List<Transaction> findByType(TransactionType type);
    public List<Transaction> findByDateRange(LocalDateTime from, LocalDateTime to);
    public void deleteAll(); // For testing
}
```

#### AccountBalanceRepository Interface
```java
@Repository
public class AccountBalanceRepository {
    private final ConcurrentHashMap<String, BigDecimal> balances = 
        new ConcurrentHashMap<>();
    
    public void updateBalance(String accountId, BigDecimal amount);
    public BigDecimal getBalance(String accountId);
    public boolean accountExists(String accountId);
}
```

### 6.3 Thread Safety Considerations

**Why ConcurrentHashMap:**
- Thread-safe without external synchronization
- Better performance than synchronized HashMap
- Supports concurrent reads and writes
- Appropriate for REST API with concurrent requests

**Transaction Processing:**
```java
@Service
public class TransactionService {
    
    @Transactional // Custom transactional behavior
    public Transaction createTransaction(TransactionRequest request) {
        // Atomic operation to ensure consistency
        synchronized(accountLock.get(request.getFromAccount())) {
            // 1. Validate sufficient balance
            // 2. Create transaction
            // 3. Update account balances
            // 4. Return transaction
        }
    }
}
```

### 6.4 Data Initialization

```java
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        // Initialize with sample accounts
        accountBalanceRepository.updateBalance("ACC-12345", new BigDecimal("5000.00"));
        accountBalanceRepository.updateBalance("ACC-67890", new BigDecimal("3000.00"));
        accountBalanceRepository.updateBalance("ACC-11111", new BigDecimal("10000.00"));
        
        // Log initialization
        log.info("Initialized {} accounts", 3);
    }
}
```

---

## 7. Error Handling Architecture

### 7.1 Exception Hierarchy

```
Exception
    └── RuntimeException
        ├── ValidationException (400)
        ├── ResourceNotFoundException (404)
        ├── RateLimitExceededException (429)
        ├── InsufficientBalanceException (400)
        └── InvalidTransactionException (400)
```

### 7.2 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(
        MethodArgumentNotValidException ex, 
        HttpServletRequest request) {
        
        List<ValidationError> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ValidationError.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .rejectedValue(error.getRejectedValue())
                .build())
            .collect(Collectors.toList());
        
        return ErrorResponse.builder()
            .error("Validation failed")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .details(errors)
            .build();
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(
        ResourceNotFoundException ex,
        HttpServletRequest request) {
        // Return 404 error response
    }
    
    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ErrorResponse handleRateLimitException(
        RateLimitExceededException ex,
        HttpServletRequest request,
        HttpServletResponse response) {
        
        // Set rate limit headers
        response.setHeader("X-RateLimit-Limit", "100");
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("X-RateLimit-Reset", 
            String.valueOf(ex.getResetTime()));
        response.setHeader("Retry-After", 
            String.valueOf(ex.getRetryAfter()));
        
        // Return 429 error response
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(
        Exception ex,
        HttpServletRequest request) {
        
        log.error("Unexpected error", ex);
        
        return ErrorResponse.builder()
            .error("Internal server error")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .details(Collections.emptyList())
            .build();
    }
}
```

### 7.3 HTTP Status Code Strategy

| Status Code | Usage | Example |
|-------------|-------|---------|
| **200 OK** | Successful GET requests | Get transaction, list transactions |
| **201 Created** | Successful POST requests | Create transaction |
| **400 Bad Request** | Validation failures | Invalid amount, currency, format |
| **404 Not Found** | Resource not found | Transaction ID or account not found |
| **429 Too Many Requests** | Rate limit exceeded | More than 100 requests/minute |
| **500 Internal Server Error** | Unexpected errors | System failures |

---

## 8. Middleware & Interceptors

### 8.1 Request Processing Pipeline

```
HTTP Request
    │
    ▼
┌─────────────────────────────┐
│ RateLimitInterceptor        │ ◄─── Check rate limits
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│ LoggingInterceptor          │ ◄─── Log request details
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│ Controller                   │ ◄─── Handle request
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│ Validation Layer            │ ◄─── Validate input
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│ Service Layer               │ ◄─── Business logic
└────────────┬────────────────┘
             │
             ▼
┌─────────────────────────────┐
│ Repository Layer            │ ◄─── Data access
└────────────┬────────────────┘
             │
             ▼
HTTP Response
```

### 8.2 Rate Limit Interceptor

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private final ConcurrentHashMap<String, RequestLog> requestLogs = 
        new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler) throws Exception {
        
        String ipAddress = getClientIpAddress(request);
        RequestLog log = requestLogs.computeIfAbsent(
            ipAddress, 
            k -> new RequestLog()
        );
        
        // Clean old entries (> 1 minute)
        log.cleanOldEntries();
        
        // Check rate limit
        if (log.getRequestCount() >= MAX_REQUESTS_PER_MINUTE) {
            throw new RateLimitExceededException(
                "Rate limit exceeded",
                log.getResetTime()
            );
        }
        
        // Record request
        log.addRequest();
        
        // Set rate limit headers
        response.setHeader("X-RateLimit-Limit", 
            String.valueOf(MAX_REQUESTS_PER_MINUTE));
        response.setHeader("X-RateLimit-Remaining", 
            String.valueOf(MAX_REQUESTS_PER_MINUTE - log.getRequestCount()));
        response.setHeader("X-RateLimit-Reset", 
            String.valueOf(log.getResetTime()));
        
        return true;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

### 8.3 Logging Interceptor

```java
@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler) {
        
        log.info("Incoming request: {} {} from {}",
            request.getMethod(),
            request.getRequestURI(),
            request.getRemoteAddr()
        );
        
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }
    
    @Override
    public void afterCompletion(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception ex) {
        
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("Request completed: {} {} - Status: {} - Duration: {}ms",
            request.getMethod(),
            request.getRequestURI(),
            response.getStatus(),
            duration
        );
    }
}
```

### 8.4 Web Configuration

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;
    
    @Autowired
    private LoggingInterceptor loggingInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/**");
        
        registry.addInterceptor(loggingInterceptor)
            .addPathPatterns("/api/**");
    }
}
```

---

## 9. Security Considerations

### 9.1 CORS Configuration

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:8080"
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Content-Type", "Authorization", "X-Requested-With"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
            "X-RateLimit-Limit",
            "X-RateLimit-Remaining",
            "X-RateLimit-Reset"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = 
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }
}
```

### 9.2 Input Sanitization

```java
@Component
public class InputSanitizer {
    
    /**
     * Sanitize string input to prevent injection attacks
     */
    public String sanitize(String input) {
        if (input == null) return null;
        
        return input
            .trim()
            .replaceAll("[<>\"']", "")  // Remove potential HTML/SQL chars
            .substring(0, Math.min(input.length(), 255)); // Limit length
    }
    
    /**
     * Validate account ID format
     */
    public boolean isValidAccountId(String accountId) {
        return accountId != null && 
               accountId.matches("^ACC-[A-Z0-9]{5}$");
    }
}
```

### 9.3 Security Best Practices

| Security Aspect | Implementation |
|-----------------|----------------|
| **Input Validation** | All inputs validated with custom validators |
| **SQL Injection** | N/A (in-memory storage, no SQL) |
| **XSS Prevention** | Input sanitization, JSON encoding |
| **CSRF Protection** | Stateless REST API (no session cookies) |
| **Rate Limiting** | IP-based, 100 requests/minute |
| **CORS** | Configured allowed origins |
| **Error Messages** | No sensitive data in error responses |
| **Logging** | No sensitive data logged |

### 9.4 Future Security Enhancements

For production systems, consider:
- **Authentication:** JWT tokens or OAuth2
- **Authorization:** Role-based access control (RBAC)
- **HTTPS:** TLS/SSL encryption
- **API Keys:** Client authentication
- **Audit Logging:** Track all transactions
- **Data Encryption:** Encrypt sensitive data at rest

---

## 10. Testing Strategy

### 10.1 Testing Pyramid

```
        ┌─────────────────┐
        │   E2E Tests     │  ◄── Integration tests (10%)
        │   (Few)         │
        └────────┬────────┘
                 │
        ┌────────▼─────────┐
        │  Integration     │  ◄── Controller + Service tests (30%)
        │  Tests (Some)    │
        └────────┬─────────┘
                 │
        ┌────────▼──────────┐
        │   Unit Tests      │  ◄── Service, Validator tests (60%)
        │   (Many)          │
        └───────────────────┘
```

### 10.2 Unit Tests

#### Test Coverage Goals
- **Services:** 90%+ coverage
- **Validators:** 100% coverage
- **Controllers:** 80%+ coverage
- **Utilities:** 90%+ coverage

#### Example Service Test
```java
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private AccountBalanceRepository accountBalanceRepository;
    
    @InjectMocks
    private TransactionService transactionService;
    
    @Test
    void createTransaction_ValidDeposit_Success() {
        // Given
        TransactionRequest request = TransactionRequest.builder()
            .fromAccount("ACC-12345")
            .toAccount("ACC-12345")
            .amount(new BigDecimal("100.00"))
            .currency("USD")
            .type("deposit")
            .build();
        
        when(accountBalanceRepository.getBalance("ACC-12345"))
            .thenReturn(new BigDecimal("1000.00"));
        
        // When
        Transaction result = transactionService.createTransaction(request);
        
        // Then
        assertNotNull(result.getId());
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        verify(accountBalanceRepository).updateBalance(
            "ACC-12345", 
            new BigDecimal("1100.00")
        );
    }
    
    @Test
    void createTransaction_InsufficientBalance_ThrowsException() {
        // Given
        TransactionRequest request = TransactionRequest.builder()
            .fromAccount("ACC-12345")
            .toAccount("ACC-67890")
            .amount(new BigDecimal("5000.00"))
            .currency("USD")
            .type("transfer")
            .build();
        
        when(accountBalanceRepository.getBalance("ACC-12345"))
            .thenReturn(new BigDecimal("100.00"));
        
        // When & Then
        assertThrows(InsufficientBalanceException.class, () ->
            transactionService.createTransaction(request)
        );
    }
}
```

#### Example Validator Test
```java
class AccountNumberValidatorTest {
    
    private AccountNumberValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new AccountNumberValidator();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "ACC-12345", "ACC-ABCDE", "ACC-A1B2C"
    })
    void isValid_ValidFormats_ReturnsTrue(String accountNumber) {
        assertTrue(validator.isValid(accountNumber, null));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "ACC-123", "ACC-1234567", "INVALID", "ACC-", "12345"
    })
    void isValid_InvalidFormats_ReturnsFalse(String accountNumber) {
        assertFalse(validator.isValid(accountNumber, null));
    }
}
```

### 10.3 Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void createTransaction_ValidRequest_Returns201() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
            .fromAccount("ACC-12345")
            .toAccount("ACC-67890")
            .amount(new BigDecimal("100.00"))
            .currency("USD")
            .type("transfer")
            .build();
        
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.amount").value(100.00))
            .andExpect(jsonPath("$.status").value("completed"));
    }
    
    @Test
    void createTransaction_InvalidAmount_Returns400() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
            .fromAccount("ACC-12345")
            .toAccount("ACC-67890")
            .amount(new BigDecimal("-100.00"))
            .currency("USD")
            .type("transfer")
            .build();
        
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation failed"))
            .andExpect(jsonPath("$.details[0].field").value("amount"));
    }
}
```

### 10.4 Test Data Management

```java
@Component
@Profile("test")
public class TestDataInitializer {
    
    @Autowired
    private AccountBalanceRepository accountBalanceRepository;
    
    public void initializeTestAccounts() {
        accountBalanceRepository.updateBalance(
            "ACC-12345", new BigDecimal("10000.00"));
        accountBalanceRepository.updateBalance(
            "ACC-67890", new BigDecimal("5000.00"));
        accountBalanceRepository.updateBalance(
            "ACC-TEST1", new BigDecimal("1000.00"));
    }
    
    public void clearAllData() {
        // Clear repositories for clean test state
    }
}
```

---

## 11. Architecture Decisions & Rationale

### 11.1 Technology Choices

| Decision | Rationale |
|----------|-----------|
| **Java 17+** | Modern LTS version with records, pattern matching, improved text blocks |
| **Spring Boot 3.x** | Industry-standard framework, comprehensive ecosystem, excellent documentation |
| **Maven** | Mature dependency management, wide adoption, extensive plugin ecosystem |
| **Lombok** | Reduces boilerplate code, improves readability with annotations |
| **JUnit 5** | Modern testing framework with better assertions and parameterized tests |
| **Mockito** | Standard mocking framework for unit tests |
| **Jackson** | Default Spring Boot JSON processor, excellent performance |

### 11.2 Architectural Patterns

#### Layered Architecture
```
Controller → Service → Repository
     ↓          ↓          ↓
   DTOs    Business    Data
           Logic       Access
```

**Benefits:**
- Clear separation of concerns
- Easy to test each layer independently
- Maintainable and scalable
- Common Spring Boot pattern

#### Repository Pattern
- Abstracts data access logic
- Easy to swap storage implementations (in-memory → database)
- Centralized data access logic
- Supports testing with mocks

#### DTO Pattern
- Separates API contracts from domain models
- Prevents over-exposure of internal structure
- Allows API versioning without domain changes
- Better validation control

### 11.3 In-Memory Storage Decision

**Chosen Approach:** `ConcurrentHashMap` for each data store

**Rationale:**
- **Requirement compliance:** Assignment specifies in-memory storage
- **Thread safety:** ConcurrentHashMap provides built-in synchronization
- **Performance:** O(1) average-case lookup and insert
- **Simplicity:** No external dependencies or setup required
- **Fast development:** Focus on API logic, not database configuration

**Trade-offs:**
| Advantage | Disadvantage |
|-----------|--------------|
| ✅ Fast development | ❌ Data lost on restart |
| ✅ No database setup | ❌ Not suitable for production |
| ✅ Thread-safe | ❌ Limited by JVM memory |
| ✅ Simple testing | ❌ No persistence layer benefits |

**Future Migration Path:**
To migrate to a real database (e.g., PostgreSQL):
1. Keep repository interfaces unchanged
2. Add JPA entities with annotations
3. Extend `JpaRepository` instead of custom implementation
4. Update configuration in `application.properties`
5. No changes needed in service or controller layers

### 11.4 API Design Decisions

#### RESTful Principles
- **Resource-based URLs:** `/transactions`, `/accounts`
- **HTTP verbs:** GET (read), POST (create)
- **Status codes:** Meaningful HTTP status codes (200, 201, 400, 404, 429)
- **Stateless:** No server-side session state

#### API Versioning
- **Strategy:** URL path versioning (`/api/v1/...`)
- **Rationale:** 
  - Clear and explicit
  - Easy to route and maintain
  - Supports multiple versions simultaneously
  - Common in enterprise APIs

#### Response Format
- **Consistent structure:** All responses follow predictable format
- **Error handling:** Standardized error response with details
- **Timestamps:** ISO 8601 format for international compatibility
- **Currency amounts:** BigDecimal for precision, avoid floating-point errors

### 11.5 Validation Strategy

**Multi-layer validation approach:**

1. **Controller Layer:** Basic parameter validation with `@Valid`
2. **Custom Validators:** Reusable validation logic with annotations
3. **Service Layer:** Business logic validation (e.g., sufficient balance)

**Benefits:**
- Early rejection of invalid requests
- Reusable validation components
- Clear error messages
- Separation of concerns

### 11.6 Rate Limiting Design

**Chosen Approach:** IP-based sliding window

**Rationale:**
- Simple to implement with ConcurrentHashMap
- Fair distribution across clients
- Sliding window prevents burst abuse
- HTTP headers provide client feedback

**Alternative Considerations:**
| Approach | Pros | Cons |
|----------|------|------|
| Fixed window | Simple | Allows burst at boundary |
| Token bucket | Smooth rate | More complex |
| API key-based | Better tracking | Requires authentication |
| **Sliding window** | ✅ Fair, prevents burst | ✅ Moderate complexity |

### 11.7 Error Handling Philosophy

**Principles:**
1. **Fail fast:** Validate early in the request pipeline
2. **Informative errors:** Provide actionable error messages
3. **Security-conscious:** Don't expose internal implementation details
4. **Consistency:** All errors follow same response format
5. **Logging:** Log errors server-side for debugging

### 11.8 Performance Considerations

| Aspect | Implementation | Expected Performance |
|--------|----------------|---------------------|
| **Data lookup** | ConcurrentHashMap | O(1) average |
| **Transaction filtering** | Stream API with filters | O(n) worst case |
| **CSV generation** | Streaming write | Memory efficient |
| **Rate limiting** | In-memory tracking | O(1) check |
| **Concurrent requests** | Thread-safe collections | High throughput |

**Optimization Opportunities:**
- Add caching for frequently accessed accounts
- Index transactions by account for faster filtering
- Implement pagination for large result sets
- Use parallel streams for large data processing

### 11.9 Scalability Considerations

**Current Limitations:**
- Single JVM instance (no horizontal scaling)
- In-memory storage (limited by heap size)
- No distributed rate limiting

**Path to Scale:**
1. **Horizontal scaling:**
   - Migrate to database (shared state)
   - Implement distributed rate limiting (Redis)
   - Add load balancer

2. **Vertical scaling:**
   - Increase JVM heap size
   - Optimize data structures
   - Add caching layer

3. **Microservices:**
   - Split into transaction service + account service
   - Independent scaling
   - Event-driven communication

### 11.10 Code Organization Principles

**Package Structure:**
- **By layer:** Clear separation (controller, service, repository)
- **By feature:** Related components together
- **Flat hierarchy:** Avoid deep nesting

**Naming Conventions:**
- **Controllers:** Suffix with `Controller` (e.g., `TransactionController`)
- **Services:** Suffix with `Service` (e.g., `TransactionService`)
- **Repositories:** Suffix with `Repository`
- **DTOs:** Suffix with `Request`/`Response`
- **Exceptions:** Suffix with `Exception`

**Code Quality:**
- Single Responsibility Principle (SRP)
- Dependency Injection for loose coupling
- Interface-based design for flexibility
- Comprehensive documentation (JavaDoc)
- Consistent formatting and style

---

## 📊 Summary

This architecture provides a **solid foundation** for building the Banking Transactions REST API with:

✅ **Complete feature set:** All required tasks + 4 bonus features  
✅ **Clean architecture:** Layered design with clear separation  
✅ **Robust validation:** Multi-layer validation with detailed error messages  
✅ **Production-ready patterns:** Exception handling, rate limiting, logging  
✅ **Scalable design:** Easy migration path to real databases  
✅ **Comprehensive testing:** Unit, integration, and E2E test strategies  
✅ **Security-conscious:** Input sanitization, CORS, rate limiting  
✅ **Well-documented:** Clear specifications and rationale  

The design balances **simplicity** for rapid development with **best practices** that prepare the codebase for future growth.

---

## 📚 References & Resources

- **Spring Boot Documentation:** https://spring.io/projects/spring-boot
- **ISO 4217 Currency Codes:** https://www.iso.org/iso-4217-currency-codes.html
- **ISO 8601 Date/Time Format:** https://www.iso.org/iso-8601-date-and-time-format.html
- **REST API Best Practices:** https://restfulapi.net/
- **Java Concurrency:** Java Concurrency in Practice (Book)
- **HTTP Status Codes:** https://developer.mozilla.org/en-US/docs/Web/HTTP/Status

---

**End of Architecture Document**
