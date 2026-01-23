/**
 * In-memory transaction storage and model
 */

let transactions = [];
let transactionIdCounter = 1000;

/**
 * Create a new transaction
 * @param {Object} data - Transaction data
 * @param {string} data.fromAccount - Source account
 * @param {string} data.toAccount - Destination account
 * @param {number} data.amount - Transaction amount
 * @param {string} data.currency - ISO 4217 currency code
 * @param {string} data.type - Transaction type (deposit|withdrawal|transfer)
 * @returns {Object} Created transaction object
 */
function createTransaction(data) {
  const transaction = {
    id: String(transactionIdCounter++),
    fromAccount: data.fromAccount,
    toAccount: data.toAccount,
    amount: data.amount,
    currency: data.currency,
    type: data.type,
    timestamp: new Date().toISOString(),
    status: 'completed'
  };

  transactions.push(transaction);
  return transaction;
}

/**
 * Get all transactions
 * @returns {Array<Object>} Array of all transactions
 */
function getAllTransactions() {
  return transactions;
}

/**
 * Get transaction by ID
 * @param {string} id - Transaction ID
 * @returns {Object|undefined} Transaction object or undefined if not found
 */
function getTransactionById(id) {
  return transactions.find(t => t.id === id);
}

/**
 * Get transactions with filters
 * @param {Object} filters - Filter criteria
 * @param {string} [filters.accountId] - Filter by account ID
 * @param {string} [filters.type] - Filter by transaction type
 * @param {string} [filters.from] - Filter by start date (ISO format)
 * @param {string} [filters.to] - Filter by end date (ISO format)
 * @returns {Array<Object>} Filtered array of transactions
 */
function getTransactionsFiltered(filters = {}) {
  let result = [...transactions];

  if (filters.accountId) {
    result = result.filter(
      t => t.fromAccount === filters.accountId || t.toAccount === filters.accountId
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
 * Get account balance
 * @param {string} accountId - Account ID to calculate balance for
 * @returns {number} Current account balance
 */
function getAccountBalance(accountId) {
  let balance = 0;

  transactions.forEach(t => {
    // Money leaving this account (withdrawals and transfers out)
    if (t.fromAccount === accountId && (t.type === 'withdrawal' || t.type === 'transfer')) {
      balance -= t.amount;
    }
    // Money entering this account (deposits and transfers in)
    if (t.toAccount === accountId && (t.type === 'deposit' || t.type === 'transfer')) {
      balance += t.amount;
    }
  });

  return balance;
}

module.exports = {
  createTransaction,
  getAllTransactions,
  getTransactionById,
  getTransactionsFiltered,
  getAccountBalance
};
