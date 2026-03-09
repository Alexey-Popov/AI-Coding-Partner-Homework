# Banking Transactions API

> **Student Name**: Oleksandr Stadnyk
> **Date Submitted**: 2026-01-26
> **AI Tools Used**: Claude Code

---

A REST API for managing banking transactions built with Spring Boot and Java 17.

## Overview

This application provides endpoints for:
- Creating transactions (deposits, withdrawals, transfers)
- Querying transactions with filters
- Checking account balances and summaries

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Validation** - Request validation with custom validators
- **Gradle** - Build tool

## Project Structure

```
├── src/main/java/com/example/banking/
│   ├── controller/          # REST controllers
│   ├── dto/                  # Request/Response DTOs
│   ├── exception/            # Global exception handling
│   ├── model/                # Domain models
│   ├── repository/           # Data access layer
│   ├── service/              # Business logic
│   └── validation/           # Custom validators
├── demo/
│   ├── run.sh                # Script to run the application
│   ├── sample-requests.http  # Sample HTTP requests
│   └── sample-data.json      # Sample transaction data
└── docs/screenshots/         # AI interaction screenshots
```

## Quick Start

```bash
# Run the application
./demo/run.sh

# Or using Gradle directly
./gradlew bootRun
```

The API will be available at **http://localhost:8080**

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/transactions` | Create a new transaction |
| GET | `/transactions` | Get all transactions (with optional filters) |
| GET | `/transactions/{id}` | Get transaction by ID |
| GET | `/accounts/{accountId}/balance` | Get account balance |
| GET | `/accounts/{accountId}/summary` | Get account summary |

## Transaction Types

- **deposit** - Add funds to an account
- **withdrawal** - Remove funds from an account
- **transfer** - Move funds between accounts

**Note:** Both `fromAccount` and `toAccount` are required for all transaction types.

## Validation

The API includes comprehensive validation:
- Both fromAccount and toAccount are required
- ISO 4217 currency codes (USD, EUR, GBP, etc.)
- Account number format validation
- Amount must be positive with max 2 decimal places
- All required fields must be present

## Running Tests

```bash
./gradlew test
```

## Documentation

- [HOWTORUN.md](HOWTORUN.md) - Detailed setup and run instructions
- [demo/sample-requests.http](demo/sample-requests.http) - Sample API requests
- [demo/sample-data.json](demo/sample-data.json) - Sample transaction data
