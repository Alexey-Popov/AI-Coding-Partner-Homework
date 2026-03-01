# Implementation Plan: Bug API-404

**Bug**: GET /api/users/:id returns 404 for valid user IDs
**Planned by**: Bug Planner
**Date**: 2026-03-01
**Research input**: `context/bugs/API-404/research/verified-research.md` (EXCELLENT quality, 4.7/5.0)

---

## Summary

A single-line change in `userController.js` converts the URL parameter string to an integer before the array lookup. The fix is minimal, targeted, and does not affect any other functionality.

---

## Change Specification

### Change 1 (the only change required)

| Field | Value |
|-------|-------|
| **File** | `demo-bug-fix/src/controllers/userController.js` |
| **Line** | 19 |
| **Function** | `getUserById` |
| **Type** | Bug fix — type conversion |

**Before (current broken code):**
```javascript
const userId = req.params.id;
```

**After (fixed code):**
```javascript
const userId = parseInt(req.params.id, 10);
```

**Why `parseInt(str, 10)`:**
- `req.params.id` is always a string (Express URL parameter behavior)
- The users array stores numeric IDs (`123`, `456`, `789`)
- `parseInt("123", 10)` → `123` (number), enabling `===` to match
- The explicit radix `10` prevents octal parsing in non-strict environments
- Alternative `Number(req.params.id)` would also work, but `parseInt` is more explicit and standard for integer IDs

**Also update the comment at lines 21–22:**

Before:
```javascript
  // BUG: req.params.id returns a string, but users array uses numeric IDs
  // Strict equality (===) comparison will always fail: "123" !== 123
```

After:
```javascript
  // FIX: parseInt converts the string URL param to a number to match numeric IDs in the array
```

---

## No Other Changes Required

The following files are NOT modified:
- `demo-bug-fix/server.js` — no changes needed
- `demo-bug-fix/src/routes/users.js` — no changes needed
- Any other files — no changes needed

The bug is fully contained within the single assignment on line 19 of `userController.js`.

---

## Test Verification

### Prerequisites
```bash
cd demo-bug-fix
npm install
```

### Test command (after Jest is added by Unit Test Generator)
```bash
npm test
```

### Manual verification
After applying the fix, start the server and test:

```bash
# Terminal 1: start server
cd demo-bug-fix && npm start

# Terminal 2: test fixed endpoint
curl http://localhost:3000/api/users/123
# Expected: {"id":123,"name":"Alice Smith","email":"alice@example.com"}

curl http://localhost:3000/api/users/456
# Expected: {"id":456,"name":"Bob Johnson","email":"bob@example.com"}

curl http://localhost:3000/api/users/789
# Expected: {"id":789,"name":"Charlie Brown","email":"charlie@example.com"}

# Verify 404 still works for non-existent users
curl http://localhost:3000/api/users/999
# Expected: {"error":"User not found"} with 404 status

# Verify list endpoint still works
curl http://localhost:3000/api/users
# Expected: array of all 3 users
```

---

## Rollback Plan

If the fix causes unexpected issues:

1. Revert line 19 to original: `const userId = req.params.id;`
2. Revert lines 21–22 comment to original BUG comment
3. Run tests to confirm rollback is clean

The rollback is a 2-line revert with no database or configuration dependencies.

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| parseInt returns NaN for non-numeric input | Low | Low (returns 404, same as before) | Acceptable; unit tests cover this case |
| Fix breaks existing getAllUsers | None | N/A | getAllUsers does not use userId |
| Side effects in other endpoints | None | N/A | Only one controller function is changed |

**Overall risk**: Low. Single-line change in a well-understood code path.

---

## References

- Verified research: `context/bugs/API-404/research/verified-research.md`
- Bug context: `demo-bug-fix/bugs/API-404/bug-context.md`
- Target file: `demo-bug-fix/src/controllers/userController.js`
