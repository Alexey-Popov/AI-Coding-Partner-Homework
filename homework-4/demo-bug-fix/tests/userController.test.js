const assert = require('assert');
const { getUserById } = require('../src/controllers/userController');

function makeRes() {
  let status = 200;
  let body = null;
  return {
    status(code) {
      status = code;
      return this;
    },
    json(obj) {
      body = obj;
      this._status = status;
      this._body = body;
      return this;
    },
    _get() {
      return { status, body };
    }
  };
}

async function runTests() {
  // Test 1: existing user 123
  let req = { params: { id: '123' } };
  let res = makeRes();
  await getUserById(req, res);
  let out = res._get();
  assert.strictEqual(out.status, 200, 'Expected status 200 for id=123');
  assert.strictEqual(out.body.id, 123, 'Expected returned user id 123');

  // Test 2: non-existent numeric id 999 -> 404
  req = { params: { id: '999' } };
  res = makeRes();
  await getUserById(req, res);
  out = res._get();
  assert.strictEqual(out.status, 404, 'Expected status 404 for id=999');
  assert.deepStrictEqual(out.body, { error: 'User not found' });

  // Test 3: invalid id -> 400
  req = { params: { id: 'abc' } };
  res = makeRes();
  await getUserById(req, res);
  out = res._get();
  assert.strictEqual(out.status, 400, 'Expected status 400 for invalid id');
  assert.deepStrictEqual(out.body, { error: 'Invalid user id' });

  console.log('ALL TESTS PASSED');
}

runTests().catch(err => {
  console.error('TESTS FAILED');
  console.error(err && err.stack ? err.stack : err);
  process.exit(1);
});
