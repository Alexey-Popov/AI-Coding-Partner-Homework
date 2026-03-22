# Specification Template — Quick Reference

---

## Template Structure

```markdown
# Specification: [Project Name]

## 1. High-Level Objective
[One sentence. What does this system do?]
Example: "Build a 3-agent Python pipeline that validates, scores for fraud risk,
and settles banking transactions using file-based JSON message passing."

## 2. Mid-Level Objectives
- [Testable requirement 1 — something you can write a test for]
- [Testable requirement 2]
- [Testable requirement 3]
- [Testable requirement 4]
- [Testable requirement 5]

Example:
- Transactions with invalid currency codes are rejected with a "INVALID_CURRENCY" reason
- Transactions above $10,000 are assigned fraud_risk: "HIGH" by the Fraud Detector
- All agent operations write to the audit log within 100ms of processing
- The pipeline processes 8 sample transactions and writes 8 result files to shared/results/

## 3. Implementation Notes
- Monetary calculations: decimal.Decimal only, never float
- Currency validation: ISO 4217 whitelist (USD, EUR, GBP minimum)
- Logging: audit trail with timestamp, agent name, transaction_id, outcome
- PII: mask account numbers in all log output
- [Add your own constraints]

## 4. Context
- **Beginning state**: `sample-transactions.json` exists with 8 raw transaction records.
  No agents exist. No shared/ directories exist.
- **Ending state**: All transactions processed. Results in `shared/results/`.
  Test coverage ≥ 90%. README and HOWTORUN complete.

## 5. Low-Level Tasks

### Task: Transaction Validator
**Prompt**: "[Your exact AI prompt for this agent]"
**File to CREATE**: `agents/transaction_validator.py`
**Function to CREATE**: `process_message(message: dict) -> dict`
**Details**:
- Check required fields: transaction_id, amount, currency, source_account, destination_account
- Validate amount is positive Decimal
- Validate currency against ISO 4217 whitelist
- Return message with status: "validated" or "rejected" + reason field

### Task: Fraud Detector
**Prompt**: "[Your exact AI prompt]"
**File to CREATE**: `agents/fraud_detector.py`
**Function to CREATE**: `process_message(message: dict) -> dict`
**Details**:
- Score transaction for fraud risk (0–10 scale)
- Flag triggers: amount > $10,000 (+3 pts), amount > $50,000 (+4 pts),
  unusual hour (2am–5am, +2 pts), cross-border (+1 pt)
- Risk levels: LOW (0–2), MEDIUM (3–6), HIGH (7–10)
- Return message with fraud_risk_score and fraud_risk_level fields

### Task: [Your Third Agent]
**Prompt**: "[Your exact AI prompt]"
**File to CREATE**: `agents/[name].py`
**Function to CREATE**: `process_message(message: dict) -> dict`
**Details**: [Describe what it checks, transforms, or decides]
```

---

## Prompt Engineering Reminder

Structure every AI prompt as:

```
Context:   [What exists — files, tech stack, constraints]
Task:      [Exactly what to build]
Rules:     [Non-negotiable requirements — Decimal, logging, error handling]
Examples:  [Sample input/output if helpful]
Output:    [What format the result should take]
```

---

## Tips

- **Be specific in your Mid-Level Objectives** — vague goals produce vague code
- **Write your Low-Level Task prompts before you open Claude Code** — this forces clarity
- **The prompt IS the specification for the AI** — the more precise it is, the better the output
- **Revisit your spec after each agent** — update it if your understanding changed
