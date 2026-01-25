import { v4 as uuidv4 } from 'uuid';

// In-memory storage for transactions
const transactions = [];

// Valid ISO 4217 currency codes (subset of commonly used ones)
export const VALID_CURRENCIES = Object.freeze(['USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'CNY', 'INR', 'PLN']);

// Valid transaction types
export const VALID_TYPES = Object.freeze(['deposit', 'withdrawal', 'transfer']);

// Valid transaction statuses
export const VALID_STATUSES = Object.freeze(['pending', 'completed', 'failed']);

/**
 * Create a new transaction
 * @param {Object} data - Transaction data
 * @returns {Object} Created transaction
 */
export const createTransaction = ({ fromAccount, toAccount, amount, currency, type, status = 'completed' }) => {
  const transaction = {
    id: uuidv4(),
    fromAccount,
    toAccount,
    amount,
    currency,
    type,
    timestamp: new Date().toISOString(),
    status
  };

  transactions.push(transaction);
  return transaction;
};

/**
 * Get all transactions with optional filtering
 * @param {Object} filters - Filter criteria
 * @returns {Array} Filtered transactions
 */
export const getTransactions = ({ accountId, type, from, to } = {}) => {
  let result = [...transactions];

  // Filter by accountId (matches either fromAccount or toAccount)
  if (accountId) {
    result = result.filter(
      (t) => t.fromAccount === accountId || t.toAccount === accountId
    );
  }

  // Filter by transaction type
  if (type) {
    result = result.filter((t) => t.type === type);
  }

  // Filter by date range (from)
  if (from) {
    const fromDate = new Date(from);
    result = result.filter((t) => new Date(t.timestamp) >= fromDate);
  }

  // Filter by date range (to)
  if (to) {
    const toDate = new Date(to);
    // Set to end of day for inclusive filtering
    toDate.setHours(23, 59, 59, 999);
    result = result.filter((t) => new Date(t.timestamp) <= toDate);
  }

  return result;
};

/**
 * Get a transaction by ID
 * @param {string} id - Transaction ID
 * @returns {Object|null} Transaction or null if not found
 */
export const getTransactionById = (id) => transactions.find((t) => t.id === id) ?? null;

/**
 * Calculate account balance based on transactions
 * @param {string} accountId - Account ID
 * @returns {Object} Balance information
 */
export const getAccountBalance = (accountId) => {
  const accountTransactions = transactions.filter(
    (t) => (t.fromAccount === accountId || t.toAccount === accountId) && t.status === 'completed'
  );

  const balance = accountTransactions.reduce((acc, t) => {
    if (t.type === 'deposit' && t.toAccount === accountId) {
      return acc + t.amount;
    }
    if (t.type === 'withdrawal' && t.fromAccount === accountId) {
      return acc - t.amount;
    }
    if (t.type === 'transfer') {
      let delta = 0;
      if (t.fromAccount === accountId) delta -= t.amount;
      if (t.toAccount === accountId) delta += t.amount;
      return acc + delta;
    }
    return acc;
  }, 0);

  // Round to 2 decimal places to avoid floating point issues
  const roundedBalance = Math.round(balance * 100) / 100;

  return {
    accountId,
    balance: roundedBalance,
    currency: accountTransactions[0]?.currency ?? 'USD',
    transactionCount: accountTransactions.length
  };
};

/**
 * Clear all transactions (useful for testing)
 */
export const clearTransactions = () => {
  transactions.length = 0;
};

/**
 * Get all transactions (raw, for export)
 */
export const getAllTransactionsRaw = () => transactions;
