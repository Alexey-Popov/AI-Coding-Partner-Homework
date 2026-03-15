# Codebase Research: API-404

**Bug**: GET /api/users/:id returns 404 for valid user IDs
**Researcher**: Bug Researcher agent
**Date**: 2026-03-15

---

## Summary

The bug is a type coercion mismatch in `getUserById`. Express route parameters are always strings, but the in-memory users array contains numeric IDs. The strict equality operator `===` is used for comparison, causing all lookups to fail.

---

## Findings

### Finding 1 — Type mismatch in getUserById

**File**: `demo-bug-fix/src/controllers/userController.js`
**Lines**: 19–23

```js
const userId = req.params.id;

// BUG: req.params.id returns a string, but users array uses numeric IDs
// Strict equality (===) comparison will always fail: "123" !== 123
const user = users.find(u => u.id === userId);
```

**Claim**: `req.params.id` at line 19 assigns the raw string from the URL parameter to `userId`. At line 23, `users.find(u => u.id === userId)` uses strict equality. Since `users[0].id` is `123` (number) and `userId` is `"123"` (string), the comparison `123 === "123"` is always `false`.

---

### Finding 2 — Users array has numeric IDs

**File**: `demo-bug-fix/src/controllers/userController.js`
**Lines**: 7–11

```js
const users = [
  { id: 123, name: 'Alice Smith', email: 'alice@example.com' },
  { id: 456, name: 'Bob Johnson', email: 'bob@example.com' },
  { id: 789, name: 'Charlie Brown', email: 'charlie@example.com' }
];
```

**Claim**: User IDs are stored as numeric literals (123, 456, 789), not as strings.

---

### Finding 3 — Route definition passes :id as string

**File**: `demo-bug-fix/src/routes/users.js`
**Line**: 14

```js
router.get('/api/users/:id', userController.getUserById);
```

**Claim**: The `:id` route parameter is passed to `getUserById` via `req.params.id`, which Express always provides as a string.

---

### Finding 4 — getAllUsers works correctly (no ID comparison)

**File**: `demo-bug-fix/src/controllers/userController.js`
**Lines**: 37–39

```js
async function getAllUsers(req, res) {
  res.json(users);
}
```

**Claim**: `getAllUsers` returns the full users array without any ID comparison, which is why it works while `getUserById` does not.

---

## Call Graph

```
GET /api/users/:id
  → src/routes/users.js:14
  → src/controllers/userController.js:getUserById
  → users.find(u => u.id === req.params.id)  ← BUG HERE
```

---

## Proposed Fix

In `src/controllers/userController.js`, line 19, change:
```js
const userId = req.params.id;
```
to:
```js
const userId = parseInt(req.params.id, 10);
```

This converts the string `"123"` to the number `123` before comparison, making `u.id === userId` work correctly.

Additionally, add a guard for invalid (non-numeric) input:
```js
if (isNaN(userId)) {
  return res.status(400).json({ error: 'Invalid user ID' });
}
```
