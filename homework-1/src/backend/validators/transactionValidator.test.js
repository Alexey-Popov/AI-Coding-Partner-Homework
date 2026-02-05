const {
  validateTransaction,
  isValidAccountNumber,
  isValidAmount,
  isValidCurrency,
  isValidType,
  VALID_CURRENCIES,
  VALID_TYPES
} = require('./transactionValidator');

describe('transactionValidator', () => {
  describe('isValidAccountNumber', () => {
    test('accepts valid account format ACC-XXXXX', () => {
      expect(isValidAccountNumber('ACC-12345')).toBe(true);
      expect(isValidAccountNumber('ACC-ABCDE')).toBe(true);
      expect(isValidAccountNumber('ACC-a1b2c')).toBe(true);
      expect(isValidAccountNumber('ACC-00000')).toBe(true);
    });

    test('rejects invalid account formats', () => {
      expect(isValidAccountNumber('INVALID')).toBe(false);
      expect(isValidAccountNumber('ACC-1234')).toBe(false); // Too short
      expect(isValidAccountNumber('ACC-123456')).toBe(false); // Too long
      expect(isValidAccountNumber('ACC12345')).toBe(false); // Missing dash
      expect(isValidAccountNumber('acc-12345')).toBe(false); // Lowercase ACC
      expect(isValidAccountNumber('')).toBe(false);
      expect(isValidAccountNumber(null)).toBe(false);
      expect(isValidAccountNumber(undefined)).toBe(false);
    });
  });

  describe('isValidAmount', () => {
    test('accepts positive numbers with up to 2 decimal places', () => {
      expect(isValidAmount(100)).toBe(true);
      expect(isValidAmount(100.5)).toBe(true);
      expect(isValidAmount(100.55)).toBe(true);
      expect(isValidAmount(0.01)).toBe(true);
      expect(isValidAmount(1000000)).toBe(true);
    });

    test('rejects zero and negative amounts', () => {
      expect(isValidAmount(0)).toBe(false);
      expect(isValidAmount(-100)).toBe(false);
      expect(isValidAmount(-0.01)).toBe(false);
    });

    test('rejects amounts with more than 2 decimal places', () => {
      expect(isValidAmount(100.999)).toBe(false);
      expect(isValidAmount(100.001)).toBe(false);
      expect(isValidAmount(0.001)).toBe(false);
    });

    test('rejects non-numeric values', () => {
      expect(isValidAmount('100')).toBe(false);
      expect(isValidAmount(null)).toBe(false);
      expect(isValidAmount(undefined)).toBe(false);
      expect(isValidAmount(NaN)).toBe(false);
    });
  });

  describe('isValidCurrency', () => {
    test('accepts valid ISO 4217 currency codes', () => {
      expect(isValidCurrency('USD')).toBe(true);
      expect(isValidCurrency('EUR')).toBe(true);
      expect(isValidCurrency('GBP')).toBe(true);
      expect(isValidCurrency('JPY')).toBe(true);
    });

    test('rejects invalid currency codes', () => {
      expect(isValidCurrency('FAKE')).toBe(false);
      expect(isValidCurrency('usd')).toBe(false); // Lowercase
      expect(isValidCurrency('US')).toBe(false);
      expect(isValidCurrency('')).toBe(false);
      expect(isValidCurrency(null)).toBe(false);
    });

    test('VALID_CURRENCIES contains expected currencies', () => {
      expect(VALID_CURRENCIES).toContain('USD');
      expect(VALID_CURRENCIES).toContain('EUR');
      expect(VALID_CURRENCIES).toContain('GBP');
      expect(VALID_CURRENCIES.length).toBeGreaterThan(10);
    });
  });

  describe('isValidType', () => {
    test('accepts valid transaction types', () => {
      expect(isValidType('deposit')).toBe(true);
      expect(isValidType('withdrawal')).toBe(true);
      expect(isValidType('transfer')).toBe(true);
    });

    test('rejects invalid transaction types', () => {
      expect(isValidType('DEPOSIT')).toBe(false); // Uppercase
      expect(isValidType('refund')).toBe(false);
      expect(isValidType('')).toBe(false);
      expect(isValidType(null)).toBe(false);
    });

    test('VALID_TYPES contains exactly 3 types', () => {
      expect(VALID_TYPES).toEqual(['deposit', 'withdrawal', 'transfer']);
    });
  });

  describe('validateTransaction', () => {
    const validTransaction = {
      fromAccount: 'ACC-12345',
      toAccount: 'ACC-67890',
      amount: 100.50,
      currency: 'USD',
      type: 'transfer'
    };

    test('returns empty array for valid transaction', () => {
      const errors = validateTransaction(validTransaction);
      expect(errors).toEqual([]);
    });

    test('returns error for missing amount', () => {
      const transaction = { ...validTransaction, amount: undefined };
      const errors = validateTransaction(transaction);
      expect(errors).toContainEqual({ field: 'amount', message: 'Amount is required' });
    });

    test('returns error for non-numeric amount', () => {
      const transaction = { ...validTransaction, amount: 'not a number' };
      const errors = validateTransaction(transaction);
      expect(errors).toContainEqual({ field: 'amount', message: 'Amount must be a number' });
    });

    test('returns error for negative amount', () => {
      const transaction = { ...validTransaction, amount: -50 };
      const errors = validateTransaction(transaction);
      expect(errors).toContainEqual({ field: 'amount', message: 'Amount must be a positive number' });
    });

    test('returns error for too many decimal places', () => {
      const transaction = { ...validTransaction, amount: 100.999 };
      const errors = validateTransaction(transaction);
      expect(errors).toContainEqual({ field: 'amount', message: 'Amount must have maximum 2 decimal places' });
    });

    test('returns error for missing fromAccount', () => {
      const transaction = { ...validTransaction, fromAccount: undefined };
      const errors = validateTransaction(transaction);
      expect(errors).toContainEqual({ field: 'fromAccount', message: 'fromAccount is required' });
    });

    test('returns error for invalid fromAccount format', () => {
      const transaction = { ...validTransaction, fromAccount: 'INVALID' };
      const errors = validateTransaction(transaction);
      expect(errors).toContainEqual({ field: 'fromAccount', message: 'fromAccount must follow format ACC-XXXXX (X is alphanumeric)' });
    });

    test('returns error for missing toAccount', () => {
      const transaction = { ...validTransaction, toAccount: undefined };
      const errors = validateTransaction(transaction);
      expect(errors).toContainEqual({ field: 'toAccount', message: 'toAccount is required' });
    });

    test('returns error for invalid toAccount format', () => {
      const transaction = { ...validTransaction, toAccount: 'bad' };
      const errors = validateTransaction(transaction);
      expect(errors).toContainEqual({ field: 'toAccount', message: 'toAccount must follow format ACC-XXXXX (X is alphanumeric)' });
    });

    test('returns error for missing currency', () => {
      const transaction = { ...validTransaction, currency: undefined };
      const errors = validateTransaction(transaction);
      expect(errors).toContainEqual({ field: 'currency', message: 'Currency is required' });
    });

    test('returns error for invalid currency', () => {
      const transaction = { ...validTransaction, currency: 'FAKE' };
      const errors = validateTransaction(transaction);
      expect(errors).toContainEqual({ field: 'currency', message: 'Invalid currency code. Must be a valid ISO 4217 code' });
    });

    test('returns error for missing type', () => {
      const transaction = { ...validTransaction, type: undefined };
      const errors = validateTransaction(transaction);
      expect(errors).toContainEqual({ field: 'type', message: 'Transaction type is required' });
    });

    test('returns error for invalid type', () => {
      const transaction = { ...validTransaction, type: 'refund' };
      const errors = validateTransaction(transaction);
      expect(errors).toContainEqual({ field: 'type', message: 'Type must be one of: deposit, withdrawal, transfer' });
    });

    test('returns multiple errors for multiple invalid fields', () => {
      const transaction = {
        fromAccount: 'bad',
        toAccount: '',
        amount: -10,
        currency: 'FAKE',
        type: 'invalid'
      };
      const errors = validateTransaction(transaction);
      expect(errors.length).toBe(5);
    });
  });
});
