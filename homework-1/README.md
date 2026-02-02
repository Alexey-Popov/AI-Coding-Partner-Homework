# Banking Transactions REST API

> **Student Name**: [Your Name]
> **Date Submitted**: 2026-02-02
> **AI Tools Used**: Claude Code (Opus 4.5)

---

## Project Overview

A REST API for managing banking transactions, built with Micronaut and Java 21. Uses in-memory storage with thread-safe concurrent data structures.

### Features

- Create, list, and retrieve banking transactions
- Filter transactions by account, type, and date range
- Calculate account balances from transaction history
- Account transaction summaries (deposits, withdrawals, count, most recent date)
- Input validation with meaningful error messages

### Architecture

```
src/main/java/com/banking/
  Application.java          - Entry point
  controller/
    TransactionController.java    - REST endpoints
    GlobalExceptionHandler.java   - Validation error handling
  service/
    TransactionService.java       - Business logic & validation
  model/
    Transaction.java              - Data model with Jakarta validation
  repository/
    TransactionRepository.java    - In-memory ConcurrentHashMap storage
```

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | /transactions | Create a transaction |
| GET | /transactions | List/filter transactions |
| GET | /transactions/{id} | Get transaction by ID |
| GET | /accounts/{accountId}/balance | Get account balance |
| GET | /accounts/{accountId}/summary | Get account summary |

### Tech Stack

- Java 21, Micronaut 4, Gradle
- Jakarta Validation for input validation
- Micronaut Serde (Jackson) for JSON serialization
- ConcurrentHashMap for thread-safe in-memory storage

---

*This project was completed as part of the AI-Assisted Development course.*
