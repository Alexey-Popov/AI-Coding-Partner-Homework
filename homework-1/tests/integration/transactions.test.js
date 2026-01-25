const { describe, it, beforeEach, after } = require('node:test');
const assert = require('node:assert');
const request = require('supertest');
const app = require('../../src/index');
const { clearTransactions } = require('../../src/models/transaction');

describe('Transactions API Integration Tests', () => {
  beforeEach(() => {
    clearTransactions();
  });

  describe('POST /transactions', () => {
    it('should create a deposit transaction', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          toAccount: 'ACC-12345',
          amount: 1000,
          currency: 'USD',
          type: 'deposit'
        })
        .expect('Content-Type', /json/)
        .expect(201);

      assert.ok(response.body.data);
      assert.ok(response.body.data.id);
      assert.strictEqual(response.body.data.toAccount, 'ACC-12345');
      assert.strictEqual(response.body.data.amount, 1000);
      assert.strictEqual(response.body.data.currency, 'USD');
      assert.strictEqual(response.body.data.type, 'deposit');
      assert.strictEqual(response.body.data.status, 'completed');
      assert.strictEqual(response.body.message, 'Transaction created successfully');
    });

    it('should create a withdrawal transaction', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'ACC-12345',
          amount: 500,
          currency: 'EUR',
          type: 'withdrawal'
        })
        .expect(201);

      assert.strictEqual(response.body.data.fromAccount, 'ACC-12345');
      assert.strictEqual(response.body.data.type, 'withdrawal');
    });

    it('should create a transfer transaction', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890',
          amount: 250.50,
          currency: 'GBP',
          type: 'transfer'
        })
        .expect(201);

      assert.strictEqual(response.body.data.fromAccount, 'ACC-12345');
      assert.strictEqual(response.body.data.toAccount, 'ACC-67890');
      assert.strictEqual(response.body.data.amount, 250.50);
      assert.strictEqual(response.body.data.type, 'transfer');
    });

    it('should convert currency to uppercase', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          toAccount: 'ACC-12345',
          amount: 100,
          currency: 'usd',
          type: 'deposit'
        })
        .expect(201);

      assert.strictEqual(response.body.data.currency, 'USD');
    });

    it('should return 400 for missing required fields', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({})
        .expect(400);

      assert.strictEqual(response.body.error, 'Validation failed');
      assert.ok(Array.isArray(response.body.details));
      assert.ok(response.body.details.length >= 3);
    });

    it('should return 400 for invalid amount', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          toAccount: 'ACC-12345',
          amount: -100,
          currency: 'USD',
          type: 'deposit'
        })
        .expect(400);

      assert.ok(response.body.details.some(e => e.field === 'amount'));
    });

    it('should return 400 for invalid currency', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          toAccount: 'ACC-12345',
          amount: 100,
          currency: 'XYZ',
          type: 'deposit'
        })
        .expect(400);

      assert.ok(response.body.details.some(e => e.field === 'currency'));
    });

    it('should return 400 for invalid account format', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          toAccount: 'invalid-account',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        })
        .expect(400);

      assert.ok(response.body.details.some(e => e.field === 'toAccount'));
    });

    it('should return 400 for same fromAccount and toAccount in transfer', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-12345',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        })
        .expect(400);

      assert.ok(response.body.details.some(e => e.message.includes('must be different')));
    });

    it('should return 400 for amount with more than 2 decimal places', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          toAccount: 'ACC-12345',
          amount: 100.123,
          currency: 'USD',
          type: 'deposit'
        })
        .expect(400);

      assert.ok(response.body.details.some(e => e.field === 'amount'));
    });
  });

  describe('GET /transactions', () => {
    beforeEach(async () => {
      // Create test transactions
      await request(app).post('/transactions').send({
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      await request(app).post('/transactions').send({
        toAccount: 'ACC-67890',
        amount: 500,
        currency: 'EUR',
        type: 'deposit'
      });
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 100,
        currency: 'USD',
        type: 'transfer'
      });
    });

    it('should return all transactions', async () => {
      const response = await request(app)
        .get('/transactions')
        .expect('Content-Type', /json/)
        .expect(200);

      assert.ok(response.body.data);
      assert.strictEqual(response.body.data.length, 3);
    });

    it('should filter by accountId', async () => {
      const response = await request(app)
        .get('/transactions')
        .query({ accountId: 'ACC-12345' })
        .expect(200);

      assert.strictEqual(response.body.data.length, 2);
    });

    it('should filter by type', async () => {
      const response = await request(app)
        .get('/transactions')
        .query({ type: 'deposit' })
        .expect(200);

      assert.strictEqual(response.body.data.length, 2);
    });

    it('should filter by date range', async () => {
      const yesterday = new Date(Date.now() - 86400000).toISOString();
      const tomorrow = new Date(Date.now() + 86400000).toISOString();

      const response = await request(app)
        .get('/transactions')
        .query({ from: yesterday, to: tomorrow })
        .expect(200);

      assert.strictEqual(response.body.data.length, 3);
    });

    it('should combine multiple filters', async () => {
      const response = await request(app)
        .get('/transactions')
        .query({ accountId: 'ACC-12345', type: 'transfer' })
        .expect(200);

      assert.strictEqual(response.body.data.length, 1);
    });

    it('should return empty array for no matches', async () => {
      const response = await request(app)
        .get('/transactions')
        .query({ accountId: 'ACC-99999' })
        .expect(200);

      assert.deepStrictEqual(response.body.data, []);
    });
  });

  describe('GET /transactions/:id', () => {
    it('should return a transaction by ID', async () => {
      const createResponse = await request(app)
        .post('/transactions')
        .send({
          toAccount: 'ACC-12345',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });

      const id = createResponse.body.data.id;

      const response = await request(app)
        .get(`/transactions/${id}`)
        .expect('Content-Type', /json/)
        .expect(200);

      assert.strictEqual(response.body.data.id, id);
      assert.strictEqual(response.body.data.amount, 100);
    });

    it('should return 404 for non-existent transaction', async () => {
      const response = await request(app)
        .get('/transactions/nonexistent-id')
        .expect(404);

      assert.strictEqual(response.body.error, 'Transaction not found');
    });
  });

  describe('GET /transactions/export', () => {
    beforeEach(async () => {
      await request(app).post('/transactions').send({
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-12345',
        amount: 50,
        currency: 'USD',
        type: 'withdrawal'
      });
    });

    it('should export transactions as CSV', async () => {
      const response = await request(app)
        .get('/transactions/export')
        .query({ format: 'csv' })
        .expect('Content-Type', /text\/csv/)
        .expect(200);

      assert.ok(response.text.includes('id,fromAccount,toAccount,amount,currency,type,timestamp,status'));
      assert.ok(response.text.includes('ACC-12345'));
      assert.ok(response.text.includes('1000'));
    });

    it('should set Content-Disposition header for download', async () => {
      const response = await request(app)
        .get('/transactions/export')
        .query({ format: 'csv' })
        .expect(200);

      assert.ok(response.headers['content-disposition'].includes('attachment'));
      assert.ok(response.headers['content-disposition'].includes('transactions.csv'));
    });

    it('should filter exported transactions', async () => {
      const response = await request(app)
        .get('/transactions/export')
        .query({ format: 'csv', type: 'deposit' })
        .expect(200);

      const lines = response.text.split('\n');
      assert.strictEqual(lines.length, 2);  // Header + 1 deposit
    });

    it('should return 400 for invalid format', async () => {
      const response = await request(app)
        .get('/transactions/export')
        .query({ format: 'xml' })
        .expect(400);

      assert.strictEqual(response.body.error, 'Invalid format. Supported formats: csv');
    });

    it('should return 400 for missing format', async () => {
      const response = await request(app)
        .get('/transactions/export')
        .expect(400);

      assert.strictEqual(response.body.error, 'Invalid format. Supported formats: csv');
    });

    it('should return empty response for no transactions', async () => {
      clearTransactions();

      const response = await request(app)
        .get('/transactions/export')
        .query({ format: 'csv' })
        .expect(200);

      assert.strictEqual(response.text, '');
    });
  });
});
