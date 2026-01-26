# 🏦 Homework 1: Banking Transactions API

> **Student Name**: Oleksandr Stadnyk
> **Date Submitted**: 24 January 2026
> **AI Tools Used**: GitHub Copilot

---

## 📋 Project Overview

This is a fully-functional REST API for managing banking transactions, built with Java 21 and Spring Boot 3.2.1. The API provides endpoints for creating transactions, retrieving transaction history with advanced filtering, checking account balances, and generating account summaries.

### Key Features Implemented

- ✅ **Complete CRUD Operations** - Create, read, and list transactions
- ✅ **Multi-Currency Support** - ISO 4217 currency validation (USD, EUR, GBP, JPY, and 50+ more)
- ✅ **Advanced Validation** - Comprehensive input validation with detailed error messages
- ✅ **Transaction Filtering** - Filter by account, type, and date range with support for combined filters
- ✅ **Duplicate Detection** - Prevents duplicate transactions within a 5-minute window
- ✅ **Account Balance Calculation** - Multi-currency balance tracking per account
- ✅ **Transaction Summary** - Statistical overview including deposit/withdrawal counts
- ✅ **Security** - Protected against enum ordinal injection attacks
- ✅ **In-Memory Storage** - Fast ConcurrentHashMap-based storage for concurrent access

### Technology Stack

- **Language**: Java 21.0.8
- **Framework**: Spring Boot 3.2.1
- **Build Tool**: Gradle 8.5
- **Server**: Embedded Tomcat (Port 3000)
- **Data Storage**: In-memory ConcurrentHashMap
- **JSON Processing**: Jackson
- **Architecture**: RESTful API with layered architecture (Controller → Service → Model)

---

## 🎯 Tasks Completed

### ✅ Task 1: Core API Implementation
All required endpoints implemented and fully functional:
- `POST /transactions` - Create new transaction with auto-generated UUID
- `GET /transactions` - List all transactions
- `GET /transactions/:id` - Get specific transaction by ID (404 if not found)
- `GET /accounts/:accountId/balance` - Calculate account balance across all currencies

### ✅ Task 2: Transaction Validation
Comprehensive validation with field-level error reporting:
- **Amount**: Must be positive with maximum 2 decimal places
- **Account Format**: Strict `ACC-XXXXX` pattern (alphanumeric)
- **Currency**: ISO 4217 code validation (uppercase only)
- **Transaction Type**: Only accepts "deposit", "withdrawal", or "transfer" (lowercase)
- **Account Duplication**: Prevents same account in fromAccount and toAccount
- **Duplicate Transactions**: Detects and blocks identical transactions within 5 minutes

### ✅ Task 3: Transaction Filtering
Advanced filtering capabilities on `GET /transactions`:
- **By Account**: `?accountId=ACC-12345` (matches fromAccount OR toAccount)
- **By Type**: `?type=transfer` (deposit, withdrawal, or transfer)
- **By Date Range**: `?from=2026-01-01&to=2026-01-31` (ISO 8601 format)
- **Combined Filters**: All filters can be combined simultaneously

### ✅ Task 4: Transaction Summary Endpoint (Option A)
`GET /accounts/:accountId/summary` returns:
- **totalDeposits**: Count of deposit transactions
- **totalWithdrawals**: Count of withdrawal transactions
- **numberOfTransactions**: Total transaction count
- **mostRecentTransactionDate**: Latest transaction timestamp

---

## 🏗️ Architecture Decisions

### Layered Architecture
- **Controller Layer** - REST endpoints, request/response handling
- **Service Layer** - Business logic, validation, and data management
- **Model Layer** - Transaction entity with enums for type and status
- **DTO Layer** - Data transfer objects for responses
- **Exception Layer** - Custom exceptions with global exception handling

### Validation Strategy
All validation is centralized in `TransactionService.createTransaction()`:
1. Field-level validation with accumulated errors
2. Format validation (regex patterns, currency codes)
3. Business rule validation (duplicate detection, account matching)
4. Structured error responses with `ValidationErrorResponse` DTO

### Enum Security
Protected against enum ordinal injection using `@JsonCreator`:
- Custom `fromValue()` methods enforce string-only deserialization
- Numeric values like "0", "1", "2" are rejected
- Clear error messages guide users to valid values

### Concurrency
`ConcurrentHashMap` ensures thread-safe operations without explicit locking, suitable for high-concurrency scenarios.

### HTTP Status Codes
- `200 OK` - Successful GET requests
- `201 Created` - Successful transaction creation
- `400 Bad Request` - Validation errors with detailed field-level messages
- `404 Not Found` - Transaction ID not found
- `500 Internal Server Error` - Unexpected server errors

---

## 📊 API Endpoints Summary

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| POST | `/transactions` | Create new transaction | 201, 400, 500 |
| GET | `/transactions` | List all transactions (with optional filters) | 200, 500 |
| GET | `/transactions/:id` | Get transaction by ID | 200, 404, 500 |
| GET | `/accounts/:accountId/balance` | Get account balance | 200, 500 |
| GET | `/accounts/:accountId/summary` | Get account summary statistics | 200, 500 |

---

## 🔒 Security Features

1. **Enum Ordinal Protection** - Jackson `@JsonCreator` prevents numeric enum injection
2. **Input Sanitization** - Strict regex patterns for account numbers
3. **Currency Whitelist** - Only valid ISO 4217 codes accepted
4. **Duplicate Prevention** - Time-based duplicate detection mechanism
5. **Account Validation** - Prevents self-transfer operations


<div align="center">

*This project was completed as part of the AI-Assisted Development course.*

</div>
