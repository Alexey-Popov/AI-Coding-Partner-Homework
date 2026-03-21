# Implementation Plan: API-404

**Bug**: GET /api/users/:id returns 404 for valid user IDs
**Planner**: Bug Planner agent
**Based on**: `context/bugs/API-404/research/verified-research.md` (PASS — EXCELLENT quality)
**Date**: 2026-03-15

---

## Root Cause

In `src/controllers/userController.js`, `req.params.id` returns a string (e.g., `"123"`), but the users array uses numeric IDs (`123`). The strict equality check `u.id === userId` always evaluates to `false` because `"123" !== 123`.

---

## Changes

### Change 1 — Convert route param to integer and add NaN guard

**File**: `demo-bug-fix/src/controllers/userController.js`
**Function**: `getUserById`

**Before** (lines 18–23):
```js
async function getUserById(req, res) {
  const userId = req.params.id;

  // BUG: req.params.id returns a string, but users array uses numeric IDs
  // Strict equality (===) comparison will always fail: "123" !== 123
  const user = users.find(u => u.id === userId);
```

**After**:
```js
async function getUserById(req, res) {
  const userId = parseInt(req.params.id, 10);

  if (isNaN(userId)) {
    return res.status(400).json({ error: 'Invalid user ID' });
  }

  const user = users.find(u => u.id === userId);
```

**Why**: `parseInt(req.params.id, 10)` converts the string `"123"` to the number `123`, making the strict equality check work. The `isNaN` guard handles non-numeric inputs like `/api/users/abc`.

---

## No Other Files Need Changes

- `src/routes/users.js` — route definition is correct, no change needed
- `server.js` — server setup is correct, no change needed

---

## Test Command

After applying the change, run:
```bash
cd demo-bug-fix && npm test
```

If no test framework is installed, first run:
```bash
cd demo-bug-fix && npm install --save-dev jest && npm test
```

Expected: all tests pass.

---

## Manual Verification

After applying the fix:
```bash
# Start server
cd demo-bug-fix && npm start

# In another terminal:
curl http://localhost:3000/api/users/123
# Expected: {"id":123,"name":"Alice Smith","email":"alice@example.com"}

curl http://localhost:3000/api/users/999
# Expected: {"error":"User not found"} with HTTP 404

curl http://localhost:3000/api/users/abc
# Expected: {"error":"Invalid user ID"} with HTTP 400
```

---

## Scope Constraints

- Apply ONLY the change described above.
- Do not refactor `getAllUsers` or any other function.
- Do not change the users array or data structure.
- Do not modify routes, server setup, or middleware.
