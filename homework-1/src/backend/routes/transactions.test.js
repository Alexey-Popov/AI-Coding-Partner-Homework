const request = require('supertest');
const express = require('express');
const transactionRoutes = require('./transactions');
const { resetStore } = require('../data/store');

const app = express();
app.use(express.json());
app.use('/transactions', transactionRoutes);

describe('Transaction Routes', () => {
  beforeEach(() => {
    resetStore();
  });

  describe('POST /transactions', () => {
    const validTransaction = {
      fromAccount: 'ACC-12345',
      toAccount: 'ACC-67890',
      amount: 100.50,
      currency: 'USD',
      type: 'transfer'
    };

    test('creates a valid transaction', async () => {
      const res = await request(app)
        .post('/transactions')
        .send(validTransaction);

      expect(res.status).toBe(201);
      expect(res.body.id).toBe('TXN-000001');
      expect(res.body.fromAccount).toBe('ACC-12345');
      expect(res.body.toAccount).toBe('ACC-67890');
      expect(res.body.amount).toBe(100.50);
      expect(res.body.currency).toBe('USD');
      expect(res.body.type).toBe('transfer');
      expect(res.body.status).toBe('completed');
      expect(res.body.timestamp).toBeDefined();
    });

    test('returns 400 for invalid transaction', async () => {
      const res = await request(app)
        .post('/transactions')
        .send({ amount: -100 });

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Validation failed');
      expect(res.body.details).toBeDefined();
      expect(res.body.details.length).toBeGreaterThan(0);
    });

    test('returns 400 for negative amount', async () => {
      const res = await request(app)
        .post('/transactions')
        .send({ ...validTransaction, amount: -50 });

      expect(res.status).toBe(400);
      expect(res.body.details).toContainEqual({
        field: 'amount',
        message: 'Amount must be a positive number'
      });
    });

    test('returns 400 for invalid account format', async () => {
      const res = await request(app)
        .post('/transactions')
        .send({ ...validTransaction, fromAccount: 'INVALID' });

      expect(res.status).toBe(400);
      expect(res.body.details).toContainEqual({
        field: 'fromAccount',
        message: 'fromAccount must follow format ACC-XXXXX (X is alphanumeric)'
      });
    });

    test('returns 400 for invalid currency', async () => {
      const res = await request(app)
        .post('/transactions')
        .send({ ...validTransaction, currency: 'FAKE' });

      expect(res.status).toBe(400);
      expect(res.body.details).toContainEqual({
        field: 'currency',
        message: 'Invalid currency code. Must be a valid ISO 4217 code'
      });
    });
  });

  describe('GET /transactions', () => {
    beforeEach(async () => {
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 50,
        currency: 'EUR',
        type: 'transfer'
      });
    });

    test('returns all transactions', async () => {
      const res = await request(app).get('/transactions');

      expect(res.status).toBe(200);
      expect(res.body.length).toBe(2);
    });

    test('filters by accountId', async () => {
      const res = await request(app).get('/transactions?accountId=ACC-67890');

      expect(res.status).toBe(200);
      expect(res.body.length).toBe(1);
      expect(res.body[0].type).toBe('transfer');
    });

    test('filters by type', async () => {
      const res = await request(app).get('/transactions?type=deposit');

      expect(res.status).toBe(200);
      expect(res.body.length).toBe(1);
      expect(res.body[0].type).toBe('deposit');
    });

    test('filters by date range', async () => {
      const today = new Date().toISOString().split('T')[0];
      const res = await request(app).get(`/transactions?from=${today}&to=${today}`);

      expect(res.status).toBe(200);
      expect(res.body.length).toBe(2);
    });

    test('returns empty array when no matches', async () => {
      const res = await request(app).get('/transactions?type=withdrawal');

      expect(res.status).toBe(200);
      expect(res.body.length).toBe(0);
    });
  });

  describe('GET /transactions/:id', () => {
    beforeEach(async () => {
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
    });

    test('returns transaction by ID', async () => {
      const res = await request(app).get('/transactions/TXN-000001');

      expect(res.status).toBe(200);
      expect(res.body.id).toBe('TXN-000001');
      expect(res.body.amount).toBe(100);
    });

    test('returns 404 for non-existent ID', async () => {
      const res = await request(app).get('/transactions/TXN-999999');

      expect(res.status).toBe(404);
      expect(res.body.error).toBe('Not found');
      expect(res.body.message).toContain('TXN-999999');
    });
  });

  describe('GET /transactions/export', () => {
    beforeEach(async () => {
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 50.50,
        currency: 'EUR',
        type: 'transfer'
      });
    });

    test('exports transactions as CSV', async () => {
      const res = await request(app).get('/transactions/export?format=csv');

      expect(res.status).toBe(200);
      expect(res.headers['content-type']).toContain('text/csv');
      expect(res.headers['content-disposition']).toContain('transactions.csv');

      const lines = res.text.split('\n');
      expect(lines[0]).toBe('id,fromAccount,toAccount,amount,currency,type,timestamp,status');
      expect(lines.length).toBe(3); // header + 2 transactions
    });

    test('returns 400 for invalid format', async () => {
      const res = await request(app).get('/transactions/export?format=json');

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Invalid format');
      expect(res.body.message).toContain('CSV');
    });

    test('returns 400 when format is missing', async () => {
      const res = await request(app).get('/transactions/export');

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Invalid format');
    });
  });
});
