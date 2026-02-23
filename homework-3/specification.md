# Virtual Card Lifecycle Management — Specification

> Ingest the information from this file, implement the Low-Level Tasks, and generate the code that will satisfy the High and Mid-Level Objectives.

---

## High-Level Objective

Build a **secure, auditable Virtual Card Lifecycle Management** module that allows end-users to create, control, and monitor virtual payment cards, while providing an internal operations and compliance view over all card activity — in full conformance with PCI-DSS Level 1, GDPR, and applicable AML/KYC regulations.

---

## Mid-Level Objectives

1. **Card Issuance** — Enable authenticated account holders to create single-use or multi-use virtual cards tied to their primary account, with configurable spending limits, currency, and expiry.
2. **Card Controls** — Allow card owners to freeze, unfreeze, and permanently close virtual cards at any time; expose the same controls to authorized internal ops agents.
3. **Spending Limits & Caps** — Enforce per-transaction, daily, weekly, and total-lifetime monetary caps; reject authorizations that would breach any active limit.
4. **Transaction Ledger** — Maintain an immutable, append-only ledger of all authorization attempts (approved and declined), with full metadata for each event.
5. **Compliance & Audit** — Produce an immutable, tamper-evident audit log of every state change; expose a read-only compliance dashboard view; flag suspicious activity patterns for AML review.
6. **Notifications** — Emit real-time event notifications (authorization attempt, limit breach, freeze/unfreeze, card expiry approaching) via configurable channels (webhook, email, push).
7. **Internal Ops View** — Provide a privileged, role-gated API surface that allows ops and compliance staff to search, inspect, override card state, and export audit trails without modifying ledger entries.

---

## Implementation Notes

### Monetary Values
- **Always use `Decimal` (or the platform equivalent fixed-point type)** for all monetary calculations. Floating-point arithmetic is strictly forbidden for amounts.
- Store amounts as integer minor units (e.g., cents for USD) in the database; convert to `Decimal` at application boundaries before any arithmetic.

### Security
- All API endpoints must be protected by OAuth 2.0 / OIDC with JWT bearer tokens.
- Sensitive card data (PAN, CVV) must be stored encrypted using AES-256-GCM; encryption keys managed by a dedicated secrets manager (e.g., AWS KMS, HashiCorp Vault).
- PAN must never appear in logs. Mask to last four digits in all log and display contexts.
- Implement mutual TLS for service-to-service communication.
- Apply rate limiting: max 10 card creation requests per account per hour; max 100 authorization events per card per minute (burst).

### Compliance & Regulatory
- Every write operation (card create, state change, limit update) must produce an immutable audit event written to an append-only store before the business operation is committed (outbox pattern).
- Audit events must include: actor ID, actor role, timestamp (UTC, ISO-8601), operation name, before-state, after-state, correlation ID, and source IP.
- Data retention: card data — 7 years post-closure. Transaction records — 10 years. Audit logs — 10 years. Implement automated archival pipelines.
- GDPR right-to-erasure requests may anonymize PII in card records but must preserve the financial ledger and audit trail (pseudonymization only).
- AML: automated rules engine must flag cards with velocity anomalies (e.g., > 20 transactions in 10 minutes, total exceeded 150% of 30-day average).

### Data Handling
- Apply field-level encryption for PAN, billing address, and cardholder name in the data store.
- Implement database-level row-security policies: ops role may read all rows; customer role may only read their own records.
- All external API responses must exclude PAN; return only masked PAN and card token (UUID).

### Error Handling
- All errors returned to clients must use RFC 7807 Problem Details format (JSON).
- Internal errors must be logged with full stack trace and correlation ID but surfaced to API consumers only as opaque error codes.
- Idempotency: card state-change operations must accept an `Idempotency-Key` header and deduplicate requests within a 24-hour window.

### Testing
- Minimum 90% line coverage for business logic modules.
- Every public API endpoint must have an integration test covering: happy path, authorization failure, validation failure, and idempotency.
- Dedicated compliance test suite to assert audit events are written atomically with business operations.
- Contract tests for all downstream service integrations (card processor, notification service, AML engine).

### Tech Stack Assumptions
- **Runtime**: Node.js 20 LTS with TypeScript 5 (strict mode).
- **Framework**: Fastify 4.
- **ORM**: Prisma with PostgreSQL 15.
- **Message bus**: Apache Kafka (audit outbox consumer, notification events).
- **Secrets**: AWS KMS + AWS Secrets Manager.
- **Observability**: OpenTelemetry traces and metrics; structured JSON logs (Pino).
- **CI/CD**: GitHub Actions; staging environment required before production promotion.

---

## Context

### Beginning Context
- Existing **Core Banking API** provides: account lookup, balance inquiry, and debit/credit transaction posting (internal service, accessible via REST over mTLS).
- User identity and authentication handled by an existing **Identity Provider** (Keycloak); JWT tokens include `account_id`, `roles[]`, and `tenant_id` claims.
- Empty `src/` directory for this new module; database schemas do not yet exist.
- `package.json` scaffolded with Fastify, Prisma, Pino, and OpenTelemetry dependencies declared.
- Infrastructure: PostgreSQL 15 instance, Kafka cluster, and AWS KMS key ARN available in environment variables.

### Ending Context

After full implementation, the following artifacts exist:

```
src/
  card/
    card.controller.ts        # Fastify route handlers
    card.service.ts           # Business logic
    card.repository.ts        # Prisma data access
    card.schema.ts            # Zod validation schemas & TypeScript types
    card.events.ts            # Kafka event producers
  limit/
    limit.service.ts
    limit.repository.ts
    limit.schema.ts
  transaction/
    transaction.repository.ts
    transaction.types.ts
  audit/
    audit.outbox.ts           # Outbox pattern writer
    audit.consumer.ts         # Kafka consumer → append-only log
  notification/
    notification.producer.ts
  ops/
    ops.controller.ts         # Internal ops privileged routes
    ops.service.ts
  shared/
    errors.ts                 # RFC 7807 error factory
    money.ts                  # Decimal monetary helpers
    idempotency.ts            # Idempotency key deduplication middleware
    crypto.ts                 # AES-256-GCM field-level encryption helpers
prisma/
  schema.prisma               # Full DB schema with row-security policies
  migrations/
tests/
  unit/
  integration/
  compliance/
  contract/
docs/
  openapi.yaml                # Generated OpenAPI 3.1 spec
```

- All API endpoints documented in `openapi.yaml`.
- Compliance test suite passes with zero failures.
- Audit log verified to be append-only (no UPDATE/DELETE permissions on audit table).

---

## Low-Level Tasks

---

### Task 1 — Database Schema & Migrations

**Prompt to run:**
```
Create the Prisma schema for the virtual card module. Define models for VirtualCard, SpendingLimit,
Transaction, AuditEvent, and IdempotencyKey. Apply row-level security policies in a migration so
that the 'customer' role can only select rows where account_id matches their JWT claim, while the
'ops' role can select all rows. Use integer minor-unit columns (e.g. amount_minor_units Int) for
all monetary fields. Enable UUID primary keys. Add appropriate indexes for hot query paths
(account_id, card_id, created_at).
```

**Files to CREATE or UPDATE:**
- `prisma/schema.prisma`
- `prisma/migrations/001_initial_virtual_card_schema/migration.sql`

**Functions / Classes to CREATE:**
- Prisma models: `VirtualCard`, `SpendingLimit`, `Transaction`, `AuditEvent`, `IdempotencyKey`

**Key Details:**
- `VirtualCard` fields: `id` (UUID PK), `account_id`, `status` (enum: `ACTIVE | FROZEN | CLOSED`), `currency` (ISO 4217), `masked_pan`, `card_token` (UUID), `expiry_month`, `expiry_year`, `created_at`, `updated_at`, `closed_at`.
- `SpendingLimit` fields: `id`, `card_id` (FK), `period` (enum: `PER_TRANSACTION | DAILY | WEEKLY | LIFETIME`), `limit_minor_units`, `spent_minor_units`, `currency`.
- `Transaction` fields: `id`, `card_id` (FK), `amount_minor_units`, `currency`, `merchant_name`, `merchant_category_code`, `status` (enum: `APPROVED | DECLINED`), `decline_reason` (nullable), `authorization_at`.
- `AuditEvent` fields: `id`, `correlation_id`, `actor_id`, `actor_role`, `operation`, `entity_type`, `entity_id`, `before_state` (JSONB), `after_state` (JSONB), `source_ip`, `occurred_at`. **No UPDATE or DELETE privilege on this table.**
- `IdempotencyKey` fields: `key` (PK), `operation`, `response_body`, `expires_at`.

---

### Task 2 — Monetary Helpers & Field-Level Encryption

**Prompt to run:**
```
Create a shared money.ts module with Decimal-safe helpers: minorUnitsToDecimal, decimalToMinorUnits,
addMinorUnits, subtractMinorUnits, and formatMoney. Create a shared crypto.ts module using
Node's built-in crypto with AES-256-GCM: encryptField(plaintext, keyId) and decryptField(ciphertext, keyId).
Keys must be fetched from AWS KMS via the AWS SDK and cached per keyId with a 5-minute TTL.
Never log the plaintext value.
```

**Files to CREATE:**
- `src/shared/money.ts`
- `src/shared/crypto.ts`

**Functions to CREATE:**
- `money.ts`: `minorUnitsToDecimal(units: bigint, currency: string): Decimal`, `decimalToMinorUnits(amount: Decimal, currency: string): bigint`, `addMinorUnits`, `subtractMinorUnits`, `formatMoney`
- `crypto.ts`: `encryptField(plaintext: string, keyId: string): Promise<EncryptedBlob>`, `decryptField(blob: EncryptedBlob, keyId: string): Promise<string>`

**Key Details:**
- Use the `decimal.js-light` package. Never use `Number` arithmetic on monetary amounts.
- `EncryptedBlob = { ciphertext: string; iv: string; authTag: string; keyId: string }` (all base64).
- KMS data-key caching must use `aws-encryption-sdk` or equivalent; TTL must be configurable via environment variable `KMS_DATA_KEY_TTL_SECONDS`.

---

### Task 3 — Card Issuance Endpoint

**Prompt to run:**
```
Implement POST /v1/cards — the card issuance endpoint. Validate the request body with Zod.
Call the Core Banking API to verify the account is in good standing before creating the card.
Generate a card token (UUID v4) and a masked PAN (last 4 digits of a randomly generated 16-digit
number). Write an AuditEvent via the outbox pattern atomically in the same Prisma transaction.
Enforce the idempotency key. Return 201 with the card token and masked PAN — never the full PAN.
```

**Files to CREATE or UPDATE:**
- `src/card/card.schema.ts` (Zod schemas: `CreateCardRequestSchema`, `CardResponseSchema`)
- `src/card/card.repository.ts` (`createCard` Prisma call)
- `src/card/card.service.ts` (`issueCard` business logic)
- `src/card/card.controller.ts` (Fastify route registration)
- `src/audit/audit.outbox.ts` (`writeAuditEvent`)
- `src/shared/idempotency.ts` (middleware)

**Key Details:**
- Request body: `{ accountId: UUID, currency: ISO-4217 string, label?: string, limits?: SpendingLimitInput[] }`.
- Response (201): `{ cardId: UUID, cardToken: UUID, maskedPan: string, currency: string, status: 'ACTIVE', expiryMonth: number, expiryYear: number }`.
- The actual PAN is generated in-process, immediately encrypted via `crypto.ts`, and stored encrypted. The plaintext PAN must be zeroed from memory and must never be logged or returned.
- Reject with 422 if account is not in good standing. Reject with 409 if idempotency key has been seen within 24h with `{ cardId, status }` from the cached response.
- Audit operation name: `CARD_ISSUED`.

---

### Task 4 — Card State Control (Freeze / Unfreeze / Close)

**Prompt to run:**
```
Implement PATCH /v1/cards/:cardId/state for card owners and POST /v1/ops/cards/:cardId/state
for ops agents. Valid transitions: ACTIVE→FROZEN, FROZEN→ACTIVE, (ACTIVE|FROZEN)→CLOSED.
CLOSED is terminal — no further transitions allowed. Enforce ownership: card must belong to
the requesting account_id from the JWT. Ops agents bypass ownership check but must have
the 'ops' or 'compliance' role. Write an AuditEvent with before/after state. Publish a
Kafka event on every transition.
```

**Files to CREATE or UPDATE:**
- `src/card/card.service.ts` (`updateCardState`)
- `src/card/card.controller.ts` (new PATCH route)
- `src/card/card.events.ts` (`publishCardStateChanged`)
- `src/ops/ops.controller.ts` (POST route for ops)
- `src/ops/ops.service.ts`

**Key Details:**
- State machine must be explicit — a lookup table of allowed `[from, to]` pairs; any unlisted transition returns 422 with `INVALID_STATE_TRANSITION` error code.
- Request body: `{ state: 'FROZEN' | 'ACTIVE' | 'CLOSED', reason?: string }`.
- Kafka topic: `virtual-card.state-changed`; event schema includes `cardToken`, `accountId`, `previousState`, `newState`, `changedAt`, `actorId`, `reason`.
- `CLOSED` transitions must set `closed_at = NOW()` and trigger a cascade freeze on all authorizations in-flight.
- Audit operation names: `CARD_FROZEN`, `CARD_UNFROZEN`, `CARD_CLOSED`.

---

### Task 5 — Spending Limits Enforcement

**Prompt to run:**
```
Implement PUT /v1/cards/:cardId/limits to set or update spending limits, and implement the
internal checkAndDeductLimit(cardId, amountMinorUnits, currency) function that is called during
authorization. Use a pessimistic row-level lock (SELECT ... FOR UPDATE) to prevent race conditions.
Reject the authorization by returning { approved: false, reason: 'LIMIT_EXCEEDED' } if any
active limit would be breached. Use Decimal arithmetic only — no floating point.
```

**Files to CREATE or UPDATE:**
- `src/limit/limit.schema.ts`
- `src/limit/limit.repository.ts`
- `src/limit/limit.service.ts` (`setLimits`, `checkAndDeductLimit`, `rollbackDeduction`)

**Key Details:**
- Supported limit periods: `PER_TRANSACTION`, `DAILY`, `WEEKLY`, `LIFETIME`.
- `DAILY` and `WEEKLY` limits must reset at midnight UTC using a scheduled job (cron); the reset must also write an `AuditEvent` with operation `LIMIT_RESET`.
- `checkAndDeductLimit` must be called inside the same database transaction as the transaction record creation.
- If authorization is subsequently reversed, call `rollbackDeduction` to restore `spent_minor_units`.
- PUT /v1/cards/:cardId/limits: array of `{ period, limitAmount, currency }`. A `limitAmount` of `null` removes the limit for that period.
- Audit operation names: `LIMIT_SET`, `LIMIT_REMOVED`, `LIMIT_EXCEEDED_REJECTED`.

---

### Task 6 — Transaction Ledger & Authorization Flow

**Prompt to run:**
```
Implement the authorization handler POST /v1/cards/:cardId/authorize (called by the card processor
via mTLS). Validate the mTLS client certificate's CN against an allowlist. Call checkAndDeductLimit.
If approved, insert a Transaction record with status APPROVED. If declined, insert with status DECLINED
and decline_reason. Emit a Kafka event. Return { approved: boolean, transactionId: UUID, reason?: string }.
Also implement GET /v1/cards/:cardId/transactions with cursor-based pagination (limit 50, forward-only).
```

**Files to CREATE or UPDATE:**
- `src/transaction/transaction.repository.ts`
- `src/transaction/transaction.types.ts`
- `src/card/card.controller.ts` (new routes)
- `src/card/card.events.ts` (`publishAuthorizationEvent`)

**Key Details:**
- mTLS allowlist stored in environment variable `CARD_PROCESSOR_CN_ALLOWLIST` (comma-separated CNs).
- The Transaction record insert and limit deduction must execute in a single database transaction.
- Pagination: cursor is an opaque base64-encoded `{ id, authorization_at }` tuple. Response includes `{ data: Transaction[], nextCursor: string | null }`.
- Kafka topic: `virtual-card.authorization-attempted`.
- Authorization endpoint must respond within 250 ms p99 — add an OpenTelemetry span named `virtual-card.authorize`.
- Declined authorizations must not deduct from spending limits; apply `rollbackDeduction` on decline.

---

### Task 7 — Compliance Audit Trail & AML Flagging

**Prompt to run:**
```
Implement the Kafka consumer in audit.consumer.ts that reads from the audit outbox topic and
writes records to the append-only AuditEvent table. Implement the AML rules engine in
aml.service.ts that evaluates velocity rules and writes AML_ALERT AuditEvents when triggered.
Implement GET /v1/ops/audit-trail with date-range filtering, exportable as NDJSON.
```

**Files to CREATE:**
- `src/audit/audit.consumer.ts`
- `src/aml/aml.service.ts`
- `src/aml/aml.rules.ts`
- `src/ops/ops.controller.ts` (audit-trail route)

**Key Details:**
- AML Rule 1 (Velocity): > 20 transactions in any rolling 10-minute window on a single card → flag.
- AML Rule 2 (Amount Anomaly): transaction total in a calendar month > 150% of the card's prior 3-month average → flag.
- AML alerts produce an `AuditEvent` with operation `AML_ALERT_RAISED` and `after_state: { ruleId, triggeredValue, threshold }`.
- The `AuditEvent` table must have a `CHECK` constraint or trigger that prevents any UPDATE or DELETE at the database level (enforced in the migration, not just at application level).
- Export endpoint: `GET /v1/ops/audit-trail?from=ISO8601&to=ISO8601&entityType=VirtualCard&entityId=UUID` — streams NDJSON; requires `compliance` role.
- Audit consumer must be idempotent: use the `correlation_id` to deduplicate re-delivered Kafka messages.

---

### Task 8 — Notifications

**Prompt to run:**
```
Implement notification.producer.ts that publishes typed events to the notification Kafka topic
for: card authorization (approved/declined), spending limit breach warning (at 80% and 100%),
card state changes, and card expiry approaching (30 days and 7 days before expiry). Implement
a scheduled job that runs daily to detect expiring cards. Do not include PII or PAN in Kafka
event payloads — use cardToken only.
```

**Files to CREATE:**
- `src/notification/notification.producer.ts`
- `src/notification/notification.types.ts`
- `src/notification/expiry.job.ts`

**Key Details:**
- Kafka topic: `virtual-card.notifications`.
- Payload must never include PAN, full card number, or CVV. Use `cardToken` (UUID) as the card identifier.
- Notification event types (enum): `AUTHORIZATION_APPROVED`, `AUTHORIZATION_DECLINED`, `LIMIT_WARNING_80PCT`, `LIMIT_BREACH`, `CARD_STATE_CHANGED`, `CARD_EXPIRY_APPROACHING_30D`, `CARD_EXPIRY_APPROACHING_7D`.
- Expiry job: query all `ACTIVE` cards where `expiry_year * 100 + expiry_month` is within 30 or 7 days from today. Run at 08:00 UTC daily.
- Notification events must be published after the business operation is committed — never inside the same database transaction (use transactional outbox or post-commit hook).
