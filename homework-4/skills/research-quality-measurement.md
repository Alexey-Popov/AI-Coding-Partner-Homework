# Skill: Research Quality Measurement

## Overview

In agentic bug-fixing pipelines, research quality directly determines downstream success. If file:line references are wrong, the Bug Implementer edits the wrong code. If root cause is shallow, the fix addresses symptoms rather than the cause. This skill provides a reproducible, weighted scoring framework so the Research Verifier produces consistent, objective quality assessments that downstream agents can trust.

---

## Quality Levels

| Level | Label | Score Range | Meaning |
|-------|-------|-------------|---------|
| 5 | **EXCELLENT** | 4.0 – 5.0 | Research is precise, complete, and directly actionable. All references verified. |
| 4 | **GOOD** | 3.0 – 3.9 | Research is accurate with minor gaps. Actionable with small clarification. |
| 3 | **ACCEPTABLE** | 2.0 – 2.9 | Research identifies the issue but is incomplete. Requires additional verification. |
| 2 | **POOR** | 1.0 – 1.9 | Research has significant errors or gaps. Cannot be used without re-investigation. |
| 1 | **INSUFFICIENT** | 0.0 – 0.9 | Research is too incomplete or inaccurate to act on. Must be redone. |

---

## Measurement Criteria

Score each criterion 0–5, then compute the weighted average.

### Criterion 1: File and Line Reference Accuracy (weight: 30%)

Measures whether every `file:line` claim in the research points to the correct location in the actual source code.

| Score | Description |
|-------|-------------|
| 5 | All file:line references verified against source; all correct |
| 4 | All references verified; ≤ 1 minor off-by-one error (e.g., line 23 vs 24) |
| 3 | Most references correct; ≤ 2 errors; correct file identified |
| 2 | Multiple wrong line numbers; or one wrong file reference |
| 1 | References present but majority incorrect |
| 0 | No file:line references provided |

### Criterion 2: Code Snippet Completeness (weight: 25%)

Measures whether code snippets include enough context to understand the bug without opening the file.

| Score | Description |
|-------|-------------|
| 5 | Snippets include full function context; exact match with source |
| 4 | Key lines present; minor surrounding context missing |
| 3 | Only the bug line shown; no context (function signature, etc.) |
| 2 | Paraphrased or reformatted code — not exact source |
| 1 | Described in prose instead of code |
| 0 | No code snippets |

### Criterion 3: Root Cause Depth (weight: 25%)

Measures whether the research explains the underlying mechanism, not just the symptom.

| Score | Description |
|-------|-------------|
| 5 | Explains the mechanism (e.g., type coercion rules, operator behavior), language behavior, AND why it matters |
| 4 | Correct root cause with some mechanism explanation |
| 3 | Correct root cause identified but explanation is surface-level |
| 2 | Symptom described as root cause (e.g., "the function returns 404") |
| 1 | Root cause guessed without evidence |
| 0 | No root cause analysis |

### Criterion 4: Reproduction Steps Clarity (weight: 10%)

Measures whether a developer (or agent) can reproduce the bug from the research alone.

| Score | Description |
|-------|-------------|
| 5 | Numbered steps; exact command/request; expected vs. actual output shown |
| 4 | Steps present with minor gaps (e.g., missing exact curl flag) |
| 3 | Steps are vague but a developer can follow them |
| 2 | Steps incomplete or partially inaccurate |
| 1 | Steps mentioned but not usable |
| 0 | No reproduction steps |

### Criterion 5: Evidence Quality (weight: 10%)

Measures whether the research supports its claims with concrete evidence.

| Score | Description |
|-------|-------------|
| 5 | Code evidence + reproduction evidence + impact evidence all present |
| 4 | Two types of evidence present |
| 3 | One type of evidence present |
| 2 | Assertions made without evidence |
| 1 | Evidence present but contradictory or irrelevant |
| 0 | No supporting evidence |

---

## Scoring Formula

```
weighted_score = (criterion1 × 0.30)
              + (criterion2 × 0.25)
              + (criterion3 × 0.25)
              + (criterion4 × 0.10)
              + (criterion5 × 0.10)
```

**Level mapping:**

| Score | Level |
|-------|-------|
| 4.0 – 5.0 | EXCELLENT |
| 3.0 – 3.9 | GOOD |
| 2.0 – 2.9 | ACCEPTABLE |
| 1.0 – 1.9 | POOR |
| 0.0 – 0.9 | INSUFFICIENT |

---

## Output Format for Agents

When using this skill, include the following block in your output document:

```markdown
## Research Quality Assessment

**Level**: EXCELLENT
**Score**: 4.7 / 5.0

| Criterion | Weight | Score | Reasoning |
|-----------|--------|-------|-----------|
| File/Line Reference Accuracy | 30% | 5/5 | All 2 file:line references verified and correct |
| Code Snippet Completeness | 25% | 5/5 | Full function context provided, exact source match |
| Root Cause Depth | 25% | 4/5 | Mechanism explained; minor gap on JS strict equality semantics |
| Reproduction Steps Clarity | 10% | 5/5 | curl command + expected/actual output provided |
| Evidence Quality | 10% | 4/5 | Code + reproduction evidence present; impact stated |

**Reasoning**: The research accurately identifies the type mismatch root cause with precise
file:line references and working reproduction steps. One point deducted from root cause
depth as the strict equality vs. loose equality distinction could be explained more deeply.
```

---

## Minimum Bar for Downstream Use

| Level | Can Bug Planner proceed? | Can Bug Implementer proceed? |
|-------|--------------------------|------------------------------|
| EXCELLENT | Yes, immediately | Yes, immediately |
| GOOD | Yes, with minor note | Yes, with minor verification |
| ACCEPTABLE | Yes, with clarification note | Only with manual verification |
| POOR | No — must revise research | No — re-research required |
| INSUFFICIENT | No — re-research required | No — re-research required |
