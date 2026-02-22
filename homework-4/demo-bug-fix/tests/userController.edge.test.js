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

async function runEdgeTests() {
  // Hex input: '0x10' -> Number('0x10') === 16 -> ID 16 not present -> 404 expected
  let req = { params: { id: '0x10' } };
  let res = makeRes();
  await getUserById(req, res);
  let out = res._get();
  assert.strictEqual(out.status, 404, "Expected 404 for hex input '0x10'");

  // Leading zeros: '0123' -> Number('0123') === 123 -> should return user 123
  req = { params: { id: '0123' } };
  res = makeRes();
  await getUserById(req, res);
  out = res._get();
  assert.strictEqual(out.status, 200, "Expected 200 for '0123'");
  assert.strictEqual(out.body.id, 123);

  // Whitespace: ' 123 ' -> Number(' 123 ') === 123 -> 200
  req = { params: { id: ' 123 ' } };
  res = makeRes();
  await getUserById(req, res);
  out = res._get();
  assert.strictEqual(out.status, 200, "Expected 200 for ' 123 '");
  assert.strictEqual(out.body.id, 123);

  // Very large number beyond safe integer -> parsed and treated, expected 404 (no user)
  req = { params: { id: '9007199254740993' } };
  res = makeRes();
  await getUserById(req, res);
  out = res._get();
  // Current implementation uses Number.isInteger (not isSafeInteger), so it's treated as integer and lookup fails => 404
  assert.strictEqual(out.status, 404, 'Expected 404 for very large numeric id');

  console.log('EDGE TESTS PASSED');
}

runEdgeTests().catch(err => {
  console.error('EDGE TESTS FAILED');
  console.error(err && err.stack ? err.stack : err);
  process.exit(1);
});
