# Claude Code Project Rules — Virtual Card Lifecycle

These rules steer AI behavior when working on this codebase. They cover naming conventions, architectural patterns, anti-patterns to avoid, and error handling standards.

---

## Naming Conventions

- **Python modules and packages**: `snake_case` (e.g., `card_service.py`, `spending_limit.py`)
- **Classes**: `PascalCase` (e.g., `CardService`, `AuditEvent`, `SpendingLimitResponse`)
- **Functions and variables**: `snake_case` (e.g., `create_card`, `masked_pan`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_CARDS_PER_DAY`, `DEFAULT_PAGE_SIZE`)
- **URL paths**: `kebab-case` with API version prefix (e.g., `/api/v1/cards/{card-id}/freeze`)
- **Database tables**: `snake_case`, plural (e.g., `cards`, `audit_events`, `spending_limits`)
- **Database columns**: `snake_case` (e.g., `user_id`, `created_at`, `masked_pan`)
- **Enum members**: `UPPER_SNAKE_CASE` (e.g., `CardStatus.ACTIVE`, `LimitType.PER_TRANSACTION`)

---

## Architectural Patterns

### Follow these patterns:

1. **Repository Pattern** — Data access is encapsulated in repository classes (`CardRepository`, `TransactionRepository`). Services never call the ORM directly.

2. **Service Layer** — Business logic lives in service classes (`CardService`, `SpendingLimitService`). Services depend on repositories via dependency injection.

3. **DTOs (Data Transfer Objects)** — Use Pydantic models for all API input/output. Never return ORM models directly from API endpoints.

4. **Dependency Injection** — Use FastAPI's `Depends()` for injecting services, repositories, database sessions, and the current user.

5. **Structured Error Responses** — All errors follow the format:
   ```json
   {
     "error": {
       "code": "CARD_ALREADY_FROZEN",
       "message": "Card is already in FROZEN state",
       "details": []
     }
   }
   ```

6. **Event-Driven Audit** — Every state mutation emits an audit event in the same database transaction as the business operation.

---

## Anti-Patterns — What to Avoid

- **Never use `float` for money** — always `Decimal` in Python and `NUMERIC(19,4)` in PostgreSQL
- **Never log sensitive card data** — PAN, CVV, and full card numbers must never appear in logs, error messages, or audit snapshots
- **Never use raw SQL strings** — always use parameterized queries via SQLAlchemy ORM or `text()` with bound parameters
- **Never use `print()` for logging** — use `structlog` or the `logging` module with structured JSON output
- **Never return ORM objects from API endpoints** — serialize through Pydantic response models
- **Never hardcode secrets** — database URLs, JWT keys, encryption keys must come from environment variables
- **Never allow type coercion in request models** — use Pydantic `strict=True` mode
- **Never skip audit events** — if a mutation occurs without an audit event, it is a bug
- **Never commit `.env` files** — use `.env.example` with placeholders; add `.env` to `.gitignore`
- **Never use `*` imports** — always import specific names

---

## Error Handling

- Define a custom exception hierarchy rooted at `AppError`:
  - `NotFoundError` → 404
  - `ValidationError` → 422
  - `AuthenticationError` → 401
  - `AuthorizationError` → 403
  - `ConflictError` → 409 (e.g., invalid state transition)
  - `RateLimitError` → 429
- Register global exception handlers in FastAPI that map exceptions to structured error responses
- Never leak stack traces, file paths, or database details in production error responses
- Log full exception details server-side at `ERROR` level with request context

---

## File Organization

```
homework-3/
├── src/
│   ├── api/              # FastAPI route handlers
│   │   ├── cards.py
│   │   ├── transactions.py
│   │   └── audit.py
│   ├── models/           # SQLAlchemy ORM models
│   │   ├── card.py
│   │   ├── transaction.py
│   │   ├── audit_event.py
│   │   ├── user.py
│   │   └── spending_limit.py
│   ├── schemas/          # Pydantic DTOs
│   │   ├── card.py
│   │   ├── transaction.py
│   │   ├── spending_limit.py
│   │   ├── audit.py
│   │   └── common.py
│   ├── services/         # Business logic
│   │   ├── card_service.py
│   │   ├── spending_limit_service.py
│   │   └── transaction_service.py
│   ├── repositories/     # Data access layer
│   │   ├── card_repository.py
│   │   └── transaction_repository.py
│   ├── middleware/        # Auth, audit, rate limiting
│   │   ├── auth.py
│   │   └── audit.py
│   └── core/             # Config, exceptions, constants
│       ├── config.py
│       ├── exceptions.py
│       └── constants.py
├── tests/
│   ├── conftest.py
│   ├── unit/
│   ├── integration/
│   └── compliance/
├── alembic/
├── pyproject.toml
└── .env.example
```

---

## Commit Messages

Use conventional commits format:

```
<type>(<scope>): <short description>

[optional body]
```

Types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`
Scopes: `cards`, `transactions`, `limits`, `audit`, `auth`, `api`, `models`, `config`

Examples:
- `feat(cards): add freeze/unfreeze endpoint with audit logging`
- `fix(limits): enforce positive Decimal validation on spending limits`
- `test(compliance): verify audit event emission on card creation`
