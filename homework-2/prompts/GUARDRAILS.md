# AI Prompt Guardrails

## Universal Rules for All AI Interactions

These guardrails must be included in every prompt to ensure safe, accurate, and reliable AI-generated outputs.

---

## 🛡️ Core Guardrails

### 1. No Guessing Policy
```
If you are unsure of the answer, state that you do not know. Do not guess.
```

**When to apply:**
- Technical specifications are unclear
- Version compatibility is uncertain
- Implementation details depend on external factors
- Security implications are unknown

**Expected AI behavior:**
- Clearly state: "I am not certain about [specific aspect]"
- Provide what is known with confidence levels
- Suggest how to verify the uncertain information

---

### 2. Ambiguity Clarification
```
If the input data is ambiguous, ask 3 clarifying questions before proceeding.
```

**When to apply:**
- Requirements can be interpreted multiple ways
- Technical terms could have different meanings
- Scope boundaries are unclear
- Dependencies are not specified

**Expected AI behavior:**
- Identify the ambiguous elements
- Ask exactly 3 targeted questions
- Wait for clarification before generating code/documentation
- Review output for consistency before finalizing

**Example questions format:**
1. "Regarding [ambiguous element], do you mean [option A] or [option B]?"
2. "Should [feature] support [variation 1], [variation 2], or both?"
3. "What is the expected behavior when [edge case] occurs?"

---

### 3. Missing Data Declaration
```
If data is missing from the provided text, explicitly list what is missing.
```

**When to apply:**
- Required fields are not provided
- Context is incomplete
- Dependencies are not specified
- Configuration values are absent

**Expected AI behavior:**
- Generate a clear list of missing items
- Categorize by: Required vs Optional
- Suggest reasonable defaults where applicable
- Proceed with explicit assumptions if requested

**Missing data format:**
```markdown
## Missing Information

### Required (blocking):
- [ ] Database connection string
- [ ] Authentication method (JWT/OAuth/API Key)

### Optional (defaults available):
- [ ] Port number (default: 8000)
- [ ] Log level (default: INFO)

### Assumptions made:
- Using Python 3.8+ (based on type hints)
- FastAPI framework (based on project structure)
```

---

## 📋 How to Reference in Templates

Include this block at the top of every prompt:

```markdown
## Guardrails
Follow the rules defined in [GUARDRAILS.md](../GUARDRAILS.md):
- Do not guess if uncertain
- Ask 3 clarifying questions for ambiguous input
- List all missing data explicitly
```

---

## 🔗 Integration with Templates

| Template Type | Primary Guardrail Focus |
|--------------|------------------------|
| Planner | Ambiguity clarification, missing requirements |
| Coder | No guessing on implementation details |
| Tester | Missing test data, unclear assertions |
| Documenter | Incomplete information, audience assumptions |
| Reviewer | Security uncertainties, best practice variations |

---

## ✅ Validation Checklist

Before submitting any AI-generated output, verify:

- [ ] No speculative code or documentation
- [ ] All assumptions are explicitly stated
- [ ] Missing information is clearly listed
- [ ] Ambiguities were clarified or flagged
- [ ] Confidence level is indicated for uncertain areas
