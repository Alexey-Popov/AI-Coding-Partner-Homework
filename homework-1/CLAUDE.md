# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew build          # Build and run tests
./gradlew compileJava    # Compile only
./gradlew bootRun        # Run application (port 3000)
./gradlew test           # Run tests
./gradlew test --tests "com.banking.validator.*"  # Run specific test class
```

## Project Overview

Java 21 Spring Boot Banking Transactions REST API with PostgreSQL persistence, Flyway migrations, and CSV export.

## Architecture

- `controller/` - REST endpoints and DTOs
- `service/` - Business logic
- `repository/` - Spring Data JPA with Specifications for filtering
- `validator/` - Custom validation logic
- `model/` - JPA entities and enums
- `util/` - CSV exporter

## Database

PostgreSQL configured via environment variables:
- `DB_HOST` (default: localhost)
- `DB_PORT` (default: 5432)
- `DB_NAME` (default: banking)
- `DB_USER` (default: postgres)
- `DB_PASSWORD`

Flyway migrations in `src/main/resources/db/migration/`.

## API Endpoints

- `POST /transactions` - Create transaction
- `GET /transactions` - List all (filters: `accountId`, `type`, `from`, `to`)
- `GET /transactions/{id}` - Get by ID
- `GET /accounts/{accountId}/balance` - Get balance
- `GET /transactions/export?format=csv` - CSV export (honors same filters)

## Validation Rules

- Amount: positive, max 2 decimal places
- Account format: `ACC-XXXXX` (X = alphanumeric)
- Currency: valid ISO 4217 codes
- Type-specific: deposit requires toAccount, withdrawal requires fromAccount, transfer requires both
