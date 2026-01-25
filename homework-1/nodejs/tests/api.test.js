/**
 * Integration tests for API endpoints
 */

const request = require('supertest');
const app = require('../src/index');
const { resetStorage } = require('../src/transaction');

describe('API Endpoints', () => {
  beforeEach(() => {
    resetStorage();
  });

  describe('GET /health', () => {
    test('should return healthy status', async () => {
      const response = await request(app).get('/health');
      expect(response.status).toBe(200);
      expect(response.body.status).toBe('ok');
    });
  });

  describe('POST /transactions', () => {
    test('should create deposit transaction', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'ACC-BANK1',
          toAccount: 'ACC-12345',
          amount: 1000,
          currency: 'USD',
          type: 'deposit'
        });

      expect(response.status).toBe(201);
      expect(response.body.toAccount).toBe('ACC-12345');
      expect(response.body.amount).toBe(1000);
      expect(response.body.id).toBeDefined();
    });

    test('should create transfer transaction', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890',
          amount: 150.50,
          currency: 'USD',
          type: 'transfer'
        });

      expect(response.status).toBe(201);
      expect(response.body.fromAccount).toBe('ACC-12345');
      expect(response.body.toAccount).toBe('ACC-67890');
    });

    test('should return 400 for invalid account format', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'INVALID',
          toAccount: 'ACC-12345',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });

      expect(response.status).toBe(400);
      expect(response.body.error).toBe('Validation failed');
    });

    test('should return 400 for negative amount', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'ACC-BANK1',
          toAccount: 'ACC-12345',
          amount: -100,
          currency: 'USD',
          type: 'deposit'
        });

      expect(response.status).toBe(400);
    });

    test('should return 400 for invalid currency', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'ACC-BANK1',
          toAccount: 'ACC-12345',
          amount: 100,
          currency: 'INVALID',
          type: 'deposit'
        });

      expect(response.status).toBe(400);
    });

    test('should return 400 for same accounts in transfer', async () => {
      const response = await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-12345',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        });

      expect(response.status).toBe(400);
    });
  });

  describe('GET /transactions', () => {
    beforeEach(async () => {
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 200,
        currency: 'USD',
        type: 'transfer'
      });
    });

    test('should return all transactions', async () => {
      const response = await request(app).get('/transactions');
      expect(response.status).toBe(200);
      expect(response.body.length).toBe(2);
    });

    test('should filter by accountId', async () => {
      const response = await request(app).get('/transactions?accountId=ACC-12345');
      expect(response.status).toBe(200);
      expect(response.body.length).toBe(2);
    });

    test('should filter by type', async () => {
      const response = await request(app).get('/transactions?type=deposit');
      expect(response.status).toBe(200);
      expect(response.body.length).toBe(1);
      expect(response.body[0].type).toBe('deposit');
    });
  });

  describe('GET /transactions/:id', () => {
    test('should return transaction by ID', async () => {
      const createResponse = await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'ACC-BANK1',
          toAccount: 'ACC-12345',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });

      const id = createResponse.body.id;
      const response = await request(app).get(`/transactions/${id}`);

      expect(response.status).toBe(200);
      expect(response.body.id).toBe(id);
    });

    test('should return 404 for nonexistent ID', async () => {
      const response = await request(app).get('/transactions/nonexistent-id');
      expect(response.status).toBe(404);
    });
  });

  describe('GET /transactions/export', () => {
    beforeEach(async () => {
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
    });

    test('should export transactions as CSV', async () => {
      const response = await request(app).get('/transactions/export?format=csv');
      expect(response.status).toBe(200);
      expect(response.headers['content-type']).toContain('text/csv');
      expect(response.text).toContain('ACC-12345');
      expect(response.text).toContain('deposit');
    });

    test('should return 400 for invalid format', async () => {
      const response = await request(app).get('/transactions/export?format=xml');
      expect(response.status).toBe(400);
    });
  });

  describe('GET /accounts/:accountId/balance', () => {
    test('should return correct balance after deposit', async () => {
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });

      const response = await request(app).get('/accounts/ACC-12345/balance');
      expect(response.status).toBe(200);
      expect(response.body.balance).toBe(1000);
    });

    test('should return correct balance after multiple transactions', async () => {
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 300,
        currency: 'USD',
        type: 'transfer'
      });

      const response = await request(app).get('/accounts/ACC-12345/balance');
      expect(response.status).toBe(200);
      expect(response.body.balance).toBe(700);
    });
  });

  describe('GET /accounts/:accountId/summary', () => {
    test('should return correct account summary', async () => {
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-BANK1',
        amount: 200,
        currency: 'USD',
        type: 'withdrawal'
      });

      const response = await request(app).get('/accounts/ACC-12345/summary');
      expect(response.status).toBe(200);
      expect(response.body.totalDeposits).toBe(1000);
      expect(response.body.totalWithdrawals).toBe(200);
      expect(response.body.numberOfTransactions).toBe(2);
      expect(response.body.currentBalance).toBe(800);
    });
  });

  describe('404 Handler', () => {
    test('should return 404 for unknown routes', async () => {
      const response = await request(app).get('/unknown-route');
      expect(response.status).toBe(404);
    });
  });
});
