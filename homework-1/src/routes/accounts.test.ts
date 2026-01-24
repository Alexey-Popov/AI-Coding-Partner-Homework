import request from 'supertest';
import app from '../app';
import { clearStorage } from '../utils/storage';
import * as storage from '../utils/storage';

describe('Accounts API', () => {
  beforeEach(() => {
    clearStorage();
  });

  describe('GET /accounts/:accountId/balance', () => {
    describe('Error Handling', () => {
      it('should return 500 when storage throws an error', async () => {
        const mockError = new Error('Database connection failed');
        jest.spyOn(storage, 'getAccountBalance').mockImplementationOnce(() => {
          throw mockError;
        });

        const response = await request(app).get('/accounts/ACC001/balance');

        expect(response.status).toBe(500);
        expect(response.body.error).toBe('Internal server error');
        expect(response.body.message).toBe('Database connection failed');
      });

      it('should handle non-Error exceptions', async () => {
        jest.spyOn(storage, 'getAccountBalance').mockImplementationOnce(() => {
          throw 'String error';
        });

        const response = await request(app).get('/accounts/ACC001/balance');

        expect(response.status).toBe(500);
        expect(response.body.error).toBe('Internal server error');
        expect(response.body.message).toBe('Unknown error');
      });
    });

    it('should return 404 for non-existent account', async () => {
      const response = await request(app).get('/accounts/NON_EXISTENT/balance');

      expect(response.status).toBe(404);
      expect(response.body.error).toBe('Account not found');
      expect(response.body.message).toBe('Account NON_EXISTENT does not exist');
    });

    it('should return balance after deposit', async () => {
      await request(app)
        .post('/transactions')
        .send({
          toAccount: 'ACC001',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });

      const response = await request(app).get('/accounts/ACC001/balance');

      expect(response.status).toBe(200);
      expect(response.body.accountId).toBe('ACC001');
      expect(response.body.balance).toBe(100);
      expect(response.body.currency).toBe('USD');
    });

    it('should return updated balance after multiple transactions', async () => {
      await request(app)
        .post('/transactions')
        .send({
          toAccount: 'ACC001',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });

      await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'ACC001',
          amount: 30,
          currency: 'USD',
          type: 'withdrawal'
        });

      const response = await request(app).get('/accounts/ACC001/balance');

      expect(response.status).toBe(200);
      expect(response.body.balance).toBe(70);
    });

    it('should return correct balances for transfer', async () => {
      await request(app)
        .post('/transactions')
        .send({
          toAccount: 'ACC001',
          amount: 200,
          currency: 'USD',
          type: 'deposit'
        });

      await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'ACC001',
          toAccount: 'ACC002',
          amount: 75,
          currency: 'USD',
          type: 'transfer'
        });

      const response1 = await request(app).get('/accounts/ACC001/balance');
      const response2 = await request(app).get('/accounts/ACC002/balance');

      expect(response1.body.balance).toBe(125);
      expect(response2.body.balance).toBe(75);
    });

    it('should return negative balance (overdraft)', async () => {
      await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'ACC001',
          amount: 50,
          currency: 'USD',
          type: 'withdrawal'
        });

      const response = await request(app).get('/accounts/ACC001/balance');

      expect(response.status).toBe(200);
      expect(response.body.balance).toBe(-50);
    });

    it('should return zero balance after deposit and equal withdrawal', async () => {
      await request(app)
        .post('/transactions')
        .send({
          toAccount: 'ACC001',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });

      await request(app)
        .post('/transactions')
        .send({
          fromAccount: 'ACC001',
          amount: 100,
          currency: 'USD',
          type: 'withdrawal'
        });

      const response = await request(app).get('/accounts/ACC001/balance');

      expect(response.status).toBe(200);
      expect(response.body.balance).toBe(0);
    });
  });
});
