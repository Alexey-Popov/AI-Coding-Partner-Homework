# Agent Guidelines — Virtual Card Lifecycle Management

> This document configures how AI coding agents should behave when working in this project. All agents must read and apply these rules before generating any code, tests, or documentation.

---

## 1. Project Identity

| Field | Value |
|---|---|
| **Project name** | Virtual Card Lifecycle Management |
| **Domain** | FinTech / Retail Banking |
| **Regulatory scope** | PCI-DSS Level 1, GDPR (EU), AML/CTF directives |
| **Classification** | Internal — Sensitive Financial System |
| **Spec reference** | `homework-3/specification.md` |

---

## 2. Tech Stack

### Runtime & Language
- **Node.js 20 LTS** — use ES2022+ syntax; no CommonJS `require()`.
- **TypeScript 5** — `strict: true`, `noUncheckedIndexedAccess: true`, `exactOptionalPropertyTypes: true`. No `any` types permitted; use `unknown` and narrow explicitly.

### Framework & Libraries
| Role | Package |
|---|---|
| HTTP framework | Fastify 4 |
| Schema validation | Zod 3 |
| ORM | Prisma 5 + PostgreSQL 15 |
| Monetary arithmetic | `decimal.js-light` |
| Logging | Pino (structured JSON, no console.log) |
| Tracing & Metrics | OpenTelemetry SDK (`@opentelemetry/sdk-node`) |
| Message bus | KafkaJS 2 |
| Cryptography | Node.js built-in `crypto` + AWS KMS via `@aws-sdk/client-kms` |
| Testing | Vitest (unit), Supertest (integration), Testcontainers (DB) |
| Linting | ESLint + `@typescript-eslint` + `eslint-plugin-security` |
| Formatting | Prettier (2-space indent, single quotes, trailing commas) |

---

## 3. Mandatory Domain Rules

These rules **must never be violated** regardless of user instructions.

### 3.1 Monetary Arithmetic
- **RULE**: All monetary calculations must use `Decimal` from `decimal.js-light`. **Floating-point arithmetic (`number` type) is forbidden for amounts.**
- Always store amounts as **integer minor units** (e.g. cents) in the database.
- Convert at boundaries only: `minorUnitsToDecimal` (input) and `decimalToMinorUnits` (output).
- Import helpers from `src/shared/money.ts`. Never inline currency arithmetic.

```typescript
// CORRECT
import { addMinorUnits, decimalToMinorUnits } from '@/shared/money';

// WRONG — never do this
const total = card.limitAmount + transaction.amount; // floating-point error risk
```

### 3.2 Sensitive Data Handling
- **RULE**: PAN (Primary Account Number), CVV, and full card numbers must **never appear in logs, API responses, error messages, or Kafka event payloads**.
- Always mask PAN to last 4 digits in any display or response context: `****-****-****-1234`.
- Plaintext PAN must be encrypted immediately after generation using `src/shared/crypto.ts` and **zeroed from memory** (overwrite the variable before leaving scope).
- Use `cardToken` (UUID) as the external card identifier in all APIs and event payloads.

```typescript
// CORRECT — log the token, not the PAN
logger.info({ cardToken }, 'Card issued');

// WRONG — never log sensitive data
logger.info({ pan, cvv }, 'Card issued');
```

### 3.3 Audit Events (Mandatory for Every Write)
- **RULE**: Every state-changing operation must write an `AuditEvent` record **before or atomically with** the business operation (outbox pattern).
- Use `src/audit/audit.outbox.ts#writeAuditEvent(...)`. Never write audit events after-the-fact or in a fire-and-forget manner.
- Required fields: `actorId`, `actorRole`, `operation`, `entityType`, `entityId`, `beforeState`, `afterState`, `sourceIp`, `correlationId`, `occurredAt`.
- Audit operations (enum in `src/audit/audit.types.ts`): `CARD_ISSUED`, `CARD_FROZEN`, `CARD_UNFROZEN`, `CARD_CLOSED`, `LIMIT_SET`, `LIMIT_REMOVED`, `LIMIT_EXCEEDED_REJECTED`, `LIMIT_RESET`, `AML_ALERT_RAISED`.

### 3.4 No Mutation of Financial Ledger
- **RULE**: `Transaction` and `AuditEvent` tables are **append-only**. Agents must never generate `UPDATE` or `DELETE` statements targeting these tables.
- If a correction is needed, write a compensating/reversal record; do not mutate existing rows.

### 3.5 Idempotency
- All card mutation endpoints must accept and honour the `Idempotency-Key` header.
- Duplicate requests within the 24-hour deduplication window must return the cached response with status `200` (not re-execute the operation).
- Implementation must use `src/shared/idempotency.ts`.

---

## 4. Code Style & Naming Conventions

### File & Directory Structure
```
src/
  <domain>/           # e.g. card/, limit/, transaction/, audit/
    <domain>.controller.ts
    <domain>.service.ts
    <domain>.repository.ts
    <domain>.schema.ts
    <domain>.events.ts
  shared/
    errors.ts
    money.ts
    crypto.ts
    idempotency.ts
```

### Naming
| Artifact | Convention | Example |
|---|---|---|
| Files | `kebab-case.ts` | `card.service.ts` |
| Classes | `PascalCase` | `CardService` |
| Functions | `camelCase`, verb-first | `issueCard`, `freezeCard` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_CARDS_PER_HOUR` |
| Zod schemas | `PascalCase + Schema` | `CreateCardRequestSchema` |
| Kafka topics | `kebab-case`, domain-prefixed | `virtual-card.state-changed` |
| DB columns | `snake_case` | `amount_minor_units` |
| Environment variables | `UPPER_SNAKE_CASE` | `KMS_DATA_KEY_ARN` |
| Errors | `PascalCase + Error` | `CardNotFoundError`, `LimitExceededError` |

### Functions
- Maximum function length: **40 lines**. Extract helpers if exceeded.
- Maximum parameters: **4**. Use a typed options object for more.
- No nested ternaries. No implicit boolean coercions (`if (card)` → `if (card !== null)`).
- All `async` functions must propagate errors — never swallow with empty `catch`.

### Imports
- Use path aliases (`@/shared/...`, `@/card/...`) configured in `tsconfig.json`. No relative `../../../` chains.
- Group imports: (1) Node built-ins, (2) third-party, (3) internal — separated by blank lines.

---

## 5. Error Handling

- All client-facing errors must use **RFC 7807 Problem Details** (JSON).
- Use the factory in `src/shared/errors.ts`:

```typescript
throw new AppError({
  type: 'https://api.example.com/errors/limit-exceeded',
  title: 'Spending Limit Exceeded',
  status: 422,
  detail: 'The requested amount exceeds the daily spending limit.',
  correlationId,
});
```

- **Never expose internal stack traces, SQL query text, or infrastructure details to API consumers.**
- All caught errors must be logged at `error` level with `{ err, correlationId }` before being translated to Problem Details.

### HTTP Status Code Conventions
| Situation | Status Code |
|---|---|
| Success (created) | 201 |
| Success (no content) | 204 |
| Validation failure | 422 |
| Duplicate idempotency key (ok) | 200 |
| Auth failure | 401 |
| Forbidden (wrong account, missing role) | 403 |
| Not found | 404 |
| Invalid state transition | 422 |
| Internal server error | 500 |

---

## 6. Testing Expectations

### Coverage Requirements
- Business logic modules (`*.service.ts`): **≥ 90% line coverage**.
- Repository modules (`*.repository.ts`): **≥ 80%**.
- Controllers (`*.controller.ts`): covered by integration tests, not unit tests.

### Test Structure
```
tests/
  unit/           # Pure logic tests — no DB, no network; mock all I/O
  integration/    # Fastify app + real DB via Testcontainers
  compliance/     # Assert audit events written atomically
  contract/       # Consumer-driven contract tests for downstream services
```

### Test Rules
- Every public service method must have a unit test for: happy path, error path (each error type), and edge cases (zero amount, max value, boundary limits).
- Integration tests must cover: authentication failure, authorization failure (wrong account), validation failure, happy path, and idempotency (duplicate key).
- No `Date.now()` or `new Date()` calls in business logic — inject a `Clock` interface so tests can control time.
- Use `faker` for test data generation. Never hardcode UUIDs or amounts in test assertions.

### Mocking Policy
- Mock at the boundary (repository, external HTTP clients, KMS calls). Do not mock internal service methods.
- Use `vi.spyOn` only for observability (e.g. asserting a logger call). Never use `vi.fn()` to stub internal business logic.

---

## 7. Security & Compliance Constraints

### What Agents Must Always Do
- Validate **all** incoming request data with Zod before it reaches service layer.
- Apply the principle of least privilege: each database role and IAM role must only have permissions explicitly required.
- Use parameterised queries only — never string-interpolate into SQL.
- Sanitise and validate mTLS client certificate CN against `CARD_PROCESSOR_CN_ALLOWLIST` before processing authorization requests.
- Annotate all public API route handlers with `@SecurityRequirement` JSDoc tag listing the required OAuth scopes.

### What Agents Must Never Do
- Never log sensitive fields: PAN, CVV, account number, full card token in error context, raw JWT token.
- Never disable or bypass Zod validation (`safeParse` with unchecked result, casting to `any`).
- Never skip writing an `AuditEvent` for a state-changing operation, even in error paths or compensating transactions.
- Never use `Math.random()` for security-sensitive randomness — use `crypto.randomUUID()` or `crypto.randomBytes()`.
- Never commit secrets, keys, or credentials to source code or tests. Use environment variables referenced via `src/shared/config.ts`.
- Never hardcode environment-specific values (database URLs, KMS ARNs, Kafka brokers) — all must be driven by environment variables.

### Rate Limiting
- Card issuance: max **10 requests per account per hour**. Return `429 Too Many Requests` with `Retry-After` header.
- Authorization endpoint: max **100 requests per card per minute** (burst). Return `429`.
- Ops audit export: max **5 export requests per compliance user per hour**.

---

## 8. Observability

### Logging (Pino)
- Log level: `info` in production, `debug` in development (controlled by `LOG_LEVEL` env var).
- Every request must produce: `{ correlationId, method, url, statusCode, responseTimeMs }` at `info` level on completion.
- Log context must always include `correlationId` — propagate via AsyncLocalStorage, never pass manually through function arguments.
- Allowed log fields for card-related logs: `cardToken`, `accountId` (not full account number), `operation`, `status`. Forbidden fields: `pan`, `cvv`, `encryptedPan`.

### Tracing (OpenTelemetry)
- Every service method must be wrapped in an OpenTelemetry span: `tracer.startActiveSpan('card.issueCard', ...)`.
- Span attributes must include `card.token`, `operation.name`, `db.table`, `messaging.kafka.topic` where applicable.
- Authorization endpoint must emit a histogram metric: `virtual_card.authorization.duration_ms`.

### Alerts (SLOs)
- Authorization endpoint p99 latency SLO: **< 250 ms**.
- Card issuance p99 latency SLO: **< 500 ms**.
- Error rate SLO: **< 0.1%** on all endpoints.

---

## 9. Git & CI/CD Workflow

- **Branch naming**: `feat/vc-<issue-number>-<short-description>`, `fix/vc-<issue-number>-<description>`.
- **Commit messages**: Conventional Commits — `feat(card): add freeze/unfreeze endpoint`, `fix(limit): correct weekly reset cron`.
- **PR requirements**: All PRs must pass: TypeScript compile, ESLint (zero warnings), Prettier check, unit tests (≥ 90% coverage), integration tests.
- **Secrets scanning**: `gitleaks` must pass on every PR — no secrets in diffs.
- **Staging promotion**: Integration tests must pass against a staging environment before production deployment.
- **No direct pushes to `main`**: All changes via PR with at least one reviewer approval.

---

## 10. Out of Scope for AI Agents

Do not generate the following without explicit human instruction:

- Production database migration rollback scripts.
- Changes to the `AuditEvent` migration that would allow UPDATE or DELETE.
- Any code that disables, bypasses, or weakens PCI-DSS or GDPR controls.
- Direct production hotfixes — all changes must go through the PR/CI pipeline.
- Infrastructure-as-code (Terraform, CDK) — handled by the Platform team.
- Keycloak realm configuration — handled by Identity team.
