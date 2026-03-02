# Agents Configuration — Regulated Virtual Card Domain

This file defines how an AI coding partner should behave when implementing the feature described in `homework-3/specification.md`.

## 1) Assumed Tech Stack (for future implementation)
- Language: **Python 3.11+**
- API framework: **FastAPI** (OpenAPI generated)
- Storage: **PostgreSQL**
- ORM/migrations: SQLAlchemy + Alembic (or equivalent)
- Testing: pytest
- Formatting/linting: black, ruff, mypy (or pyright)
- Observability: structlog (or stdlib logging with JSON formatter)

> Note: No implementation is required for Homework 3; this stack is a guideline for consistent code generation.

## 2) Domain Rules (FinTech / Banking constraints)
1. **No PAN/CVV storage**: Never persist full PAN or CVV. Do not log them.
2. **Tokenization**: Use processor tokens/ids for card references.
3. **Money correctness**: Use Decimal or integer minor units; never float.
4. **UTC everywhere**: Store and compute timestamps in UTC.
5. **Audit trail**: Every state change must create an append-only audit event.
6. **Least privilege**: End-user access is scoped to their `account_id`.
7. **Ops/compliance access is exceptional**: Must be read-only by default, requires justification, and must be audited.

## 3) Security Requirements (non-negotiable)
- Treat all inputs as untrusted; validate and sanitize.
- Use allowlists for fields written to logs/audits.
- Secrets never committed to repo; configuration via environment variables.
- Prefer defense-in-depth:
  - authn/authz middleware
  - rate limiting
  - correlation IDs
  - structured error handling
- Avoid information leakage in errors (no internal IDs in user-facing messages beyond safe identifiers).

## 4) Compliance & Governance Expectations
- Design with auditability in mind: who/what/when/why.
- Support data retention policies via configuration.
- Keep a clear data classification list (Sensitive / Confidential / Internal / Public).
- Provide traceability from requirements → tests.

## 5) Coding Guidelines
- Favor a layered architecture:
  - routes/controllers (thin)
  - services (business logic)
  - storage/repositories (DB)
  - integrations/adapters (card processor)
- Prefer pure functions for money/limits evaluation (easy to test).
- Add docstrings for domain concepts; avoid framework-heavy coupling in domain models.

## 6) Testing Expectations
Minimum tests for any future implementation:
- State machine tests for card transitions.
- Idempotency tests (replay does not duplicate external calls).
- Limit evaluator boundary tests (exactly-at-limit, off-by-one, currency mismatch).
- Authorization tests (end-user vs ops/compliance).
- Redaction tests (no PAN-like data in logs/audits).

## 7) “Stop the line” conditions
The agent must stop and request clarification (instead of guessing) if:
- Requirements imply storing PAN/CVV.
- Actor permissions are ambiguous for ops/compliance.
- Currency/rounding rules are unspecified for a new money flow.
