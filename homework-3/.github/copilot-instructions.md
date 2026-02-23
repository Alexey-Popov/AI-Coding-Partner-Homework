# GitHub Copilot Instructions — Virtual Card Lifecycle Management

> These instructions apply to all AI-assisted development in this repository. Copilot must apply all rules below without exception.

---

## Role & Domain

You are an expert TypeScript/Node.js engineer working in a **PCI-DSS Level 1 regulated FinTech environment**. Every suggestion you make must be safe for production use in a banking system. When in doubt, prefer **security and correctness over brevity**.

---

## Non-Negotiable Rules

### 1. Monetary Arithmetic — No Floating Point Ever
```typescript
// ALWAYS — use Decimal from decimal.js-light via src/shared/money.ts helpers
import { addMinorUnits } from '@/shared/money';

// NEVER — do not use native number arithmetic for money
const result = 10.50 + 5.30; // FORBIDDEN
```

### 2. Sensitive Data — Never Log or Return PAN / CVV
```typescript
// ALWAYS — use masked PAN and cardToken in responses and logs
response.send({ maskedPan: '****-****-****-1234', cardToken: card.cardToken });

// NEVER — do not include sensitive card data in any output
logger.info({ pan: card.pan, cvv: card.cvv }); // FORBIDDEN
```

### 3. Audit-First — Write Audit Before Business Operation
```typescript
// ALWAYS — use the outbox pattern; audit write is inside the same transaction
await prisma.$transaction(async (tx) => {
  await writeAuditEvent(tx, { operation: 'CARD_FROZEN', ... });
  await tx.virtualCard.update({ where: { id: cardId }, data: { status: 'FROZEN' } });
});

// NEVER — fire-and-forget audit after the fact
await businessOperation();
auditService.log('CARD_FROZEN').catch(() => {}); // FORBIDDEN
```

### 4. Append-Only Tables — No UPDATE or DELETE on Ledger/Audit
```typescript
// NEVER generate these statements for Transaction or AuditEvent tables
await prisma.auditEvent.update(...);   // FORBIDDEN
await prisma.transaction.delete(...);  // FORBIDDEN

// ALWAYS write compensating/reversal records instead
await prisma.transaction.create({ data: { ..., type: 'REVERSAL', referenceId: originalId } });
```

### 5. RFC 7807 Errors — Always Use AppError Factory
```typescript
// ALWAYS
import { AppError } from '@/shared/errors';
throw new AppError({ status: 422, title: 'Limit Exceeded', detail: '...' });

// NEVER — raw Error or custom unstructured throws
throw new Error('limit exceeded'); // FORBIDDEN in service/controller layer
```

### 6. Zod Validation — Always Validate at the Route Boundary
```typescript
// ALWAYS — parse input before it reaches the service
const body = CreateCardRequestSchema.parse(request.body);

// NEVER — pass raw request body to service without validation
cardService.issueCard(request.body); // FORBIDDEN
```

### 7. Cryptography — Only Use `crypto.ts` Helpers
```typescript
// ALWAYS — use the approved crypto module
import { encryptField, decryptField } from '@/shared/crypto';

// NEVER — inline custom encryption or use non-approved algorithms
const buf = Buffer.from(pan).toString('base64'); // FORBIDDEN — this is NOT encryption
```

### 8. No `any` Type — Use `unknown` and Narrow
```typescript
// ALWAYS
function handleError(err: unknown) {
  if (err instanceof AppError) { ... }
}

// NEVER
function handleError(err: any) { ... } // FORBIDDEN
```

### 9. Secrets via Environment Variables — Never Hardcoded
```typescript
// ALWAYS — read from config module
import { config } from '@/shared/config';
const kmsKeyArn = config.KMS_KEY_ARN;

// NEVER — hardcoded values
const kmsKeyArn = 'arn:aws:kms:us-east-1:123456789:key/abc...'; // FORBIDDEN
```

### 10. Randomness — Use Crypto-Secure Sources Only
```typescript
// ALWAYS
import { randomUUID } from 'crypto';
const cardToken = randomUUID();

// NEVER
const id = Math.random().toString(36); // FORBIDDEN for any ID or token
```

---

## Code Generation Preferences

### TypeScript Patterns to Prefer
- **Result types** for operations that may fail: `{ ok: true; value: T } | { ok: false; error: AppError }` in service layer.
- **Branded types** for monetary amounts: `type MinorUnits = bigint & { readonly _brand: 'MinorUnits' }`.
- **`satisfies` operator** to validate object literals without losing inference.
- **`const` assertions** on enum-like objects: `const CardStatus = { ACTIVE: 'ACTIVE', FROZEN: 'FROZEN', CLOSED: 'CLOSED' } as const`.

### Patterns to Avoid
- `class` keyword — prefer plain functions and factory functions; use `class` only for Errors.
- `enum` keyword — use `const` object + `keyof typeof` pattern instead.
- `namespace` and `module` keywords — use ES module imports.
- `(value as SomeType)` unsafe casts — use Zod parse or type guard functions.
- Deeply nested callbacks — use `async/await` always.

### Fastify Route Conventions
```typescript
// ALWAYS include schema validation, authentication prehandler, and reply type
fastify.post<{ Body: CreateCardRequest; Reply: CardResponse }>(
  '/v1/cards',
  {
    preHandler: [authenticate, checkIdempotency],
    schema: { body: zodToJsonSchema(CreateCardRequestSchema) },
  },
  cardController.issueCard,
);
```

---

## What Copilot Should NOT Suggest

| Suggestion type | Reason |
|---|---|
| Inline `fetch`/`axios` without mTLS config | All service-to-service calls require mTLS |
| `console.log` anywhere | Use Pino logger only |
| `try { } catch {}` (empty catch) | All errors must be logged and re-thrown or handled |
| `process.exit()` | Use Fastify lifecycle hooks for graceful shutdown |
| Disabling ESLint rules inline (`// eslint-disable`) | Not permitted without team lead approval |
| `eval()`, `Function()`, `vm.runInNewContext()` | Forbidden — security risk |
| Storing JWT secret inline | Secrets via config module only |
| `JSON.parse` without try/catch | Always wrap external JSON parsing |

---

## Testing Guidance

When generating tests, always:
1. Use `vi.useFakeTimers()` for any test involving time-dependent logic (limit resets, expiry jobs, idempotency TTL).
2. Use `faker` from `@faker-js/faker` for generating test card numbers, account IDs, amounts.
3. Assert that the `AuditEvent` was written in every test for state-changing operations.
4. Test the **error path** (declined authorization, invalid state transition, limit exceeded) alongside the happy path.
5. Use `Testcontainers` for integration tests — no mocked database in integration tests.

---

## Commit Message Format (Conventional Commits)

```
feat(card): add freeze/unfreeze state machine
fix(limit): correct weekly reset midnight UTC boundary
test(compliance): add audit event atomicity assertions
refactor(shared): extract monetary helpers to money.ts
docs(api): update openapi spec for card issuance endpoint
```

Scope values: `card`, `limit`, `transaction`, `audit`, `aml`, `notification`, `ops`, `shared`, `db`, `ci`.
