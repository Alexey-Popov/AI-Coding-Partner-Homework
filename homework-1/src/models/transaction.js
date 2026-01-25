const { v4: uuidv4 } = require('uuid');

// In-memory storage for transactions
const transactions = [];

// Valid ISO 4217 currency codes (subset of commonly used ones)
const VALID_CURRENCIES = ['USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'CNY', 'INR', 'PLN'];

// Valid transaction types
const VALID_TYPES = ['deposit', 'withdrawal', 'transfer'];

// Valid transaction statuses
const VALID_STATUSES = ['pending', 'completed', 'failed'];

/**
 * Create a new transaction
 * @param {Object} data - Transaction data
 * @returns {Object} Created transaction
 */
function createTransaction(data) {
  const transaction = {
    id: uuidv4(),
    fromAccount: data.fromAccount,
    toAccount: data.toAccount,
    amount: data.amount,
    currency: data.currency,
    type: data.type,
    timestamp: new Date().toISOString(),
    status: data.status || 'completed'
  };

  transactions.push(transaction);
  return transaction;
}

/**
 * Get all transactions with optional filtering
 * @param {Object} filters - Filter criteria
 * @returns {Array} Filtered transactions
 */
function getTransactions(filters = {}) {
  let result = [...transactions];

  // Filter by accountId (matches either fromAccount or toAccount)
  if (filters.accountId) {
    result = result.filter(
      t => t.fromAccount === filters.accountId || t.toAccount === filters.accountId
    );
  }

  // Filter by transaction type
  if (filters.type) {
    result = result.filter(t => t.type === filters.type);
  }

  // Filter by date range (from)
  if (filters.from) {
    const fromDate = new Date(filters.from);
    result = result.filter(t => new Date(t.timestamp) >= fromDate);
  }

  // Filter by date range (to)
  if (filters.to) {
    const toDate = new Date(filters.to);
    // Set to end of day for inclusive filtering
    toDate.setHours(23, 59, 59, 999);
    result = result.filter(t => new Date(t.timestamp) <= toDate);
  }

  return result;
}

/**
 * Get a transaction by ID
 * @param {string} id - Transaction ID
 * @returns {Object|null} Transaction or null if not found
 */
function getTransactionById(id) {
  return transactions.find(t => t.id === id) || null;
}

/**
 * Calculate account balance based on transactions
 * @param {string} accountId - Account ID
 * @returns {Object} Balance information
 */
function getAccountBalance(accountId) {
  const accountTransactions = transactions.filter(
    t => (t.fromAccount === accountId || t.toAccount === accountId) && t.status === 'completed'
  );

  let balance = 0;

  for (const t of accountTransactions) {
    if (t.type === 'deposit' && t.toAccount === accountId) {
      balance += t.amount;
    } else if (t.type === 'withdrawal' && t.fromAccount === accountId) {
      balance -= t.amount;
    } else if (t.type === 'transfer') {
      if (t.fromAccount === accountId) {
        balance -= t.amount;
      }
      if (t.toAccount === accountId) {
        balance += t.amount;
      }
    }
  }

  // Round to 2 decimal places to avoid floating point issues
  balance = Math.round(balance * 100) / 100;

  return {
    accountId,
    balance,
    currency: accountTransactions.length > 0 ? accountTransactions[0].currency : 'USD',
    transactionCount: accountTransactions.length
  };
}

/**
 * Clear all transactions (useful for testing)
 */
function clearTransactions() {
  transactions.length = 0;
}

/**
 * Get all transactions (raw, for export)
 */
function getAllTransactionsRaw() {
  return transactions;
}

module.exports = {
  createTransaction,
  getTransactions,
  getTransactionById,
  getAccountBalance,
  clearTransactions,
  getAllTransactionsRaw,
  VALID_CURRENCIES,
  VALID_TYPES,
  VALID_STATUSES
};
