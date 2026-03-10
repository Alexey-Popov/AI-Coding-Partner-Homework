# Agents Configuration — Virtual Card Lifecycle Service

> This file configures how AI coding agents should behave when working in this project. All agents must read and apply these rules before generating or modifying any code.

---

## 1. Project Identity

| Property | Value |
|---|---|
| Domain | FinTech — Virtual Card Lifecycle Management |
| Regulatory context | PCI-DSS v4, GDPR, internal AML/KYC policy |
| Criticality | High — handles cardholder data (CHD) |
| Language | Python 3.12 |
| Framework | FastAPI 0.115+ |
| ORM | SQLAlchemy 2.x (async) |
| DB | PostgreSQL 16 |
| Cache / Rate-limiting | Redis 7 |
| Schema validation | Pydantic v2 |
| Testing | pytest, pytest-asyncio, testcontainers, factory_boy |
| CI | GitHub Actions with SAST (Semgrep) and dependency audit (pip-audit) |

---

## 2. Tech Stack Rules

### Python
- Target **Python 3.12**; use `match` statements for state machine transitions (PEP 634).
- All async I/O must use `asyncio`; no synchronous blocking calls inside async functions.
- Use `from __future__ import annotations` at the top of every module.
- Dependency injection via FastAPI `Depends()`; no module-level singletons for DB sessions.

### FastAPI
- All routers must be mounted with explicit `prefix` and `tags`.
- All endpoints must declare explicit `response_model` to prevent accidental data leakage.
- Use `status.HTTP_*` constants — never hardcode numeric HTTP status codes.
- Background tasks (e.g. key rotation) must use FastAPI `BackgroundTasks` or Celery, not `asyncio.create_task` in a request handler.

### SQLAlchemy
- Use `AsyncSession` exclusively; no synchronous `Session`.
- Never write raw SQL strings — use ORM queries or `text()` with bound parameters only.
- All migrations must go through Alembic; never call `Base.metadata.create_all()` in production code.
- All monetary columns must be `Numeric(19, 4)` — never `Float`.

### Pydantic v2
- All request models must have `model_config = ConfigDict(strict=True)`.
- All response models must have `model_config = ConfigDict(populate_by_name=True, from_attributes=True)`.
- Never use `model.dict()` — use `model.model_dump()`.

---

## 3. Domain Rules (Banking / FinTech)

### Monetary Calculations
- **Always** use `decimal.Decimal` for money. Import `from decimal import Decimal, ROUND_HALF_EVEN`.
- Round to 4 decimal places using `ROUND_HALF_EVEN` (banker's rounding) for intermediate steps; round to 2 decimal places only for final display/reporting.
- Never convert a monetary `Decimal` to `float` at any point in the pipeline.

### Card State Machine
- The only valid state graph is: `PENDING → ACTIVE ↔ FROZEN → CLOSED`.
- `CLOSED` is a terminal state — no transitions out of it are permitted under any circumstances.
- Every state transition must be wrapped in a DB transaction and followed by an audit log write within the same transaction. If either fails, the entire operation rolls back.
- State transition errors must return HTTP 409 Conflict with a Problem Detail body.

### Cardholder Data (CHD)
- **Never** log, print, or include in any exception message: PAN (full or partial beyond last 4), CVV, PIN, or expiry date.
- PANs in memory must be `bytearray` (mutable, zeroable) when decrypted, and must be zeroed before the function returns.
- The `encrypted_pan_blob` column must never appear in any API response schema, even as `null`.
- CVV must not be persisted to any storage after the initial provisioning response is sent.

### Audit Trail
- Every CREATE, UPDATE, or state-change operation must produce an audit log entry **within the same DB transaction**.
- Audit log entries are immutable — no code path may issue `UPDATE` or `DELETE` against the `audit.log` table.
- Audit snapshots (`before_state`, `after_state`) must be serialized as JSON with PANs replaced by their masked form.

### Idempotency
- All state-mutating endpoints (POST, PUT, DELETE) require an `Idempotency-Key` header.
- The idempotency store is Redis with a 24-hour TTL.
- If a duplicate key is received, return the cached response with HTTP 200 and header `Idempotency-Replayed: true`.

---

## 4. Code Style

- **Formatter**: `ruff format` (line length 100).
- **Linter**: `ruff check` with rules `E, F, I, N, S, ANN, B, C4, SIM, UP`.
- **Type checking**: `mypy --strict` — all functions must have full type annotations.
- **Docstrings**: Google-style docstrings on all public classes and functions.
- **Naming conventions**:
  - Files: `snake_case.py`
  - Classes: `PascalCase`
  - Functions / variables: `snake_case`
  - Constants: `UPPER_SNAKE_CASE`
  - DB table names: `snake_case` (plural), always in explicit `__tablename__`
  - Enum values: `UPPER_SNAKE_CASE`

---

## 5. Testing Expectations

- Minimum **80% line coverage** enforced by CI (`pytest --cov --cov-fail-under=80`).
- **Unit tests**: mock all external dependencies (DB, Redis, KMS). Use `pytest.mark.unit`.
- **Integration tests**: use `testcontainers` (real PostgreSQL + Redis). Use `pytest.mark.integration`.
- **Security tests**: assert that PAN never appears in any response, log line, or exception. Use `pytest.mark.security`.
- **Performance tests**: assert p95 latency benchmarks using `pytest-benchmark`. Use `pytest.mark.performance`.
- Every PR must pass all test marks before merge.
- Test data factories (`factory_boy`) must never use real card numbers; use Luhn-valid test PANs only.

---

## 6. Security and Compliance Constraints

### What the AI Agent MUST do
- Always validate and sanitize all user-supplied input before using it in queries or business logic.
- Always use parameterized queries / ORM — never string-format SQL.
- Always apply the principle of least privilege: each DB role/service account gets only the permissions it needs.
- Always add `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, and `Strict-Transport-Security` headers via middleware.
- Always return RFC 7807 Problem Detail JSON on errors — never expose internal details.
- Always run Semgrep SAST rules `p/owasp-top-10` and `p/python` on changed files in CI.

### What the AI Agent MUST NOT do
- ❌ Never log sensitive fields: PAN, CVV, full card number, encryption keys, passwords, tokens.
- ❌ Never use `eval()`, `exec()`, or `pickle` for untrusted data.
- ❌ Never disable SSL verification (`verify=False`) in any HTTP client call.
- ❌ Never hardcode secrets, API keys, or connection strings — use environment variables via `pydantic-settings`.
- ❌ Never use `float` for monetary values.
- ❌ Never write migration scripts that alter or drop the `audit.log` table.
- ❌ Never expose stack traces or internal error messages in API responses.
- ❌ Never bypass RBAC checks "for testing purposes" in production code paths.
- ❌ Never store or transmit the full PAN in plaintext after the provisioning step.

### Dependency Management
- Pin all dependencies to exact versions in `requirements.txt`; use `pip-audit` in CI to scan for CVEs.
- Before adding any new dependency, check its CVE history and license (only MIT, Apache-2.0, BSD allowed).
- Flag any dependency with a known critical/high CVE for immediate remediation.

---

## 7. Ops / Compliance Agent Behaviour

When generating code that touches compliance or reporting features:
- Always check that the requesting user has `role in {OPS, COMPLIANCE}` before returning audit data.
- Compliance report endpoints must produce immutable, timestamped snapshots — not live queries that can change after the fact.
- Any bulk data export must be rate-limited and logged in the audit trail.
- PII fields in compliance reports must be pseudonymized unless the report is explicitly for a regulatory authority request (indicated by a `regulatory_request_id` field).

