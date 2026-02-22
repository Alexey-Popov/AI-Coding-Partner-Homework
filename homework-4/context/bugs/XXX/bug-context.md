# Bug: [BUG-ID]

  TEMPLATE — DO NOT FILL IN THIS FILE DIRECTLY

  Steps:
  1. Copy this entire folder  (context/bugs/XXX/)  to a new folder
     named after your bug, e.g.  context/bugs/AUTH-101/
  2. Replace every [BUG-ID] placeholder with your actual bug ID.
  3. Fill in each section below and the other skeleton files in the
     same folder (implementation-plan.md, fix-summary.md, etc.)
     before running the agent pipeline.

Replace [BUG-ID] with your bug identifier, e.g. API-404, AUTH-101

**Title**: [One-line description of the bug]
**Priority**: [Critical / High / Medium / Low]
**Status**: [Open / In Progress / Resolved]
**Reporter**: [reporter@example.com]

## Description

[Describe the bug in 2–3 sentences. What is broken? Who is affected? How was it discovered?]

## Steps to Reproduce

1. [First step]
2. [Second step]
3. [Third step — what you call or trigger]
4. Observe the unexpected result

```bash
# Example command to trigger the bug
# [command here]
# Expected: [what should happen]
# Actual:   [what happens instead]
```

## Expected Behavior

- [What the system should do]
- Response format (if applicable):
```json
{
  "field": "value"
}
```

## Actual Behavior

- [What the system actually does]
- Response / error (if applicable):
```json
{
  "error": "description"
}
```

- **Affected area**: [endpoint / module / function / component]

## Additional Context

- [Any relevant history — e.g. "introduced after refactoring X"]
- [Scope — does it affect all inputs or only specific ones?]
- [Any investigation already done — e.g. "database query shows records exist"]
- [Related components that work correctly]

## Related Areas

- `[area 1]` — [WORKS / BROKEN] — [brief note]
- `[area 2]` — [WORKS / BROKEN] — [brief note]

## Impact

- **Severity**: [Critical / High / Medium / Low] — [one-line reason]
- **Affected Users**: [percentage or description, e.g. "100% of users doing X"]
- **Workaround**: [None / describe workaround if one exists]
