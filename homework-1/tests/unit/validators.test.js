const { describe, it, beforeEach } = require('node:test');
const assert = require('node:assert');
const { validateTransaction, isValidAccountId, ACCOUNT_REGEX } = require('../../src/validators/transactionValidator');

describe('Transaction Validator', () => {
  describe('validateTransaction', () => {
    describe('amount validation', () => {
      it('should fail when amount is missing', () => {
        const result = validateTransaction({
          currency: 'USD',
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'amount' && e.message === 'Amount is required'));
      });

      it('should fail when amount is null', () => {
        const result = validateTransaction({
          amount: null,
          currency: 'USD',
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'amount' && e.message === 'Amount is required'));
      });

      it('should fail when amount is not a number', () => {
        const result = validateTransaction({
          amount: 'one hundred',
          currency: 'USD',
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'amount' && e.message === 'Amount must be a number'));
      });

      it('should fail when amount is NaN', () => {
        const result = validateTransaction({
          amount: NaN,
          currency: 'USD',
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'amount' && e.message === 'Amount must be a number'));
      });

      it('should fail when amount is zero', () => {
        const result = validateTransaction({
          amount: 0,
          currency: 'USD',
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'amount' && e.message === 'Amount must be a positive number'));
      });

      it('should fail when amount is negative', () => {
        const result = validateTransaction({
          amount: -50,
          currency: 'USD',
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'amount' && e.message === 'Amount must be a positive number'));
      });

      it('should fail when amount has more than 2 decimal places', () => {
        const result = validateTransaction({
          amount: 100.123,
          currency: 'USD',
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'amount' && e.message === 'Amount must have maximum 2 decimal places'));
      });

      it('should pass when amount has exactly 2 decimal places', () => {
        const result = validateTransaction({
          amount: 100.12,
          currency: 'USD',
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, true);
      });

      it('should pass when amount has 1 decimal place', () => {
        const result = validateTransaction({
          amount: 100.5,
          currency: 'USD',
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, true);
      });

      it('should pass when amount is a whole number', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, true);
      });
    });

    describe('currency validation', () => {
      it('should fail when currency is missing', () => {
        const result = validateTransaction({
          amount: 100,
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'currency' && e.message === 'Currency is required'));
      });

      it('should fail when currency is invalid', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'XYZ',
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'currency' && e.message.includes('Invalid currency code')));
      });

      it('should pass with valid currency codes', () => {
        const validCurrencies = ['USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'CNY', 'INR', 'PLN'];
        for (const currency of validCurrencies) {
          const result = validateTransaction({
            amount: 100,
            currency,
            type: 'deposit',
            toAccount: 'ACC-12345'
          });
          assert.strictEqual(result.isValid, true, `Failed for currency: ${currency}`);
        }
      });

      it('should accept lowercase currency codes', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'usd',
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, true);
      });
    });

    describe('type validation', () => {
      it('should fail when type is missing', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'type' && e.message === 'Transaction type is required'));
      });

      it('should fail when type is invalid', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          type: 'refund',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'type' && e.message.includes('Invalid transaction type')));
      });

      it('should pass with valid types', () => {
        const validTypes = ['deposit', 'withdrawal', 'transfer'];
        for (const type of validTypes) {
          const data = {
            amount: 100,
            currency: 'USD',
            type
          };
          if (type === 'deposit') {
            data.toAccount = 'ACC-12345';
          } else if (type === 'withdrawal') {
            data.fromAccount = 'ACC-12345';
          } else {
            data.fromAccount = 'ACC-12345';
            data.toAccount = 'ACC-67890';
          }
          const result = validateTransaction(data);
          assert.strictEqual(result.isValid, true, `Failed for type: ${type}`);
        }
      });
    });

    describe('deposit validation', () => {
      it('should require toAccount for deposits', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          type: 'deposit'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'toAccount' && e.message === 'toAccount is required for deposits'));
      });

      it('should validate toAccount format for deposits', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          type: 'deposit',
          toAccount: 'invalid'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'toAccount' && e.message.includes('must follow format')));
      });

      it('should pass valid deposit', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          type: 'deposit',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, true);
      });
    });

    describe('withdrawal validation', () => {
      it('should require fromAccount for withdrawals', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          type: 'withdrawal'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'fromAccount' && e.message === 'fromAccount is required for withdrawals'));
      });

      it('should validate fromAccount format for withdrawals', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          type: 'withdrawal',
          fromAccount: 'invalid'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'fromAccount' && e.message.includes('must follow format')));
      });

      it('should pass valid withdrawal', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          type: 'withdrawal',
          fromAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, true);
      });
    });

    describe('transfer validation', () => {
      it('should require both accounts for transfers', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'fromAccount' && e.message === 'fromAccount is required for transfers'));
        assert.ok(result.errors.some(e => e.field === 'toAccount' && e.message === 'toAccount is required for transfers'));
      });

      it('should validate fromAccount format for transfers', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          type: 'transfer',
          fromAccount: 'invalid',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'fromAccount' && e.message.includes('must follow format')));
      });

      it('should validate toAccount format for transfers', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          type: 'transfer',
          fromAccount: 'ACC-12345',
          toAccount: 'invalid'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'toAccount' && e.message.includes('must follow format')));
      });

      it('should fail when fromAccount and toAccount are the same', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          type: 'transfer',
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-12345'
        });
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.some(e => e.field === 'toAccount' && e.message === 'fromAccount and toAccount must be different'));
      });

      it('should pass valid transfer', () => {
        const result = validateTransaction({
          amount: 100,
          currency: 'USD',
          type: 'transfer',
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890'
        });
        assert.strictEqual(result.isValid, true);
      });
    });

    describe('multiple errors', () => {
      it('should return all validation errors at once', () => {
        const result = validateTransaction({});
        assert.strictEqual(result.isValid, false);
        assert.ok(result.errors.length >= 3, 'Should have at least 3 errors (amount, currency, type)');
      });
    });
  });

  describe('isValidAccountId', () => {
    it('should return true for valid account IDs', () => {
      assert.strictEqual(isValidAccountId('ACC-12345'), true);
      assert.strictEqual(isValidAccountId('ACC-ABCDE'), true);
      assert.strictEqual(isValidAccountId('ACC-a1b2c'), true);
      assert.strictEqual(isValidAccountId('ACC-00000'), true);
    });

    it('should return false for invalid account IDs', () => {
      assert.strictEqual(isValidAccountId('ACC-1234'), false);  // Too short
      assert.strictEqual(isValidAccountId('ACC-123456'), false);  // Too long
      assert.strictEqual(isValidAccountId('ACC12345'), false);  // Missing hyphen
      assert.strictEqual(isValidAccountId('ACX-12345'), false);  // Wrong prefix
      assert.strictEqual(isValidAccountId('acc-12345'), false);  // Lowercase ACC
      assert.strictEqual(isValidAccountId(''), false);  // Empty
      assert.strictEqual(isValidAccountId('ACC-'), false);  // Missing digits
      assert.strictEqual(isValidAccountId('ACC-12!45'), false);  // Special character
    });
  });

  describe('ACCOUNT_REGEX', () => {
    it('should be exported correctly', () => {
      assert.ok(ACCOUNT_REGEX instanceof RegExp);
      assert.strictEqual(ACCOUNT_REGEX.test('ACC-12345'), true);
    });
  });
});
