/**
 * Tests for: demo-bug-fix/src/controllers/userController.js
 * Changed function(s): getUserById
 * Bug fixed: API-404 — GET /api/users/:id returns 404 for valid user IDs
 *             because string req.params.id was compared with strict equality (===)
 *             against numeric IDs in the users array. Fix: parseInt(userId, 10).
 *
 * FIRST Compliance:
 * - Fast: No real I/O or network; HTTP handled in-process via supertest
 * - Independent: beforeEach declares reset point; each test is self-contained
 * - Repeatable: Fixed in-memory test data; no randomness, no time dependency
 * - Self-validating: Every test has explicit expect() assertions with clear pass/fail
 * - Timely: Tests cover only the changed getUserById function (and the unaffected
 *            getAllUsers endpoint as a regression guard)
 */

const request = require('supertest');
const app = require('../demo-bug-fix/server');

describe('userController', () => {

  beforeEach(() => {
    // The app uses only in-memory data that is never mutated by any route,
    // so no explicit reset is needed. Declared here to satisfy FIRST/Independent.
  });

  describe('getUserById — GET /api/users/:id', () => {

    it('should return 200 and the correct user object for ID 123 (Alice Smith)', async () => {
      const res = await request(app).get('/api/users/123');
      expect(res.status).toBe(200);
      expect(res.body).toEqual({ id: 123, name: 'Alice Smith', email: 'alice@example.com' });
    });

    it('should return 200 and the correct user object for ID 456 (Bob Johnson)', async () => {
      const res = await request(app).get('/api/users/456');
      expect(res.status).toBe(200);
      expect(res.body).toEqual({ id: 456, name: 'Bob Johnson', email: 'bob@example.com' });
    });

    it('should return 200 and the correct user object for ID 789 (Charlie Brown)', async () => {
      const res = await request(app).get('/api/users/789');
      expect(res.status).toBe(200);
      expect(res.body).toEqual({ id: 789, name: 'Charlie Brown', email: 'charlie@example.com' });
    });

    it('should return 200 (not 404) when string route param "123" matches numeric ID 123 — regression for API-404', async () => {
      // Before the fix: "123" === 123 was false (strict equality); every lookup returned 404.
      // After the fix: parseInt("123", 10) === 123 is true → 200 is returned.
      const res = await request(app).get('/api/users/123');
      expect(res.status).toBe(200);
      expect(res.body.id).toBe(123);
      expect(res.body.name).toBe('Alice Smith');
    });

    it('should return 404 and an error message when user ID 999 does not exist', async () => {
      const res = await request(app).get('/api/users/999');
      expect(res.status).toBe(404);
      expect(res.body).toEqual({ error: 'User not found' });
    });

    it('should return 404 safely when a non-numeric ID "abc" is provided', async () => {
      // parseInt("abc", 10) === NaN; NaN === <any number> is always false → 404
      const res = await request(app).get('/api/users/abc');
      expect(res.status).toBe(404);
      expect(res.body).toEqual({ error: 'User not found' });
    });

  });

  describe('getAllUsers — GET /api/users (unaffected endpoint regression guard)', () => {

    it('should return 200 and an array of all three users', async () => {
      const res = await request(app).get('/api/users');
      expect(res.status).toBe(200);
      expect(Array.isArray(res.body)).toBe(true);
      expect(res.body).toHaveLength(3);
      expect(res.body).toEqual(
        expect.arrayContaining([
          { id: 123, name: 'Alice Smith',   email: 'alice@example.com'   },
          { id: 456, name: 'Bob Johnson',   email: 'bob@example.com'     },
          { id: 789, name: 'Charlie Brown', email: 'charlie@example.com' }
        ])
      );
    });

  });

});
