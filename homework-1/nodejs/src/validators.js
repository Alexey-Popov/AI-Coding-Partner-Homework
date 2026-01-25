// Validation utilities for transactions and accounts
const VALID_CURRENCIES = [
  'USD', 'EUR', 'GBP', 'JPY', 'AUD', 'CAD', 'CHF', 'CNY', 'INR', 'MXN',
  'SGD', 'HKD', 'NZD', 'SEK', 'NOK', 'DKK', 'ZAR', 'BRL', 'RUB', 'KRW'
];

const ACCOUNT_FORMAT = /^ACC-[A-Z0-9]{5}$/;
const TRANSACTION_TYPES = ['deposit', 'withdrawal', 'transfer'];

/**
 * Validates transaction data
 * @param {Object} transaction - The transaction object to validate
 * @returns {Object} - { isValid: boolean, errors: Array }
 */
function validateTransaction(transaction) {
  const errors = [];

  // Validate fromAccount
  if (!transaction.fromAccount) {
    errors.push({ field: 'fromAccount', message: 'fromAccount is required' });
  } else if (!ACCOUNT_FORMAT.test(transaction.fromAccount)) {
    errors.push({ field: 'fromAccount', message: 'Account number must follow format ACC-XXXXX (where X is alphanumeric)' });
  }

  // Validate toAccount
  if (!transaction.toAccount) {
    errors.push({ field: 'toAccount', message: 'toAccount is required' });
  } else if (!ACCOUNT_FORMAT.test(transaction.toAccount)) {
    errors.push({ field: 'toAccount', message: 'Account number must follow format ACC-XXXXX (where X is alphanumeric)' });
  }

  // Validate amount
  if (transaction.amount === undefined || transaction.amount === null) {
    errors.push({ field: 'amount', message: 'amount is required' });
  } else if (typeof transaction.amount !== 'number' || transaction.amount <= 0) {
    errors.push({ field: 'amount', message: 'Amount must be a positive number' });
  } else if (!/^\d+(\.\d{1,2})?$/.test(transaction.amount.toString())) {
    errors.push({ field: 'amount', message: 'Amount must have maximum 2 decimal places' });
  }

  // Validate currency
  if (!transaction.currency) {
    errors.push({ field: 'currency', message: 'currency is required' });
  } else if (!VALID_CURRENCIES.includes(transaction.currency.toUpperCase())) {
    errors.push({ field: 'currency', message: 'Invalid currency code. Use valid ISO 4217 codes (e.g., USD, EUR, GBP)' });
  }

  // Validate type
  if (!transaction.type) {
    errors.push({ field: 'type', message: 'type is required' });
  } else if (!TRANSACTION_TYPES.includes(transaction.type)) {
    errors.push({ field: 'type', message: 'Type must be one of: deposit, withdrawal, transfer' });
  }

  // Validate that fromAccount and toAccount are different for transfers
  if (transaction.type === 'transfer' && transaction.fromAccount === transaction.toAccount) {
    errors.push({ field: 'accounts', message: 'fromAccount and toAccount cannot be the same for transfers' });
  }

  return {
    isValid: errors.length === 0,
    errors
  };
}

module.exports = {
  validateTransaction,
  VALID_CURRENCIES,
  ACCOUNT_FORMAT,
  TRANSACTION_TYPES
};
