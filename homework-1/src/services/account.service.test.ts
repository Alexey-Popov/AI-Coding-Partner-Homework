import * as accountService from './account.service';
import * as transactionService from './transaction.service';
import { clearStorage } from '../utils/storage';
import { NotFoundException } from '../errors';

describe('Account Service', () => {
  beforeEach(() => {
    clearStorage();
  });

  describe('getAccountBalance', () => {
    it('should throw NotFoundException for non-existent account', () => {
      expect(() => accountService.getAccountBalance('NON_EXISTENT'))
        .toThrow(NotFoundException);
    });

    it('should return balance for existing account', () => {
      transactionService.createTransaction({
        toAccount: 'ACC-6N3P9',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      const result = accountService.getAccountBalance('ACC-6N3P9');

      expect(result.accountId).toBe('ACC-6N3P9');
      expect(result.balance).toBe(100);
      expect(result.currency).toBe('USD');
    });
  });
});
