import * as transactionService from './transaction.service';
import { clearStorage } from '../utils/storage';
import { NotFoundException } from '../errors';

describe('Transaction Service', () => {
  beforeEach(() => {
    clearStorage();
  });

  describe('createTransaction', () => {
    it('should create and return a transaction', () => {
      const result = transactionService.createTransaction({
        toAccount: 'ACC001',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      expect(result.id).toBeDefined();
      expect(result.amount).toBe(100);
      expect(result.status).toBe('completed');
    });
  });

  describe('getAllTransactions', () => {
    it('should return empty array when no transactions', () => {
      const result = transactionService.getAllTransactions();
      expect(result).toEqual([]);
    });

    it('should return all transactions', () => {
      transactionService.createTransaction({
        toAccount: 'ACC001',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      const result = transactionService.getAllTransactions();
      expect(result).toHaveLength(1);
    });
  });

  describe('getTransactionById', () => {
    it('should return transaction when found', () => {
      const created = transactionService.createTransaction({
        toAccount: 'ACC001',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      const result = transactionService.getTransactionById(created.id);
      expect(result).toEqual(created);
    });

    it('should throw NotFoundException when not found', () => {
      expect(() => transactionService.getTransactionById('non-existent'))
        .toThrow(NotFoundException);
    });
  });
});
