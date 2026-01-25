/**
 * Tests for transaction business logic
 */

const {
  createTransaction,
  getTransactions,
  getTransactionById,
  getAccountBalance,
  getAccountSummary,
  transactions,
  accountBalances,
  resetStorage
} = require('../src/transaction');

describe('Transaction Service', () => {
  beforeEach(() => {
    resetStorage();
  });

  describe('createTransaction', () => {
    test('should create a deposit transaction', () => {
      const transaction = createTransaction({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });

      expect(transaction.id).toBeDefined();
      expect(transaction.toAccount).toBe('ACC-12345');
      expect(transaction.amount).toBe(1000);
      expect(transaction.currency).toBe('USD');
      expect(transaction.type).toBe('deposit');
      expect(transaction.status).toBe('completed');
      expect(transaction.timestamp).toBeDefined();
    });

    test('should create a withdrawal transaction', () => {
      const transaction = createTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-BANK1',
        amount: 500,
        currency: 'EUR',
        type: 'withdrawal'
      });

      expect(transaction.id).toBeDefined();
      expect(transaction.fromAccount).toBe('ACC-12345');
      expect(transaction.type).toBe('withdrawal');
    });

    test('should create a transfer transaction', () => {
      const transaction = createTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 250.50,
        currency: 'GBP',
        type: 'transfer'
      });

      expect(transaction.id).toBeDefined();
      expect(transaction.fromAccount).toBe('ACC-12345');
      expect(transaction.toAccount).toBe('ACC-67890');
      expect(transaction.type).toBe('transfer');
    });

    test('should store transaction in memory', () => {
      expect(transactions.length).toBe(0);

      createTransaction({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      expect(transactions.length).toBe(1);
    });

    test('should generate unique IDs', () => {
      const t1 = createTransaction({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      const t2 = createTransaction({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 200,
        currency: 'USD',
        type: 'deposit'
      });

      expect(t1.id).not.toBe(t2.id);
    });
  });

  describe('Account Balances', () => {
    test('deposit should increase balance', () => {
      createTransaction({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });

      const balance = getAccountBalance('ACC-12345');
      expect(balance.balance).toBe(1000);
    });

    test('withdrawal should decrease balance', () => {
      // First deposit
      createTransaction({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });

      // Then withdraw
      createTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-BANK1',
        amount: 300,
        currency: 'USD',
        type: 'withdrawal'
      });

      const balance = getAccountBalance('ACC-12345');
      expect(balance.balance).toBe(700);
    });

    test('transfer should update both accounts', () => {
      // Deposit to source
      createTransaction({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });

      // Transfer
      createTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 400,
        currency: 'USD',
        type: 'transfer'
      });

      expect(getAccountBalance('ACC-12345').balance).toBe(600);
      expect(getAccountBalance('ACC-67890').balance).toBe(400);
    });

    test('new account should have zero balance', () => {
      const balance = getAccountBalance('ACC-NEW01');
      expect(balance.balance).toBe(0);
    });
  });

  describe('getTransactions', () => {
    beforeEach(() => {
      createTransaction({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      createTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 200,
        currency: 'USD',
        type: 'transfer'
      });
      createTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-BANK1',
        amount: 100,
        currency: 'EUR',
        type: 'withdrawal'
      });
    });

    test('should return all transactions', () => {
      const result = getTransactions();
      expect(result.length).toBe(3);
    });

    test('should filter by accountId', () => {
      const result = getTransactions({ accountId: 'ACC-12345' });
      expect(result.length).toBe(3); // All involve ACC-12345

      const result2 = getTransactions({ accountId: 'ACC-67890' });
      expect(result2.length).toBe(1); // Only the transfer
    });

    test('should filter by type', () => {
      const deposits = getTransactions({ type: 'deposit' });
      expect(deposits.length).toBe(1);

      const transfers = getTransactions({ type: 'transfer' });
      expect(transfers.length).toBe(1);

      const withdrawals = getTransactions({ type: 'withdrawal' });
      expect(withdrawals.length).toBe(1);
    });

    test('should support combined filters', () => {
      const result = getTransactions({
        accountId: 'ACC-12345',
        type: 'deposit'
      });
      expect(result.length).toBe(1);
    });
  });

  describe('getTransactionById', () => {
    test('should return transaction by ID', () => {
      const created = createTransaction({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      const found = getTransactionById(created.id);
      expect(found).not.toBeNull();
      expect(found.id).toBe(created.id);
    });

    test('should return null for nonexistent ID', () => {
      const found = getTransactionById('nonexistent-id');
      expect(found).toBeNull();
    });
  });

  describe('getAccountSummary', () => {
    test('should return correct account summary', () => {
      createTransaction({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      createTransaction({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 500,
        currency: 'USD',
        type: 'deposit'
      });
      createTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-BANK1',
        amount: 200,
        currency: 'USD',
        type: 'withdrawal'
      });

      const summary = getAccountSummary('ACC-12345');

      expect(summary.accountId).toBe('ACC-12345');
      expect(summary.totalDeposits).toBe(1500);
      expect(summary.totalWithdrawals).toBe(200);
      expect(summary.numberOfTransactions).toBe(3);
      expect(summary.currentBalance).toBe(1300);
      expect(summary.mostRecentTransactionDate).toBeDefined();
    });

    test('should return zeros for account with no transactions', () => {
      const summary = getAccountSummary('ACC-EMPTY');

      expect(summary.totalDeposits).toBe(0);
      expect(summary.totalWithdrawals).toBe(0);
      expect(summary.numberOfTransactions).toBe(0);
      expect(summary.currentBalance).toBe(0);
    });
  });

  describe('resetStorage', () => {
    test('should clear all transactions and balances', () => {
      createTransaction({
        fromAccount: 'ACC-BANK1',
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });

      expect(transactions.length).toBe(1);
      expect(Object.keys(accountBalances).length).toBeGreaterThan(0);

      resetStorage();

      expect(transactions.length).toBe(0);
      expect(Object.keys(accountBalances).length).toBe(0);
    });
  });
});
