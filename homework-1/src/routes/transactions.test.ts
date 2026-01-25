import request from 'supertest';
import app from '../app';
import { clearStorage } from '../utils/storage';
import * as storage from '../utils/storage';

describe('Transactions API', () => {
  beforeEach(() => {
    clearStorage();
  });

  describe('POST /transactions', () => {
    describe('Validation', () => {
      it('should reject transaction without amount', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            toAccount: 'ACC001',
            currency: 'USD',
            type: 'deposit'
          });

        expect(response.status).toBe(400);
        expect(response.body.error).toBe('Validation failed');
        expect(response.body.details).toContainEqual({
          field: 'amount',
          message: 'Amount must be a positive number'
        });
      });

      it('should reject transaction with zero amount', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            toAccount: 'ACC001',
            amount: 0,
            currency: 'USD',
            type: 'deposit'
          });

        expect(response.status).toBe(400);
        expect(response.body.details).toContainEqual({
          field: 'amount',
          message: 'Amount must be a positive number'
        });
      });

      it('should reject transaction with negative amount', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            toAccount: 'ACC001',
            amount: -100,
            currency: 'USD',
            type: 'deposit'
          });

        expect(response.status).toBe(400);
        expect(response.body.details).toContainEqual({
          field: 'amount',
          message: 'Amount must be a positive number'
        });
      });

      it('should reject transaction with non-numeric amount', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            toAccount: 'ACC001',
            amount: 'one hundred',
            currency: 'USD',
            type: 'deposit'
          });

        expect(response.status).toBe(400);
        expect(response.body.details).toContainEqual({
          field: 'amount',
          message: 'Amount must be a positive number'
        });
      });

      it('should reject transaction without currency', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            toAccount: 'ACC001',
            amount: 100,
            type: 'deposit'
          });

        expect(response.status).toBe(400);
        expect(response.body.details).toContainEqual({
          field: 'currency',
          message: 'Currency is required'
        });
      });

      it('should reject transaction without type', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            toAccount: 'ACC001',
            amount: 100,
            currency: 'USD'
          });

        expect(response.status).toBe(400);
        expect(response.body.details).toContainEqual({
          field: 'type',
          message: 'Type must be deposit, withdrawal, or transfer'
        });
      });

      it('should reject transaction with invalid type', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            toAccount: 'ACC001',
            amount: 100,
            currency: 'USD',
            type: 'invalid'
          });

        expect(response.status).toBe(400);
        expect(response.body.details).toContainEqual({
          field: 'type',
          message: 'Type must be deposit, withdrawal, or transfer'
        });
      });

      it('should reject deposit without toAccount', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            amount: 100,
            currency: 'USD',
            type: 'deposit'
          });

        expect(response.status).toBe(400);
        expect(response.body.details).toContainEqual({
          field: 'toAccount',
          message: 'toAccount is required for deposits'
        });
      });

      it('should reject withdrawal without fromAccount', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            amount: 100,
            currency: 'USD',
            type: 'withdrawal'
          });

        expect(response.status).toBe(400);
        expect(response.body.details).toContainEqual({
          field: 'fromAccount',
          message: 'fromAccount is required for withdrawals'
        });
      });

      it('should reject transfer without fromAccount', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            toAccount: 'ACC002',
            amount: 100,
            currency: 'USD',
            type: 'transfer'
          });

        expect(response.status).toBe(400);
        expect(response.body.details).toContainEqual({
          field: 'accounts',
          message: 'Both fromAccount and toAccount are required for transfers'
        });
      });

      it('should reject transfer without toAccount', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            fromAccount: 'ACC001',
            amount: 100,
            currency: 'USD',
            type: 'transfer'
          });

        expect(response.status).toBe(400);
        expect(response.body.details).toContainEqual({
          field: 'accounts',
          message: 'Both fromAccount and toAccount are required for transfers'
        });
      });

      it('should return multiple validation errors', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({});

        expect(response.status).toBe(400);
        expect(response.body.details.length).toBeGreaterThan(1);
      });
    });

    describe('Success Cases', () => {
      it('should create a deposit transaction', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            toAccount: 'ACC001',
            amount: 100,
            currency: 'USD',
            type: 'deposit'
          });

        expect(response.status).toBe(201);
        expect(response.body.id).toBeDefined();
        expect(response.body.amount).toBe(100);
        expect(response.body.type).toBe('deposit');
        expect(response.body.status).toBe('completed');
        expect(response.body.timestamp).toBeDefined();
      });

      it('should create a withdrawal transaction', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            fromAccount: 'ACC001',
            amount: 50,
            currency: 'EUR',
            type: 'withdrawal'
          });

        expect(response.status).toBe(201);
        expect(response.body.type).toBe('withdrawal');
        expect(response.body.fromAccount).toBe('ACC001');
      });

      it('should create a transfer transaction', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            fromAccount: 'ACC001',
            toAccount: 'ACC002',
            amount: 200,
            currency: 'USD',
            type: 'transfer'
          });

        expect(response.status).toBe(201);
        expect(response.body.type).toBe('transfer');
        expect(response.body.fromAccount).toBe('ACC001');
        expect(response.body.toAccount).toBe('ACC002');
      });

      it('should include description when provided', async () => {
        const response = await request(app)
          .post('/transactions')
          .send({
            toAccount: 'ACC001',
            amount: 100,
            currency: 'USD',
            type: 'deposit',
            description: 'Monthly salary'
          });

        expect(response.status).toBe(201);
        expect(response.body.description).toBe('Monthly salary');
      });
    });
  });

  describe('GET /transactions', () => {
    it('should return empty array when no transactions exist', async () => {
      const response = await request(app).get('/transactions');

      expect(response.status).toBe(200);
      expect(response.body).toEqual([]);
    });

    it('should return all created transactions', async () => {
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
          fromAccount: 'ACC002',
          amount: 50,
          currency: 'EUR',
          type: 'withdrawal'
        });

      const response = await request(app).get('/transactions');

      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(2);
    });

    describe('Filtering', () => {
      beforeEach(async () => {
        await request(app).post('/transactions').send({
          toAccount: 'ACC001',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });
        await request(app).post('/transactions').send({
          fromAccount: 'ACC001',
          amount: 50,
          currency: 'USD',
          type: 'withdrawal'
        });
        await request(app).post('/transactions').send({
          fromAccount: 'ACC002',
          toAccount: 'ACC003',
          amount: 200,
          currency: 'EUR',
          type: 'transfer'
        });
      });

      it('should filter by accountId (toAccount match)', async () => {
        const response = await request(app).get('/transactions?accountId=ACC001');

        expect(response.status).toBe(200);
        expect(response.body).toHaveLength(2);
        response.body.forEach((tx: any) => {
          expect(tx.toAccount === 'ACC001' || tx.fromAccount === 'ACC001').toBe(true);
        });
      });

      it('should filter by accountId (fromAccount match)', async () => {
        const response = await request(app).get('/transactions?accountId=ACC002');

        expect(response.status).toBe(200);
        expect(response.body).toHaveLength(1);
        expect(response.body[0].fromAccount).toBe('ACC002');
      });

      it('should filter by type', async () => {
        const response = await request(app).get('/transactions?type=deposit');

        expect(response.status).toBe(200);
        expect(response.body).toHaveLength(1);
        expect(response.body[0].type).toBe('deposit');
      });

      it('should filter by type transfer', async () => {
        const response = await request(app).get('/transactions?type=transfer');

        expect(response.status).toBe(200);
        expect(response.body).toHaveLength(1);
        expect(response.body[0].type).toBe('transfer');
      });

      it('should combine accountId and type filters', async () => {
        const response = await request(app).get('/transactions?accountId=ACC001&type=deposit');

        expect(response.status).toBe(200);
        expect(response.body).toHaveLength(1);
        expect(response.body[0].type).toBe('deposit');
        expect(response.body[0].toAccount).toBe('ACC001');
      });

      it('should return empty array when no matches', async () => {
        const response = await request(app).get('/transactions?accountId=NONEXISTENT');

        expect(response.status).toBe(200);
        expect(response.body).toEqual([]);
      });

      it('should filter by date range (from)', async () => {
        const yesterday = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        const fromDate = yesterday.toISOString().split('T')[0];

        const response = await request(app).get(`/transactions?from=${fromDate}`);

        expect(response.status).toBe(200);
        expect(response.body).toHaveLength(3);
      });

      it('should filter by date range (to)', async () => {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        const toDate = tomorrow.toISOString().split('T')[0];

        const response = await request(app).get(`/transactions?to=${toDate}`);

        expect(response.status).toBe(200);
        expect(response.body).toHaveLength(3);
      });

      it('should filter by date range excluding future transactions', async () => {
        const yesterday = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        const toDate = yesterday.toISOString().split('T')[0];

        const response = await request(app).get(`/transactions?to=${toDate}`);

        expect(response.status).toBe(200);
        expect(response.body).toHaveLength(0);
      });

      it('should combine all filters', async () => {
        const yesterday = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);

        const response = await request(app).get(
          `/transactions?accountId=ACC001&type=deposit&from=${yesterday.toISOString().split('T')[0]}&to=${tomorrow.toISOString().split('T')[0]}`
        );

        expect(response.status).toBe(200);
        expect(response.body).toHaveLength(1);
        expect(response.body[0].type).toBe('deposit');
      });
    });
  });

  describe('GET /transactions/:id', () => {
    it('should return 404 for non-existent transaction', async () => {
      const response = await request(app).get('/transactions/non-existent-id');

      expect(response.status).toBe(404);
      expect(response.body.error).toBe('Transaction non-existent-id not found');
    });

    it('should return the correct transaction by id', async () => {
      const createResponse = await request(app)
        .post('/transactions')
        .send({
          toAccount: 'ACC001',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });

      const transactionId = createResponse.body.id;

      const response = await request(app).get(`/transactions/${transactionId}`);

      expect(response.status).toBe(200);
      expect(response.body.id).toBe(transactionId);
      expect(response.body.amount).toBe(100);
    });
  });

  describe('Error Handling', () => {
    describe('POST /transactions errors', () => {
      it('should return 500 when createTransaction throws an error', async () => {
        jest.spyOn(storage, 'createTransaction').mockImplementationOnce(() => {
          throw new Error('Storage failure');
        });

        const response = await request(app)
          .post('/transactions')
          .send({
            toAccount: 'ACC001',
            amount: 100,
            currency: 'USD',
            type: 'deposit'
          });

        expect(response.status).toBe(500);
        expect(response.body.error).toBe('Internal server error');
        expect(response.body.message).toBe('Storage failure');
      });

      it('should handle non-Error exceptions in POST', async () => {
        jest.spyOn(storage, 'createTransaction').mockImplementationOnce(() => {
          throw 'String error';
        });

        const response = await request(app)
          .post('/transactions')
          .send({
            toAccount: 'ACC001',
            amount: 100,
            currency: 'USD',
            type: 'deposit'
          });

        expect(response.status).toBe(500);
        expect(response.body.message).toBe('Unknown error');
      });
    });

    describe('GET /transactions errors', () => {
      it('should return 500 when getAllTransactions throws an error', async () => {
        jest.spyOn(storage, 'getAllTransactions').mockImplementationOnce(() => {
          throw new Error('Database error');
        });

        const response = await request(app).get('/transactions');

        expect(response.status).toBe(500);
        expect(response.body.error).toBe('Internal server error');
        expect(response.body.message).toBe('Database error');
      });

      it('should handle non-Error exceptions in GET all', async () => {
        jest.spyOn(storage, 'getAllTransactions').mockImplementationOnce(() => {
          throw null;
        });

        const response = await request(app).get('/transactions');

        expect(response.status).toBe(500);
        expect(response.body.message).toBe('Unknown error');
      });
    });

    describe('GET /transactions/:id errors', () => {
      it('should return 500 when getTransactionById throws an error', async () => {
        jest.spyOn(storage, 'getTransactionById').mockImplementationOnce(() => {
          throw new Error('Query failed');
        });

        const response = await request(app).get('/transactions/some-id');

        expect(response.status).toBe(500);
        expect(response.body.error).toBe('Internal server error');
        expect(response.body.message).toBe('Query failed');
      });

      it('should handle non-Error exceptions in GET by id', async () => {
        jest.spyOn(storage, 'getTransactionById').mockImplementationOnce(() => {
          throw undefined;
        });

        const response = await request(app).get('/transactions/some-id');

        expect(response.status).toBe(500);
        expect(response.body.message).toBe('Unknown error');
      });
    });
  });
});
