# Homework 3 — Specification-Driven Design

## Student & Task Summary

**Student**: Ruslan Pistriak  
**Homework**: Homework 3 — Specification-Driven Design  
**Date**: March 10, 2026  

### Summary

This submission designs a complete specification package for a **Virtual Card Lifecycle Management** service — a finance-oriented feature that allows end-users to create, activate, freeze/unfreeze, configure spending limits on, and view transactions for virtual payment cards, while providing internal ops/compliance teams with full audit visibility.

The deliverables are:

| File | Purpose |
|---|---|
| `specification.md` | Full product specification with high/mid/low-level objectives and 7 actionable AI tasks |
| `agents.md` | AI agent configuration: tech stack, domain rules, security constraints, testing expectations |
| `.github/copilot-instructions.md` | Editor-level AI rules: naming, patterns to follow/avoid, security checklist |
| `README.md` (this file) | Rationale and industry best practices map |

**No implementation code is included.** The specification is designed to be handed directly to an AI coding agent (or a developer) to implement without further clarification.

---

## Rationale

### Why Virtual Card Lifecycle?

Virtual cards are a high-value, compliance-heavy FinTech feature: they involve cardholder data (PAN, CVV), monetary calculations, state management, and audit requirements all at once. This makes them an ideal subject for a specification exercise because every layer of the spec — from data models to API design — has meaningful security and compliance implications to reason about.

### Why This Specification Structure?

The specification follows a **three-level decomposition**:

1. **High-Level Objective** — one sentence that an executive or product owner can validate.
2. **Mid-Level Objectives** — 6 measurable outcomes that a tech lead can use to scope the work and define acceptance criteria.
3. **Low-Level Tasks** — 7 tasks, each structured as a concrete AI prompt + target file + function name + detailed constraints. This format is directly consumable by an AI coding agent without ambiguity.

This structure was chosen because vague specifications are the primary cause of AI-generated code that is functionally correct but compliance-deficient. By forcing every task to name the exact file, function, and constraints, the spec leaves no room for the agent to make unsafe assumptions.

### Why `agents.md` and `copilot-instructions.md` as Separate Files?

- **`agents.md`** is project-level configuration for an AI agent executing a multi-step build plan. It covers tech stack versions, domain invariants (state machine rules, monetary precision), and a binary list of allowed/forbidden patterns. It is consumed at the start of a session.
- **`copilot-instructions.md`** is editor-level, used by GitHub Copilot on every keystroke autocomplete. It is optimized for brevity and immediate applicability: short rules, inline code examples, and a security checklist the model can evaluate on each suggestion.

Keeping them separate avoids bloating the inline autocomplete context with project-setup details, and avoids losing security rules in a long tech-stack document.

---

## Industry Best Practices — Where They Appear

### 1. PCI-DSS v4 — Cardholder Data Protection

**Practice**: Store only what is necessary; encrypt PAN at rest; never persist CVV; mask PAN in all outputs.

**Where it appears**:
- `specification.md` → Mid-Level Objective 5: encryption at rest (AES-256-GCM), masking policy, CVV non-persistence.
- `specification.md` → Task 4: envelope encryption with KMS, memory zeroing of plaintext PAN.
- `agents.md` → Section 3 "Cardholder Data (CHD)": explicit rules on never logging PAN/CVV.
- `agents.md` → Section 6: `encrypted_pan_blob` forbidden in API responses; CVV not persisted.
- `.github/copilot-instructions.md` → "Patterns to Avoid" + "Security Checklist" items 1 and 5.

---

### 2. Append-Only Audit Trail

**Practice**: Regulated systems must maintain tamper-evident logs of all sensitive operations. Audit entries must be immutable (ISAE 3402, SOC 2 Type II, internal AML policy).

**Where it appears**:
- `specification.md` → Mid-Level Objective 4: audit log schema with actor, action, before/after state, IP, UTC timestamp.
- `specification.md` → Task 5: DB-level trigger preventing UPDATE/DELETE on `audit.log`; UUID v7 for time-ordered IDs; separate `audit.*` DB schema with restricted write access.
- `agents.md` → Section 3 "Audit Trail": every mutation must produce an audit entry within the same DB transaction; no UPDATE/DELETE on audit table.
- `agents.md` → Section 6 "Must NOT do": explicit prohibition on migrations that alter or drop the audit table.
- `.github/copilot-instructions.md` → "Patterns to Avoid": `No direct UPDATE or DELETE on audit.log`.

---

### 3. Explicit State Machine with Terminal States

**Practice**: Card lifecycle must be modelled as a finite state machine with enforced valid transitions to prevent invalid states (e.g. reactivating a cancelled card). This is standard in card-issuing platforms (Marqeta, Stripe Issuing).

**Where it appears**:
- `specification.md` → Implementation Notes: state graph `PENDING → ACTIVE ↔ FROZEN → CLOSED`; `CLOSED` is terminal.
- `specification.md` → Task 3: `CardStateTransitionError` with structured fields; transitions wrapped in DB transactions.
- `agents.md` → Section 3 "Card State Machine": explicit transition graph; 409 Conflict on invalid transitions.
- `.github/copilot-instructions.md` → "Patterns to Follow" — `match` statement example for state transitions.

---

### 4. Idempotency for Mutating Operations

**Practice**: Financial APIs must be idempotent to prevent double-charges or duplicate state changes caused by network retries (Stripe, Adyen, and most ISO 20022-aligned systems require this).

**Where it appears**:
- `specification.md` → Implementation Notes: `Idempotency-Key` header, Redis-backed store, 24-hour TTL.
- `specification.md` → Task 3: idempotency check before any mutating operation in `CardService`.
- `specification.md` → Task 6: header validation in router; `Idempotency-Replayed: true` response header.
- `agents.md` → Section 3 "Idempotency": full policy including Redis TTL, duplicate response caching.

---

### 5. Decimal Precision for Monetary Values

**Practice**: IEEE 754 floating-point arithmetic causes rounding errors in financial calculations. All regulated financial systems require fixed-point or decimal arithmetic (Basel III reporting guidelines, IFRS 9).

**Where it appears**:
- `specification.md` → Implementation Notes: "Use `Decimal` — never `float`".
- `specification.md` → Task 1: `Numeric(19,4)` DB column type; check constraint `amount > 0`.
- `specification.md` → Task 2: `condecimal(gt=0, max_digits=19, decimal_places=4)` Pydantic validator.
- `agents.md` → Section 2 "SQLAlchemy": `Numeric(19,4)` rule; Section 3 "Monetary Calculations": `ROUND_HALF_EVEN`.
- `agents.md` → Section 6 "Must NOT do": `Never use float for monetary values`.
- `.github/copilot-instructions.md` → "Patterns to Avoid" + inline code example.

---

### 6. Role-Based Access Control (RBAC) with Ownership Checks

**Practice**: Zero-trust authorization — authenticate identity, authorize role, and verify resource ownership separately. Required by GDPR (data access minimization) and standard FinTech security posture.

**Where it appears**:
- `specification.md` → Mid-Level Objective 5: RBAC (cardholder vs. ops/compliance).
- `specification.md` → Task 6: `require_cardholder` / `require_ops` FastAPI dependencies; ownership check before service delegation.
- `agents.md` → Section 7 "Ops/Compliance Agent Behaviour": compliance endpoints always check role.
- `.github/copilot-instructions.md` → "Patterns to Avoid": no skipping RBAC checks; "Security Checklist" item 5.

---

### 7. RFC 7807 Problem Details for Error Responses

**Practice**: Consistent, machine-readable error responses reduce client-side guesswork and prevent accidental leakage of internal system details (OWASP API Security Top 10 — API3:2023 Broken Object Property Level Authorization).

**Where it appears**:
- `specification.md` → Implementation Notes: "Return RFC 7807 Problem Detail JSON on all error responses."
- `specification.md` → Task 6: HTTP 422 responses in RFC 7807 format with `type`, `title`, `detail`, `instance`.
- `agents.md` → Section 3 "Card State Machine": 409 Conflict with Problem Detail.
- `.github/copilot-instructions.md` → "Patterns to Follow" — inline RFC 7807 `HTTPException` example.

---

### 8. Dependency Security and SAST in CI

**Practice**: Continuous security scanning catches vulnerable dependencies and insecure code patterns before they reach production (OWASP DevSecOps, NIST SSDF).

**Where it appears**:
- `agents.md` → Section 1 "Project Identity": Semgrep SAST + `pip-audit` in CI pipeline.
- `agents.md` → Section 6 "Security Constraints": `p/owasp-top-10` and `p/python` Semgrep rules on every changed file.
- `agents.md` → "Dependency Management": pin exact versions; check CVE history before adding a dependency.
- `specification.md` → Implementation Notes: "CI pipeline runs linting, tests, and SAST scanning on every PR."

---

### 9. Data Minimization and GDPR Soft-Delete

**Practice**: Collect and retain only the data necessary for the stated purpose. Provide a deletion mechanism that respects the right to erasure while preserving the audit trail for regulatory obligations (GDPR Article 17 + Recital 65).

**Where it appears**:
- `specification.md` → Implementation Notes: "GDPR/data minimization" note with soft-delete stub and PAN tombstone token.
- `agents.md` → Section 7: PII in compliance reports must be pseudonymized unless for a regulatory authority.
- `.github/copilot-instructions.md` → "Compliance Notes": return only what the consumer needs.

---

### 10. Structured Logging with Correlation IDs

**Practice**: Structured (JSON) logs with correlation IDs are essential for incident response and forensic analysis in regulated environments. Unstructured plaintext logs are insufficient for automated SIEM ingestion.

**Where it appears**:
- `specification.md` → Implementation Notes: "Structured JSON logs (no plaintext). Log correlation IDs on every request. Never log PANs."
- `agents.md` → Section 6 "Must NOT do": no logging of PAN, CVV, or keys.
- `.github/copilot-instructions.md` → "Patterns to Avoid": `No print() statements — use structlog`.

