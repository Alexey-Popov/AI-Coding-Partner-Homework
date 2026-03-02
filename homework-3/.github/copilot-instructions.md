# Copilot Instructions (Project Rules)

You are an AI coding partner working on a regulated financial domain (virtual cards).

## Non-negotiable rules
- **Never** store or log PAN/CVV. Assume tokenized card references.
- Use **Decimal** or integer minor units for money. Never float.
- Use **UTC** timestamps and timezone-aware types.
- Every state change must emit an **append-only audit event**.
- Follow **least privilege**: users can only access their own account data.
- Ops/compliance access must be **read-only**, must include a **justification**, and must be audited.

## Engineering rules
- Prefer small, testable units of logic (pure functions for limit evaluation).
- Add idempotency keys for mutating actions and avoid duplicate processor calls.
- Implement optimistic concurrency for state changes.
- Structured logging with redaction/allowlists.

## Style & quality gates
- Write code with types (mypy/pyright compatible where possible).
- Add unit tests for new behavior and negative cases.
- Keep modules cohesive; avoid giant files.

## What to avoid
- “Convenient” shortcuts that weaken audit/security (e.g., logging request bodies).
- Implicit defaults for currency or timezone.
- Returning sensitive internal details in user-facing errors.

## How to work in this repo
- Read the relevant spec first: `homework-3/specification.md`.
- Update docs when changing domain behavior.
- If requirements conflict, call it out explicitly and propose a safe default.
