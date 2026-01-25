import { describe, it, beforeEach } from 'node:test';
import assert from 'node:assert';
import {
  createTransaction,
  getTransactions,
  getTransactionById,
  getAccountBalance,
  clearTransactions,
  getAllTransactionsRaw,
  VALID_CURRENCIES,
  VALID_TYPES,
  VALID_STATUSES
} from '../../src/models/transaction.js';

describe('Transaction Model', () => {
  // Clear transactions before each test to ensure isolation
  beforeEach(() => {
    clearTransactions();
  });

  describe('Constants', () => {
    it('should export valid currencies', () => {
      assert.ok(Array.isArray(VALID_CURRENCIES));
      assert.ok(VALID_CURRENCIES.includes('USD'));
      assert.ok(VALID_CURRENCIES.includes('EUR'));
      assert.ok(VALID_CURRENCIES.includes('GBP'));
      assert.strictEqual(VALID_CURRENCIES.length, 10);
    });

    it('should export valid transaction types', () => {
      assert.ok(Array.isArray(VALID_TYPES));
      assert.deepStrictEqual([...VALID_TYPES], ['deposit', 'withdrawal', 'transfer']);
    });

    it('should export valid statuses', () => {
      assert.ok(Array.isArray(VALID_STATUSES));
      assert.deepStrictEqual([...VALID_STATUSES], ['pending', 'completed', 'failed']);
    });
  });

  describe('createTransaction', () => {
    it('should create a deposit transaction', () => {
      const transaction = createTransaction({
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      assert.ok(transaction.id);
      assert.strictEqual(transaction.toAccount, 'ACC-12345');
      assert.strictEqual(transaction.fromAccount, undefined);
      assert.strictEqual(transaction.amount, 100);
      assert.strictEqual(transaction.currency, 'USD');
      assert.strictEqual(transaction.type, 'deposit');
      assert.strictEqual(transaction.status, 'completed');
      assert.ok(transaction.timestamp);
    });

    it('should create a withdrawal transaction', () => {
      const transaction = createTransaction({
        fromAccount: 'ACC-12345',
        amount: 50,
        currency: 'EUR',
        type: 'withdrawal'
      });

      assert.ok(transaction.id);
      assert.strictEqual(transaction.fromAccount, 'ACC-12345');
      assert.strictEqual(transaction.toAccount, undefined);
      assert.strictEqual(transaction.amount, 50);
      assert.strictEqual(transaction.currency, 'EUR');
      assert.strictEqual(transaction.type, 'withdrawal');
    });

    it('should create a transfer transaction', () => {
      const transaction = createTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 200,
        currency: 'GBP',
        type: 'transfer'
      });

      assert.ok(transaction.id);
      assert.strictEqual(transaction.fromAccount, 'ACC-12345');
      assert.strictEqual(transaction.toAccount, 'ACC-67890');
      assert.strictEqual(transaction.amount, 200);
      assert.strictEqual(transaction.type, 'transfer');
    });

    it('should generate unique IDs for each transaction', () => {
      const t1 = createTransaction({
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
      const t2 = createTransaction({
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      assert.notStrictEqual(t1.id, t2.id);
    });

    it('should allow custom status', () => {
      const transaction = createTransaction({
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit',
        status: 'pending'
      });

      assert.strictEqual(transaction.status, 'pending');
    });

    it('should set timestamp in ISO format', () => {
      const transaction = createTransaction({
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      const parsedDate = new Date(transaction.timestamp);
      assert.ok(!Number.isNaN(parsedDate.getTime()));
    });
  });

  describe('getTransactions', () => {
    beforeEach(() => {
      // Create some test transactions
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      createTransaction({
        toAccount: 'ACC-67890',
        amount: 500,
        currency: 'EUR',
        type: 'deposit'
      });
      createTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 100,
        currency: 'USD',
        type: 'transfer'
      });
      createTransaction({
        fromAccount: 'ACC-12345',
        amount: 50,
        currency: 'USD',
        type: 'withdrawal'
      });
    });

    it('should return all transactions when no filters provided', () => {
      const transactions = getTransactions();
      assert.strictEqual(transactions.length, 4);
    });

    it('should filter by accountId (fromAccount)', () => {
      const transactions = getTransactions({ accountId: 'ACC-12345' });
      assert.strictEqual(transactions.length, 3);  // deposit, transfer (from), withdrawal
    });

    it('should filter by accountId (toAccount)', () => {
      const transactions = getTransactions({ accountId: 'ACC-67890' });
      assert.strictEqual(transactions.length, 2);  // deposit, transfer (to)
    });

    it('should filter by type', () => {
      const deposits = getTransactions({ type: 'deposit' });
      assert.strictEqual(deposits.length, 2);

      const transfers = getTransactions({ type: 'transfer' });
      assert.strictEqual(transfers.length, 1);

      const withdrawals = getTransactions({ type: 'withdrawal' });
      assert.strictEqual(withdrawals.length, 1);
    });

    it('should filter by date range (from)', () => {
      const pastDate = new Date(Date.now() - 86400000).toISOString();  // Yesterday
      const transactions = getTransactions({ from: pastDate });
      assert.strictEqual(transactions.length, 4);
    });

    it('should filter by date range (to)', () => {
      const futureDate = new Date(Date.now() + 86400000).toISOString();  // Tomorrow
      const transactions = getTransactions({ to: futureDate });
      assert.strictEqual(transactions.length, 4);
    });

    it('should combine multiple filters', () => {
      const transactions = getTransactions({
        accountId: 'ACC-12345',
        type: 'deposit'
      });
      assert.strictEqual(transactions.length, 1);
    });

    it('should return empty array when no matches', () => {
      const transactions = getTransactions({ accountId: 'ACC-99999' });
      assert.strictEqual(transactions.length, 0);
    });

    it('should return a copy of transactions (not the original array)', () => {
      const transactions = getTransactions();
      transactions.push({ fake: true });
      const transactionsAgain = getTransactions();
      assert.strictEqual(transactionsAgain.length, 4);
    });
  });

  describe('getTransactionById', () => {
    it('should return transaction when found', () => {
      const created = createTransaction({
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      const found = getTransactionById(created.id);
      assert.deepStrictEqual(found, created);
    });

    it('should return null when not found', () => {
      const found = getTransactionById('nonexistent-id');
      assert.strictEqual(found, null);
    });

    it('should return null for empty string', () => {
      const found = getTransactionById('');
      assert.strictEqual(found, null);
    });
  });

  describe('getAccountBalance', () => {
    it('should calculate balance for deposits', () => {
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });

      const balance = getAccountBalance('ACC-12345');
      assert.strictEqual(balance.accountId, 'ACC-12345');
      assert.strictEqual(balance.balance, 1000);
      assert.strictEqual(balance.transactionCount, 1);
    });

    it('should calculate balance for withdrawals', () => {
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      createTransaction({
        fromAccount: 'ACC-12345',
        amount: 300,
        currency: 'USD',
        type: 'withdrawal'
      });

      const balance = getAccountBalance('ACC-12345');
      assert.strictEqual(balance.balance, 700);
      assert.strictEqual(balance.transactionCount, 2);
    });

    it('should calculate balance for transfers (outgoing)', () => {
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      createTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 250,
        currency: 'USD',
        type: 'transfer'
      });

      const balance = getAccountBalance('ACC-12345');
      assert.strictEqual(balance.balance, 750);
    });

    it('should calculate balance for transfers (incoming)', () => {
      createTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 250,
        currency: 'USD',
        type: 'transfer'
      });

      const balance = getAccountBalance('ACC-67890');
      assert.strictEqual(balance.balance, 250);
    });

    it('should return zero balance for account with no transactions', () => {
      const balance = getAccountBalance('ACC-99999');
      assert.strictEqual(balance.balance, 0);
      assert.strictEqual(balance.transactionCount, 0);
      assert.strictEqual(balance.currency, 'USD');  // Default currency
    });

    it('should only count completed transactions', () => {
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit',
        status: 'completed'
      });
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 500,
        currency: 'USD',
        type: 'deposit',
        status: 'pending'
      });
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 200,
        currency: 'USD',
        type: 'deposit',
        status: 'failed'
      });

      const balance = getAccountBalance('ACC-12345');
      assert.strictEqual(balance.balance, 1000);
      assert.strictEqual(balance.transactionCount, 1);
    });

    it('should handle complex transaction history', () => {
      // Initial deposit
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 1000,
        currency: 'USD',
        type: 'deposit'
      });
      // Transfer out
      createTransaction({
        fromAccount: 'ACC-12345',
        toAccount: 'ACC-67890',
        amount: 100,
        currency: 'USD',
        type: 'transfer'
      });
      // Withdrawal
      createTransaction({
        fromAccount: 'ACC-12345',
        amount: 50,
        currency: 'USD',
        type: 'withdrawal'
      });
      // Another deposit
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 200,
        currency: 'USD',
        type: 'deposit'
      });

      const balance = getAccountBalance('ACC-12345');
      // 1000 - 100 - 50 + 200 = 1050
      assert.strictEqual(balance.balance, 1050);
      assert.strictEqual(balance.transactionCount, 4);
    });

    it('should handle floating point precision', () => {
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 100.10,
        currency: 'USD',
        type: 'deposit'
      });
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 0.01,
        currency: 'USD',
        type: 'deposit'
      });
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 0.02,
        currency: 'USD',
        type: 'deposit'
      });

      const balance = getAccountBalance('ACC-12345');
      assert.strictEqual(balance.balance, 100.13);
    });
  });

  describe('clearTransactions', () => {
    it('should remove all transactions', () => {
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });
      createTransaction({
        toAccount: 'ACC-67890',
        amount: 200,
        currency: 'USD',
        type: 'deposit'
      });

      assert.strictEqual(getTransactions().length, 2);
      clearTransactions();
      assert.strictEqual(getTransactions().length, 0);
    });
  });

  describe('getAllTransactionsRaw', () => {
    it('should return the raw transactions array', () => {
      createTransaction({
        toAccount: 'ACC-12345',
        amount: 100,
        currency: 'USD',
        type: 'deposit'
      });

      const raw = getAllTransactionsRaw();
      assert.ok(Array.isArray(raw));
      assert.strictEqual(raw.length, 1);
    });

    it('should return the same reference (not a copy)', () => {
      const raw1 = getAllTransactionsRaw();
      const raw2 = getAllTransactionsRaw();
      assert.strictEqual(raw1, raw2);
    });
  });
});
