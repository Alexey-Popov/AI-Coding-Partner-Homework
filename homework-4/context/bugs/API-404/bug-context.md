# Bug: API-404

**Title**: GET /api/users/:id returns 404 for valid user IDs
**Priority**: High
**Status**: Fixed
**Reporter**: qa-team@company.com

## Description

The user API endpoint was returning 404 errors even when user IDs exist in the database. Multiple users reported being unable to retrieve user profiles via the API.

## Steps to Reproduce

1. Start the API server: `npm start`
2. Call `GET /api/users/123`
3. Observe 404 response even though user 123 exists

```bash
curl http://localhost:3000/api/users/123
# Expected: User object
# Actual: {"error": "User not found"} with 404 status
```

## Root Cause

`req.params.id` returns a string (e.g., `"123"`), but the in-memory users array stores numeric IDs (e.g., `123`). The strict equality check `u.id === userId` always fails because `"123" !== 123`. The fix converts the string param to an integer using `parseInt(req.params.id, 10)`.

## Fix

Applied in `demo-bug-fix/src/controllers/userController.js:19`:
- Before: `const userId = req.params.id;`
- After: `const userId = parseInt(req.params.id, 10);`
- Added NaN guard for invalid (non-numeric) IDs

## Impact

- **Severity**: High — blocked all individual user profile lookups
- **Affected Users**: 100% of users trying to fetch individual profiles
- **Fixed**: Yes
