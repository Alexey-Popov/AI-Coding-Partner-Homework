const {
  getAllTransactions,
  getTransactionById,
  addTransaction,
  getFilteredTransactions,
  getAccountBalance,
  resetStore
} = require('./store');

describe('store', () => {
  beforeEach(() => {
    resetStore();
  });

  describe('addTransaction', () => {
    test('creates transaction with generated ID', () => {
      const txn = addTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 100,
        currency: 'USD',
        type: 'transfer'
      });

      expect(txn.id).toBe('TXN-000001');
      expect(txn.fromAccount).toBe('ACC-12345');
      expect(txn.toAccount).toBe('ACC-67890');
      expect(txn.amount).toBe(100);
      expect(txn.currency).toBe('USD');
      expect(txn.type).toBe('transfer');
      expect(txn.status).toBe('completed');
      expect(txn.timestamp).toBeDefined();
    });

    test('generates sequential IDs', () => {
      const txn1 = addTransaction({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
      const txn2 = addTransaction({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 200,
        currency: 'USD',
        type: 'deposit'
      });

      expect(txn1.id).toBe('TXN-000001');
      expect(txn2.id).toBe('TXN-000002');
    });
  });

  describe('getAllTransactions', () => {
    test('returns empty array when no transactions', () => {
      expect(getAllTransactions()).toEqual([]);
    });

    test('returns all added transactions', () => {
      addTransaction({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
      addTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 50,
        currency: 'EUR',
        type: 'transfer'
      });

      const transactions = getAllTransactions();
      expect(transactions.length).toBe(2);
    });
  });

  describe('getTransactionById', () => {
    test('returns transaction by ID', () => {
      addTransaction({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      const txn = getTransactionById('TXN-000001');
      expect(txn).toBeDefined();
      expect(txn.amount).toBe(100);
    });

    test('returns undefined for non-existent ID', () => {
      const txn = getTransactionById('TXN-999999');
      expect(txn).toBeUndefined();
    });
  });

  describe('getFilteredTransactions', () => {
    beforeEach(() => {
      addTransaction({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
      addTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 50,
        currency: 'EUR',
        type: 'transfer'
      });
      addTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-00000',
        amount: 25,
        currency: 'USD',
        type: 'withdrawal'
      });
    });

    test('filters by accountId (matches fromAccount)', () => {
      const filtered = getFilteredTransactions({ accountId: 'ACC-00000' });
      expect(filtered.length).toBe(2); // deposit to ACC-12345 and withdrawal from ACC-12345
    });

    test('filters by accountId (matches toAccount)', () => {
      const filtered = getFilteredTransactions({ accountId: 'ACC-67890' });
      expect(filtered.length).toBe(1);
      expect(filtered[0].type).toBe('transfer');
    });

    test('filters by type', () => {
      const filtered = getFilteredTransactions({ type: 'transfer' });
      expect(filtered.length).toBe(1);
      expect(filtered[0].type).toBe('transfer');
    });

    test('filters by date range', () => {
      const today = new Date().toISOString().split('T')[0];
      const filtered = getFilteredTransactions({ from: today, to: today });
      expect(filtered.length).toBe(3);
    });

    test('filters exclude transactions outside date range', () => {
      const futureDate = '2099-01-01';
      const filtered = getFilteredTransactions({ from: futureDate });
      expect(filtered.length).toBe(0);
    });

    test('combines multiple filters', () => {
      const filtered = getFilteredTransactions({ accountId: 'ACC-12345', type: 'transfer' });
      expect(filtered.length).toBe(1);
    });

    test('returns all transactions when no filters', () => {
      const filtered = getFilteredTransactions({});
      expect(filtered.length).toBe(3);
    });
  });

  describe('getAccountBalance', () => {
    test('returns 0 for account with no transactions', () => {
      expect(getAccountBalance('ACC-XXXXX')).toBe(0);
    });

    test('calculates deposit correctly', () => {
      addTransaction({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      expect(getAccountBalance('ACC-12345')).toBe(100);
    });

    test('calculates withdrawal correctly', () => {
      addTransaction({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
      addTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-00000',
        amount: 30,
        currency: 'USD',
        type: 'withdrawal'
      });

      expect(getAccountBalance('ACC-12345')).toBe(70);
    });

    test('calculates transfer correctly for sender', () => {
      addTransaction({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
      addTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 40,
        currency: 'USD',
        type: 'transfer'
      });

      expect(getAccountBalance('ACC-12345')).toBe(60);
    });

    test('calculates transfer correctly for receiver', () => {
      addTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 40,
        currency: 'USD',
        type: 'transfer'
      });

      expect(getAccountBalance('ACC-67890')).toBe(40);
    });

    test('handles multiple transaction types', () => {
      addTransaction({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      addTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 200,
        currency: 'USD',
        type: 'transfer'
      });
      addTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-00000',
        amount: 100,
        currency: 'USD',
        type: 'withdrawal'
      });
      addTransaction({
        fromAccount: 'ACC-ABCDE',
        toAccount: 'ACC-12345',
        amount: 50,
        currency: 'USD',
        type: 'transfer'
      });

      // 1000 (deposit) - 200 (transfer out) - 100 (withdrawal) + 50 (transfer in) = 750
      expect(getAccountBalance('ACC-12345')).toBe(750);
    });
  });

  describe('resetStore', () => {
    test('clears all transactions', () => {
      addTransaction({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      expect(getAllTransactions().length).toBe(1);

      resetStore();

      expect(getAllTransactions().length).toBe(0);
    });

    test('resets ID counter', () => {
      addTransaction({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      resetStore();

      const txn = addTransaction({
        fromAccount: 'ACC-00000',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      expect(txn.id).toBe('TXN-000001');
    });
  });
});
