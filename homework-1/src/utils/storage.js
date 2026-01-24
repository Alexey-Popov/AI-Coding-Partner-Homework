const { v4: uuidv4 } = require('uuid');

// In-memory storage
let transactions = [];
let accounts = {};

/**
 * Create a new transaction
 */
function createTransaction(transactionData) {
  const transaction = {
    id: uuidv4(),
    ...transactionData,
    timestamp: new Date().toISOString(),
    status: 'completed'
  };
  
  transactions.push(transaction);
  
  // Update account balances
  updateAccountBalances(transaction);
  
  return transaction;
}

/**
 * Get all transactions
 */
function getAllTransactions() {
  return transactions;
}

/**
 * Get transaction by ID
 */
function getTransactionById(id) {
  return transactions.find(t => t.id === id);
}

/**
 * Update account balances based on transaction
 */
function updateAccountBalances(transaction) {
  const { fromAccount, toAccount, amount, type } = transaction;
  
  // Initialize accounts if they don't exist
  if (fromAccount && !accounts[fromAccount]) {
    accounts[fromAccount] = 0;
  }
  if (toAccount && !accounts[toAccount]) {
    accounts[toAccount] = 0;
  }
  
  // Update balances based on transaction type
  if (type === 'deposit') {
    accounts[toAccount] = (accounts[toAccount] || 0) + amount;
  } else if (type === 'withdrawal') {
    accounts[fromAccount] = (accounts[fromAccount] || 0) - amount;
  } else if (type === 'transfer') {
    accounts[fromAccount] = (accounts[fromAccount] || 0) - amount;
    accounts[toAccount] = (accounts[toAccount] || 0) + amount;
  }
}

/**
 * Get account balance
 */
function getAccountBalance(accountId) {
  if (!accounts[accountId]) {
    return null;
  }
  return accounts[accountId];
}

module.exports = {
  createTransaction,
  getAllTransactions,
  getTransactionById,
  getAccountBalance
};
