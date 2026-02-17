# Virtual Card Lifecycle Management — Specification

> Ingest the information from this file, implement the Low-Level Tasks, and generate the code that will satisfy the High and Mid-Level Objectives.

---

## High-Level Objective

Build a **Virtual Card Lifecycle Management** microservice for a regulated FinTech platform that enables cardholders to create, activate, freeze/unfreeze, set spending limits, view transactions, and close virtual cards — with full **PCI-DSS** compliance, immutable **audit trail**, and **GDPR**-compliant data handling.

---

## Mid-Level Objectives

### MO-1 · PCI-DSS Compliant Data Storage
- Store card PANs only in tokenized form; raw PANs never persist in the application database.
- Encrypt all sensitive fields (PAN token, CVV hash) at rest using AES-256.
- Mask card numbers in every API response and log entry (show last 4 digits only: `**** **** **** 1234`).
- No sensitive cardholder data in query strings, URLs, or non-encrypted logs.

### MO-2 · Immutable Audit Trail
- Record every card state transition, limit change, and admin action as an immutable audit event.
- Each event stores: `event_id` (UUID), `card_id`, `actor_id`, `actor_role`, `action`, `previous_state`, `new_state`, `timestamp`, `ip_address`, `metadata` (JSON).
- Audit records are append-only; no UPDATE or DELETE operations permitted on the audit table.
- Provide a query endpoint for compliance officers to search audit logs by card, actor, date range, or action type.

### MO-3 · Card Lifecycle State Machine
- Enforce the following state graph with strict transition rules:

```
  ┌──────────┐   activate   ┌──────────┐
  │ CREATED  │─────────────▶│  ACTIVE  │
  └──────────┘              └────┬─┬───┘
                                 │ │
                          freeze │ │ unfreeze
                                 │ │
                            ┌────▼─┴───┐
                            │  FROZEN  │
                            └──────────┘
                                 │
                    close (from ACTIVE or FROZEN)
                                 │
                            ┌────▼─────┐
                            │  CLOSED  │  (terminal — no transitions out)
                            └──────────┘
```

- Reject invalid transitions with `409 Conflict` and a machine-readable error code.
- Every transition emits an audit event (MO-2).

### MO-4 · Spending Limits & Controls
- Support three limit tiers per card: `per_transaction`, `daily`, `monthly`.
- All monetary values use `Decimal` with 2-digit precision; never `float`.
- Validate that `per_transaction ≤ daily ≤ monthly`.
- Enforce limits at transaction-authorization time (simulated endpoint).
- Limit changes emit an audit event with before/after values.

### MO-5 · Transaction Visibility
- Provide a read-only endpoint to query transactions by card_id, date range, merchant name, min/max amount.
- Mask card PAN in every transaction response.
- Paginate results (default 20, max 100 per page).
- Sort by `created_at DESC` by default.

### MO-6 · Role-Based Access Control (RBAC)
- Two roles: **cardholder** (owns cards, manages own limits, views own transactions) and **ops_compliance** (can view any card, search audit logs, freeze/close any card for compliance reasons).
- Cardholder can only access their own resources; any cross-user access returns `403 Forbidden`.
- Ops/compliance actions are flagged in the audit trail with `actor_role = "ops_compliance"`.
- Authentication uses JWT with role claims; tokens validated on every request.

### MO-7 · GDPR & Data Retention
- Support a **right-to-erasure** endpoint: pseudonymize cardholder PII (name, email) while preserving audit integrity via non-reversible hashing of `actor_id`.
- Enforce a configurable retention policy: auto-archive cards closed for > N days (default 365).
- Data exports (for data-portability requests) return only the requesting cardholder's data in JSON.
- All PII fields are annotated in models so a future DLP scan can identify them programmatically.

---

## Implementation Notes

### Tech Stack
| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Language | Python 3.11+ | Type hints, performance, ecosystem |
| Framework | FastAPI 0.110+ | Async, OpenAPI auto-docs, dependency injection |
| ORM | SQLAlchemy 2.0 (async) | Mature, type-safe, supports column-level encryption |
| Migrations | Alembic | Standard for SQLAlchemy |
| Database | PostgreSQL 16 | ACID, JSON support, row-level security |
| Cache / Rate-limit | Redis 7 | Token caching, sliding-window rate limiting |
| Testing | pytest + pytest-asyncio + httpx | Async TestClient support |
| Linting | Ruff + mypy (strict) | Fast, comprehensive, type safety |
| Formatting | Black (line-length 99) | Deterministic formatting |

### Coding Standards
- All monetary values: `Decimal` (never `float`).
- All IDs: UUID v4.
- All timestamps: UTC `datetime` with timezone info.
- All public functions: type-annotated, docstring with Args/Returns/Raises.
- No `print()` — use `structlog` for structured JSON logging.
- No raw SQL — use SQLAlchemy ORM or `text()` with parameterized queries only.
- No secrets in source — read from environment via `pydantic-settings`.

### Security
- PAN tokenization: generate a non-reversible token (HMAC-SHA256 with server-side key).
- CVV: store only bcrypt hash for verification; never return in any response.
- Rate limiting: 100 req/min per user, 10 req/min for card-creation.
- CORS: allowlist only known frontend origins.
- Input sanitization: strip control characters, enforce max lengths at Pydantic level.
- HTTP security headers: HSTS, X-Content-Type-Options, X-Frame-Options via middleware.

### Error Handling
- Consistent error envelope: `{"error": {"code": "CARD_INVALID_TRANSITION", "message": "...", "details": {...}}}`.
- Map domain exceptions to HTTP codes: `ValidationError→400`, `NotFound→404`, `InvalidTransition→409`, `Forbidden→403`, `Unauthorized→401`.
- Never leak stack traces or internal paths in production responses.

### Performance
- Single-card operations: < 50 ms p95.
- List/query operations: < 200 ms p95 for up to 1 000 records.
- Bulk operations: < 2 s for 100 cards.
- Database indexes on: `card_id`, `cardholder_id`, `status`, `created_at`, `audit.card_id + timestamp`.

---

## Context

### Beginning Context
```
homework-3/                         ← you are here (spec only — no code yet)
  specification.md                  ← this file
  agents.md                         ← AI agent guidelines
  .github/copilot-instructions.md   ← editor rules
  README.md                         ← rationale & practices
```
When implementation begins, the developer starts from an empty `src/` directory with only a Python virtual-environment and the dependency list below.

**Pre-existing artifacts available:**
- `requirements.txt` (FastAPI, SQLAlchemy, Alembic, Redis, pydantic-settings, structlog, pytest, httpx, bcrypt, cryptography)
- `.env.template` (DATABASE_URL, REDIS_URL, JWT_SECRET, PAN_HMAC_KEY, LOG_LEVEL)

### Ending Context
```
src/
├── __init__.py
├── main.py                        # FastAPI app factory, router mounting, startup/shutdown
├── config.py                      # pydantic-settings BaseSettings
├── database.py                    # async engine, session factory
├── models/
│   ├── __init__.py
│   ├── card.py                    # Card SQLAlchemy model + Pydantic schemas
│   ├── audit_event.py             # AuditEvent model (append-only table)
│   ├── transaction.py             # Transaction model
│   └── spending_limit.py          # SpendingLimit model
├── services/
│   ├── __init__.py
│   ├── card_service.py            # Create, state transitions
│   ├── spending_limit_service.py  # Set/update/validate limits
│   ├── transaction_service.py     # Query transactions
│   ├── audit_service.py           # Write & query audit events
│   └── gdpr_service.py            # Erasure, export, retention
├── routes/
│   ├── __init__.py
│   ├── card_routes.py             # /cards CRUD + transitions
│   ├── spending_limit_routes.py   # /cards/{id}/limits
│   ├── transaction_routes.py      # /cards/{id}/transactions
│   ├── audit_routes.py            # /audit (ops_compliance only)
│   └── gdpr_routes.py             # /gdpr/erase, /gdpr/export
├── middleware/
│   ├── __init__.py
│   ├── auth.py                    # JWT validation, role extraction
│   ├── audit_middleware.py        # Auto-log request metadata
│   └── security_headers.py        # HSTS, CSP, X-Frame-Options
├── validators/
│   ├── __init__.py
│   └── card_validators.py         # PAN format, limit hierarchy, email
├── utils/
│   ├── __init__.py
│   ├── pan_tokenizer.py           # HMAC-SHA256 tokenization + masking
│   ├── encryption.py              # AES-256 column encryption helpers
│   └── money.py                   # Decimal helpers, rounding rules
├── exceptions/
│   ├── __init__.py
│   └── domain.py                  # CardNotFound, InvalidTransition, etc.
├── migrations/
│   └── versions/
│       └── 001_initial.py         # Cards, audit_events, transactions, spending_limits
tests/
├── conftest.py                    # Fixtures: test DB, test client, factories
├── test_card_model.py             # 10 tests — schema validation
├── test_card_service.py           # 12 tests — CRUD + state machine
├── test_spending_limits.py        # 8 tests — limit CRUD + hierarchy
├── test_transactions.py           # 6 tests — query, pagination, masking
├── test_audit.py                  # 8 tests — event creation, query, immutability
├── test_auth_rbac.py              # 8 tests — role enforcement, JWT
├── test_gdpr.py                   # 6 tests — erasure, export, retention
├── test_integration.py            # 10 tests — full lifecycle workflows
├── test_compliance.py             # 8 tests — PAN masking, audit completeness
├── test_performance.py            # 6 tests — latency benchmarks
└── fixtures/
    ├── cards.json
    ├── transactions.csv
    └── audit_events.json
docs/
├── API_REFERENCE.md
├── ARCHITECTURE.md
└── COMPLIANCE.md
```

---

## Low-Level Tasks

---

### Task 1 · Project Scaffolding & Configuration

**What prompt would you run to complete this task?**
> Create the project directory structure, FastAPI app factory with lifespan handler, and a pydantic-settings config class that reads DATABASE_URL, REDIS_URL, JWT_SECRET, PAN_HMAC_KEY, and LOG_LEVEL from environment variables. Include a `.env.template` with placeholder values.

**What file do you want to CREATE or UPDATE?**
`src/main.py`, `src/config.py`, `.env.template`, `requirements.txt`

**What function do you want to CREATE or UPDATE?**
`create_app()` in `main.py`; `class Settings(BaseSettings)` in `config.py`

**What are details you want to add to drive the code changes?**
- Use FastAPI lifespan context manager (not deprecated `on_event`).
- Mount all routers with `/api/v1` prefix.
- Configure `structlog` for JSON logging in `create_app()`.
- Settings must raise `ValidationError` on startup if any required env var is missing.
- Include `LOG_LEVEL` with default `"INFO"`, `PORT` with default `8000`.
- Add CORS middleware with an empty default allowlist (configured via env).

---

### Task 2 · Database Engine & Session Factory

**What prompt would you run to complete this task?**
> Set up async SQLAlchemy 2.0 engine and session factory using asyncpg. Create the Base declarative model. Configure Alembic for async migrations.

**What file do you want to CREATE or UPDATE?**
`src/database.py`, `alembic.ini`, `src/migrations/env.py`

**What function do you want to CREATE or UPDATE?**
`async_engine`, `async_session_factory`, `get_db()` async generator

**What are details you want to add to drive the code changes?**
- Use `create_async_engine` with pool size 10, max overflow 20.
- `get_db()` is a FastAPI dependency that yields an `AsyncSession` and rolls back on exception.
- Alembic `env.py` must use `run_async()` with the same engine config.
- Include a health-check query (`SELECT 1`) in the lifespan startup.

---

### Task 3 · Card Data Model (PCI-DSS Compliant)

**What prompt would you run to complete this task?**
> Create the Card SQLAlchemy model and corresponding Pydantic schemas. The PAN must be tokenized via HMAC-SHA256 before storage. Include card status enum, PAN masking in all response schemas, and encrypted CVV hash column.

**What file do you want to CREATE or UPDATE?**
`src/models/card.py`, `src/utils/pan_tokenizer.py`, `src/utils/encryption.py`

**What function do you want to CREATE or UPDATE?**
`class Card(Base)`, `class CardCreate(BaseModel)`, `class CardResponse(BaseModel)`, `tokenize_pan()`, `mask_pan()`, `encrypt_column()`, `decrypt_column()`

**What are details you want to add to drive the code changes?**
- Card fields: `id` (UUID PK), `cardholder_id` (UUID FK), `pan_token` (str, unique, indexed), `pan_last_four` (str, 4 chars), `cvv_hash` (str, bcrypt), `expiry_month` (int), `expiry_year` (int), `cardholder_name` (str, encrypted at rest), `status` (enum: CREATED, ACTIVE, FROZEN, CLOSED), `created_at`, `updated_at`.
- `CardResponse` must NEVER include `pan_token` or `cvv_hash`; display number as `**** **** **** {last_four}`.
- `tokenize_pan(raw_pan: str, key: str) -> str` — HMAC-SHA256, returns hex digest.
- `mask_pan(last_four: str) -> str` — returns `"**** **** **** {last_four}"`.
- AES-256-GCM encryption for `cardholder_name` using a dedicated encryption key.
- PCI-DSS note: raw PAN exists only in-memory during card creation; never logged, never stored unencrypted.

---

### Task 4 · Audit Event Model (Append-Only)

**What prompt would you run to complete this task?**
> Create the AuditEvent SQLAlchemy model for an immutable, append-only audit log. Include Pydantic schemas for creation and read. Add an application-level guard that prevents UPDATE and DELETE on the audit table.

**What file do you want to CREATE or UPDATE?**
`src/models/audit_event.py`

**What function do you want to CREATE or UPDATE?**
`class AuditEvent(Base)`, `class AuditEventCreate(BaseModel)`, `class AuditEventResponse(BaseModel)`

**What are details you want to add to drive the code changes?**
- Fields: `id` (UUID PK), `card_id` (UUID, indexed), `actor_id` (UUID), `actor_role` (enum: cardholder, ops_compliance), `action` (str, e.g. "card.created", "card.frozen", "limit.updated"), `previous_state` (JSON nullable), `new_state` (JSON nullable), `ip_address` (str), `user_agent` (str), `metadata` (JSON), `created_at` (UTC, server_default).
- NO `updated_at` column — records are immutable.
- Application guard: `AuditService.update()` and `AuditService.delete()` raise `NotImplementedError("Audit records are immutable")`.
- Composite index on `(card_id, created_at)` for efficient timeline queries.

---

### Task 5 · Spending Limit & Transaction Models

**What prompt would you run to complete this task?**
> Create SpendingLimit and Transaction SQLAlchemy models with Pydantic schemas. SpendingLimit enforces per_transaction ≤ daily ≤ monthly using a Pydantic validator. Transaction amounts use Decimal(12,2).

**What file do you want to CREATE or UPDATE?**
`src/models/spending_limit.py`, `src/models/transaction.py`, `src/utils/money.py`

**What function do you want to CREATE or UPDATE?**
`class SpendingLimit(Base)`, `class Transaction(Base)`, `class SpendingLimitCreate(BaseModel)`, `class TransactionResponse(BaseModel)`, `validate_limit_hierarchy()`, `to_decimal()`, `round_money()`

**What are details you want to add to drive the code changes?**
- SpendingLimit fields: `id` (UUID), `card_id` (UUID, unique FK), `per_transaction` (Decimal(12,2)), `daily` (Decimal(12,2)), `monthly` (Decimal(12,2)), `updated_at`.
- Transaction fields: `id` (UUID), `card_id` (UUID, indexed), `amount` (Decimal(12,2)), `currency` (str, default "USD"), `merchant_name` (str), `merchant_category` (str), `status` (enum: APPROVED, DECLINED, PENDING), `created_at`.
- Pydantic `@model_validator(mode="after")` on `SpendingLimitCreate` to enforce `per_transaction ≤ daily ≤ monthly`.
- `TransactionResponse` masks card PAN (show last 4 only via `mask_pan()`).
- `money.py`: `to_decimal(value) -> Decimal`, `round_money(value) -> Decimal` (ROUND_HALF_UP, 2 places).

---

### Task 6 · Audit Service & Middleware

**What prompt would you run to complete this task?**
> Implement AuditService for writing and querying audit events, and an audit middleware that automatically captures request metadata (IP, user-agent) and attaches it to audit events created during the request lifecycle.

**What file do you want to CREATE or UPDATE?**
`src/services/audit_service.py`, `src/middleware/audit_middleware.py`

**What function do you want to CREATE or UPDATE?**
`AuditService.record_event()`, `AuditService.query_events()`, `AuditMiddleware.__call__()`

**What are details you want to add to drive the code changes?**
- `record_event(card_id, actor_id, actor_role, action, previous_state, new_state, metadata)` — persists an `AuditEvent`; raises if any required field is `None`.
- `query_events(card_id?, actor_id?, action?, date_from?, date_to?, page, page_size)` — paginated, ordered by `created_at DESC`.
- Middleware stores `ip_address` and `user_agent` in a `contextvars.ContextVar` so any service can access them without explicit passing.
- The middleware must NOT read or buffer the request body (performance; privacy).

---

### Task 7 · Authentication & RBAC Middleware

**What prompt would you run to complete this task?**
> Implement JWT authentication dependency and a role-based access control guard. Decode JWT from Authorization header, extract user_id and role claims, and provide FastAPI dependencies `require_cardholder` and `require_ops_compliance`.

**What file do you want to CREATE or UPDATE?**
`src/middleware/auth.py`

**What function do you want to CREATE or UPDATE?**
`decode_jwt()`, `get_current_user()`, `require_cardholder()`, `require_ops_compliance()`, `require_owner_or_ops()`

**What are details you want to add to drive the code changes?**
- JWT payload: `{"sub": "<user_uuid>", "role": "cardholder"|"ops_compliance", "exp": <timestamp>}`.
- `decode_jwt()` validates signature (HS256 with `JWT_SECRET`), expiry, and required claims; raises `401` on failure.
- `get_current_user()` is a FastAPI `Depends()` that returns a `CurrentUser(id, role)` named tuple.
- `require_owner_or_ops(card_id, current_user)` checks that the user either owns the card or has `ops_compliance` role; raises `403` otherwise.
- Rate-limit decorator: 100 req/min general, 10 req/min for card creation.

---

### Task 8 · Card Service (Create + State Transitions)

**What prompt would you run to complete this task?**
> Implement CardService with methods to create a card (with PAN tokenization) and perform state transitions (activate, freeze, unfreeze, close). Each operation must validate the transition, update the card, and emit an audit event atomically.

**What file do you want to CREATE or UPDATE?**
`src/services/card_service.py`, `src/exceptions/domain.py`

**What function do you want to CREATE or UPDATE?**
`CardService.create_card()`, `CardService.activate()`, `CardService.freeze()`, `CardService.unfreeze()`, `CardService.close()`, `CardService.get_card()`, `CardService.list_cards()`; classes `InvalidTransition`, `CardNotFound`

**What are details you want to add to drive the code changes?**
- `create_card(cardholder_id, pan, cvv, cardholder_name, expiry_month, expiry_year)`:
  1. Tokenize PAN → `pan_token`.
  2. Hash CVV with bcrypt.
  3. Encrypt `cardholder_name`.
  4. Persist card with `status = CREATED`.
  5. Emit audit event `"card.created"`.
  6. Return `CardResponse` (masked PAN).
- Transition methods (`activate`, `freeze`, `unfreeze`, `close`):
  1. Load card, verify current state allows transition (see state graph in MO-3).
  2. Update `status` and `updated_at`.
  3. Emit audit event with `previous_state` and `new_state`.
  4. Raise `InvalidTransition(current, attempted)` on illegal moves → maps to `409`.
- `list_cards(cardholder_id, status?, page, page_size)` — filters by owner; ops_compliance can pass `cardholder_id=None` to list all.
- All DB writes in a single transaction (commit or rollback together).

---

### Task 9 · Spending Limit Service

**What prompt would you run to complete this task?**
> Implement SpendingLimitService to set, update, and retrieve spending limits for a card. Validate the limit hierarchy. Emit audit events on every change with before/after values.

**What file do you want to CREATE or UPDATE?**
`src/services/spending_limit_service.py`

**What function do you want to CREATE or UPDATE?**
`SpendingLimitService.set_limits()`, `SpendingLimitService.get_limits()`, `SpendingLimitService.check_transaction()`

**What are details you want to add to drive the code changes?**
- `set_limits(card_id, per_transaction, daily, monthly, actor)`:
  1. Validate `per_transaction ≤ daily ≤ monthly` (raise `400` on violation).
  2. Upsert limits.
  3. Audit event `"limit.updated"` with `previous_state={old values}`, `new_state={new values}`.
- `check_transaction(card_id, amount)`:
  1. Load limits and today's/this month's spend totals.
  2. Return `{allowed: bool, reason?: "exceeds_daily"|"exceeds_monthly"|"exceeds_per_transaction"}`.
- Card must be in `ACTIVE` status to accept limit changes or transactions; raise `409` otherwise.

---

### Task 10 · Transaction Query Service

**What prompt would you run to complete this task?**
> Implement TransactionService for querying transactions with filtering, pagination, and PAN masking. Include a simulated authorization endpoint that checks spending limits.

**What file do you want to CREATE or UPDATE?**
`src/services/transaction_service.py`

**What function do you want to CREATE or UPDATE?**
`TransactionService.query_transactions()`, `TransactionService.authorize_transaction()`, `TransactionService.get_transaction()`

**What are details you want to add to drive the code changes?**
- `query_transactions(card_id, date_from?, date_to?, merchant?, min_amount?, max_amount?, page=1, page_size=20)`:
  - Max `page_size` capped at 100.
  - Order by `created_at DESC`.
  - All responses use `TransactionResponse` with masked PAN.
- `authorize_transaction(card_id, amount, merchant_name, merchant_category)`:
  1. Verify card is `ACTIVE`.
  2. Call `SpendingLimitService.check_transaction()`.
  3. If allowed: persist transaction with `status=APPROVED`, emit audit event.
  4. If denied: persist with `status=DECLINED`, emit audit event with reason.
- Returns `{transaction_id, status, decline_reason?}`.

---

### Task 11 · Card Routes (CRUD + Transitions)

**What prompt would you run to complete this task?**
> Create FastAPI router for card endpoints: create, get, list (with filters), activate, freeze, unfreeze, close. Apply auth dependencies and emit appropriate HTTP status codes.

**What file do you want to CREATE or UPDATE?**
`src/routes/card_routes.py`

**What function do you want to CREATE or UPDATE?**
`create_card()`, `get_card()`, `list_cards()`, `activate_card()`, `freeze_card()`, `unfreeze_card()`, `close_card()`

**What are details you want to add to drive the code changes?**
- `POST /api/v1/cards` → 201 Created; requires `cardholder` role.
- `GET /api/v1/cards` → 200; cardholder sees own cards, ops sees all.
- `GET /api/v1/cards/{card_id}` → 200 or 404; `require_owner_or_ops`.
- `POST /api/v1/cards/{card_id}/activate` → 200 or 409; `require_owner_or_ops`.
- `POST /api/v1/cards/{card_id}/freeze` → 200 or 409; `require_owner_or_ops`.
- `POST /api/v1/cards/{card_id}/unfreeze` → 200 or 409; `require_owner_or_ops`.
- `POST /api/v1/cards/{card_id}/close` → 200 or 409; `require_owner_or_ops`.
- All responses use `CardResponse` (masked PAN, no CVV).
- All 4xx/5xx responses use the standard error envelope.

---

### Task 12 · Spending Limit & Transaction Routes

**What prompt would you run to complete this task?**
> Create FastAPI routers for spending-limit management and transaction queries. Include a simulated authorization endpoint.

**What file do you want to CREATE or UPDATE?**
`src/routes/spending_limit_routes.py`, `src/routes/transaction_routes.py`

**What function do you want to CREATE or UPDATE?**
`set_limits()`, `get_limits()`, `list_transactions()`, `authorize_transaction()`

**What are details you want to add to drive the code changes?**
- `PUT /api/v1/cards/{card_id}/limits` → 200; body `{per_transaction, daily, monthly}`; `require_owner_or_ops`.
- `GET /api/v1/cards/{card_id}/limits` → 200; `require_owner_or_ops`.
- `GET /api/v1/cards/{card_id}/transactions` → 200; query params for date_from, date_to, merchant, min_amount, max_amount, page, page_size.
- `POST /api/v1/cards/{card_id}/transactions/authorize` → 200; body `{amount, merchant_name, merchant_category}`; returns `{transaction_id, status, decline_reason?}`.

---

### Task 13 · Audit & GDPR Routes

**What prompt would you run to complete this task?**
> Create FastAPI routers for audit log queries (ops_compliance only) and GDPR endpoints (erasure, data export). Ensure audit queries support filtering by card_id, actor, action, date range.

**What file do you want to CREATE or UPDATE?**
`src/routes/audit_routes.py`, `src/routes/gdpr_routes.py`, `src/services/gdpr_service.py`

**What function do you want to CREATE or UPDATE?**
`query_audit_logs()`, `erase_cardholder_data()`, `export_cardholder_data()`; `GDPRService.pseudonymize()`, `GDPRService.export_data()`

**What are details you want to add to drive the code changes?**
- `GET /api/v1/audit` → 200; query params: card_id, actor_id, action, date_from, date_to, page, page_size. **Requires `ops_compliance` role.**
- `POST /api/v1/gdpr/erase` → 200; body `{cardholder_id}`; requires `ops_compliance` role.
  - Pseudonymize: replace `cardholder_name` with `"[REDACTED]"`, email with hash, encrypted card name with redacted value.
  - Audit events: replace `actor_id` with SHA-256 hash (non-reversible) — events themselves are preserved for regulatory retention.
- `GET /api/v1/gdpr/export/{cardholder_id}` → 200 JSON; returns all cards, transactions, limits for the cardholder. **Requires `ops_compliance` OR the cardholder themselves.**

---

### Task 14 · Input Validation & Error Handling

**What prompt would you run to complete this task?**
> Create centralized input validators for PAN format, email, card expiry, and monetary amounts. Implement a global exception handler that maps domain exceptions to the standard error envelope.

**What file do you want to CREATE or UPDATE?**
`src/validators/card_validators.py`, `src/exceptions/domain.py`, add exception handlers in `src/main.py`

**What function do you want to CREATE or UPDATE?**
`validate_pan_format()`, `validate_expiry()`, `validate_email()`, `validate_amount()`; exception classes `CardNotFound`, `InvalidTransition`, `LimitViolation`, `AccessDenied`; `register_exception_handlers(app)`

**What are details you want to add to drive the code changes?**
- `validate_pan_format(pan: str)` — must be 16 digits, Luhn-check valid.
- `validate_expiry(month, year)` — must be in the future.
- `validate_amount(amount)` — must be positive Decimal, max 2 decimal places, max value 999_999_999.99.
- Exception handler returns: `{"error": {"code": "CARD_NOT_FOUND", "message": "...", "details": {...}}}` with correct HTTP status.
- Pydantic `ValidationError` caught globally and reformatted into the same envelope (400).

---

### Task 15 · Security Headers Middleware

**What prompt would you run to complete this task?**
> Add a middleware that injects security headers (HSTS, X-Content-Type-Options, X-Frame-Options, Content-Security-Policy, Referrer-Policy) into every response. Make header values configurable.

**What file do you want to CREATE or UPDATE?**
`src/middleware/security_headers.py`

**What function do you want to CREATE or UPDATE?**
`SecurityHeadersMiddleware.__call__()`

**What are details you want to add to drive the code changes?**
- Headers: `Strict-Transport-Security: max-age=31536000; includeSubDomains`, `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Content-Security-Policy: default-src 'self'`, `Referrer-Policy: strict-origin-when-cross-origin`.
- Use ASGI middleware (not Starlette `BaseHTTPMiddleware`) for performance.
- Headers configurable via `Settings` for different environments (dev may relax CSP).

---

### Task 16 · Unit Tests (Models & Services)

**What prompt would you run to complete this task?**
> Create unit tests for all models (Card, AuditEvent, SpendingLimit, Transaction) and all services (CardService, SpendingLimitService, TransactionService, AuditService). Mock the database layer. Achieve >90% coverage on models and services.

**What file do you want to CREATE or UPDATE?**
`tests/conftest.py`, `tests/test_card_model.py`, `tests/test_card_service.py`, `tests/test_spending_limits.py`, `tests/test_transactions.py`, `tests/test_audit.py`

**What function do you want to CREATE or UPDATE?**
Test functions per file; `conftest.py`: `test_client`, `db_session`, `card_factory`, `user_token_factory`

**What are details you want to add to drive the code changes?**
- `test_card_model.py` (10 tests): valid creation, PAN masking in response, CVV never in response, invalid PAN fails Luhn, expired card rejected, status enum validation, encrypted name round-trip, UUID generation, required fields, Pydantic serialization.
- `test_card_service.py` (12 tests): create card happy path, create emits audit, activate from CREATED, freeze from ACTIVE, unfreeze from FROZEN, close from ACTIVE, close from FROZEN, reject activate from FROZEN (409), reject double-activate, list cards by owner, list cards ops sees all, get card not found (404).
- `test_spending_limits.py` (8 tests): set limits happy path, hierarchy violation rejected, update emits audit, check_transaction approved, declined per-txn, declined daily, declined monthly, limits on non-active card rejected.
- `test_transactions.py` (6 tests): query with date range, merchant filter, pagination, max page_size capped, authorize approved, authorize declined.
- `test_audit.py` (8 tests): record event, query by card_id, query by actor, query by date range, pagination, update raises NotImplementedError, delete raises NotImplementedError, immutability enforced.

---

### Task 17 · Integration & Performance Tests

**What prompt would you run to complete this task?**
> Create integration tests for full lifecycle workflows and performance benchmarks. Use httpx AsyncClient against the real app (with test database).

**What file do you want to CREATE or UPDATE?**
`tests/test_integration.py`, `tests/test_performance.py`

**What function do you want to CREATE or UPDATE?**
Integration test functions (10), performance benchmark functions (6)

**What are details you want to add to drive the code changes?**
- **Integration tests (10):**
  1. Full lifecycle: create → activate → freeze → unfreeze → close; verify audit trail has 5 events.
  2. Create card + set limits + authorize approved transaction + query it.
  3. Authorize transaction exceeding daily limit → DECLINED.
  4. Cardholder cannot access another user's card (403).
  5. Ops can freeze any user's card + audit shows `ops_compliance` role.
  6. GDPR erasure: create card, erase, verify PII is pseudonymized, audit events preserved.
  7. GDPR export returns all cardholder data.
  8. Concurrent card operations (20 simultaneous freeze/unfreeze) — no data corruption.
  9. Invalid state transition returns 409 with correct error envelope.
  10. Bulk create 50 cards, list with pagination, verify page counts.
- **Performance tests (6):**
  1. Single card creation < 50 ms p95.
  2. Card state transition < 50 ms p95.
  3. Transaction authorization < 50 ms p95.
  4. List 1000 transactions with filters < 200 ms p95.
  5. Audit log query (10 000 events, filtered) < 200 ms p95.
  6. Bulk create 100 cards < 2 s total.

---

### Task 18 · Compliance Tests (PCI-DSS, GDPR, Audit)

**What prompt would you run to complete this task?**
> Create a dedicated compliance test suite that verifies PCI-DSS, GDPR, and audit requirements are met at the API level. These tests prove that sensitive data never leaks.

**What file do you want to CREATE or UPDATE?**
`tests/test_compliance.py`, `tests/test_auth_rbac.py`, `tests/test_gdpr.py`

**What function do you want to CREATE or UPDATE?**
Compliance test functions (8), RBAC test functions (8), GDPR test functions (6)

**What are details you want to add to drive the code changes?**
- **Compliance tests (8):**
  1. No API response contains a full 16-digit PAN (regex scan all JSON responses).
  2. No API response contains a CVV or CVV hash.
  3. No log output contains a full PAN (capture structlog output, regex scan).
  4. Audit event is created for every state transition (create 5 transitions, expect 5 events).
  5. Audit events cannot be updated via any endpoint (attempt PUT/PATCH/DELETE on `/audit`, expect 405 or 403).
  6. Error responses never contain stack traces or internal file paths.
  7. Security headers present on every response (check all 5 headers).
  8. Rate limiting enforced on card creation (>10 requests in 1 min → 429).
- **RBAC tests (8):**
  1. Unauthenticated request → 401.
  2. Expired JWT → 401.
  3. Cardholder accessing another's card → 403.
  4. Cardholder accessing audit endpoint → 403.
  5. Ops can access any card → 200.
  6. Ops can query audit logs → 200.
  7. Ops can trigger GDPR erasure → 200.
  8. Cardholder can export own data → 200.
- **GDPR tests (6):**
  1. Erasure pseudonymizes cardholder name and email.
  2. Erasure preserves audit events with hashed actor_id.
  3. Erasure is idempotent (run twice, no error).
  4. Export returns all cards, transactions, limits for one cardholder only.
  5. Export by wrong cardholder_id (not self, not ops) → 403.
  6. Archived cards (closed > retention period) excluded from standard queries but included in compliance exports.
