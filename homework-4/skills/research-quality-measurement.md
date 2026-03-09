# Skill: Research Quality Measurement

## Purpose

This skill defines a standardized framework for measuring and labeling the quality of bug research output. It must be used by the **Bug Research Verifier** agent when producing `verified-research.md`.

---

## Quality Levels

### EXCELLENT
- All file:line references are accurate and verifiable
- All code snippets exactly match source files
- Root cause is precisely identified with supporting evidence
- No false claims or hallucinated references
- Every claim has a corresponding file location
- Impact and affected code paths are fully traced

### GOOD
- ≥ 90% of file:line references are accurate
- Minor discrepancies in snippets (whitespace, comment differences) — no logic errors
- Root cause is correctly identified
- At most 1 unverified claim that is still plausible
- Impact scope is mostly correct

### ACCEPTABLE
- 70–89% of file:line references are accurate
- Some snippets differ from source but core logic is represented
- Root cause hypothesis is directionally correct but may be incomplete
- 2–4 unverified or imprecise claims
- Impact scope partially described

### POOR
- < 70% of file:line references are accurate
- Multiple snippets do not match source code
- Root cause is partially wrong or missing
- More than 4 unverified claims
- Significant gaps in impact analysis

### INSUFFICIENT
- References cannot be verified (files/lines don't exist)
- Fabricated code snippets not present in codebase
- Root cause is wrong or absent
- Research is not usable for implementation planning

---

## Scoring Criteria (Checklist)

| Criterion | Weight | Description |
|-----------|--------|-------------|
| Reference Accuracy | 30% | Percentage of file:line refs that are exact |
| Snippet Fidelity | 25% | Code snippets match actual source |
| Root Cause Correctness | 25% | Root cause is identified and accurate |
| Completeness | 10% | All relevant code paths covered |
| Clarity | 10% | Research is understandable and well-structured |

---

## How to Apply This Skill

1. For each claim in the research, locate the cited file and line.
2. Compare the cited snippet with the actual file content.
3. Mark each reference as: `VERIFIED` | `PARTIAL` | `INCORRECT` | `NOT FOUND`
4. Count verified vs total references.
5. Assess root cause against actual code behavior.
6. Map scores against criteria above.
7. Assign final quality label: EXCELLENT / GOOD / ACCEPTABLE / POOR / INSUFFICIENT

---

## Output Format in verified-research.md

```
## Research Quality Assessment

**Quality Level**: [EXCELLENT | GOOD | ACCEPTABLE | POOR | INSUFFICIENT]

**Score Breakdown**:
- Reference Accuracy: X/Y references verified (Z%)
- Snippet Fidelity: X/Y snippets match (Z%)
- Root Cause Correctness: [Correct / Partial / Incorrect]
- Completeness: [Full / Partial / Incomplete]
- Clarity: [Clear / Adequate / Unclear]

**Reasoning**: [2–4 sentences explaining the quality label]
```
