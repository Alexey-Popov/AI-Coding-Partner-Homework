# GitHub Copilot Instructions — Virtual Card Lifecycle Service

## Project Context

This is a **regulated FinTech** project implementing a virtual card lifecycle management service. It handles cardholder data (CHD) and is subject to **PCI-DSS v4** and **GDPR** requirements. Every suggestion Copilot makes must respect the security, compliance, and code-quality rules below.

---

## Language & Framework

- **Language**: Python 3.12 with full type annotations (`mypy --strict`).
- **Framework**: FastAPI 0.115+ with async/await throughout.
- **ORM**: SQLAlchemy 2.x async (`AsyncSession`).
- **Validation**: Pydantic v2 — use `model_dump()`, never `.dict()`.
- **Formatting**: `ruff format` (line length 100); linting via `ruff check`.

---

## Naming Conventions

| Artefact | Convention | Example |
|---|---|---|
| Python files | `snake_case` | `card_service.py` |
| Classes | `PascalCase` | `CardService` |
| Functions / variables | `snake_case` | `freeze_card` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_LIMIT_AMOUNT` |
| DB table names | plural `snake_case` | `virtual_cards` |
| Enum members | `UPPER_SNAKE_CASE` | `CardStatus.FROZEN` |
| Test functions | `test_<behaviour>_<condition>` | `test_freeze_card_when_already_closed_raises_error` |

---

## Patterns to Follow

### Money
```python
# ✅ Always use Decimal
from decimal import Decimal, ROUND_HALF_EVEN
amount = Decimal("100.50")

# ❌ Never use float for money
amount = 100.50  # WRONG
```

### State Transitions
```python
# ✅ Use match statement for state machine
match card.status:
    case CardStatus.ACTIVE:
        card.status = CardStatus.FROZEN
    case _:
        raise CardStateTransitionError(card.status, "freeze")
```

### Error Responses
```python
# ✅ Always return RFC 7807 Problem Detail
raise HTTPException(
    status_code=status.HTTP_409_CONFLICT,
    detail={
        "type": "https://errors.cards.example.com/invalid-state-transition",
        "title": "Invalid State Transition",
        "detail": f"Cannot freeze a card in status {card.status}.",
        "instance": f"/cards/{card_id}",
    },
)
```

### DB Queries
```python
# ✅ Use ORM — never raw string SQL
result = await session.execute(
    select(VirtualCard).where(VirtualCard.id == card_id)
)

# ❌ Never do this
await session.execute(text(f"SELECT * FROM virtual_cards WHERE id = '{card_id}'"))
```

### Secrets / Config
```python
# ✅ Always load from environment via pydantic-settings
from app.config import settings
kms_key_id = settings.KMS_KEY_ID

# ❌ Never hardcode
kms_key_id = "arn:aws:kms:us-east-1:123456789:key/abc123"  # WRONG
```

---

## Patterns to Avoid

- ❌ **No `float` for money** — always `Decimal`.
- ❌ **No plaintext PAN** in logs, exceptions, responses, or variables named anything other than `pan_bytes` (a `bytearray` that is zeroed immediately after use).
- ❌ **No CVV persistence** — CVV must not appear in any DB column, cache, or log.
- ❌ **No raw SQL strings** with user input — always use ORM or bound parameters.
- ❌ **No `eval()`, `exec()`, `pickle`** on untrusted data.
- ❌ **No `verify=False`** in any HTTP/requests/httpx call.
- ❌ **No hardcoded secrets, keys, or connection strings** anywhere in the codebase.
- ❌ **No `print()` statements** — use structured logging via `structlog`.
- ❌ **No synchronous blocking I/O** inside async functions (no `time.sleep`, no synchronous DB calls).
- ❌ **No `model.dict()`** — always use `model.model_dump()`.
- ❌ **No untyped functions** — all parameters and return types must be annotated.
- ❌ **No direct `UPDATE` or `DELETE` on `audit.log`** table — the audit log is append-only.
- ❌ **No skipping RBAC checks** in any code path, even in test helpers loaded into production modules.

---

## Security Checklist (apply to every suggestion)

Before completing any code suggestion, mentally verify:

1. [ ] Does this expose CHD (PAN, CVV, full card number) in a log, response, or exception?
2. [ ] Does this use `float` for a monetary value?
3. [ ] Does this introduce a SQL injection risk?
4. [ ] Does this hardcode a secret or credential?
5. [ ] Does this bypass or weaken an authorization check?
6. [ ] Does this modify or delete an audit log row?

If any answer is **yes**, revise the suggestion before offering it.

---

## Testing Expectations

- Every new function must have at least one unit test.
- Every state transition (valid and invalid) must have a dedicated test case.
- Authorization boundaries must be tested: cardholder cannot access another user's card.
- Security tests must assert that PANs never appear in API responses or log output.
- Use `factory_boy` for test fixtures; never hardcode real card numbers.
- Mock all external services (KMS, Redis) in unit tests.
- Integration tests use `testcontainers` (real PostgreSQL + Redis).

---

## Compliance Notes

- Audit log entries must be created **within the same DB transaction** as the business operation.
- Compliance report endpoints are **ops/compliance-role only** — always add `Depends(require_ops)`.
- PII in any response must be minimized; return only what the consumer needs.
- Key rotation must be logged in the audit trail; plaintext keys must never appear in logs.

