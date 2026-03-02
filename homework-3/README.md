# Homework 3 — Specification-Driven Design (Virtual Card Lifecycle)

## Student & task summary
- **Student**: Garnaga
- **What I built (documents only)**: A specification package for a regulated-fintech **virtual card lifecycle** feature that covers card creation, freeze/unfreeze, spending limits, and transaction viewing for both end-users and ops/compliance.
- **Deliverables**:
  - `homework-3/specification.md`
  - `homework-3/agents.md`
  - `.github/copilot-instructions.md`

## Rationale
I chose the virtual card lifecycle because it’s a compact, realistic FinTech feature that still forces strong design decisions around:
- **Sensitive data boundaries** (tokenization, redaction, no PAN/CVV)
- **Auditability** (immutable audit events, correlation IDs)
- **Correctness** (money math, UTC timestamps, deterministic limit checks)
- **Operational needs** (ops/compliance read access, justification logging)

The spec is written so an AI implementation agent can move from high-level objectives → mid-level objectives → low-level tasks, while keeping compliance and security constraints explicit and testable.

## Industry best practices (and where they appear)
1. **No PAN/CVV storage + tokenization**
   - In `specification.md`: “Data handling & sensitive data boundaries” and “Out of scope (explicit)”
   - In `agents.md`: Domain Rules #1–2
   - In `.github/copilot-instructions.md`: “Non-negotiable rules”

2. **Immutable, append-only audit trail**
   - In `specification.md`: “Audit logging (immutability)” + Low-Level Task #2
   - In `agents.md`: Domain Rule #5

3. **Least privilege + separated ops/compliance access**
   - In `specification.md`: “Authorization model (high-level)” + Low-Level Task #5
   - In `agents.md`: Domain Rules #6–7

4. **Money correctness (Decimal/minor units) and explicit currency**
   - In `specification.md`: “Monetary & time rules” + Low-Level Task #4
   - In `agents.md`: Domain Rule #3

5. **Idempotency + concurrency safety**
   - In `specification.md`: “Idempotency & concurrency” + Low-Level Task #3
   - In `.github/copilot-instructions.md`: “Engineering rules”

6. **Security-by-design logging (redaction/allowlists)**
   - In `specification.md`: “Audit logging (immutability)” and “Observability & reliability”
   - In `agents.md`: “Security Requirements (non-negotiable)”

## Notes
- Homework 3 requires **documents only**; no API or UI is implemented.
- If this were implemented, I’d add a lightweight threat model (STRIDE-style) and run dependency security scanning in CI.
