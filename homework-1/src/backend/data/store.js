// In-memory storage for transactions
const transactions = [];

// Counter for generating unique IDs
let idCounter = 1;

/**
 * Generate a unique transaction ID
 */
function generateId() {
  return `TXN-${String(idCounter++).padStart(6, '0')}`;
}

/**
 * Get all transactions
 */
function getAllTransactions() {
  return transactions;
}

/**
 * Get a transaction by ID
 */
function getTransactionById(id) {
  return transactions.find(t => t.id === id);
}

/**
 * Add a new transaction
 */
function addTransaction(transactionData) {
  const transaction = {
    id: generateId(),
    fromAccount: transactionData.fromAccount,
    toAccount: transactionData.toAccount,
    amount: transactionData.amount,
    currency: transactionData.currency,
    type: transactionData.type,
    timestamp: new Date().toISOString(),
    status: 'completed'
  };
  transactions.push(transaction);
  return transaction;
}

/**
 * Get transactions filtered by criteria
 */
function getFilteredTransactions(filters) {
  let result = [...transactions];

  // Filter by accountId (matches fromAccount or toAccount)
  if (filters.accountId) {
    result = result.filter(t =>
      t.fromAccount === filters.accountId || t.toAccount === filters.accountId
    );
  }

  // Filter by type
  if (filters.type) {
    result = result.filter(t => t.type === filters.type);
  }

  // Filter by date range
  if (filters.from) {
    const fromDate = new Date(filters.from);
    result = result.filter(t => new Date(t.timestamp) >= fromDate);
  }

  if (filters.to) {
    const toDate = new Date(filters.to);
    // Set to end of day
    toDate.setHours(23, 59, 59, 999);
    result = result.filter(t => new Date(t.timestamp) <= toDate);
  }

  return result;
}

/**
 * Calculate account balance based on transactions
 */
function getAccountBalance(accountId) {
  let balance = 0;

  for (const t of transactions) {
    if (t.status !== 'completed') continue;

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

  return balance;
}

module.exports = {
  getAllTransactions,
  getTransactionById,
  addTransaction,
  getFilteredTransactions,
  getAccountBalance
};
