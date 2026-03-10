# Virtual Card Lifecycle Management — Specification

> Ingest the information from this file, implement the Low-Level Tasks, and generate the code that will satisfy the High and Mid-Level Objectives.

---

## High-Level Objective

Build a **Virtual Card Lifecycle Management** service that allows end-users to create, activate, freeze/unfreeze, configure spending limits on, and view transactions for virtual payment cards, while providing internal ops/compliance teams with full audit visibility and control — all within a regulated FinTech environment.

---

## Mid-Level Objectives

1. **Card Lifecycle Operations**: Enable end-users to create a virtual card, activate it, freeze and unfreeze it, and permanently close (cancel) it. Every state transition must be recorded with a timestamp, actor identity, and reason.

2. **Spending Controls**: Allow end-users to set per-transaction and per-cycle spending caps (daily, weekly, monthly) on each card. Monetary values must be stored and processed with exact decimal precision to avoid floating-point errors.

3. **Transaction Visibility**: Provide end-users with a paginated, filtered view of their card transaction history (date range, amount range, merchant category code). Transactions must be immutable once written.

4. **Compliance & Audit Trail**: Every mutating operation (create, state change, limit update) must produce a tamper-evident audit log entry including: actor ID, action, before/after state snapshot, IP address, and UTC timestamp. Audit entries must never be deleted or modified.

5. **Security & Data Protection**: Card numbers (PANs) must be stored encrypted at rest (AES-256-GCM) and masked in all API responses (show last 4 digits only). CVV values must never be persisted after initial provisioning. Access is governed by role-based authorization (cardholder vs. ops/compliance roles).

6. **Ops/Compliance View**: Internal staff can search cards by user, view full audit logs, manually freeze cards for fraud/compliance reasons, and generate compliance reports without ever accessing raw card credentials.

---

## Implementation Notes

- **Monetary values**: Use `Decimal` (Python) or `BigDecimal` (JVM) — never `float` or `double` for currency amounts.
- **Card number handling**: Generate PAN using a compliant Luhn-valid algorithm. Store only the encrypted form. Decrypt only in the provisioning response; never return plaintext PAN again.
- **State machine**: Card states are `PENDING → ACTIVE ↔ FROZEN → CLOSED`. No transition outside this graph is permitted. `CLOSED` is a terminal state.
- **Audit log**: Append-only table/collection. No UPDATE or DELETE statements are permitted on audit rows. Enforce at DB level with row-level policies or a write-once object store.
- **Idempotency**: All card operation endpoints must accept an `Idempotency-Key` header; duplicate requests with the same key within 24 hours return the original response without re-executing.
- **Rate limiting**: Freeze/unfreeze and limit-update endpoints are limited to 10 requests per card per hour to prevent abuse.
- **PCI-DSS alignment**: Treat PAN and CVV as cardholder data (CHD). Apply network segmentation, key rotation every 90 days, and access logging for any key-usage event.
- **GDPR/data minimization**: Collect only the data necessary for card provisioning. Provide a data-deletion stub that soft-deletes user-facing data and replaces PAN with a tombstone token, while retaining the audit trail.
- **Error handling**: Return RFC 7807 Problem Detail JSON on all error responses. Never expose internal stack traces or database error messages to the client.
- **Testing**: Minimum 80% line coverage; mandatory tests for every state-transition edge, every validation rule, and every authorization boundary.
- **Logging**: Structured JSON logs (no plaintext). Log correlation IDs on every request. Never log PANs, CVVs, or encryption keys.

---

## Context

### Beginning context

- Empty feature module directory (`src/cards/`)
- Existing user-authentication service (JWT-based) is available as a dependency
- Existing database migration framework (e.g., Alembic / Flyway) is configured
- Shared `audit_log` infrastructure table exists with an append-only trigger
- Encryption key management service (KMS) is accessible via internal SDK
- CI pipeline runs linting, tests, and SAST scanning on every PR

### Ending context

- `src/cards/models.py` — Card, SpendingLimit, CardTransaction data models
- `src/cards/schema.py` — Request/response Pydantic schemas with PAN masking
- `src/cards/service.py` — Business logic: lifecycle state machine, limit enforcement
- `src/cards/repository.py` — DB access layer (no raw SQL in service layer)
- `src/cards/router.py` — FastAPI router with all card endpoints
- `src/cards/audit.py` — Audit log writer (append-only)
- `src/cards/encryption.py` — PAN encryption/decryption via KMS SDK
- `src/cards/permissions.py` — RBAC decorators/dependencies
- `migrations/` — DB migration scripts for new tables
- `tests/cards/` — Unit and integration tests
- `docs/cards-api.yaml` — OpenAPI 3.1 specification for card endpoints

---

## Low-Level Tasks

---

### 1. Define Data Models and Database Schema

**What prompt would you run to complete this task?**
> "Create SQLAlchemy ORM models for a virtual card lifecycle system. Include: `VirtualCard` (id, user_id, masked_pan, encrypted_pan_blob, kms_key_version, status enum [PENDING, ACTIVE, FROZEN, CLOSED], created_at, updated_at), `SpendingLimit` (id, card_id, limit_type enum [PER_TRANSACTION, DAILY, WEEKLY, MONTHLY], amount Numeric(19,4), currency ISO-4217, effective_from, effective_to), `CardTransaction` (id, card_id, amount Numeric(19,4), currency, merchant_name, mcc, direction enum [DEBIT, CREDIT], status, authorized_at, settled_at). All tables must have created_at/updated_at timestamps. Add a DB-level check constraint that prevents inserting a SpendingLimit with amount <= 0."

**What file do you want to CREATE or UPDATE?**
`src/cards/models.py`

**What function/class do you want to CREATE or UPDATE?**
`VirtualCard`, `SpendingLimit`, `CardTransaction` (SQLAlchemy declarative models)

**What details drive the code changes?**
- `status` must be a native DB enum type, not a varchar, to enforce valid values at the DB level.
- `encrypted_pan_blob` is a `LargeBinary` column; `masked_pan` (last 4 digits, e.g. `****1234`) is a plain `String(9)`.
- `CardTransaction` rows must have no UPDATE triggers allowed — enforce immutability via a DB-level `BEFORE UPDATE` trigger that raises an exception.
- Foreign keys must have `ON DELETE RESTRICT` to prevent orphaned records.
- Add a composite index on `(card_id, authorized_at DESC)` for efficient transaction history queries.

---

### 2. Implement Request/Response Schemas with PAN Masking

**What prompt would you run to complete this task?**
> "Create Pydantic v2 schemas for the virtual card API. `CardCreateRequest` (user_id UUID, currency str, initial_limit optional Decimal). `CardResponse` (id UUID, masked_pan str, status, currency, created_at datetime — never include encrypted_pan_blob or raw PAN). `SpendingLimitRequest` (limit_type, amount Decimal with validator: must be > 0 and <= 999999.9999, currency). `TransactionResponse` (id, amount, currency, merchant_name, mcc, direction, status, authorized_at). All Decimal fields must use `condecimal(gt=0, max_digits=19, decimal_places=4)`."

**What file do you want to CREATE or UPDATE?**
`src/cards/schema.py`

**What function/class do you want to CREATE or UPDATE?**
`CardCreateRequest`, `CardResponse`, `SpendingLimitRequest`, `TransactionResponse`

**What details drive the code changes?**
- `CardResponse` must have a `model_validator` that asserts `encrypted_pan_blob` is not present in the output.
- Currency field must validate against the ISO 4217 three-letter code list (e.g. `USD`, `EUR`, `GBP`).
- All datetime fields must be serialized as UTC ISO 8601 strings (e.g. `2026-03-10T14:00:00Z`).
- `masked_pan` regex validator: must match `^\*{4,12}\d{4}$`.

---

### 3. Implement Card State Machine and Business Logic

**What prompt would you run to complete this task?**
> "Implement a `CardService` class that manages the virtual card lifecycle using an explicit state machine. Valid transitions: PENDING→ACTIVE (activate), ACTIVE→FROZEN (freeze), FROZEN→ACTIVE (unfreeze), ACTIVE→CLOSED and FROZEN→CLOSED (cancel). Any attempt to transition outside these edges must raise a `CardStateTransitionError`. Spending limit updates are only allowed on ACTIVE cards. Each operation must call `AuditService.log()` after a successful DB write. Implement idempotency using a Redis-backed key store checked before any mutating operation."

**What file do you want to CREATE or UPDATE?**
`src/cards/service.py`

**What function/class do you want to CREATE or UPDATE?**
`CardService` with methods: `create_card`, `activate_card`, `freeze_card`, `unfreeze_card`, `cancel_card`, `update_spending_limit`, `get_transactions`

**What details drive the code changes?**
- All monetary comparisons must use `Decimal`, never `float`.
- `create_card` must call `EncryptionService.encrypt_pan(pan)` and store only the blob; the plaintext PAN must be zeroed from memory immediately after encryption.
- State transition must be performed inside a database transaction; if the audit log write fails, the card state change must be rolled back.
- `get_transactions` must support pagination (`offset`, `limit` max 100) and filters (`from_date`, `to_date`, `min_amount`, `max_amount`, `mcc`).
- `CardStateTransitionError` must extend a base domain exception and include `current_state` and `attempted_transition` fields for structured error responses.

---

### 4. Implement PAN Encryption and KMS Integration

**What prompt would you run to complete this task?**
> "Create an `EncryptionService` that wraps the internal KMS SDK. Implement `encrypt_pan(pan: str) -> EncryptedBlob` which uses AES-256-GCM with a KMS-managed data encryption key (envelope encryption pattern). Implement `decrypt_pan(blob: EncryptedBlob) -> str` for provisioning use only. Include key version tracking so that PANs encrypted with old key versions can be re-encrypted during scheduled key rotation without decrypting all at once. Log every KMS API call (key ID, operation, success/failure) to the audit log — never log the plaintext PAN or the key material."

**What file do you want to CREATE or UPDATE?**
`src/cards/encryption.py`

**What function/class do you want to CREATE or UPDATE?**
`EncryptionService`, `EncryptedBlob` (dataclass)

**What details drive the code changes?**
- Use envelope encryption: generate a random DEK per card, encrypt the DEK with the KMS CMK, store `(encrypted_dek + iv + ciphertext)` as the blob.
- `decrypt_pan` must only be callable by the provisioning endpoint; enforce this via a runtime check on the calling context / permission scope.
- Memory containing plaintext PAN must be explicitly overwritten (e.g. `bytearray` zeroing) before returning from `encrypt_pan`.
- KMS key rotation job must be idempotent and run as a background task, processing at most 500 cards per batch with a 1-second sleep between batches to avoid KMS throttling.

---

### 5. Implement Append-Only Audit Logging

**What prompt would you run to complete this task?**
> "Create an `AuditService` that writes append-only audit log entries. Each entry must include: `event_id` (UUID v7), `actor_id`, `actor_role`, `card_id`, `action` (enum: CARD_CREATED, CARD_ACTIVATED, CARD_FROZEN, CARD_UNFROZEN, CARD_CLOSED, LIMIT_UPDATED, LIMIT_READ, PAN_PROVISIONED), `before_state` (JSON snapshot), `after_state` (JSON snapshot), `ip_address`, `user_agent`, `idempotency_key`, `created_at` (UTC). The DB table must have a trigger that raises an error on any UPDATE or DELETE. Provide a `search_audit_log` method for ops/compliance users with filters on card_id, actor_id, action, and date range."

**What file do you want to CREATE or UPDATE?**
`src/cards/audit.py`

**What function/class do you want to CREATE or UPDATE?**
`AuditService` with methods: `log`, `search_audit_log`

**What details drive the code changes?**
- `before_state` and `after_state` snapshots must have PANs replaced with their masked form before serialization.
- `event_id` must use UUID v7 (monotonically increasing) to support efficient time-range queries without a separate timestamp index.
- `search_audit_log` is restricted to users with `role == OPS` or `role == COMPLIANCE`; enforce at service layer AND at DB row-level security policy.
- The audit table must be in a separate DB schema (`audit.*`) with write access granted only to the application service account, and read access for the compliance role.

---

### 6. Implement RBAC Permissions and FastAPI Router

**What prompt would you run to complete this task?**
> "Create FastAPI dependency functions for role-based access control. `require_cardholder` validates the JWT, extracts `user_id` and `role`, and ensures `role == CARDHOLDER`. `require_ops` ensures `role in {OPS, COMPLIANCE}`. Create a FastAPI router with endpoints: POST /cards (create), POST /cards/{id}/activate, POST /cards/{id}/freeze, POST /cards/{id}/unfreeze, DELETE /cards/{id} (cancel), PUT /cards/{id}/limits, GET /cards/{id}/transactions, GET /admin/cards/{id}/audit-log (ops only). All endpoints must extract `X-Forwarded-For` and `User-Agent` headers and pass them to the service layer for audit logging."

**What file do you want to CREATE or UPDATE?**
`src/cards/router.py`, `src/cards/permissions.py`

**What function/class do you want to CREATE or UPDATE?**
`require_cardholder`, `require_ops`, all route handler functions

**What details drive the code changes?**
- Every endpoint must validate the `Idempotency-Key` header (UUID format, required for all POST/PUT/DELETE).
- HTTP 422 responses must use RFC 7807 Problem Detail format with a `type` URI, `title`, `detail`, and `instance`.
- Rate limiting for freeze/unfreeze/limit-update: use a Redis sliding-window counter keyed on `card_id`; return HTTP 429 with `Retry-After` header when exceeded.
- Cardholder endpoints must verify that the `card_id` in the path belongs to the authenticated `user_id` (ownership check) before delegating to `CardService`.
- `GET /cards/{id}/transactions` must support query parameters: `from_date`, `to_date`, `min_amount`, `max_amount`, `mcc`, `offset`, `limit` (default 20, max 100).

---

### 7. Write Tests

**What prompt would you run to complete this task?**
> "Write pytest tests for the virtual card lifecycle service. Include: unit tests for every state machine transition (valid and invalid), unit tests for spending limit validation (zero amount, negative, exceeding max, correct Decimal precision), unit tests for PAN masking (ensure raw PAN never appears in any response object), integration tests for the full create→activate→freeze→unfreeze→cancel flow using a test database, integration tests for idempotency (same key twice returns same result without duplicate DB rows), authorization tests (cardholder cannot access another user's card, non-ops user cannot access audit log), and a performance test that asserts p95 latency < 200ms for `get_transactions` with 10,000 rows."

**What file do you want to CREATE or UPDATE?**
`tests/cards/test_lifecycle.py`, `tests/cards/test_security.py`, `tests/cards/test_performance.py`

**What function/class do you want to CREATE or UPDATE?**
Test functions and fixtures in each file

**What details drive the code changes?**
- Use `pytest-asyncio` for async FastAPI route tests.
- Use `factory_boy` for test data factories; never hardcode real PAN values — use Luhn-valid test PANs from the `16-digit test range` (e.g. `4111111111111111`).
- Mock KMS calls using `unittest.mock.patch`; never call real KMS in unit tests.
- The integration test database must be spun up via `testcontainers` (PostgreSQL) and torn down after the test session.
- Coverage must be reported with `pytest-cov`; CI pipeline must fail if coverage drops below 80%.

