/**
 * Transaction Model
 * Manages transaction data structure and in-memory storage
 * Built with AI assistance (Claude Code)
 */

const { randomUUID } = require('crypto');

// In-memory storage for transactions
let transactions = [];

/**
 * Transaction schema:
 * - id: auto-generated UUID
 * - fromAccount: string (format ACC-XXXXX)
 * - toAccount: string (format ACC-XXXXX)
 * - amount: number (positive, max 2 decimal places)
 * - currency: string (ISO 4217: USD, EUR, GBP, JPY, CHF, AUD, CAD)
 * - type: string (deposit | withdrawal | transfer)
 * - timestamp: ISO 8601 datetime
 * - status: string (pending | completed | failed)
 */

/**
 * Create a new transaction
 * @param {object} transactionData - Transaction details
 * @returns {object} Created transaction with id and timestamp
 */
function createTransaction(transactionData) {
  const transaction = {
    id: randomUUID(),
    fromAccount: transactionData.fromAccount,
    toAccount: transactionData.toAccount,
    amount: parseFloat(transactionData.amount),
    currency: transactionData.currency.toUpperCase(),
    type: transactionData.type,
    timestamp: new Date().toISOString(),
    status: transactionData.status || 'completed'
  };

  transactions.push(transaction);
  return transaction;
}

/**
 * Get all transactions
 * @returns {array} All transactions
 */
function getAllTransactions() {
  return transactions;
}

/**
 * Get transaction by ID
 * @param {string} id - Transaction ID
 * @returns {object|null} Transaction or null if not found
 */
function getTransactionById(id) {
  return transactions.find(t => t.id === id) || null;
}

/**
 * Get transactions by account ID
 * @param {string} accountId - Account ID
 * @returns {array} Transactions involving this account
 */
function getTransactionsByAccount(accountId) {
  return transactions.filter(t =>
    t.fromAccount === accountId || t.toAccount === accountId
  );
}

/**
 * Get transactions by type
 * @param {string} type - Transaction type (deposit, withdrawal, transfer)
 * @returns {array} Transactions of specified type
 */
function getTransactionsByType(type) {
  return transactions.filter(t => t.type === type);
}

/**
 * Filter transactions by date range
 * @param {string} fromDate - Start date (ISO 8601)
 * @param {string} toDate - End date (ISO 8601)
 * @returns {array} Transactions within date range
 */
function getTransactionsByDateRange(fromDate, toDate) {
  return transactions.filter(t => {
    const transactionDate = new Date(t.timestamp);
    const start = fromDate ? new Date(fromDate) : new Date(0);
    const end = toDate ? new Date(toDate) : new Date();
    return transactionDate >= start && transactionDate <= end;
  });
}

/**
 * Calculate account balance
 * @param {string} accountId - Account ID
 * @returns {object} Balance information
 */
function calculateBalance(accountId) {
  const accountTransactions = getTransactionsByAccount(accountId);

  let balance = 0;
  const currencies = new Set();

  accountTransactions.forEach(t => {
    currencies.add(t.currency);

    // Add incoming amounts (deposits and incoming transfers)
    if (t.toAccount === accountId) {
      balance += t.amount;
    }

    // Subtract outgoing amounts (withdrawals and outgoing transfers)
    if (t.fromAccount === accountId) {
      balance -= t.amount;
    }
  });

  return {
    accountId,
    balance: Math.round(balance * 100) / 100, // Round to 2 decimal places
    currency: currencies.size === 1 ? Array.from(currencies)[0] : 'MIXED',
    transactionCount: accountTransactions.length
  };
}

/**
 * Clear all transactions (useful for testing)
 */
function clearAllTransactions() {
  transactions = [];
}

/**
 * Get transaction count
 * @returns {number} Total number of transactions
 */
function getTransactionCount() {
  return transactions.length;
}

module.exports = {
  createTransaction,
  getAllTransactions,
  getTransactionById,
  getTransactionsByAccount,
  getTransactionsByType,
  getTransactionsByDateRange,
  calculateBalance,
  clearAllTransactions,
  getTransactionCount
};
