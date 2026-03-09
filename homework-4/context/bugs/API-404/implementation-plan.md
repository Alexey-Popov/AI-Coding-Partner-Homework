# Implementation Plan: Bug API-404

**Date**: 2026-03-09
**Planner**: Bug Planner
**Research Source**: research/verified-research.md (Quality: EXCELLENT)
**Bug**: GET /api/users/:id returns 404 for valid user IDs

---

## Summary

A single one-line fix in `userController.js` resolves the bug. The `req.params.id` string must be parsed to an integer before the strict equality comparison with the numeric user IDs in the `users` array.

---

## Files to Modify

| # | File | Change Type | Line(s) |
|---|------|-------------|---------|
| 1 | `demo-bug-fix/src/controllers/userController.js` | Bug fix | 23 |

No other files require modification.

---

## Change 1: Fix type mismatch in getUserById

**File**: `demo-bug-fix/src/controllers/userController.js`
**Line**: 23
**Change type**: Replace strict string comparison with integer-parsed comparison

### Before (current buggy code):

```js
const user = users.find(u => u.id === userId);
```

### After (fixed code):

```js
const user = users.find(u => u.id === parseInt(userId, 10));
```

### Why this fix:
- `req.params.id` always returns a string (Express behaviour)
- `users` array stores numeric IDs
- `"123" === 123` is `false` in JavaScript (strict equality, no coercion)
- `parseInt("123", 10)` returns `123` (number) → `123 === 123` is `true`
- `parseInt("abc", 10)` returns `NaN` → `NaN === 123` is `false` (safe for invalid input)
- The radix `10` is explicitly specified to ensure decimal parsing

---

## Test Command

After applying the change, run:

```bash
cd demo-bug-fix && npm test
```

**Expected outcome**: All tests pass, including the regression test for API-404.

If no tests exist yet, the Unit Test Generator agent will create them. However, manual verification can be done with:

```bash
# Start the server
cd demo-bug-fix && npm start

# In another terminal:
curl http://localhost:3000/api/users/123
# Expected: {"id":123,"name":"Alice Smith","email":"alice@example.com"}

curl http://localhost:3000/api/users/999
# Expected: {"error":"User not found"} with 404 status

curl http://localhost:3000/api/users
# Expected: full array of users (should still work)
```

---

## Rollback Plan

If the fix causes unexpected issues:
1. Revert line 23 to: `const user = users.find(u => u.id === userId);`
2. Investigate whether the users array contains string IDs in the actual data source
3. Consider using loose equality `==` only if both string and number IDs need to be supported

---

## Scope Boundaries

- **In scope**: Line 23 of `userController.js` only
- **Out of scope**: Route changes, middleware changes, getAllUsers function, server.js
- **Do NOT change**: `users` array data types, route definitions, response format

---

## References

- `research/verified-research.md` — verified research with EXCELLENT quality rating
- `demo-bug-fix/src/controllers/userController.js:23` — exact change location
