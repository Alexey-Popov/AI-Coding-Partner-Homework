const request = require('supertest');
const express = require('express');
const accountRoutes = require('./accounts');
const transactionRoutes = require('./transactions');
const { resetStore } = require('../data/store');

const app = express();
app.use(express.json());
app.use('/accounts', accountRoutes);
app.use('/transactions', transactionRoutes);

describe('Account Routes', () => {
  beforeEach(() => {
    resetStore();
  });

  describe('GET /accounts/:accountId/balance', () => {
    test('returns 0 balance for account with no transactions', async () => {
      const res = await request(app).get('/accounts/ACC-12345/balance');

      expect(res.status).toBe(200);
      expect(res.body.accountId).toBe('ACC-12345');
      expect(res.body.balance).toBe(0);
      expect(res.body.currency).toBe('USD');
    });

    test('returns correct balance after deposit', async () => {
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 500,
        currency: 'USD',
        type: 'deposit'
      });

      const res = await request(app).get('/accounts/ACC-12345/balance');

      expect(res.status).toBe(200);
      expect(res.body.balance).toBe(500);
    });

    test('returns correct balance after withdrawal', async () => {
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-00000',
        amount: 300,
        currency: 'USD',
        type: 'withdrawal'
      });

      const res = await request(app).get('/accounts/ACC-12345/balance');

      expect(res.status).toBe(200);
      expect(res.body.balance).toBe(700);
    });

    test('returns correct balance after transfer (sender)', async () => {
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 250,
        currency: 'USD',
        type: 'transfer'
      });

      const res = await request(app).get('/accounts/ACC-12345/balance');

      expect(res.status).toBe(200);
      expect(res.body.balance).toBe(750);
    });

    test('returns correct balance after transfer (receiver)', async () => {
      await request(app).post('/transactions').send({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 250,
        currency: 'USD',
        type: 'transfer'
      });

      const res = await request(app).get('/accounts/ACC-67890/balance');

      expect(res.status).toBe(200);
      expect(res.body.balance).toBe(250);
    });

    test('returns 400 for invalid account format', async () => {
      const res = await request(app).get('/accounts/INVALID/balance');

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Invalid account');
      expect(res.body.message).toContain('ACC-XXXXX');
    });

    test('returns 400 for account format missing dash', async () => {
      const res = await request(app).get('/accounts/ACC12345/balance');

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Invalid account');
    });

    test('returns 400 for account format with wrong prefix', async () => {
      const res = await request(app).get('/accounts/XXX-12345/balance');

      expect(res.status).toBe(400);
      expect(res.body.error).toBe('Invalid account');
    });
  });
});
