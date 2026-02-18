# Agent Configuration — Virtual Card Lifecycle

This document defines how an AI coding partner should behave when implementing the virtual card lifecycle management system. It covers the tech stack, domain rules, code style, testing expectations, and security/compliance constraints.

---

## Tech Stack

| Layer          | Technology                          |
|----------------|-------------------------------------|
| Language       | Python 3.12+                        |
| Web Framework  | FastAPI                             |
| ORM            | SQLAlchemy 2.x (async)              |
| Migrations     | Alembic                             |
| Database       | PostgreSQL 15+                      |
| Validation     | Pydantic v2                         |
| Auth           | JWT (PyJWT or python-jose)          |
| Testing        | pytest, pytest-asyncio, httpx       |
| Linting        | ruff                                |
| Type Checking  | mypy (strict mode)                  |
| Containerization | Docker, docker-compose            |

---

## Domain Rules (Banking / FinTech)

1. **Money is Decimal** — All monetary values must use `Decimal` (Python) or `NUMERIC` (PostgreSQL). Never use `float` or `double` for financial amounts. Set precision to `NUMERIC(19, 4)` in the database.

2. **Currency is explicit** — Every monetary field must have an accompanying `currency` field using ISO 4217 codes (e.g., `USD`, `EUR`). Never assume a default currency.

3. **Card data is sensitive** — Full PAN (Primary Account Number) and CVV must never appear in:
   - Log files
   - API responses (except the initial card creation response, which returns the PAN once)
   - Error messages
   - Audit event snapshots
   - Use `masked_pan` (last 4 digits) for all display and reference purposes.

4. **All mutations are auditable** — Every operation that changes state (card creation, freeze, unfreeze, limit changes) must emit an `AuditEvent` containing: actor ID, actor role, timestamp, IP address, action type, and before/after snapshots of the changed entity.

5. **State transitions are explicit** — Card status changes follow a defined state machine:
   - `ACTIVE` → `FROZEN` (freeze)
   - `FROZEN` → `ACTIVE` (unfreeze)
   - `ACTIVE` or `FROZEN` → `CANCELLED` (cancel — one-way, irreversible)
   - Any other transition must be rejected with a clear error.

6. **Idempotency is required** — Mutating API endpoints must accept an `Idempotency-Key` header. If a request with the same key has already been processed, return the original response without re-executing the operation.

7. **KYC before card issuance** — A user must have `kyc_verified == True` before they can create a virtual card. This check cannot be bypassed.

---

## Code Style

- **PEP 8** with `ruff` enforcement (line length: 100)
- **Type hints** on all function signatures and return types — no `Any` without justification
- **Docstrings** on all public functions and classes (Google-style format)
- **Function length** — aim for ≤ 30 lines per function; extract helpers for longer logic
- **Naming**:
  - `snake_case` for functions, variables, and modules
  - `PascalCase` for classes
  - `UPPER_SNAKE_CASE` for constants
  - `kebab-case` for URL paths
- **Imports** — absolute imports only; group as: stdlib → third-party → local
- **No magic strings** — use enums or constants for status codes, action types, role names
- **No print statements** — use `structlog` or `logging` with structured JSON output

---

## Testing Expectations

- **Coverage target**: 80%+ line coverage (enforced in CI)
- **Unit tests**: test service-layer functions in isolation with mocked dependencies
- **Integration tests**: test API endpoints end-to-end using `TestClient` / `httpx.AsyncClient`
- **Compliance tests**: dedicated test suite that verifies:
  - Every mutating endpoint emits an audit event
  - Audit events contain all required fields
  - Sensitive data (PAN, CVV) never appears in audit snapshots
- **Negative tests**: verify that:
  - Invalid inputs return `422 Unprocessable Entity`
  - Unauthorized access returns `401` or `403`
  - Rate limit exceeded returns `429 Too Many Requests`
  - Invalid state transitions return `409 Conflict`
- **Test data**: use factories or fixtures; never use production data or real card numbers
- **Deterministic**: tests must not depend on external services, wall-clock time, or random values without seeding

---

## Security Constraints

1. **PCI-DSS awareness**:
   - Encrypt PAN at rest using AES-256-GCM
   - Transmit card data only over TLS 1.2+
   - Limit access to decrypted PAN to the card creation response only
   - Rotate encryption keys according to policy (document the mechanism, even if not implemented in MVP)

2. **Input sanitization**:
   - Validate all inputs with Pydantic strict mode
   - Reject unexpected fields (use `model_config = ConfigDict(extra="forbid")`)
   - Enforce maximum string lengths to prevent buffer abuse
   - Parameterized queries only — never construct SQL with string concatenation

3. **Authentication / Authorization**:
   - JWT-based authentication on all endpoints
   - Role-based access: `END_USER`, `OPS`, `COMPLIANCE`
   - Token expiry: short-lived access tokens (15 min), longer refresh tokens (7 days)
   - Validate token signature, issuer, and expiry on every request

4. **Rate limiting**:
   - Card creation: max 5 per user per 24 hours
   - Freeze/unfreeze: max 10 per card per hour
   - Transaction listing: max 60 requests per minute per user
   - Return `429` with `Retry-After` header when exceeded

5. **No secrets in code**:
   - Database credentials, JWT secrets, encryption keys → environment variables or secret manager
   - Never commit `.env` files with real secrets
   - Use `.env.example` with placeholder values

6. **Dependency management**:
   - Pin all dependency versions in `pyproject.toml`
   - Run `pip-audit` or similar in CI to detect known vulnerabilities

---

## Compliance Requirements

1. **Audit trail** — append-only `audit_events` table; no UPDATE or DELETE operations on this table
2. **Data retention** — transaction records retained for 7 years minimum; session/access logs for 90 days
3. **Data masking** — PII (name, email, address) must be maskable for non-production environments
4. **Right to erasure** — support GDPR Article 17 for non-regulatory-mandated data; retain data required by financial regulations
5. **Access logging** — log who accessed what data and when, especially for OPS/COMPLIANCE role access to user data
6. **Segregation of duties** — OPS/COMPLIANCE can view but not mutate user resources without elevated authorization

---

## AI Agent Behavior Guidelines

When generating code for this project, the AI agent must:

- **Ask before assuming** — if a requirement is ambiguous, ask for clarification rather than guessing
- **Follow the spec** — implement exactly what the specification describes; do not add unrequested features
- **Check the state machine** — before implementing a card status change, verify it's a valid transition
- **Use Decimal** — whenever you see a monetary value, use `Decimal`. If you catch yourself writing `float`, stop and fix it
- **Emit audit events** — after every mutation, check: "Did I emit an audit event?" If not, add one
- **Mask sensitive data** — before logging or returning data, check: "Does this contain PAN, CVV, or PII?" If yes, mask it
- **Write tests** — for every function you create, write at least one happy-path test and one error-path test
- **Run the linter** — before considering a task complete, run `ruff check` and `mypy` and fix all issues
