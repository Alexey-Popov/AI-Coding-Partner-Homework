/**
 * Tests for validation utilities
 */

const { validateTransaction, VALID_CURRENCIES, ACCOUNT_FORMAT, TRANSACTION_TYPES } = require('../src/validators');

describe('Validators', () => {
  describe('validateTransaction', () => {
    describe('Account Validation', () => {
      test('valid account formats should pass', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(true);
        expect(result.errors).toHaveLength(0);
      });

      test('valid alphanumeric account formats should pass', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-ABCDE',
          toAccount: 'ACC-A1B2C',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(true);
      });

      test('missing fromAccount should fail', () => {
        const result = validateTransaction({
          toAccount: 'ACC-12345',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(false);
        expect(result.errors.some(e => e.field === 'fromAccount')).toBe(true);
      });

      test('missing toAccount should fail', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-12345',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(false);
        expect(result.errors.some(e => e.field === 'toAccount')).toBe(true);
      });

      test('invalid account format (lowercase) should fail', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-abcde',
          toAccount: 'ACC-12345',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(false);
        expect(result.errors.some(e => e.field === 'fromAccount')).toBe(true);
      });

      test('invalid account format (wrong prefix) should fail', () => {
        const result = validateTransaction({
          fromAccount: 'XYZ-12345',
          toAccount: 'ACC-12345',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(false);
      });

      test('invalid account format (wrong length) should fail', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-1234',
          toAccount: 'ACC-12345',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(false);
      });

      test('same from and to accounts for transfer should fail', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-12345',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(false);
        expect(result.errors.some(e => e.field === 'accounts')).toBe(true);
      });
    });

    describe('Amount Validation', () => {
      test('positive amount should pass', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890',
          amount: 100,
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(true);
      });

      test('amount with 2 decimal places should pass', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890',
          amount: 100.50,
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(true);
      });

      test('missing amount should fail', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890',
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(false);
        expect(result.errors.some(e => e.field === 'amount')).toBe(true);
      });

      test('negative amount should fail', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890',
          amount: -100,
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(false);
        expect(result.errors.some(e => e.field === 'amount')).toBe(true);
      });

      test('zero amount should fail', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890',
          amount: 0,
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(false);
      });

      test('amount with more than 2 decimal places should fail', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890',
          amount: 100.123,
          currency: 'USD',
          type: 'transfer'
        });
        expect(result.isValid).toBe(false);
        expect(result.errors.some(e => e.field === 'amount' && e.message.includes('decimal'))).toBe(true);
      });
    });

    describe('Currency Validation', () => {
      test('valid currencies should pass', () => {
        const currencies = ['USD', 'EUR', 'GBP', 'JPY'];
        currencies.forEach(currency => {
          const result = validateTransaction({
            fromAccount: 'ACC-12345',
            toAccount: 'ACC-67890',
            amount: 100,
            currency,
            type: 'transfer'
          });
          expect(result.isValid).toBe(true);
        });
      });

      test('missing currency should fail', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890',
          amount: 100,
          type: 'transfer'
        });
        expect(result.isValid).toBe(false);
        expect(result.errors.some(e => e.field === 'currency')).toBe(true);
      });

      test('invalid currency should fail', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890',
          amount: 100,
          currency: 'INVALID',
          type: 'transfer'
        });
        expect(result.isValid).toBe(false);
        expect(result.errors.some(e => e.field === 'currency')).toBe(true);
      });
    });

    describe('Type Validation', () => {
      test('valid types should pass', () => {
        const types = ['deposit', 'withdrawal', 'transfer'];
        types.forEach(type => {
          const result = validateTransaction({
            fromAccount: 'ACC-12345',
            toAccount: 'ACC-67890',
            amount: 100,
            currency: 'USD',
            type
          });
          expect(result.isValid).toBe(true);
        });
      });

      test('missing type should fail', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890',
          amount: 100,
          currency: 'USD'
        });
        expect(result.isValid).toBe(false);
        expect(result.errors.some(e => e.field === 'type')).toBe(true);
      });

      test('invalid type should fail', () => {
        const result = validateTransaction({
          fromAccount: 'ACC-12345',
          toAccount: 'ACC-67890',
          amount: 100,
          currency: 'USD',
          type: 'invalid'
        });
        expect(result.isValid).toBe(false);
        expect(result.errors.some(e => e.field === 'type')).toBe(true);
      });
    });

    describe('Multiple Errors', () => {
      test('should return multiple validation errors', () => {
        const result = validateTransaction({
          fromAccount: 'INVALID',
          toAccount: 'BAD',
          amount: -100,
          currency: 'FAKE',
          type: 'wrong'
        });
        expect(result.isValid).toBe(false);
        expect(result.errors.length).toBeGreaterThan(3);
      });
    });
  });

  describe('Constants', () => {
    test('VALID_CURRENCIES should contain common currencies', () => {
      expect(VALID_CURRENCIES).toContain('USD');
      expect(VALID_CURRENCIES).toContain('EUR');
      expect(VALID_CURRENCIES).toContain('GBP');
    });

    test('TRANSACTION_TYPES should contain all types', () => {
      expect(TRANSACTION_TYPES).toContain('deposit');
      expect(TRANSACTION_TYPES).toContain('withdrawal');
      expect(TRANSACTION_TYPES).toContain('transfer');
    });

    test('ACCOUNT_FORMAT regex should match valid accounts', () => {
      expect(ACCOUNT_FORMAT.test('ACC-12345')).toBe(true);
      expect(ACCOUNT_FORMAT.test('ACC-ABCDE')).toBe(true);
      expect(ACCOUNT_FORMAT.test('ACC-A1B2C')).toBe(true);
      expect(ACCOUNT_FORMAT.test('INVALID')).toBe(false);
      expect(ACCOUNT_FORMAT.test('ACC-1234')).toBe(false);
    });
  });
});
