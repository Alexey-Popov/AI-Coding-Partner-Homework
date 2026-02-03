# Homework 1:  Banking Transactions API
> **Student Name**: Ihor Chanzhar
> **Date Submitted**: 23 Jan 2026
> **AI Tools Used**: GitHub Copilot, Claude Code

# Overview
REST API for banking transactions built with Java 21, Spring Boot 3.2, and PostgreSQL.

## Features

- Create, list, and retrieve transactions
- Transaction filtering by account, type, and date range
- Account balance calculation
- CSV export with filter support
- Input validation with detailed error messages

## Architecture

- **Framework**: Spring Boot 3.2 with Spring Data JPA
- **Database**: PostgreSQL with Flyway migrations
- **Validation**: Custom validators for amount, currency, and account format

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /transactions | Create a new transaction |
| GET | /transactions | List transactions (with optional filters) |
| GET | /transactions/{id} | Get transaction by ID |
| GET | /accounts/{accountId}/balance | Get account balance |
| GET | /transactions/export | Export transactions as CSV |

## Transaction Model

```json
{
  "id": "uuid",
  "fromAccount": "ACC-XXXXX",
  "toAccount": "ACC-XXXXX",
  "amount": 100.50,
  "currency": "USD",
  "type": "transfer",
  "timestamp": "2024-01-15T10:30:00Z",
  "status": "completed"
}
```

## Query Parameters

For `GET /transactions` and `GET /transactions/export`:
- `accountId` - Filter by account (matches fromAccount or toAccount)
- `type` - Filter by type (deposit, withdrawal, transfer)
- `from` - Start date (YYYY-MM-DD, inclusive)
- `to` - End date (YYYY-MM-DD, inclusive)
