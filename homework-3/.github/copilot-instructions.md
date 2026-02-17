# Copilot Instructions — Virtual Card Lifecycle Management

> These rules apply to ALL code generation, completion, and suggestion in this project.

---

## Project Context

This is a **regulated FinTech** application managing virtual card lifecycles.
Tech stack: Python 3.11+, FastAPI, SQLAlchemy 2.0 (async), PostgreSQL, Redis, pytest.

---

## ALWAYS Do

- Use `Decimal` for all monetary values; import from `decimal` module.
- Use `UUID` for all entity IDs; generate via `uuid.uuid4()`.
- Use `datetime` with `timezone.utc` for all timestamps.
- Use `async def` for all route handlers and service methods.
- Use `Depends()` for dependency injection in FastAPI routes.
- Use `structlog` for logging; produce JSON output.
- Use parameterized queries via SQLAlchemy ORM; never construct SQL strings.
- Mask card PANs in all responses and logs: `**** **** **** {last_four}`.
- Emit an audit event for every state-changing operation.
- Include type hints on all function signatures.
- Write Google-style docstrings on all public functions.
- Validate inputs with Pydantic models before any processing.
- Return the standard error envelope for all error responses:
  ```json
  {"error": {"code": "MACHINE_READABLE", "message": "...", "details": {}}}
  ```

## NEVER Do

- NEVER use `float` for money — always `Decimal`.
- NEVER store a raw (full) PAN in the database or any file.
- NEVER return CVV, CVV hash, or PAN token in any API response.
- NEVER log full card numbers, CVVs, or raw PII.
- NEVER use `print()` — use `structlog` logger.
- NEVER put secrets (keys, passwords, tokens) in source code.
- NEVER use `*` wildcard imports.
- NEVER allow UPDATE or DELETE on the `audit_events` table.
- NEVER expose stack traces, SQL queries, or file paths in error responses.
- NEVER hardcode configuration values — use environment variables via `pydantic-settings`.
- NEVER use `datetime.now()` without `tz=timezone.utc`.
- NEVER skip input validation — all user input must pass through Pydantic schemas.

## Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Files | `snake_case` + layer suffix | `card_service.py`, `card_routes.py` |
| Classes | `PascalCase` | `CardService`, `SpendingLimit` |
| Pydantic schemas | `{Entity}{Action}` | `CardCreate`, `CardResponse`, `CardUpdate` |
| Functions | `snake_case`, verb-first | `create_card()`, `validate_pan()` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_PAGE_SIZE`, `DEFAULT_CURRENCY` |
| Enums | `PascalCase` class, `UPPER` members | `CardStatus.ACTIVE` |
| Test functions | `test_{what}_{scenario}` | `test_create_card_happy_path` |

## Architecture Patterns

- **Layered**: Routes → Services → Models. Routes contain no business logic.
- **Repository pattern**: Services access DB through session, not directly through engine.
- **Dependency Injection**: All services and DB sessions injected via FastAPI `Depends()`.
- **Error types**: Domain exceptions in `src/exceptions/domain.py`, mapped to HTTP codes globally.
- **Middleware stack**: SecurityHeaders → AuditCapture → Auth → Route.

## Testing Rules

- One assertion focus per test function.
- Use factory fixtures from `conftest.py` — do not construct raw dicts in tests.
- Mark async tests with `@pytest.mark.asyncio`.
- Compliance tests must regex-scan response bodies for 16-digit sequences.
- Performance tests assert p95 latency thresholds using `time.perf_counter()`.
