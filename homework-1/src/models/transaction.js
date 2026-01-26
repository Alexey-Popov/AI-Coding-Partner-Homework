const { generateTransactionId, isDateInRange } = require('../utils/helpers');

// In-memory storage
const transactions = [];
const accountBalances = {};

/**
 * Create a new transaction
 */
function createTransaction(data) {
  const transaction = {
    id: generateTransactionId(),
    fromAccount: data.fromAccount || null,
    toAccount: data.toAccount || null,
    amount: data.amount,
    currency: data.currency,
    type: data.type,
    timestamp: new Date().toISOString(),
    status: 'completed'
  };

  transactions.push(transaction);
  updateBalances(transaction);

  return transaction;
}

/**
 * Update account balances based on transaction
 */
function updateBalances(transaction) {
  const { fromAccount, toAccount, amount, type } = transaction;

  // Initialize accounts if they don't exist
  if (fromAccount && !(fromAccount in accountBalances)) {
    accountBalances[fromAccount] = 0;
  }
  if (toAccount && !(toAccount in accountBalances)) {
    accountBalances[toAccount] = 0;
  }

  switch (type) {
    case 'deposit':
      // Deposit adds to toAccount
      if (toAccount) {
        accountBalances[toAccount] += amount;
      }
      break;
    case 'withdrawal':
      // Withdrawal subtracts from fromAccount
      if (fromAccount) {
        accountBalances[fromAccount] -= amount;
      }
      break;
    case 'transfer':
      // Transfer moves from fromAccount to toAccount
      if (fromAccount) {
        accountBalances[fromAccount] -= amount;
      }
      if (toAccount) {
        accountBalances[toAccount] += amount;
      }
      break;
  }
}

/**
 * Get all transactions with optional filters
 */
function getTransactions(filters = {}) {
  let result = [...transactions];

  // Filter by account (from or to)
  if (filters.accountId) {
    result = result.filter(
      t => t.fromAccount === filters.accountId || t.toAccount === filters.accountId
    );
  }

  // Filter by type
  if (filters.type) {
    result = result.filter(t => t.type === filters.type);
  }

  // Filter by date range
  if (filters.from || filters.to) {
    result = result.filter(t => isDateInRange(t.timestamp, filters.from, filters.to));
  }

  return result;
}

/**
 * Get transaction by ID
 */
function getTransactionById(id) {
  return transactions.find(t => t.id === id) || null;
}

/**
 * Get account balance
 */
function getAccountBalance(accountId) {
  if (!(accountId in accountBalances)) {
    return null;
  }
  return accountBalances[accountId];
}

/**
 * Check if account exists (has any transactions)
 */
function accountExists(accountId) {
  return accountId in accountBalances;
}

/**
 * Get all account IDs
 */
function getAllAccounts() {
  return Object.keys(accountBalances);
}

module.exports = {
  createTransaction,
  getTransactions,
  getTransactionById,
  getAccountBalance,
  accountExists,
  getAllAccounts
};
