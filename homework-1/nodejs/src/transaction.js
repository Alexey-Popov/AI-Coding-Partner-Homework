// Transaction model and storage
const { v4: uuidv4 } = require('uuid');

/**
 * In-memory storage for transactions and account balances
 */
const transactions = [];
const accountBalances = {};

/**
 * Creates a new transaction
 * @param {Object} data - Transaction data
 * @returns {Object} - Created transaction
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
    status: 'completed'
  };

  transactions.push(transaction);

  // Update account balances
  updateAccountBalances(transaction);

  return transaction;
}

/**
 * Updates account balances based on transaction
 * @param {Object} transaction - Transaction object
 */
function updateAccountBalances(transaction) {
  const amount = transaction.amount;

  if (!accountBalances[transaction.fromAccount]) {
    accountBalances[transaction.fromAccount] = 0;
  }
  if (!accountBalances[transaction.toAccount]) {
    accountBalances[transaction.toAccount] = 0;
  }

  if (transaction.type === 'transfer') {
    accountBalances[transaction.fromAccount] -= amount;
    accountBalances[transaction.toAccount] += amount;
  } else if (transaction.type === 'deposit') {
    accountBalances[transaction.toAccount] += amount;
  } else if (transaction.type === 'withdrawal') {
    accountBalances[transaction.fromAccount] -= amount;
  }
}

/**
 * Gets all transactions with optional filtering
 * @param {Object} filters - Filtering criteria
 * @returns {Array} - Filtered transactions
 */
function getTransactions(filters = {}) {
  let result = [...transactions];

  if (filters.accountId) {
    result = result.filter(t => 
      t.fromAccount === filters.accountId || t.toAccount === filters.accountId
    );
  }

  if (filters.type) {
    result = result.filter(t => t.type === filters.type);
  }

  if (filters.from) {
    const fromDate = new Date(filters.from);
    result = result.filter(t => new Date(t.timestamp) >= fromDate);
  }

  if (filters.to) {
    const toDate = new Date(filters.to);
    result = result.filter(t => new Date(t.timestamp) <= toDate);
  }

  return result;
}

/**
 * Gets a transaction by ID
 * @param {string} id - Transaction ID
 * @returns {Object|null} - Transaction or null if not found
 */
function getTransactionById(id) {
  return transactions.find(t => t.id === id) || null;
}

/**
 * Gets account balance
 * @param {string} accountId - Account ID
 * @returns {Object} - { accountId, balance, currency }
 */
function getAccountBalance(accountId) {
  return {
    accountId,
    balance: accountBalances[accountId] || 0
  };
}

/**
 * Gets account summary
 * @param {string} accountId - Account ID
 * @returns {Object} - Summary with deposits, withdrawals, etc.
 */
function getAccountSummary(accountId) {
  const accountTransactions = transactions.filter(t =>
    t.fromAccount === accountId || t.toAccount === accountId
  );

  const deposits = accountTransactions
    .filter(t => t.toAccount === accountId && (t.type === 'deposit' || t.type === 'transfer'))
    .reduce((sum, t) => sum + t.amount, 0);

  const withdrawals = accountTransactions
    .filter(t => t.fromAccount === accountId && (t.type === 'withdrawal' || t.type === 'transfer'))
    .reduce((sum, t) => sum + t.amount, 0);

  const mostRecentTransaction = accountTransactions.length > 0
    ? accountTransactions.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))[0].timestamp
    : null;

  return {
    accountId,
    totalDeposits: deposits,
    totalWithdrawals: withdrawals,
    numberOfTransactions: accountTransactions.length,
    mostRecentTransactionDate: mostRecentTransaction,
    currentBalance: accountBalances[accountId] || 0
  };
}

/**
 * Resets all storage (for testing)
 */
function resetStorage() {
  transactions.length = 0;
  Object.keys(accountBalances).forEach(key => delete accountBalances[key]);
}

module.exports = {
  createTransaction,
  getTransactions,
  getTransactionById,
  getAccountBalance,
  getAccountSummary,
  transactions,
  accountBalances,
  resetStorage
};
