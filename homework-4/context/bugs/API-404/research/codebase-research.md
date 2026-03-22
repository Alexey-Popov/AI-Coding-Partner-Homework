# Codebase Research: Bug API-404

**Date**: 2026-03-09
**Researcher**: Bug Researcher Agent
**Bug**: GET /api/users/:id returns 404 for valid user IDs

---

## Summary

The bug is a **type mismatch** in the user lookup logic. Express route parameters are always strings, but the users array stores numeric IDs. A strict equality comparison (`===`) between a string and a number always returns `false`, causing every user lookup to fail.

---

## Root Cause

**File**: `demo-bug-fix/src/controllers/userController.js`
**Line**: 23

The `getUserById` function receives `req.params.id` as a string (e.g., `"123"`). The `users` array contains objects with numeric `id` fields (e.g., `id: 123`). The `Array.find()` uses strict equality (`===`) which does not perform type coercion, so `"123" === 123` evaluates to `false` for every element. The function never finds a matching user and always returns 404.

---

## Affected Code

### File: `demo-bug-fix/src/controllers/userController.js`

```js
// Line 7-11: users array with NUMERIC ids
const users = [
  { id: 123, name: 'Alice Smith', email: 'alice@example.com' },
  { id: 456, name: 'Bob Johnson', email: 'bob@example.com' },
  { id: 789, name: 'Charlie Brown', email: 'charlie@example.com' }
];
```

```js
// Line 19: userId is a STRING from req.params
const userId = req.params.id;

// Line 23: BUG — strict equality between string "123" and number 123 → always false
const user = users.find(u => u.id === userId);
```

### File: `demo-bug-fix/src/routes/users.js`

```js
// Line 14: Route parameter :id is passed as string by Express
router.get('/api/users/:id', userController.getUserById);
```

---

## Why GET /api/users Works

The `getAllUsers` function at line 37 simply returns the entire `users` array without any comparison:

```js
// Line 37-39: No ID comparison needed — returns all users
async function getAllUsers(req, res) {
  res.json(users);
}
```

This is why the list endpoint works but the single-user endpoint fails.

---

## Impact Analysis

- **Affected function**: `getUserById` in `demo-bug-fix/src/controllers/userController.js`
- **Affected endpoint**: `GET /api/users/:id`
- **Unaffected**: `GET /api/users` (getAllUsers), health check endpoint
- **Scope**: 100% failure rate for all user ID lookups — no user can be fetched by ID

---

## Fix Recommendation

Convert `req.params.id` to a number before comparison:

```js
// Option A: parseInt with radix (recommended)
const user = users.find(u => u.id === parseInt(userId, 10));

// Option B: Number() conversion
const user = users.find(u => u.id === Number(userId));
```

`parseInt(userId, 10)` is preferred because it:
1. Explicitly specifies base-10 parsing
2. Returns `NaN` for non-numeric input (safe — `NaN !== any_number`)
3. Is a well-understood idiom in Node.js/JavaScript

---

## References

- `demo-bug-fix/src/controllers/userController.js:7-11` — users array (numeric IDs)
- `demo-bug-fix/src/controllers/userController.js:19` — userId extracted as string
- `demo-bug-fix/src/controllers/userController.js:23` — bug location (strict equality)
- `demo-bug-fix/src/routes/users.js:14` — route registration
