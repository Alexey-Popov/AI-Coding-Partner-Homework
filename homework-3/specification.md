# Virtual Card Lifecycle (Create / Freeze / Limits / Transactions) — Specification

> Ingest the information from this file, implement the Low-Level Tasks, and generate the code that will satisfy the High and Mid-Level Objectives.

## High-Level Objective
Build a regulated-fintech-ready **virtual card lifecycle** capability that lets end-users create and manage virtual cards (freeze/unfreeze, set spend limits) and view card transactions, while ensuring strong auditability, security controls, and clear boundaries for sensitive data.

## Mid-Level Objectives
1. **Card lifecycle management**: Support create, freeze, unfreeze, and close actions with correct state transitions and idempotency.
2. **Spending controls**: Support configurable limits (e.g., per-transaction, daily, monthly) enforced consistently and deterministically.
3. **Transaction visibility**: Provide reliable transaction listing/filtering with stable pagination and consistent amounts/currency handling.
4. **Auditability & traceability**: Maintain an immutable audit trail of all card changes and transaction-related events with correlation IDs.
5. **Security & compliance posture**: Enforce least-privilege access, protect sensitive data (tokenization, no PAN/CVV logs), and produce compliance-friendly logs/records.

## Implementation Notes

### Scope & assumptions
- This spec is **implementation-oriented** but does **not** define final API endpoints/UI (out of scope for homework). Low-level tasks reference likely files/modules that an implementation would create.
- Virtual cards are assumed to be backed by an external card processor (issuer/processor). The system stores **processor references and tokens**, not full PAN/CVV.
- Multi-tenant readiness: treat `account_id` / `user_id` as tenant boundaries.

### Data handling & sensitive data boundaries
- **Do not store or log PAN/CVV**. If a “display PAN” feature is ever added, it must use a dedicated vault/processor flow with strict authorization and short-lived access.
- Store only:
  - card token / processor card id
  - last4 + brand (if allowed)
  - expiration month/year (if allowed)
  - card status (active/frozen/closed)
  - limits configuration
- PII fields (e.g., name, email) are out of scope; if present in the future, classify and protect accordingly.

### Monetary & time rules
- Use **Decimal** (or exact integer minor units) for all money computations.
- Always store a currency code (ISO 4217). Do not assume a default.
- All timestamps are UTC and stored as ISO-8601 strings for transport; persist as timezone-aware types.

### State machine (card status)
- `ACTIVE` → can authorize
- `FROZEN` → declines authorizations (unless processor supports soft controls; still treat as decline)
- `CLOSED` → terminal state; cannot be unfrozen

Allowed transitions:
- Create: `NONE` → `ACTIVE`
- Freeze: `ACTIVE` → `FROZEN`
- Unfreeze: `FROZEN` → `ACTIVE`
- Close: `ACTIVE|FROZEN` → `CLOSED`

### Idempotency & concurrency
- Mutating operations must support an **idempotency key** (per user + operation type) to prevent duplicate cards or duplicate state flips.
- Use optimistic concurrency (version field) or conditional updates to avoid lost updates.

### Audit logging (immutability)
- Every mutating action must append an audit record:
  - who: user_id/service principal
  - what: action type (CREATE_CARD, FREEZE_CARD, …)
  - target: card_id
  - when: timestamp
  - where: request metadata (IP/device) if available
  - why: optional reason field (ops actions)
  - correlation_id / request_id
  - before/after snapshots of **non-sensitive** fields only
- Audit records are append-only; never updated or deleted (except via retention policy / legal requirements).

### Authorization model (high-level)
- End-users can only access cards and transactions for their own account.
- Ops/compliance users have read-only access for investigations, with query logging and justification.
- Admin-only actions (e.g., force close) require stronger controls and must be separately audited.

### Rate limiting & abuse prevention
- Rate limit card creation and state changes per user/account.
- Add fraud/abuse signals as extension points (e.g., velocity rules) but do not block initial implementation.

### Observability & reliability
- Structured logs with redaction.
- Metrics: card creates, freezes, declines by reason, limit checks, processor latency.
- Tracing: propagate correlation IDs across processor calls.

### Out of scope (explicit)
- UI/UX designs, actual HTTP API definition, processor contract details
- Disputes/chargebacks
- 3DS/SCA flows
- KYC/AML onboarding
- Card printing / physical cards
- Notifications (email/SMS/push)

## Context

### Beginning context
- Repository contains `homework-3/` with:
  - `TASKS.md`
  - `specification-TEMPLATE-example.md`
- No implementation code is required for this homework.

### Ending context
- New documents created:
  - `homework-3/specification.md` (this file)
  - `homework-3/agents.md`
  - `.github/copilot-instructions.md`
  - `homework-3/README.md`
- A future implementation (not part of homework) would include:
  - domain models (Card, CardLimit, Transaction, AuditEvent)
  - storage migrations
  - service layer + processor adapter
  - authn/authz middleware
  - tests + threat model notes

## Low-Level Tasks

### 1. Define data model & state machine for virtual cards

**What prompt would you run to complete this task?**
Create domain models for VirtualCard, CardLimit, and state transitions, ensuring no sensitive PAN/CVV is stored, money uses Decimal/minor units, and transitions are validated.

**What file do you want to CREATE or UPDATE?**
- `src/models/virtual_card.py`
- `src/models/card_limit.py`
- `src/models/enums.py`

**What function do you want to CREATE or UPDATE?**
- `VirtualCard.apply_event(...)` or `VirtualCard.transition_to(...)`
- `CardLimit.validate(...)`

**What are details you want to add to drive the code changes?**
- Fields: `card_id`, `account_id`, `processor_card_token`, `last4`, `brand`, `exp_month`, `exp_year`, `status`, `limits`, `created_at`, `updated_at`, `version`.
- Enforce allowed transitions only.
- Reject invalid/unknown currencies.
- Ensure serialization excludes sensitive fields.

### 2. Design audit event schema and immutable storage pattern

**What prompt would you run to complete this task?**
Design an append-only audit event model and storage contract that records all card mutations and ops queries, includes correlation IDs, and avoids logging sensitive card data.

**What file do you want to CREATE or UPDATE?**
- `src/models/audit_event.py`
- `src/storage/audit_store.py`
- `docs/audit-log.md`

**What function do you want to CREATE or UPDATE?**
- `AuditStore.append(event)`
- `redact_sensitive_fields(payload)`

**What are details you want to add to drive the code changes?**
- Required fields: `event_id`, `event_type`, `actor_type`, `actor_id`, `subject_type`, `subject_id`, `timestamp`, `correlation_id`, `before`, `after`, `metadata`.
- `before/after` must be allowlisted fields only.
- Provide retention guidance and tamper-evidence notes (e.g., hash chaining as a future enhancement).

### 3. Implement idempotent command handlers for create/freeze/unfreeze/close

**What prompt would you run to complete this task?**
Implement service-layer command handlers for card lifecycle actions with idempotency keys, optimistic concurrency, and audit append on success/failure.

**What file do you want to CREATE or UPDATE?**
- `src/services/virtual_card_service.py`
- `src/storage/card_store.py`
- `src/utils/idempotency.py`

**What function do you want to CREATE or UPDATE?**
- `create_virtual_card(account_id, idempotency_key, ...)`
- `freeze_card(card_id, actor, idempotency_key, reason=None)`
- `unfreeze_card(...)`
- `close_card(...)`

**What are details you want to add to drive the code changes?**
- Idempotency scope: `(account_id, operation, idempotency_key)`.
- On replay, return original result (do not re-call processor).
- Ensure `CLOSED` cannot be unfrozen.
- Audit both success and rejected attempts (with reason codes).

### 4. Implement limit evaluation & authorization decision function

**What prompt would you run to complete this task?**
Create a deterministic limit evaluation function that decides approval/decline for an authorization request, considering card status and configured limits.

**What file do you want to CREATE or UPDATE?**
- `src/services/limit_evaluator.py`
- `src/models/authorization.py`

**What function do you want to CREATE or UPDATE?**
- `evaluate_authorization(card, auth_request, ledger_snapshot) -> Decision`

**What are details you want to add to drive the code changes?**
- Inputs include amount (minor units), currency, merchant category, timestamp.
- Decision includes `approved: bool`, `decline_reason`, `remaining_limits`.
- Ensure time-window calculations are UTC and well-defined.

### 5. Produce transaction read model with pagination and ops/compliance access controls

**What prompt would you run to complete this task?**
Implement transaction listing with stable pagination, filtering, and strict authorization rules for end-users vs ops/compliance, with query audit logging.

**What file do you want to CREATE or UPDATE?**
- `src/services/transaction_query_service.py`
- `src/models/transaction.py`
- `src/auth/authorization.py`

**What function do you want to CREATE or UPDATE?**
- `list_transactions(account_id, card_id=None, from_ts=None, to_ts=None, page_token=None)`
- `authorize_principal(principal, action, resource)`

**What are details you want to add to drive the code changes?**
- Pagination uses opaque cursor/token.
- All ops/compliance queries require justification string and are audited.
- Ensure transactions store only processor-safe references, not raw card data.

### 6. Add security testing and abuse regression tests

**What prompt would you run to complete this task?**
Add a test suite covering state transitions, idempotency, limit boundaries, redaction, and access control, including negative tests for sensitive data leakage.

**What file do you want to CREATE or UPDATE?**
- `tests/test_virtual_card_state_machine.py`
- `tests/test_idempotency.py`
- `tests/test_limit_evaluator.py`
- `tests/test_audit_redaction.py`
- `tests/test_ops_access_controls.py`

**What function do you want to CREATE or UPDATE?**
- Unit tests + a small property-based or table-driven test set for limits.

**What are details you want to add to drive the code changes?**
- Verify logs/audit payloads never include PAN-like patterns.
- Verify freeze/unfreeze are race-safe with version checks.
- Verify replaying idempotency keys does not double-create cards.

---

## Appendix: Example audit event types
- `CARD_CREATED`, `CARD_FROZEN`, `CARD_UNFROZEN`, `CARD_CLOSED`
- `LIMIT_SET`, `LIMIT_UPDATED`
- `TXN_LIST_QUERIED` (ops/compliance)
- `AUTH_DECISION_MADE`

## Appendix: Decline reason codes (suggested)
- `CARD_FROZEN`
- `CARD_CLOSED`
- `LIMIT_EXCEEDED_PER_TXN`
- `LIMIT_EXCEEDED_DAILY`
- `LIMIT_EXCEEDED_MONTHLY`
- `CURRENCY_NOT_SUPPORTED`
- `POLICY_VIOLATION`
