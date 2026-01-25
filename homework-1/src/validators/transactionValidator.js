const { VALID_CURRENCIES, VALID_TYPES } = require('../models/transaction');

// Account number format: ACC-XXXXX (where X is alphanumeric)
const ACCOUNT_REGEX = /^ACC-[A-Za-z0-9]{5}$/;

/**
 * Validate a transaction request
 * @param {Object} data - Transaction data to validate
 * @returns {Object} Validation result with isValid and errors array
 */
function validateTransaction(data) {
  const errors = [];

  // Validate amount
  if (data.amount === undefined || data.amount === null) {
    errors.push({ field: 'amount', message: 'Amount is required' });
  } else if (typeof data.amount !== 'number' || isNaN(data.amount)) {
    errors.push({ field: 'amount', message: 'Amount must be a number' });
  } else if (data.amount <= 0) {
    errors.push({ field: 'amount', message: 'Amount must be a positive number' });
  } else if (!hasMaxTwoDecimals(data.amount)) {
    errors.push({ field: 'amount', message: 'Amount must have maximum 2 decimal places' });
  }

  // Validate currency
  if (!data.currency) {
    errors.push({ field: 'currency', message: 'Currency is required' });
  } else if (!VALID_CURRENCIES.includes(data.currency.toUpperCase())) {
    errors.push({
      field: 'currency',
      message: `Invalid currency code. Valid codes: ${VALID_CURRENCIES.join(', ')}`
    });
  }

  // Validate transaction type
  if (!data.type) {
    errors.push({ field: 'type', message: 'Transaction type is required' });
  } else if (!VALID_TYPES.includes(data.type)) {
    errors.push({
      field: 'type',
      message: `Invalid transaction type. Valid types: ${VALID_TYPES.join(', ')}`
    });
  }

  // Validate accounts based on transaction type
  if (data.type === 'deposit') {
    // Deposit requires toAccount
    if (!data.toAccount) {
      errors.push({ field: 'toAccount', message: 'toAccount is required for deposits' });
    } else if (!ACCOUNT_REGEX.test(data.toAccount)) {
      errors.push({ field: 'toAccount', message: 'toAccount must follow format ACC-XXXXX (5 alphanumeric characters)' });
    }
  } else if (data.type === 'withdrawal') {
    // Withdrawal requires fromAccount
    if (!data.fromAccount) {
      errors.push({ field: 'fromAccount', message: 'fromAccount is required for withdrawals' });
    } else if (!ACCOUNT_REGEX.test(data.fromAccount)) {
      errors.push({ field: 'fromAccount', message: 'fromAccount must follow format ACC-XXXXX (5 alphanumeric characters)' });
    }
  } else if (data.type === 'transfer') {
    // Transfer requires both accounts
    if (!data.fromAccount) {
      errors.push({ field: 'fromAccount', message: 'fromAccount is required for transfers' });
    } else if (!ACCOUNT_REGEX.test(data.fromAccount)) {
      errors.push({ field: 'fromAccount', message: 'fromAccount must follow format ACC-XXXXX (5 alphanumeric characters)' });
    }

    if (!data.toAccount) {
      errors.push({ field: 'toAccount', message: 'toAccount is required for transfers' });
    } else if (!ACCOUNT_REGEX.test(data.toAccount)) {
      errors.push({ field: 'toAccount', message: 'toAccount must follow format ACC-XXXXX (5 alphanumeric characters)' });
    }

    // Check that fromAccount and toAccount are different
    if (data.fromAccount && data.toAccount && data.fromAccount === data.toAccount) {
      errors.push({ field: 'toAccount', message: 'fromAccount and toAccount must be different' });
    }
  }

  return {
    isValid: errors.length === 0,
    errors
  };
}

/**
 * Check if a number has maximum 2 decimal places
 * @param {number} num - Number to check
 * @returns {boolean} True if valid
 */
function hasMaxTwoDecimals(num) {
  const decimalPart = num.toString().split('.')[1];
  return !decimalPart || decimalPart.length <= 2;
}

/**
 * Validate account ID format
 * @param {string} accountId - Account ID to validate
 * @returns {boolean} True if valid
 */
function isValidAccountId(accountId) {
  return ACCOUNT_REGEX.test(accountId);
}

module.exports = {
  validateTransaction,
  isValidAccountId,
  ACCOUNT_REGEX
};
