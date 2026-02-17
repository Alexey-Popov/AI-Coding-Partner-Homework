# Agent Configuration — Virtual Card Lifecycle Management

> Guidelines for AI coding partners working on this regulated FinTech project.

---

## 1. Tech Stack

| Layer | Choice | Version | Notes |
|-------|--------|---------|-------|
| Language | Python | 3.11+ | Use latest type-hint syntax (`X | None` not `Optional[X]`) |
| Framework | FastAPI | 0.110+ | Async endpoints, Pydantic V2 schemas |
| ORM | SQLAlchemy | 2.0+ async | `DeclarativeBase`, `mapped_column` style |
| Migrations | Alembic | latest | Async runner via `run_async()` |
| Database | PostgreSQL | 16+ | ACID, JSONB columns for metadata |
| Cache | Redis | 7+ | Rate limiting, token cache |
| Auth | PyJWT | latest | HS256, short-lived tokens |
| Testing | pytest + pytest-asyncio + httpx | latest | `AsyncClient` for integration tests |
| Logging | structlog | latest | JSON output, no plaintext |
| Linting | Ruff | latest | Replaces flake8/isort/pyflakes |
| Type check | mypy (strict) | latest | All public APIs fully typed |
| Formatting | Black | latest | Line length 99 |

**When generating code:**
- Always import from the project's own modules (`from src.models.card import Card`), never use relative imports.
- Use `async def` for all route handlers and service methods.
- Use `Depends()` for dependency injection; avoid manual instantiation of services in routes.

---

## 2. Domain Rules (Banking / FinTech)

### Monetary Values
- **ALWAYS** use `Decimal` for money. NEVER use `float`.
- Use `Decimal("0.00")` notation, never `Decimal(0.00)` (float constructor).
- Rounding: `ROUND_HALF_UP`, 2 decimal places.
- Currency stored as ISO 4217 code (e.g., `"USD"`).

### Card Numbers (PAN)
- A raw PAN must NEVER be stored in the database, logged, or returned in any API response.
- Tokenize using HMAC-SHA256 with a server-side secret key before any persistence.
- Mask for display: `**** **** **** {last_four}`.
- CVV: store only bcrypt hash; never return in any response.

### State Machine
- The card lifecycle follows a strict state graph: `CREATED → ACTIVE ↔ FROZEN → CLOSED`.
- `CLOSED` is a terminal state — no transitions out.
- Every transition must be validated before execution and audited after.

### Audit
- Every data-mutating operation must emit an audit event.
- Audit records are immutable — no UPDATE or DELETE.
- Include `actor_id`, `actor_role`, `action`, `previous_state`, `new_state`, `ip_address`, `timestamp`.

### Compliance References
- **PCI-DSS v4.0**: Requirements 3.4 (render PAN unreadable), 10.2 (audit trails).
- **PSD2**: Strong Customer Authentication awareness (SCA flag in metadata).
- **GDPR**: Articles 17 (right to erasure), 20 (data portability).

---

## 3. Code Style

### Naming
- Files: `snake_case.py` with suffix indicating layer: `_model.py`, `_service.py`, `_routes.py`, `_middleware.py`.
- Classes: `PascalCase`. Pydantic schemas: `{Entity}Create`, `{Entity}Update`, `{Entity}Response`.
- Functions: `snake_case`, verb-first (`create_card`, `validate_pan`, `query_events`).
- Constants: `UPPER_SNAKE_CASE`.
- Enums: `PascalCase` class name, `UPPER_SNAKE_CASE` members.

### Structure
- One model class per file.
- One service class per file; services receive `AsyncSession` via constructor (DI).
- Routes are thin — validate input, call service, return response. No business logic in routes.
- All configuration via `pydantic-settings`; no hardcoded values.

### Docstrings
- All public functions: Google-style docstring with `Args`, `Returns`, `Raises`.
- No docstrings on private/helper functions unless non-obvious.

### Error Handling
- Define domain exceptions in `src/exceptions/domain.py`.
- Map to HTTP codes via global exception handlers in `main.py`.
- Error envelope: `{"error": {"code": "MACHINE_READABLE_CODE", "message": "Human-readable message", "details": {}}}`.
- NEVER expose stack traces, file paths, or SQL queries in API responses.

---

## 4. Testing Expectations

### Coverage
- Target: **>90%** on models and services; **>85%** overall.
- Coverage report: `pytest --cov=src --cov-report=term-missing --cov-report=html`.

### Test Organization
```
tests/
├── conftest.py              # Shared fixtures, factories, test DB setup
├── test_card_model.py       # Pydantic schema validation (10 tests)
├── test_card_service.py     # Service logic + state machine (12 tests)
├── test_spending_limits.py  # Limit CRUD + hierarchy (8 tests)
├── test_transactions.py     # Query + authorization (6 tests)
├── test_audit.py            # Audit write, query, immutability (8 tests)
├── test_auth_rbac.py        # JWT + role enforcement (8 tests)
├── test_gdpr.py             # Erasure + export (6 tests)
├── test_integration.py      # Full lifecycle (10 tests)
├── test_compliance.py       # PAN masking, header checks (8 tests)
├── test_performance.py      # Latency benchmarks (6 tests)
└── fixtures/                # Static test data files
```

### Test Rules
- Each test function tests ONE behavior.
- Use factories (not raw dicts) to create test data.
- Use `pytest.mark.asyncio` for async tests.
- Integration tests use a real test database (SQLite async or PostgreSQL test container).
- Performance tests use `time.perf_counter()` and assert p95 thresholds.
- Compliance tests must regex-scan responses for PAN leaks.

---

## 5. Security Constraints

| Rule | Rationale |
|------|-----------|
| No secrets in source code or version control | Use `.env` + `pydantic-settings` |
| Parameterized queries only (no string interpolation) | SQL injection prevention |
| JWT validated on every request | Authentication assurance |
| RBAC checked at route level via `Depends()` | Authorization enforcement |
| Rate limiting on sensitive endpoints | Brute-force protection |
| Security headers on every response | OWASP best practice |
| Input sanitization at Pydantic level | Injection prevention |
| No PAN/CVV in logs | PCI-DSS Req 3.4 |
| bcrypt for CVV hashing, HMAC-SHA256 for PAN tokens | Industry-standard cryptography |
| AES-256-GCM for at-rest encryption of PII | Data protection |

---

## 6. Compliance Constraints

### Audit Requirements
- Every card state change, limit change, and admin action MUST produce an `AuditEvent`.
- Audit events are **append-only** — application code must raise on any attempt to update or delete.
- Audit queries restricted to `ops_compliance` role only.

### Data Retention
- Cards closed for > 365 days are auto-archived (configurable via `RETENTION_DAYS` env var).
- Archived data excluded from standard queries but available for compliance exports.

### GDPR
- Right to erasure: pseudonymize PII, preserve audit structure with hashed actor IDs.
- Data portability: export endpoint returns all cardholder data as JSON.
- Data minimization: collect only what is necessary; document each PII field's purpose.

### Regulatory Logging
- All log entries structured (JSON) for SIEM ingestion.
- Log levels: `ERROR` for failures, `WARNING` for denied operations, `INFO` for state changes, `DEBUG` for development only (never in production).
- No PII in log messages except pseudonymized IDs.
