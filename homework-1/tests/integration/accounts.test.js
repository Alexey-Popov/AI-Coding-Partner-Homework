import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert';
import request from 'supertest';
import app from '../../src/index.js';
import { clearTransactions, createTransaction } from '../../src/models/transaction.js';

describe('Accounts API Integration Tests', () => {
  beforeEach(() => {
    clearTransactions();
  });

  describe('GET /accounts/:accountId/balance', () => {
    it('should return balance for account with transactions', async () => {
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });

      const response = await request(app)
        .get('/accounts/ACC-12345/balance')
        .expect('Content-Type', /json/)
        .expect(200);

      assert.ok(response.body.data);
      assert.strictEqual(response.body.data.accountId, 'ACC-12345');
      assert.strictEqual(response.body.data.balance, 1000);
      assert.strictEqual(response.body.data.currency, 'USD');
      assert.strictEqual(response.body.data.transactionCount, 1);
    });

    it('should return zero balance for account with no transactions', async () => {
      const response = await request(app)
        .get('/accounts/ACC-00000/balance')
        .expect(200);

      assert.strictEqual(response.body.data.balance, 0);
      assert.strictEqual(response.body.data.transactionCount, 0);
    });

    it('should calculate balance correctly with multiple transactions', async () => {
      // Deposit 1000
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      // Withdraw 200
      createTransaction({
        fromAccount: 'ACC-12345',
        amount: 200,
        currency: 'USD',
        type: 'withdrawal'
      });
      // Transfer out 100
      createTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 100,
        currency: 'USD',
        type: 'transfer'
      });
      // Transfer in 50
      createTransaction({
        fromAccount: 'ACC-67890',
        toAccount: 'ACC-12345',
        amount: 50,
        currency: 'USD',
        type: 'transfer'
      });

      const response = await request(app)
        .get('/accounts/ACC-12345/balance')
        .expect(200);

      // 1000 - 200 - 100 + 50 = 750
      assert.strictEqual(response.body.data.balance, 750);
      assert.strictEqual(response.body.data.transactionCount, 4);
    });

    it('should return 400 for invalid account ID format', async () => {
      const response = await request(app)
        .get('/accounts/invalid-account/balance')
        .expect(400);

      assert.strictEqual(response.body.error, 'Invalid account ID format. Expected format: ACC-XXXXX (5 alphanumeric characters)');
    });

    it('should return 400 for account ID with wrong prefix', async () => {
      const response = await request(app)
        .get('/accounts/ACX-12345/balance')
        .expect(400);

      assert.ok(response.body.error.includes('Invalid account ID format'));
    });

    it('should return 400 for account ID with wrong length', async () => {
      const response = await request(app)
        .get('/accounts/ACC-1234/balance')
        .expect(400);

      assert.ok(response.body.error.includes('Invalid account ID format'));
    });

    it('should only include completed transactions in balance', async () => {
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit',
        status: 'completed'
      });
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 500,
        currency: 'USD',
        type: 'deposit',
        status: 'pending'
      });

      const response = await request(app)
        .get('/accounts/ACC-12345/balance')
        .expect(200);

      assert.strictEqual(response.body.data.balance, 1000);
      assert.strictEqual(response.body.data.transactionCount, 1);
    });
  });
});

describe('General API Integration Tests', () => {
  describe('GET /', () => {
    it('should return API information', async () => {
      const response = await request(app)
        .get('/')
        .expect('Content-Type', /json/)
        .expect(200);

      assert.strictEqual(response.body.message, 'Banking Transactions API');
      assert.strictEqual(response.body.version, '1.0.0');
      assert.ok(response.body.endpoints);
      assert.ok(response.body.endpoints.transactions);
      assert.ok(response.body.endpoints.accounts);
      assert.ok(response.body.endpoints.health);
    });
  });

  describe('GET /health', () => {
    it('should return health status', async () => {
      const response = await request(app)
        .get('/health')
        .expect('Content-Type', /json/)
        .expect(200);

      assert.strictEqual(response.body.status, 'ok');
      assert.ok(response.body.timestamp);
    });

    it('should return valid ISO timestamp', async () => {
      const response = await request(app)
        .get('/health')
        .expect(200);

      const timestamp = new Date(response.body.timestamp);
      assert.ok(!isNaN(timestamp.getTime()));
    });
  });

  describe('404 Handler', () => {
    it('should return 404 for unknown routes', async () => {
      const response = await request(app)
        .get('/unknown-route')
        .expect(404);

      assert.strictEqual(response.body.error, 'Not found');
    });

    it('should return 404 for unknown POST routes', async () => {
      const response = await request(app)
        .post('/unknown-route')
        .send({ data: 'test' })
        .expect(404);

      assert.strictEqual(response.body.error, 'Not found');
    });
  });
});
