# Implementation Plan — Java 21 Spring Boot + PostgreSQL (CSV Export)

## Summary
Implement a minimal banking transactions REST API using Java 21 + Spring Boot with PostgreSQL persistence, validation, filtering (CSV export honors filters), and a CSV export endpoint. Tests are limited to unit tests. No scaffolding now—this plan guides future implementation.

## Architecture
- Packages: `controller`, `service`, `model`, `validator`, `repository` (Spring Data JPA), `util`.
- Models: `Transaction`, `TransactionType` (deposit | withdrawal | transfer), `TransactionStatus` (pending | completed | failed).
- Persistence: PostgreSQL via Spring Data JPA/Hibernate.
- Migrations: Flyway for schema versioning.
- Date/Time: `Instant` for timestamp (ISO 8601), `LocalDate` for filters.
- Amounts: `BigDecimal` (scale ≤ 2), stored as `NUMERIC(19,2)`.
- Config: `application.yml` with `spring.datasource.*` and `spring.jpa.*` settings sourced from environment variables (defaults to localhost:5432).

## Configuration & Env Vars
- Environment variables (recommended):
  - `DB_HOST` (default `localhost`)
  - `DB_PORT` (default `5432`)
  - `DB_NAME` (e.g., `banking`)
  - `DB_USER` (e.g., `postgres`)
  - `DB_PASSWORD`
- Spring Boot config (example `application.yml` using defaults):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:banking}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:}
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate.jdbc.time_zone: UTC
  flyway:
    enabled: true
    locations: classpath:db/migration
```

Note: You can alternatively set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` directly in the environment.

## Data Models
- `Transaction` (table: `transactions`)
  - `id` UUID (PK)
  - `from_account` VARCHAR(10) — nullable for `deposit`
  - `to_account` VARCHAR(10) — nullable for `withdrawal`
  - `amount` NUMERIC(19,2) NOT NULL CHECK (amount > 0)
  - `currency` CHAR(3) NOT NULL
  - `type` ENUM (`deposit`, `withdrawal`, `transfer`) or VARCHAR(20)
  - `timestamp` TIMESTAMPTZ NOT NULL
  - `status` ENUM (`pending`, `completed`, `failed`) or VARCHAR(20)
  - Indexes:
    - `idx_tx_by_from_account`
    - `idx_tx_by_to_account`
    - `idx_tx_by_type`
    - `idx_tx_by_timestamp`
- (Optional) `Account` (table: `accounts`) — only if you want FK constraints and pre-registered accounts
  - `id` VARCHAR(10) PK (`ACC-XXXXX`)
  - `currency` CHAR(3)
  - Note: No endpoints are required for accounts; balances are computed from transactions.

## Endpoints
- POST `/transactions` — create transaction
- GET `/transactions` — list transactions (with filters)
- GET `/transactions/{id}` — get transaction by ID
- GET `/accounts/{accountId}/balance` — account balance
- GET `/transactions/export?format=csv` — CSV export (Feature C)

## Validation
- Amount: positive, scale ≤ 2 (e.g., `amount.scale() <= 2` and `amount > 0`).
- Account: regex `^ACC-[A-Za-z0-9]{5}$` for `fromAccount`, `toAccount`.
- Currency: ISO 4217 (use `java.util.Currency.getInstance(code)`; reject invalid).
- Type-specific rules: `deposit` requires `toAccount`; `withdrawal` requires `fromAccount`; `transfer` requires both.
- Error format: `{ "error": "Validation failed", "details": [ {"field":"amount","message":"..."}, ... ] }` with HTTP 400.

## Filtering (GET /transactions)
- `accountId=ACC-12345` — include transactions where `fromAccount` or `toAccount` equals accountId.
- `type=transfer|deposit|withdrawal`.
- `from=YYYY-MM-DD&to=YYYY-MM-DD` — filter by timestamp date range (inclusive).
- Combine filters; implement via JPA Specifications or custom repository queries.

## Balance Calculation
- Compute from DB using aggregation queries:
  - deposits to `accountId`: `SUM(amount)`
  - withdrawals from `accountId`: `-SUM(amount)`
  - transfers: `-SUM(amount)` when `from_account=accountId`; `+SUM(amount)` when `to_account=accountId`
- Implement in `TransactionRepository` via JPQL/SQL or service-level aggregation.

## CSV Export
- Endpoint: `GET /transactions/export?format=csv` → `text/csv`.
- Columns: `id,fromAccount,toAccount,amount,currency,type,timestamp,status`.
- Ordering: by timestamp ascending.
- Respect same filters as `/transactions` if query params are present (decision: yes).
- Stream results to avoid loading entire dataset; escape commas/quotes.

## Error Handling & Status Codes
- 201 on successful creation; 200 for reads; 400 for validation errors; 404 for missing IDs.
- Global exception handler (`@ControllerAdvice`) for JSON error responses.

## Testing (Unit Tests Only)
- Libraries: JUnit 5.
- Validator tests: amount scale/positivity, account regex, currency acceptance, type-specific rules.
- Service tests: balance calculation aggregation logic; filtering predicate/specification logic (using stubbed repository/service data).
- CSV utility tests: formatting, escaping, header/row ordering.
- Note: No integration tests (no MockMvc or Testcontainers). Controllers can be tested indirectly via service/validator coverage.

## Implementation Phases
1. Setup: Use Gradle build; add dependencies (Spring Web, Validation, Spring Data JPA, PostgreSQL driver, Flyway); set Java 21; create base app and health check; configure `application.yml` datasource via env vars.
2. Database & Migrations: Add Flyway; create `V1__create_transactions.sql` with schema + indexes.
3. Models & Repository: `Transaction` entity, enums; `TransactionRepository` with filters and aggregates; ID generation via UUID.
4. Validation: implement validator(s); integrate into POST.
5. Core Endpoints: create/list/get/balance with proper status codes using repository.
6. Filtering: implement query params using JPA Specifications/custom queries (CSV honors these filters).
7. CSV Export: stream filtered transactions to CSV; set `Content-Type: text/csv`.
8. Tests: Unit tests only for validators, services, and CSV utils.
9. Documentation updates: README + HOWTORUN; demo scripts later.

## Milestones & Acceptance Criteria
- M1: App boots; health check returns 200 (Java 21).
- M2: Flyway migrations applied; datasource connects successfully.
- M3: Core endpoints functional with PostgreSQL persistence.
- M4: Validation returns aggregated 400 errors; happy paths pass.
- M5: Filtering supports account/type/date combinations via DB queries.
- M6: CSV export returns well-formed CSV and honors filters.
- M7: Unit tests pass (validators, services, CSV utils).

## Risks & Mitigations
- Validation edge cases (scale/locale): use `BigDecimal` and explicit scale checks.
- Time on CSV formatting: start with simple StringBuilder/`PrintWriter` and add escaping if needed.
- Currency completeness: rely on `Currency.getInstance`, optionally maintain allow-list.

## Decisions Pending
- PostgreSQL env values: confirm `DB_NAME`, `DB_USER`, and whether `DB_PASSWORD` is required in your local environment.

## Next Steps
Confirm pending decisions; then proceed to code phases 1–3. This plan lives in `homework-1/src/implementation-plan.md` for reference during implementation.
