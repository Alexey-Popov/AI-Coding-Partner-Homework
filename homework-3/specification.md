# Virtual Card Lifecycle Management — Specification

> Ingest the information from this file, implement the Low-Level Tasks, and generate the code that will satisfy the High and Mid-Level Objectives.

## High-Level Objective

- Build a virtual card lifecycle management system that allows end-users to create, freeze/unfreeze, configure spending limits, and view transactions for virtual cards — within a regulated banking environment that enforces auditability, PCI-DSS compliance, and role-based access control.

## Mid-Level Objectives

1. **Card Creation with Identity Verification** — Users can request a new virtual card. The system gates issuance behind a KYC/identity verification check and returns masked card details (last four digits only in responses).
2. **Card State Management (Freeze / Unfreeze)** — Users can freeze or unfreeze a card instantly. Every state transition is recorded with a timestamp, actor, reason, and prior state for audit purposes.
3. **Spending Limit Configuration** — Users can set and update per-transaction, daily, and monthly spending limits. Limits are enforced at authorization time and stored as fixed-point decimal values.
4. **Transaction History** — Users can retrieve paginated, filterable transaction history for any card they own. Filters include date range, amount range, merchant, and status. Export to CSV is supported.
5. **Compliance & Audit Trail** — Every mutation (card creation, state change, limit update) emits an immutable audit event. Audit events are append-only, include the acting user, IP address, timestamp, and a before/after snapshot of the changed entity.
6. **Role-Based Access Control** — End-users can only manage their own cards. Ops/compliance users can view any card's state and audit log but cannot modify cards on behalf of users without an elevated authorization flow.

## Implementation Notes

- **Monetary values**: Use fixed-point decimal types for all monetary fields. Never use floating-point representations for money.
- **PCI-DSS**: Full PAN and CVV must never appear in logs, API responses (except at creation), or error messages. Store card numbers encrypted at rest (AES-256). Tokenize for internal references.
- **GDPR / CCPA**: PII (name, email, address) must be maskable on request. Define a data retention policy (e.g., 7 years for transaction records, 90 days for session logs). Support right-to-erasure for non-regulatory-mandated data.
- **Idempotency**: All mutating endpoints must accept an `Idempotency-Key` header to prevent duplicate operations (e.g., double card creation).
- **Event sourcing**: Card state changes are persisted as an ordered event log. The current state is a projection of all events. This provides a built-in audit trail and supports replaying state for investigations.
- **Input validation**: Validate and sanitize all inputs at the API boundary. Reject unexpected fields. Enforce maximum lengths, allowed character sets, and domain constraints (e.g., spending limits > 0).
- **Error handling**: Return structured error responses (`{ "error": { "code": "...", "message": "...", "details": [...] } }`). Never leak stack traces, internal paths, or database details in error responses.
- **Logging**: Use structured JSON logging. Log request IDs, user IDs, and operation types. Never log sensitive card data.
- **Rate limiting**: Apply rate limits to card creation (e.g., max 5 cards per user per day) and freeze/unfreeze (e.g., max 10 toggles per card per hour) to prevent abuse.
- **Testing**: Require unit tests for all service-layer functions, integration tests for API endpoints, and dedicated compliance test cases that assert audit events are emitted for every mutation.

## Formal Contracts

### Card State Machine

```
                 ┌───────────────┐
                 │    CREATED    │
                 │  (internal)   │
                 └──────┬────────┘
                        │ KYC verified
                        ▼
               ┌────────────────┐
        ┌─────►│     ACTIVE     │◄─────┐
        │      └───────┬────────┘      │
        │              │               │
        │  unfreeze    │ freeze        │
        │              ▼               │
        │      ┌────────────────┐      │
        └──────┤     FROZEN     │      │
               └───────┬────────┘      │
                       │               │
                       │ cancel        │ cancel
                       ▼               │
               ┌────────────────┐      │
               │   CANCELLED    │◄─────┘
               │  (terminal)    │
               └────────────────┘
```

**Allowed transitions:**

| From | To | Action | Reversible |
|------|----|--------|------------|
| ACTIVE | FROZEN | `freezeCard` | Yes |
| FROZEN | ACTIVE | `unfreezeCard` | Yes |
| ACTIVE | CANCELLED | `cancelCard` | No |
| FROZEN | CANCELLED | `cancelCard` | No |

Any transition not listed above must be rejected with error code `INVALID_STATE_TRANSITION` (409 Conflict).

---

### Enum Definitions

**CardStatus**
```
ACTIVE | FROZEN | CANCELLED
```

**TransactionStatus**
```
PENDING | SETTLED | DECLINED | REVERSED
```

**LimitType**
```
PER_TRANSACTION | DAILY | MONTHLY
```

**UserRole**
```
END_USER | OPS | COMPLIANCE
```

**AuditAction**
```
CARD_CREATED | CARD_FROZEN | CARD_UNFROZEN | CARD_CANCELLED | LIMIT_SET | LIMIT_REMOVED
```

---

### API Contract Table

All endpoints require `Authorization: Bearer <JWT>` header. All request/response bodies are `application/json` unless noted.

| Method | Path | Request Body | Success Response | Auth | Rate Limit |
|--------|------|-------------|-----------------|------|------------|
| `POST` | `/api/v1/cards` | `CreateCardRequest` | `201` `CardResponse` | END_USER | 5/user/day |
| `GET` | `/api/v1/cards/{cardId}` | — | `200` `CardResponse` | Owner or OPS/COMPLIANCE | — |
| `POST` | `/api/v1/cards/{cardId}/freeze` | `FreezeCardRequest` | `200` `CardResponse` | Owner or OPS | 10/card/hour |
| `POST` | `/api/v1/cards/{cardId}/unfreeze` | `UnfreezeCardRequest` | `200` `CardResponse` | Owner or OPS | 10/card/hour |
| `POST` | `/api/v1/cards/{cardId}/cancel` | `CancelCardRequest` | `200` `CardResponse` | Owner | — |
| `PUT` | `/api/v1/cards/{cardId}/limits/{limitType}` | `SetSpendingLimitRequest` | `200` `SpendingLimitResponse` | Owner | — |
| `GET` | `/api/v1/cards/{cardId}/limits` | — | `200` `SpendingLimitResponse[]` | Owner or OPS/COMPLIANCE | — |
| `GET` | `/api/v1/cards/{cardId}/transactions` | — (query params) | `200` `PaginatedResponse<TransactionResponse>` | Owner or OPS/COMPLIANCE | 60/user/min |
| `GET` | `/api/v1/cards/{cardId}/transactions/export` | — (query params) | `200` `text/csv` stream | Owner or OPS/COMPLIANCE | 10/user/hour |
| `GET` | `/api/v1/audit` | — (query params) | `200` `PaginatedResponse<AuditEventResponse>` | OPS/COMPLIANCE only | — |

**Common headers:**

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | All endpoints | `Bearer <JWT>` |
| `Idempotency-Key` | `POST` / `PUT` | UUID, prevents duplicate mutations |
| `X-Request-ID` | Optional | UUID for request tracing; auto-generated if absent |

---

### JSON Schema Contracts

#### CreateCardRequest
```json
{
  "currency": "USD"
}
```
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `currency` | string | Yes | ISO 4217, 3 uppercase letters |

#### CardResponse
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "user_id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "masked_pan": "**** **** **** 4242",
  "status": "ACTIVE",
  "currency": "USD",
  "created_at": "2025-01-15T09:30:00Z",
  "updated_at": "2025-01-15T09:30:00Z",
  "cancelled_at": null
}
```
| Field | Type | Nullable | Notes |
|-------|------|----------|-------|
| `id` | UUID | No | |
| `user_id` | UUID | No | |
| `masked_pan` | string | No | Format: `**** **** **** NNNN` |
| `status` | CardStatus | No | |
| `currency` | string | No | ISO 4217 |
| `created_at` | datetime | No | ISO 8601 |
| `updated_at` | datetime | No | ISO 8601 |
| `cancelled_at` | datetime | Yes | Set when status → CANCELLED |

#### FreezeCardRequest / UnfreezeCardRequest / CancelCardRequest
```json
{
  "reason": "Lost card reported by customer"
}
```
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `reason` | string | Yes | 1–500 characters |

#### SetSpendingLimitRequest
```json
{
  "amount": "1000.00",
  "currency": "USD"
}
```
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `amount` | decimal (string) | Yes | > 0, max 19 digits, max 4 decimal places |
| `currency` | string | Yes | ISO 4217 |

#### SpendingLimitResponse
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "card_id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "limit_type": "DAILY",
  "amount": "1000.00",
  "currency": "USD",
  "updated_at": "2025-01-15T10:00:00Z"
}
```

#### TransactionResponse
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "card_id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "amount": "42.99",
  "currency": "USD",
  "merchant_name": "Coffee Shop",
  "merchant_category_code": "5814",
  "status": "SETTLED",
  "transacted_at": "2025-01-15T08:22:00Z",
  "settled_at": "2025-01-16T02:00:00Z"
}
```

#### Transaction Query Parameters
| Parameter | Type | Default | Constraints |
|-----------|------|---------|-------------|
| `date_from` | datetime | — | ISO 8601 |
| `date_to` | datetime | — | ISO 8601, must be ≥ `date_from` |
| `amount_min` | decimal | — | ≥ 0 |
| `amount_max` | decimal | — | ≥ `amount_min` |
| `merchant_name` | string | — | Case-insensitive partial match |
| `status` | TransactionStatus | — | Enum value |
| `page` | integer | 1 | ≥ 1 |
| `page_size` | integer | 20 | 1–100 |
| `sort_by` | string | `transacted_at` | `transacted_at` or `amount` |
| `sort_order` | string | `desc` | `asc` or `desc` |

#### PaginatedResponse\<T\>
```json
{
  "items": [ ... ],
  "page": 1,
  "page_size": 20,
  "total_count": 142
}
```

#### AuditEventResponse
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "entity_type": "card",
  "entity_id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "action": "CARD_FROZEN",
  "actor_id": "8a6e0804-2bd0-4672-b79d-d97027f9e012",
  "actor_role": "END_USER",
  "ip_address": "192.168.1.42",
  "before_snapshot": { "status": "ACTIVE" },
  "after_snapshot": { "status": "FROZEN" },
  "created_at": "2025-01-15T11:45:00Z"
}
```

#### Audit Query Parameters
| Parameter | Type | Required | Constraints |
|-----------|------|----------|-------------|
| `entity_type` | string | Yes | e.g., `card`, `spending_limit` |
| `entity_id` | UUID | No | Filter to a specific entity |
| `action` | AuditAction | No | Filter by action type |
| `actor_id` | UUID | No | Filter by who performed the action |
| `date_from` | datetime | No | ISO 8601 |
| `date_to` | datetime | No | ISO 8601 |
| `page` | integer | No | Default: 1, ≥ 1 |
| `page_size` | integer | No | Default: 20, 1–100 |

---

### Error Catalog

All errors follow this structure:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable description",
    "details": []
  }
}
```

| Error Code | HTTP Status | Description | Trigger |
|------------|-------------|-------------|---------|
| `AUTHENTICATION_REQUIRED` | 401 | Missing or invalid JWT token | No token, expired token, invalid signature |
| `FORBIDDEN` | 403 | User lacks permission for this action | Wrong role or accessing another user's resource |
| `CARD_NOT_FOUND` | 404 | Card does not exist | Invalid `cardId` |
| `USER_NOT_FOUND` | 404 | User does not exist | Invalid `userId` in token |
| `INVALID_STATE_TRANSITION` | 409 | Card cannot transition to the requested state | e.g., freeze a CANCELLED card |
| `CARD_ALREADY_FROZEN` | 409 | Card is already in FROZEN state | Freeze an already-frozen card |
| `CARD_ALREADY_ACTIVE` | 409 | Card is already in ACTIVE state | Unfreeze an already-active card |
| `KYC_NOT_VERIFIED` | 403 | User has not completed KYC verification | Create card without KYC |
| `IDEMPOTENCY_CONFLICT` | 409 | Same idempotency key used with different payload | Reuse key with changed request body |
| `VALIDATION_ERROR` | 422 | Request body fails schema validation | Missing fields, wrong types, constraint violations |
| `INVALID_CURRENCY` | 422 | Currency code is not a valid ISO 4217 code | Unrecognized currency |
| `INVALID_AMOUNT` | 422 | Monetary amount is invalid | Negative, zero, or too many decimal places |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests | Exceeds per-endpoint rate limit |
| `INTERNAL_ERROR` | 500 | Unexpected server error | Unhandled exception (details never leaked) |

Rate limit errors include a `Retry-After` header (seconds until the limit resets).

---

### Domain Invariants & Executable Constraints

Every invariant below is written as a testable assertion. Implementations must enforce these as runtime guards **and** verify them in the compliance test suite.

#### Entity Invariants (must hold true at all times)

```
INV-C1: Card.masked_pan MATCHES last 4 digits of decrypt(Card.encrypted_pan)
  ASSERT masked_pan == "**** **** **** " + decrypt(encrypted_pan)[-4:]

INV-C2: Card.status IN {ACTIVE, FROZEN, CANCELLED}
  ASSERT Card.status IS a valid CardStatus enum member

INV-C3: Card.cancelled_at IS NOT NULL IF AND ONLY IF Card.status == CANCELLED
  ASSERT (status == CANCELLED) == (cancelled_at IS NOT NULL)

INV-C4: Card.user_id references an existing User
  ASSERT User with id == Card.user_id EXISTS

INV-L1: SpendingLimit.amount > 0
  ASSERT amount > 0 for every SpendingLimit record

INV-L2: At most one SpendingLimit per (card_id, limit_type) pair
  ASSERT UNIQUE(card_id, limit_type) — enforced at the database constraint level

INV-T1: Transaction.amount > 0
  ASSERT amount > 0 for every Transaction record

INV-T2: Transaction.currency is a valid ISO 4217 code
  ASSERT currency IN iso_4217_allowlist

INV-A1: AuditEvent table is append-only — no rows are ever updated or deleted
  ASSERT row count of audit_events never decreases
  ASSERT no UPDATE or DELETE statements target the audit_events table
```

#### Preconditions (must hold true BEFORE an operation executes)

```
PRE-CREATE-1: User.kyc_verified == true
  GIVEN a user with kyc_verified == false
  WHEN  createCard(userId) is called
  THEN  the system rejects with KYC_NOT_VERIFIED (403)

PRE-CREATE-2: Card count for user in last 24 hours < 5
  GIVEN a user who has created 5 cards today
  WHEN  createCard(userId) is called
  THEN  the system rejects with RATE_LIMIT_EXCEEDED (429)

PRE-FREEZE-1: Card.status == ACTIVE
  GIVEN a card with status FROZEN
  WHEN  freezeCard(cardId) is called
  THEN  the system rejects with CARD_ALREADY_FROZEN (409)

PRE-FREEZE-2: Card.status != CANCELLED
  GIVEN a card with status CANCELLED
  WHEN  freezeCard(cardId) is called
  THEN  the system rejects with INVALID_STATE_TRANSITION (409)

PRE-UNFREEZE-1: Card.status == FROZEN
  GIVEN a card with status ACTIVE
  WHEN  unfreezeCard(cardId) is called
  THEN  the system rejects with CARD_ALREADY_ACTIVE (409)

PRE-LIMIT-1: Card.status != CANCELLED
  GIVEN a card with status CANCELLED
  WHEN  setSpendingLimit(cardId, limitType, amount) is called
  THEN  the system rejects with INVALID_STATE_TRANSITION (409)

PRE-LIMIT-2: amount > 0 AND decimal places ≤ 4
  GIVEN amount == 0 OR amount < 0 OR amount has 5+ decimal places
  WHEN  setSpendingLimit is called
  THEN  the system rejects with INVALID_AMOUNT (422)

PRE-ACCESS-1: Requesting user owns the card OR has role OPS/COMPLIANCE
  GIVEN user A requesting card owned by user B, where A.role == END_USER
  WHEN  any card endpoint is called
  THEN  the system rejects with FORBIDDEN (403)
```

#### Postconditions (must hold true AFTER an operation completes successfully)

```
POST-CREATE-1: A new Card record exists with status == ACTIVE
  GIVEN a successful createCard call
  THEN  Card with the returned id EXISTS
  AND   Card.status == ACTIVE
  AND   Card.user_id == the requesting user's id

POST-CREATE-2: An AuditEvent with action == CARD_CREATED exists
  GIVEN a successful createCard call
  THEN  AuditEvent WHERE entity_id == card.id AND action == "CARD_CREATED" EXISTS
  AND   AuditEvent.actor_id == the requesting user's id
  AND   AuditEvent.after_snapshot does NOT contain "pan" or "cvv" keys

POST-FREEZE-1: Card.status == FROZEN AND updated_at is refreshed
  GIVEN a successful freezeCard call
  THEN  Card.status == FROZEN
  AND   Card.updated_at > Card.updated_at (before the call)

POST-FREEZE-2: AuditEvent captures before and after state
  GIVEN a successful freezeCard call
  THEN  AuditEvent.before_snapshot.status == "ACTIVE"
  AND   AuditEvent.after_snapshot.status == "FROZEN"

POST-UNFREEZE-1: Card.status == ACTIVE
  GIVEN a successful unfreezeCard call
  THEN  Card.status == ACTIVE

POST-LIMIT-1: SpendingLimit record reflects the new amount
  GIVEN a successful setSpendingLimit call with amount == X
  THEN  SpendingLimit WHERE card_id AND limit_type MATCHES has amount == X

POST-LIMIT-2: AuditEvent records the limit change
  GIVEN a successful setSpendingLimit call
  THEN  AuditEvent WHERE action == "LIMIT_SET" AND entity_id == limit.id EXISTS
  AND   AuditEvent.before_snapshot contains the previous amount (or null if new)
  AND   AuditEvent.after_snapshot contains the new amount

POST-CANCEL-1: Card.status == CANCELLED AND cancelled_at is set
  GIVEN a successful cancelCard call
  THEN  Card.status == CANCELLED
  AND   Card.cancelled_at IS NOT NULL
  AND   Card.cancelled_at ≈ current timestamp (within 5 seconds)
```

#### Cross-Entity Constraints

```
CROSS-1: Spending limit enforcement at authorization time
  GIVEN a card with a PER_TRANSACTION limit of $100
  WHEN  a transaction of $150 is attempted
  THEN  the transaction is DECLINED

CROSS-2: Daily spending limit accumulation
  GIVEN a card with a DAILY limit of $500
  AND   $450 in SETTLED + PENDING transactions today
  WHEN  a transaction of $75 is attempted
  THEN  the transaction is DECLINED

CROSS-3: Monthly spending limit accumulation
  GIVEN a card with a MONTHLY limit of $5000
  AND   $4900 in SETTLED + PENDING transactions this calendar month
  WHEN  a transaction of $200 is attempted
  THEN  the transaction is DECLINED

CROSS-4: Frozen cards decline all transactions
  GIVEN a card with status == FROZEN
  WHEN  any transaction is attempted
  THEN  the transaction is DECLINED

CROSS-5: Cancelled cards decline all transactions permanently
  GIVEN a card with status == CANCELLED
  WHEN  any transaction is attempted
  THEN  the transaction is DECLINED
  AND   the card cannot be unfrozen or reactivated
```

#### Idempotency Constraints

```
IDEM-1: Same idempotency key + same payload = same response
  GIVEN a successful POST /api/v1/cards with Idempotency-Key == K and body == B
  WHEN  POST /api/v1/cards is called again with Idempotency-Key == K and body == B
  THEN  the response is identical to the first response
  AND   no new Card record is created
  AND   no new AuditEvent is emitted

IDEM-2: Same idempotency key + different payload = conflict
  GIVEN a successful POST /api/v1/cards with Idempotency-Key == K and body == B1
  WHEN  POST /api/v1/cards is called again with Idempotency-Key == K and body == B2 (B2 != B1)
  THEN  the system rejects with IDEMPOTENCY_CONFLICT (409)
```

#### Security Invariants

```
SEC-1: PAN never appears in logs
  ASSERT no log entry at any level contains a 16-digit card number pattern
  ASSERT no log entry contains the key "pan", "card_number", or "cvv"

SEC-2: PAN never appears in API responses (except card creation)
  GIVEN any GET request to any endpoint
  THEN  the response body does NOT contain a field named "pan" or "card_number"

SEC-3: PAN never appears in audit event snapshots
  ASSERT for every AuditEvent: before_snapshot and after_snapshot do NOT contain "pan" or "cvv"

SEC-4: Error responses never leak internals
  GIVEN any request that triggers a 500 error
  THEN  the response body contains ONLY { error: { code, message, details } }
  AND   message does NOT contain file paths, stack traces, SQL, or class names

SEC-5: Expired or invalid JWT tokens are rejected
  GIVEN a request with an expired JWT token
  WHEN  any endpoint is called
  THEN  the system responds with AUTHENTICATION_REQUIRED (401)
  AND   no business logic is executed
```

---

### Property-Based Requirements

Property-based requirements define universally quantified rules that must hold for **all** valid inputs, not just specific examples. These drive randomized/fuzz testing with thousands of generated inputs to surface edge cases that example-based tests miss.

#### State Machine Properties

```
PROP-SM-1: Closure under valid transitions
  FOR ALL card, FOR ALL action IN {freeze, unfreeze, cancel}:
    IF transition(card.status, action) is valid per the state machine
    THEN the resulting status is a valid CardStatus member

PROP-SM-2: Freeze-unfreeze roundtrip (identity)
  FOR ALL card WHERE card.status == ACTIVE:
    freezeCard(card); unfreezeCard(card)
    ⟹ card.status == ACTIVE
    ⟹ card data (masked_pan, user_id, limits) is unchanged

PROP-SM-3: Cancel is absorbing (terminal state)
  FOR ALL card WHERE card.status == CANCELLED:
    FOR ALL action IN {freeze, unfreeze, cancel}:
      action(card) ⟹ REJECTED with INVALID_STATE_TRANSITION
    card.status remains CANCELLED indefinitely

PROP-SM-4: No phantom transitions
  FOR ALL card, FOR ALL action:
    IF action(card) is REJECTED
    THEN card.status == card.status_before_call
    AND  card.updated_at == card.updated_at_before_call
    AND  no AuditEvent is emitted for this action
```

#### Monetary Properties

```
PROP-MON-1: Spending limit enforcement is total
  FOR ALL card, FOR ALL limit of type PER_TRANSACTION with amount L,
  FOR ALL transaction with amount T:
    IF T > L THEN transaction.status == DECLINED

PROP-MON-2: Daily accumulation is sound
  FOR ALL card with DAILY limit D,
  LET today_total = SUM(amount) WHERE card_id == card.id
                    AND status IN {PENDING, SETTLED}
                    AND transacted_at is today:
    FOR ALL new transaction with amount T:
      IF today_total + T > D THEN transaction.status == DECLINED
      IF today_total + T ≤ D THEN transaction MAY be approved (subject to other limits)

PROP-MON-3: Monthly accumulation is sound
  FOR ALL card with MONTHLY limit M,
  LET month_total = SUM(amount) WHERE card_id == card.id
                    AND status IN {PENDING, SETTLED}
                    AND transacted_at is in current calendar month:
    FOR ALL new transaction with amount T:
      IF month_total + T > M THEN transaction.status == DECLINED

PROP-MON-4: Limit hierarchy consistency
  FOR ALL card with PER_TRANSACTION limit P, DAILY limit D, MONTHLY limit M:
    It is VALID for P > D or P > M (the system does not enforce hierarchy)
    BUT effective enforcement means:
      No single transaction exceeds P
      No day's total exceeds D
      No month's total exceeds M

PROP-MON-5: Decimal precision is preserved
  FOR ALL amount A represented as a decimal string with ≤ 4 decimal places:
    setSpendingLimit(card, type, A); getSpendingLimits(card)
    ⟹ returned amount == A exactly (no floating-point drift)
    Formally: parse(serialize(A)) == A
```

#### Audit Properties

```
PROP-AUD-1: Audit completeness
  FOR ALL mutations M applied to the system:
    |audit_events AFTER M| == |audit_events BEFORE M| + 1
    (exactly one audit event per successful mutation, zero for rejected mutations)

PROP-AUD-2: Audit consistency
  FOR ALL audit_event E:
    IF E.action == CARD_FROZEN
    THEN E.before_snapshot.status == ACTIVE
    AND  E.after_snapshot.status == FROZEN
    (snapshot content always matches the actual state transition)

PROP-AUD-3: Audit monotonicity
  FOR ALL audit_events E1, E2 WHERE E1.created_at < E2.created_at:
    E1.id != E2.id
    (audit events are strictly ordered, no duplicates, no gaps in time)

PROP-AUD-4: Audit immutability
  FOR ALL audit_event E at time T1 and the same E at time T2 (T2 > T1):
    E at T1 == E at T2
    (no field of any audit event ever changes after creation)
```

#### Idempotency Properties

```
PROP-IDEM-1: Idempotent replay
  FOR ALL valid request R with Idempotency-Key K:
    LET response1 = execute(R with K)
    LET response2 = execute(R with K)  -- identical payload
    response1 == response2
    AND database state after response2 == database state after response1

PROP-IDEM-2: Key isolation
  FOR ALL keys K1 != K2, FOR ALL requests R1 and R2:
    execute(R1 with K1) and execute(R2 with K2) are independent
    (different keys never interfere with each other)
```

#### Security Properties

```
PROP-SEC-1: PAN non-observability
  FOR ALL system outputs O IN {API responses, log entries, audit snapshots, error messages}:
    FOR ALL cards C in the system:
      decrypt(C.encrypted_pan) does NOT appear as a substring of O
    (except: the initial createCard response, exactly once)

PROP-SEC-2: Authorization totality
  FOR ALL requests R, FOR ALL users U:
    IF U.role == END_USER AND R.target_card.user_id != U.id
    THEN R is REJECTED with 403
    (no endpoint ever leaks another user's data, regardless of path or parameters)

PROP-SEC-3: Rate limit fairness
  FOR ALL users U, FOR ALL rate-limited endpoints E with limit L per window W:
    |successful requests from U to E within W| ≤ L
    (rate limits cannot be bypassed by varying headers, parameters, or timing)
```

---

### Formal Verification Annotations

The following annotations express operation contracts in a mathematical notation suitable for formal verification tools, code-level contract assertions, or rigorous code review. Each annotation uses Hoare-triple-style `{P} operation {Q}` syntax where `P` is the precondition and `Q` is the postcondition.

#### Type Definitions

```
type CardId     = UUID
type UserId     = UUID
type Amount     = { value: Decimal | value > 0 ∧ decimalPlaces(value) ≤ 4 }
type Currency   = { code: String  | code ∈ ISO_4217 }
type CardStatus = ACTIVE | FROZEN | CANCELLED
type LimitType  = PER_TRANSACTION | DAILY | MONTHLY

type Card = {
  id:            CardId,
  user_id:       UserId,
  masked_pan:    String,
  encrypted_pan: Bytes,
  status:        CardStatus,
  created_at:    Timestamp,
  updated_at:    Timestamp,
  cancelled_at:  Timestamp | null
}

type SpendingLimit = {
  card_id:    CardId,
  limit_type: LimitType,
  amount:     Amount,
  currency:   Currency
}

type AuditEvent = {
  entity_id:       UUID,
  action:          String,
  actor_id:        UserId,
  before_snapshot: JSON,
  after_snapshot:  JSON,
  created_at:      Timestamp
}
```

#### Operation Contracts

```
──────────────────────────────────────────────────────────────
createCard(userId: UserId, key: IdempotencyKey) → Card
──────────────────────────────────────────────────────────────
  requires:
    ∃ user ∈ Users : user.id = userId ∧ user.kyc_verified = true
    |{ c ∈ Cards : c.user_id = userId ∧ c.created_at > now() - 24h }| < 5
    ¬∃ card ∈ Cards : card.idempotency_key = key ∧ card.user_id = userId
      ∨ (∃ card : card.idempotency_key = key → return card)  -- idempotent
  ensures:
    ∃ card' ∈ Cards' : card'.id ∉ Cards ∧ card'.status = ACTIVE
                       ∧ card'.user_id = userId
    card'.masked_pan = mask(decrypt(card'.encrypted_pan))
    ∃ e ∈ AuditEvents' : e.entity_id = card'.id ∧ e.action = "CARD_CREATED"
                         ∧ "pan" ∉ keys(e.after_snapshot)
    |Cards'| = |Cards| + 1
    |AuditEvents'| = |AuditEvents| + 1

──────────────────────────────────────────────────────────────
freezeCard(cardId: CardId, actorId: UserId, reason: String) → Card
──────────────────────────────────────────────────────────────
  requires:
    ∃ card ∈ Cards : card.id = cardId ∧ card.status = ACTIVE
    ∃ actor ∈ Users : actor.id = actorId
      ∧ (card.user_id = actorId ∨ actor.role ∈ {OPS})
    |{ e ∈ AuditEvents : e.entity_id = cardId
       ∧ e.action ∈ {"CARD_FROZEN", "CARD_UNFROZEN"}
       ∧ e.created_at > now() - 1h }| < 10
  ensures:
    card'.status = FROZEN
    card'.updated_at > card.updated_at
    ∀ fields f ∈ {id, user_id, masked_pan, encrypted_pan, created_at}:
      card'.f = card.f  -- unchanged fields
    ∃ e ∈ AuditEvents' : e.entity_id = cardId ∧ e.action = "CARD_FROZEN"
      ∧ e.before_snapshot.status = "ACTIVE"
      ∧ e.after_snapshot.status = "FROZEN"

──────────────────────────────────────────────────────────────
unfreezeCard(cardId: CardId, actorId: UserId, reason: String) → Card
──────────────────────────────────────────────────────────────
  requires:
    ∃ card ∈ Cards : card.id = cardId ∧ card.status = FROZEN
    ∃ actor ∈ Users : actor.id = actorId
      ∧ (card.user_id = actorId ∨ actor.role ∈ {OPS})
  ensures:
    card'.status = ACTIVE
    card'.updated_at > card.updated_at
    ∃ e ∈ AuditEvents' : e.action = "CARD_UNFROZEN"
      ∧ e.before_snapshot.status = "FROZEN"
      ∧ e.after_snapshot.status = "ACTIVE"

──────────────────────────────────────────────────────────────
cancelCard(cardId: CardId, actorId: UserId, reason: String) → Card
──────────────────────────────────────────────────────────────
  requires:
    ∃ card ∈ Cards : card.id = cardId ∧ card.status ∈ {ACTIVE, FROZEN}
    card.user_id = actorId  -- only owner can cancel
  ensures:
    card'.status = CANCELLED
    card'.cancelled_at ≠ null ∧ |card'.cancelled_at - now()| < 5s
    card'.updated_at > card.updated_at
    ∃ e ∈ AuditEvents' : e.action = "CARD_CANCELLED"
    -- terminal: no further transitions possible
    ∀ future operations op on card': op ⟹ REJECTED

──────────────────────────────────────────────────────────────
setSpendingLimit(cardId: CardId, limitType: LimitType,
                 amount: Amount, actorId: UserId) → SpendingLimit
──────────────────────────────────────────────────────────────
  requires:
    ∃ card ∈ Cards : card.id = cardId ∧ card.status ≠ CANCELLED
    card.user_id = actorId
    amount.value > 0 ∧ decimalPlaces(amount.value) ≤ 4
  ensures:
    ∃ limit' ∈ SpendingLimits' :
      limit'.card_id = cardId ∧ limit'.limit_type = limitType
      ∧ limit'.amount = amount
    -- upsert semantics:
    IF ∃ limit ∈ SpendingLimits : limit.card_id = cardId ∧ limit.limit_type = limitType
      THEN |SpendingLimits'| = |SpendingLimits|  -- update, count unchanged
    ELSE
      |SpendingLimits'| = |SpendingLimits| + 1   -- insert
    ∃ e ∈ AuditEvents' : e.action = "LIMIT_SET"
      ∧ e.after_snapshot.amount = amount
```

#### System-Wide Invariants (must hold after every operation)

```
──────────────────────────────────────────────────────────────
Global Invariants (∀ reachable states S)
──────────────────────────────────────────────────────────────
  -- Referential integrity
  ∀ card ∈ S.Cards : ∃ user ∈ S.Users : user.id = card.user_id
  ∀ txn ∈ S.Transactions : ∃ card ∈ S.Cards : card.id = txn.card_id
  ∀ limit ∈ S.SpendingLimits : ∃ card ∈ S.Cards : card.id = limit.card_id

  -- Uniqueness
  ∀ limit1, limit2 ∈ S.SpendingLimits :
    (limit1.card_id = limit2.card_id ∧ limit1.limit_type = limit2.limit_type)
    ⟹ limit1.id = limit2.id

  -- Cancelled state consistency
  ∀ card ∈ S.Cards :
    (card.status = CANCELLED) ⟺ (card.cancelled_at ≠ null)

  -- Audit completeness
  LET mutations = |{ op : op is a successful state-changing operation }|
  |S.AuditEvents| ≥ mutations
  -- (≥ because audit events from before the system started may exist)

  -- Audit immutability
  ∀ e ∈ S.AuditEvents, ∀ time t > e.created_at :
    S(t).AuditEvents contains e with identical field values

  -- Monetary precision
  ∀ limit ∈ S.SpendingLimits : decimalPlaces(limit.amount) ≤ 4
  ∀ txn ∈ S.Transactions : decimalPlaces(txn.amount) ≤ 4

  -- PAN confidentiality
  ∀ card ∈ S.Cards :
    card.masked_pan = "**** **** **** " + lastFour(decrypt(card.encrypted_pan))
  ∀ e ∈ S.AuditEvents :
    "pan" ∉ keys(e.before_snapshot) ∧ "pan" ∉ keys(e.after_snapshot)
    "cvv" ∉ keys(e.before_snapshot) ∧ "cvv" ∉ keys(e.after_snapshot)
```

#### Transaction Authorization Decision Function

```
──────────────────────────────────────────────────────────────
authorize(card: Card, txn: PendingTransaction) → APPROVED | DECLINED
──────────────────────────────────────────────────────────────
  -- Step 1: Card status check
  IF card.status ≠ ACTIVE THEN return DECLINED

  -- Step 2: Per-transaction limit
  LET ptl = SpendingLimits.find(card.id, PER_TRANSACTION)
  IF ptl ≠ null ∧ txn.amount > ptl.amount THEN return DECLINED

  -- Step 3: Daily limit
  LET dl = SpendingLimits.find(card.id, DAILY)
  IF dl ≠ null:
    LET today_spent = SUM(t.amount) FOR t IN Transactions
                      WHERE t.card_id = card.id
                        AND t.status ∈ {PENDING, SETTLED}
                        AND t.transacted_at ∈ today()
    IF today_spent + txn.amount > dl.amount THEN return DECLINED

  -- Step 4: Monthly limit
  LET ml = SpendingLimits.find(card.id, MONTHLY)
  IF ml ≠ null:
    LET month_spent = SUM(t.amount) FOR t IN Transactions
                      WHERE t.card_id = card.id
                        AND t.status ∈ {PENDING, SETTLED}
                        AND t.transacted_at ∈ currentMonth()
    IF month_spent + txn.amount > ml.amount THEN return DECLINED

  return APPROVED

  -- Correctness property:
  -- authorize is a pure function: same inputs ⟹ same output
  -- authorize is monotonically restrictive: adding a limit can only
  --   change APPROVED → DECLINED, never DECLINED → APPROVED
```

---

## Context

### Beginning Context

- Empty project directory
- No existing code, models, or configuration
- A relational database is available

### Ending Context

- Data model definitions for Card, Transaction, AuditEvent, User, and SpendingLimit
- Service layer implementing card lifecycle business logic, spending limits, and transaction queries
- RESTful API layer with route definitions for all endpoints
- Middleware for audit logging, authentication, and rate limiting
- Request/response validation schemas with strict constraints
- Test suites: unit, integration, and compliance
- Database migration setup
- Project dependency manifest

## Low-Level Tasks

### 1. Define data models

**What prompt would you run to complete this task?**
Create data model definitions for the virtual card system: Card, Transaction, AuditEvent, User, and SpendingLimit. Use UUIDs as primary keys, fixed-point decimal for monetary fields, and include created_at/updated_at timestamps on all entities.

**What file do you want to CREATE or UPDATE?**
Data model files for each entity (one file per model, or a combined models file depending on project conventions).

**What function do you want to CREATE or UPDATE?**
Entity definitions: `Card`, `Transaction`, `AuditEvent`, `User`, `SpendingLimit`

**What are details you want to add to drive the code changes?**
- `Card`: id (UUID), user_id (FK → User), masked_pan (string, last 4 digits), encrypted_pan (binary), status (enum: ACTIVE, FROZEN, CANCELLED), created_at, updated_at, cancelled_at (nullable)
- `Transaction`: id (UUID), card_id (FK → Card), amount (decimal), currency (string, ISO 4217), merchant_name (string), merchant_category_code (string), status (enum: PENDING, SETTLED, DECLINED, REVERSED), transacted_at, settled_at
- `AuditEvent`: id (UUID), entity_type (string), entity_id (UUID), action (string), actor_id (UUID), actor_role (string), ip_address (string), before_snapshot (JSON), after_snapshot (JSON), created_at — this table must be append-only (no UPDATE or DELETE permitted)
- `User`: id (UUID), email (string, unique), role (enum: END_USER, OPS, COMPLIANCE), kyc_verified (boolean), created_at
- `SpendingLimit`: id (UUID), card_id (FK → Card, unique per limit_type), limit_type (enum: PER_TRANSACTION, DAILY, MONTHLY), amount (decimal), currency (string), updated_at

---

### 2. Card creation service with KYC gate

**What prompt would you run to complete this task?**
Implement a card creation service that verifies the user's KYC status before issuing a new virtual card. Generate a PAN, encrypt it with AES-256, store only the masked last-4 digits in plaintext, emit an audit event, and return masked card details.

**What file do you want to CREATE or UPDATE?**
Card service module (business logic layer).

**What function do you want to CREATE or UPDATE?**
`createCard(userId, idempotencyKey) → CardResponse`

**What are details you want to add to drive the code changes?**
- Check that the user exists and KYC is verified; raise a domain error otherwise
- Enforce rate limit: max 5 cards per user per day
- Generate a 16-digit PAN (use a test-range BIN prefix like 4000-00)
- Encrypt PAN with AES-256-GCM before storage; store masked_pan as `"**** **** **** 1234"`
- Create a Card record with status ACTIVE
- Emit an AuditEvent with action `CARD_CREATED`, after_snapshot containing the card (without PAN)
- Support idempotency: if a card with the same idempotency key exists for this user, return the existing card instead of creating a duplicate

---

### 3. Card state management (freeze / unfreeze)

**What prompt would you run to complete this task?**
Implement freeze and unfreeze operations for virtual cards. Validate the current state (only ACTIVE cards can be frozen, only FROZEN cards can be unfrozen), update the card status, and emit an audit event with before/after snapshots.

**What file do you want to CREATE or UPDATE?**
Card service module (same as Task 2).

**What function do you want to CREATE or UPDATE?**
`freezeCard(cardId, actorId, reason) → CardResponse`
`unfreezeCard(cardId, actorId, reason) → CardResponse`

**What are details you want to add to drive the code changes?**
- Verify the card exists and belongs to the acting user (or actor has OPS role)
- Validate state transitions: ACTIVE → FROZEN, FROZEN → ACTIVE. Reject CANCELLED cards.
- Rate limit: max 10 state toggles per card per hour
- Emit AuditEvent with action `CARD_FROZEN` or `CARD_UNFROZEN`, including before_snapshot (old status) and after_snapshot (new status), reason, actor IP
- Return the updated card (masked)

---

### 4. Spending limits CRUD

**What prompt would you run to complete this task?**
Implement create/update and read operations for spending limits on a card. Support three limit types: per-transaction, daily, and monthly. Validate that amounts are positive decimals and emit audit events on every change.

**What file do you want to CREATE or UPDATE?**
Spending limit service module (business logic layer).

**What function do you want to CREATE or UPDATE?**
`setSpendingLimit(cardId, limitType, amount, actorId) → SpendingLimitResponse`
`getSpendingLimits(cardId) → list of SpendingLimitResponse`

**What are details you want to add to drive the code changes?**
- `setSpendingLimit`: upsert — create if no limit of that type exists, update if it does
- Validate: amount must be > 0, currency must be ISO 4217, card must exist and not be CANCELLED
- Emit AuditEvent with action `LIMIT_SET`, before/after snapshots of the limit
- `getSpendingLimits`: return all limits for a card; only the card owner or OPS/COMPLIANCE roles may read
- Use fixed-point decimal throughout; reject requests with floating-point precision issues

---

### 5. Transaction listing and filtering

**What prompt would you run to complete this task?**
Implement a paginated transaction listing endpoint with filtering by date range, amount range, merchant name (partial match), and status. Support sorting by date (ascending/descending) and CSV export.

**What file do you want to CREATE or UPDATE?**
Transaction service module (business logic layer).

**What function do you want to CREATE or UPDATE?**
`listTransactions(cardId, filters, pagination) → PaginatedResponse of TransactionResponse`
`exportTransactionsCsv(cardId, filters) → streaming CSV response`

**What are details you want to add to drive the code changes?**
- Verify the requesting user owns the card (or has OPS/COMPLIANCE role)
- Filters: `date_from`, `date_to` (ISO 8601), `amount_min`, `amount_max` (decimal), `merchant_name` (case-insensitive partial match), `status` (enum)
- Pagination: `page` (default 1), `page_size` (default 20, max 100)
- Sorting: `sort_by` (default: transaction date), `sort_order` (default: descending)
- CSV export: stream rows to avoid loading entire result set into memory; include headers: date, merchant, amount, currency, status
- Never include PAN or card number in transaction responses

---

### 6. Audit logging middleware

**What prompt would you run to complete this task?**
Create middleware that automatically captures audit context (request ID, actor ID, IP address, timestamp) and makes it available to service-layer functions so they can emit audit events with consistent metadata.

**What file do you want to CREATE or UPDATE?**
Audit middleware module.

**What function do you want to CREATE or UPDATE?**
`AuditMiddleware` (class or function depending on framework)
`emitAuditEvent(entityType, entityId, action, beforeSnapshot, afterSnapshot)`

**What are details you want to add to drive the code changes?**
- Extract `X-Request-ID` header (or generate a UUID if missing)
- Extract actor identity from the authenticated token
- Extract client IP from `X-Forwarded-For` or direct connection
- Store all context in the request scope (per-request state)
- `emitAuditEvent` reads from the current request context and creates an AuditEvent record
- Audit events must be written in the same database transaction as the business operation (consistency)
- Never include sensitive fields (PAN, CVV) in before/after snapshots — use a scrubbing function

---

### 7. Authorization and RBAC middleware

**What prompt would you run to complete this task?**
Implement role-based access control middleware. Define permission checks for END_USER, OPS, and COMPLIANCE roles. End-users can only access their own resources; OPS/COMPLIANCE can view (but not mutate) any resource.

**What file do you want to CREATE or UPDATE?**
Auth middleware module.

**What function do you want to CREATE or UPDATE?**
`getCurrentUser(token) → User`
`requireRole(allowedRoles) → middleware/guard`
`requireCardOwner(cardId, currentUser) → void (throws on failure)`

**What are details you want to add to drive the code changes?**
- `getCurrentUser`: decode and validate a JWT token; return the User; raise 401 Unauthorized on invalid/expired tokens
- `requireRole`: middleware or guard that checks the current user's role against an allowlist; raise 403 Forbidden if not permitted
- `requireCardOwner`: verify the card belongs to the current user or the user has OPS/COMPLIANCE role; raise 403 otherwise
- OPS/COMPLIANCE roles can read cards and audit logs for any user but cannot create, freeze, or set limits on behalf of a user without an elevated authorization flow (out of scope for MVP but document the boundary)

---

### 8. API endpoint definitions

**What prompt would you run to complete this task?**
Define RESTful API endpoints for virtual card lifecycle management. Apply authentication, RBAC, input validation, rate limiting, and idempotency-key handling.

**What file do you want to CREATE or UPDATE?**
API route modules: cards, transactions, audit.

**What function do you want to CREATE or UPDATE?**
Route handlers: `createCard`, `freezeCard`, `unfreezeCard`, `setSpendingLimit`, `getSpendingLimits`, `getTransactions`, `exportTransactionsCsv`, `getAuditEvents`

**What are details you want to add to drive the code changes?**
- `POST /api/v1/cards` — create card (requires `Idempotency-Key` header)
- `POST /api/v1/cards/{cardId}/freeze` — freeze card (body: `{ "reason": "..." }`)
- `POST /api/v1/cards/{cardId}/unfreeze` — unfreeze card (body: `{ "reason": "..." }`)
- `PUT /api/v1/cards/{cardId}/limits/{limitType}` — set spending limit
- `GET /api/v1/cards/{cardId}/limits` — get spending limits
- `GET /api/v1/cards/{cardId}/transactions` — list transactions (query params for filters/pagination)
- `GET /api/v1/cards/{cardId}/transactions/export` — CSV export
- `GET /api/v1/audit?entity_type=card&entity_id={id}` — audit log (OPS/COMPLIANCE only)
- All endpoints return structured JSON errors; all require JWT authentication
- API versioning via URL prefix `/api/v1/`

---

### 9. Input validation schemas

**What prompt would you run to complete this task?**
Create request and response validation schemas for all API endpoints. Enforce strict validation: positive decimals for money, ISO 4217 currency codes, ISO 8601 dates, UUID formats, enum constraints, and maximum string lengths.

**What file do you want to CREATE or UPDATE?**
Schema/DTO modules for cards, transactions, spending limits, audit, and common types.

**What function do you want to CREATE or UPDATE?**
Schema definitions: `CreateCardRequest`, `CardResponse`, `FreezeCardRequest`, `SetSpendingLimitRequest`, `SpendingLimitResponse`, `TransactionFilters`, `TransactionResponse`, `PaginationParams`, `PaginatedResponse`, `AuditEventResponse`, `ErrorResponse`

**What are details you want to add to drive the code changes?**
- `CardResponse`: never include full PAN — only `masked_pan`
- `SetSpendingLimitRequest`: amount must be a positive decimal; currency validated against ISO 4217 allowlist
- `TransactionFilters`: all fields optional; dates in ISO 8601; amounts as decimal
- `PaginatedResponse`: generic wrapper with items list, page, page_size, total_count
- `ErrorResponse`: structured error with code, message, details list
- Use strict mode to reject implicit type coercion; reject unexpected/extra fields

---

### 10. Test suite structure

**What prompt would you run to complete this task?**
Set up the test suite. Create unit tests for service-layer functions, integration tests for API endpoints, and compliance tests that verify audit events are emitted for every mutating operation.

**What file do you want to CREATE or UPDATE?**
Test configuration and test modules: shared fixtures, unit tests (card service, spending limits), integration tests (card API), compliance tests (audit trail).

**What function do you want to CREATE or UPDATE?**
Fixtures: `dbSession`, `testClient`, `sampleUser`, `sampleCard`
Test cases: `testCreateCardSuccess`, `testCreateCardKycRejected`, `testFreezeCard`, `testUnfreezeCancelledCardFails`, `testSetSpendingLimit`, `testListTransactionsPagination`, `testAuditEventEmittedOnFreeze`, `testRbacEndUserCannotAccessOtherCards`

**What are details you want to add to drive the code changes?**
- Test configuration: set up an in-memory or test database, create tables, provide shared fixtures for database session and HTTP client
- Unit tests: mock the data access layer, test business logic in isolation (KYC check, state transitions, limit validation)
- Integration tests: send real HTTP requests through the API layer, verify response codes and bodies
- Compliance tests: after each mutating operation, query the AuditEvent table and assert the correct event was recorded with required fields (actor, timestamp, before/after, IP)
- Negative tests: invalid inputs return 422, unauthorized access returns 403, rate limit exceeded returns 429
- Target 80%+ code coverage
