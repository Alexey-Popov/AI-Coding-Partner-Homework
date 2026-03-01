# Codebase Research: Bug API-404

**Bug**: GET /api/users/:id returns 404 for valid user IDs
**Research by**: Bug Researcher Agent
**Date**: 2026-03-01
**Status**: Complete

---

## 1. Bug Location

### Primary File
**File**: `demo-bug-fix/src/controllers/userController.js`

### Affected Lines

| Line | Code | Role |
|------|------|------|
| 19 | `const userId = req.params.id;` | Captures URL parameter as-is |
| 23 | `const user = users.find(u => u.id === userId);` | Strict equality comparison that always fails |

---

## 2. Code Snippets

### Full `getUserById` function (lines 18–30)

```javascript
async function getUserById(req, res) {
  const userId = req.params.id;

  // BUG: req.params.id returns a string, but users array uses numeric IDs
  // Strict equality (===) comparison will always fail: "123" !== 123
  const user = users.find(u => u.id === userId);

  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }

  res.json(user);
}
```

### Users array (lines 7–11)

```javascript
const users = [
  { id: 123, name: 'Alice Smith', email: 'alice@example.com' },
  { id: 456, name: 'Bob Johnson', email: 'bob@example.com' },
  { id: 789, name: 'Charlie Brown', email: 'charlie@example.com' }
];
```

---

## 3. Root Cause Analysis

### Primary cause: JavaScript type mismatch with strict equality

When Express parses a URL parameter like `/api/users/123`, the value `req.params.id` is **always a string** — in this case, the string `"123"`. This is documented Express.js behavior: all URL parameters are strings regardless of their apparent numeric content.

The `users` array stores IDs as **JavaScript numbers** (e.g., `123`, `456`, `789`), not strings.

The comparison on line 23 uses the **strict equality operator** (`===`), which checks both value AND type:

```javascript
"123" === 123  // → false (string !== number, no type coercion)
```

JavaScript's strict equality never coerces types, so `"123" === 123` is always `false`. This means `users.find()` never matches any user, `user` is always `undefined`, and the endpoint always returns a 404.

### Why `getAllUsers` works but `getUserById` does not

`getAllUsers` (line 37) simply returns the entire array without any ID comparison:
```javascript
async function getAllUsers(req, res) {
  res.json(users);
}
```
No type comparison is involved, so it works correctly.

### Why this is not caught without explicit testing

The bug exists in every call to the endpoint. There is no conditional path where the comparison succeeds. A developer testing only `GET /api/users` would see correct behavior, masking the failure in `GET /api/users/:id`.

---

## 4. Reproduction Steps

1. Navigate to the demo app directory: `cd demo-bug-fix`
2. Install dependencies: `npm install`
3. Start the server: `npm start`
4. Send request for a known valid user:
   ```bash
   curl http://localhost:3000/api/users/123
   ```
5. **Actual result** (broken):
   ```json
   {"error": "User not found"}
   ```
   HTTP status: 404
6. **Expected result** (after fix):
   ```json
   {"id": 123, "name": "Alice Smith", "email": "alice@example.com"}
   ```
   HTTP status: 200

### Confirming `getAllUsers` still works

```bash
curl http://localhost:3000/api/users
```
Returns all 3 users correctly — confirming the data exists and the issue is isolated to the ID comparison.

---

## 5. Impact Analysis

| Dimension | Assessment |
|-----------|------------|
| **Severity** | High |
| **Scope** | 100% of `GET /api/users/:id` requests fail |
| **Users affected** | All users trying to fetch individual profiles |
| **Workaround** | None — no way to fetch a single user via the API |
| **Data integrity** | Not affected — data is intact, only lookup is broken |
| **Related endpoints** | `GET /api/users` is unaffected |

---

## 6. References

- Bug report: `demo-bug-fix/bugs/API-404/bug-context.md`
- Affected controller: `demo-bug-fix/src/controllers/userController.js`
- Route definition: `demo-bug-fix/src/routes/users.js`
- Express.js documentation: URL params are always strings (req.params type)
