# Security Policy

> Referenced by: `agents.md`, `specification.md`, `.github/copilot-instructions.md`.

## OWASP Top 10 Mapping

| OWASP Category | Mitigation |
|---|---|
| A01 Broken Access Control | Role-based authorization at service layer; deny by default; end-user can only access own cards; ops role required for cross-user access |
| A02 Cryptographic Failures | Card tokens are opaque (not PANs); never log tokens or secrets; TLS assumed at transport |
| A03 Injection | Parameterized queries only (Spring Data JPA); Jakarta Bean Validation on all inputs; no string concatenation in queries |
| A04 Insecure Design | Strict state machine; spending limits enforced before persistence; idempotency keys prevent replay |
| A05 Security Misconfiguration | Secure defaults in Spring Security config; no wildcard CORS; actuator endpoints locked down |
| A06 Vulnerable Components | Dependency management via Maven with version pinning; run dependency-check in CI |
| A07 Auth Failures | Rate limiting on all endpoints; JWT/session assumed external |
| A08 Data Integrity Failures | Optimistic locking on card entity; append-only audit; immutable ledger entries |
| A09 Logging Failures | Structured JSON logging; every state change audited; sensitive fields redacted |
| A10 SSRF | No outbound HTTP calls in this service scope; if added later, allowlist-only |

## Logging Redaction Rules

- **NEVER log:** `cardToken`, authorization headers, passwords, API keys, full card numbers.
- **ALWAYS log:** `correlationId`, `actorId`, `actorRole`, `action`, `cardId`, `timestamp`.
- **Redact patterns:** any field name matching `*token*`, `*secret*`, `*password*`, `*authorization*`.

## Domain Validation Constraints

These constraints enforce A03 (Injection prevention) at the system boundary. All values below MUST be applied via Jakarta Bean Validation on DTOs and verified by the service layer.

| Field | Constraint |
|---|---|
| UUID fields (id, cardId, userId, idempotencyKey) | RFC 4122 format; reject malformed UUIDs |
| `displayName` | 1–100 characters; pattern: `^[a-zA-Z0-9 -]+$` (alphanumeric, spaces, hyphens) |
| `currency` | Exactly 3 uppercase letters; pattern: `^[A-Z]{3}$`; validated against an allowlist of supported ISO-4217 codes |
| `perTransactionLimit`, `dailyLimit`, `monthlyLimit` | Non-negative (`@DecimalMin("0")`); max 1,000,000.00; at most 4 decimal places (`@Digits(integer=15, fraction=4)`); nullable (null = no limit) |
| `reason` (ops overrides) | 1–500 characters; `@NotBlank`; `@Size(min=1, max=500)` |
| Pagination `page` | >= 0 |
| Pagination `size` | 1–100 |
| `Idempotency-Key` header | UUID format; mandatory on all state-changing endpoints |
| `merchantInfo` | Max 255 characters |

## Error Response Contract

All errors MUST use the `ErrorResponse` DTO envelope:

```
{ errorCode, message, correlationId, timestamp, fieldErrors (nullable) }
```

**Rules:**
- NEVER include stack traces, SQL error messages, internal class names, or entity field names.
- Log the full exception internally at WARN/ERROR with correlation ID.

**HTTP Status Code Mapping:**

| Status | When |
|---|---|
| 400 Bad Request | Validation failure (`MethodArgumentNotValidException`); include field-level details |
| 403 Forbidden | Authorization failure (`AccessDeniedException`); generic message only |
| 404 Not Found | Entity not found (`CardNotFoundException`); generic message only |
| 409 Conflict | Optimistic lock failure, idempotency key conflict with different payload, invalid state transition |
| 422 Unprocessable Entity | Spending limit exceeded (`SpendingLimitExceededException`) |
| 429 Too Many Requests | Rate limit exceeded; include `Retry-After` header |
| 500 Internal Server Error | Unhandled exceptions; generic message only |

## Input Validation Strategy

- **Boundary validation (controllers):** All incoming data validated with Jakarta Bean Validation annotations on DTOs. Controller layer MUST use `@Valid` on all request bodies. UUID path parameters MUST be validated as RFC 4122 format (use Spring's built-in UUID conversion).
- **Business-rule validation (services):** State machine transitions, limit checks, currency match, authorization.
