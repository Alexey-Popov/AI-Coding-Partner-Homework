import {
  createTransaction,
  getAllTransactions,
  getTransactionById,
  getAccountBalance,
  clearStorage
} from './storage';
import { CreateTransactionInput } from '../models';

describe('Storage - Business Logic', () => {
  beforeEach(() => {
    clearStorage();
  });

  describe('createTransaction', () => {
    it('should create a deposit transaction with generated id and timestamp', () => {
      const input: CreateTransactionInput = {
        toAccount: 'ACC-7H9K2',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      };

      const result = createTransaction(input);

      expect(result.id).toBeDefined();
      expect(result.id).toMatch(/^[0-9a-f-]{36}$/); // UUID format
      expect(result.timestamp).toBeDefined();
      expect(result.status).toBe('completed');
      expect(result.amount).toBe(100);
      expect(result.currency).toBe('USD');
      expect(result.type).toBe('deposit');
      expect(result.toAccount).toBe('ACC-7H9K2');
    });

    it('should create a withdrawal transaction', () => {
      const input: CreateTransactionInput = {
        fromAccount: 'ACC-3M8N5',
        amount: 50,
        currency: 'EUR',
        type: 'withdrawal'
      };

      const result = createTransaction(input);

      expect(result.id).toBeDefined();
      expect(result.status).toBe('completed');
      expect(result.amount).toBe(50);
      expect(result.type).toBe('withdrawal');
      expect(result.fromAccount).toBe('ACC-3M8N5');
    });

    it('should create a transfer transaction', () => {
      const input: CreateTransactionInput = {
        fromAccount: 'ACC-5Q7R4',
        toAccount: 'ACC-9T2W6',
        amount: 200,
        currency: 'USD',
        type: 'transfer'
      };

      const result = createTransaction(input);

      expect(result.id).toBeDefined();
      expect(result.status).toBe('completed');
      expect(result.type).toBe('transfer');
      expect(result.fromAccount).toBe('ACC-5Q7R4');
      expect(result.toAccount).toBe('ACC-9T2W6');
    });

    it('should include optional description if provided', () => {
      const input: CreateTransactionInput = {
        toAccount: 'ACC-1B4C8',
        amount: 100,
        currency: 'USD',
        type: 'deposit',
        description: 'Monthly salary'
      };

      const result = createTransaction(input);

      expect(result.description).toBe('Monthly salary');
    });

    it('should store the transaction and make it retrievable', () => {
      const input: CreateTransactionInput = {
        toAccount: 'ACC-6P8S3',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      };

      const created = createTransaction(input);
      const retrieved = getTransactionById(created.id);

      expect(retrieved).toEqual(created);
    });
  });

  describe('getAllTransactions', () => {
    it('should return empty array when no transactions exist', () => {
      const result = getAllTransactions();
      expect(result).toEqual([]);
    });

    it('should return all created transactions', () => {
      createTransaction({
        toAccount: 'ACC-2D7E9',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
      createTransaction({
        fromAccount: 'ACC-4L6M1',
        amount: 50,
        currency: 'USD',
        type: 'withdrawal'
      });

      const result = getAllTransactions();

      expect(result).toHaveLength(2);
    });

    it('should return a copy to prevent external modifications', () => {
      createTransaction({
        toAccount: 'ACC-8V3X7',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      const result = getAllTransactions();
      result.push({} as any);

      expect(getAllTransactions()).toHaveLength(1);
    });
  });

  describe('getTransactionById', () => {
    it('should return undefined for non-existent transaction', () => {
      const result = getTransactionById('non-existent-id');
      expect(result).toBeUndefined();
    });

    it('should return the correct transaction by id', () => {
      const tx1 = createTransaction({
        toAccount: 'ACC-9Y5Z2',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
      const tx2 = createTransaction({
        toAccount: 'ACC-1A4B6',
        amount: 200,
        currency: 'EUR',
        type: 'deposit'
      });

      expect(getTransactionById(tx1.id)).toEqual(tx1);
      expect(getTransactionById(tx2.id)).toEqual(tx2);
    });
  });

  describe('Account Balance Updates', () => {
    describe('Deposits', () => {
      it('should increase account balance on deposit', () => {
        createTransaction({
          toAccount: 'ACC-3C7D8',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });

        expect(getAccountBalance('ACC-3C7D8')).toBe(100);
      });

      it('should accumulate multiple deposits', () => {
        createTransaction({
          toAccount: 'ACC-5E9F1',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });
        createTransaction({
          toAccount: 'ACC-5E9F1',
          amount: 50,
          currency: 'USD',
          type: 'deposit'
        });

        expect(getAccountBalance('ACC-5E9F1')).toBe(150);
      });

      it('should handle deposits to different accounts independently', () => {
        createTransaction({
          toAccount: 'ACC-7G2H4',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });
        createTransaction({
          toAccount: 'ACC-9J6K3',
          amount: 200,
          currency: 'USD',
          type: 'deposit'
        });

        expect(getAccountBalance('ACC-7G2H4')).toBe(100);
        expect(getAccountBalance('ACC-9J6K3')).toBe(200);
      });
    });

    describe('Withdrawals', () => {
      it('should decrease account balance on withdrawal', () => {
        createTransaction({
          toAccount: 'ACC-1L5M7',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });
        createTransaction({
          fromAccount: 'ACC-1L5M7',
          amount: 30,
          currency: 'USD',
          type: 'withdrawal'
        });

        expect(getAccountBalance('ACC-1L5M7')).toBe(70);
      });

      it('should allow negative balance (overdraft)', () => {
        createTransaction({
          fromAccount: 'ACC-3N8P2',
          amount: 50,
          currency: 'USD',
          type: 'withdrawal'
        });

        expect(getAccountBalance('ACC-3N8P2')).toBe(-50);
      });
    });

    describe('Transfers', () => {
      it('should decrease source account and increase destination account', () => {
        createTransaction({
          toAccount: 'ACC-5Q9R6',
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });
        createTransaction({
          fromAccount: 'ACC-5Q9R6',
          toAccount: 'ACC-7S4T1',
          amount: 40,
          currency: 'USD',
          type: 'transfer'
        });

        expect(getAccountBalance('ACC-5Q9R6')).toBe(60);
        expect(getAccountBalance('ACC-7S4T1')).toBe(40);
      });

      it('should handle transfer between two new accounts', () => {
        createTransaction({
          fromAccount: 'ACC-9U3V8',
          toAccount: 'ACC-2W7X5',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        });

        expect(getAccountBalance('ACC-9U3V8')).toBe(-100);
        expect(getAccountBalance('ACC-2W7X5')).toBe(100);
      });
    });
  });

  describe('getAccountBalance', () => {
    it('should return null for non-existent account', () => {
      expect(getAccountBalance('NON_EXISTENT')).toBeNull();
    });

    it('should return 0 for account with zero balance', () => {
      createTransaction({
        toAccount: 'ACC-4Y9Z1',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
      createTransaction({
        fromAccount: 'ACC-4Y9Z1',
        amount: 100,
        currency: 'USD',
        type: 'withdrawal'
      });

      expect(getAccountBalance('ACC-4Y9Z1')).toBe(0);
    });

    it('should handle decimal amounts correctly', () => {
      createTransaction({
        toAccount: 'ACC-6A2B4',
        amount: 100.50,
        currency: 'USD',
        type: 'deposit'
      });
      createTransaction({
        fromAccount: 'ACC-6A2B4',
        amount: 25.25,
        currency: 'USD',
        type: 'withdrawal'
      });

      expect(getAccountBalance('ACC-6A2B4')).toBeCloseTo(75.25);
    });
  });

  describe('clearStorage', () => {
    it('should clear all transactions', () => {
      createTransaction({
        toAccount: 'ACC-8C5D7',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      clearStorage();

      expect(getAllTransactions()).toEqual([]);
    });

    it('should clear all account balances', () => {
      createTransaction({
        toAccount: 'ACC-1E6F9',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      clearStorage();

      expect(getAccountBalance('ACC-1E6F9')).toBeNull();
    });
  });

  describe('Edge Cases - Defensive Code Paths', () => {
    it('should handle deposit without toAccount (defensive)', () => {
      const input = {
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      } as CreateTransactionInput;

      const result = createTransaction(input);

      expect(result.type).toBe('deposit');
      expect(result.toAccount).toBeUndefined();
    });

    it('should handle withdrawal without fromAccount (defensive)', () => {
      const input = {
        amount: 50,
        currency: 'USD',
        type: 'withdrawal'
      } as CreateTransactionInput;

      const result = createTransaction(input);

      expect(result.type).toBe('withdrawal');
      expect(result.fromAccount).toBeUndefined();
    });

    it('should handle transfer without fromAccount (defensive)', () => {
      const input = {
        toAccount: 'ACC-3G8H2',
        amount: 100,
        currency: 'USD',
        type: 'transfer'
      } as CreateTransactionInput;

      const result = createTransaction(input);

      expect(result.type).toBe('transfer');
      // toAccount gets initialized but no transfer happens (missing fromAccount)
      expect(getAccountBalance('ACC-3G8H2')).toBe(0);
    });

    it('should handle transfer without toAccount (defensive)', () => {
      const input = {
        fromAccount: 'ACC-5J1K7',
        amount: 100,
        currency: 'USD',
        type: 'transfer'
      } as CreateTransactionInput;

      const result = createTransaction(input);

      expect(result.type).toBe('transfer');
      // fromAccount gets initialized but no transfer happens (missing toAccount)
      expect(getAccountBalance('ACC-5J1K7')).toBe(0);
    });

    it('should handle transfer without both accounts (defensive)', () => {
      const input = {
        amount: 100,
        currency: 'USD',
        type: 'transfer'
      } as CreateTransactionInput;

      const result = createTransaction(input);

      expect(result.type).toBe('transfer');
    });
  });
});
