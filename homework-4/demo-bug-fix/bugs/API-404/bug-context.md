# Bug: API-404

**Title**: GET /api/users/:id returns 404 for valid user IDs
**Priority**: High
**Status**: Open
**Reporter**: qa-team@company.com

## Description

The user API endpoint is returning 404 errors even when user IDs exist in the database. Multiple users have reported being unable to retrieve user profiles via the API.

## Steps to Reproduce

1. Start the API server: `npm start`
2. Create or verify user exists with ID `123` in the users array
3. Call `GET /api/users/123`
4. Observe 404 response

```bash
curl http://localhost:3000/api/users/123
# Expected: User object
# Actual: {"error": "User not found"} with 404 status
```

## Expected Behavior

- Should return user object with 200 status code
- Response format:
```json
{
  "id": 123,
  "name": "Alice Smith",
  "email": "alice@example.com"
}
```

## Actual Behavior

- Returns 404 Not Found
- Response:
```json
{
  "error": "User not found"
}
```

- **Endpoint**: GET /api/users/:id

## Additional Context

- The bug was introduced after recent refactoring
- Issue occurs for ALL user IDs, not just specific ones
- Direct database query shows users exist
- Other API endpoints (GET /api/users) work correctly and return user list

## Related Endpoints

- `GET /api/users` - Returns all users (WORKS correctly)
- `GET /api/users/:id` - Returns single user (BROKEN - 404)

## Impact

- **Severity**: High - blocks user profile functionality
- **Affected Users**: 100% of users trying to fetch individual profiles
- **Workaround**: None currently available
