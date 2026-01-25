import { VALID_CURRENCIES, VALID_TYPES } from '../models/transaction.js';

// Account number format: ACC-XXXXX (where X is alphanumeric)
export const ACCOUNT_REGEX = /^ACC-[A-Za-z0-9]{5}$/;

/**
 * Check if a number has maximum 2 decimal places
 * @param {number} num - Number to check
 * @returns {boolean} True if valid
 */
const hasMaxTwoDecimals = (num) => {
  const decimalPart = num.toString().split('.')[1];
  return !decimalPart || decimalPart.length <= 2;
};

/**
 * Validate amount field
 * @param {*} amount - Amount to validate
 * @returns {Array} Array of error objects
 */
const validateAmount = (amount) => {
  if (amount === undefined || amount === null) {
    return [{ field: 'amount', message: 'Amount is required' }];
  }
  if (typeof amount !== 'number' || Number.isNaN(amount)) {
    return [{ field: 'amount', message: 'Amount must be a number' }];
  }
  if (amount <= 0) {
    return [{ field: 'amount', message: 'Amount must be a positive number' }];
  }
  if (!hasMaxTwoDecimals(amount)) {
    return [{ field: 'amount', message: 'Amount must have maximum 2 decimal places' }];
  }
  return [];
};

/**
 * Validate currency field
 * @param {string} currency - Currency to validate
 * @returns {Array} Array of error objects
 */
const validateCurrency = (currency) => {
  if (!currency) {
    return [{ field: 'currency', message: 'Currency is required' }];
  }
  if (!VALID_CURRENCIES.includes(currency.toUpperCase())) {
    return [{
      field: 'currency',
      message: `Invalid currency code. Valid codes: ${VALID_CURRENCIES.join(', ')}`
    }];
  }
  return [];
};

/**
 * Validate transaction type field
 * @param {string} type - Type to validate
 * @returns {Array} Array of error objects
 */
const validateType = (type) => {
  if (!type) {
    return [{ field: 'type', message: 'Transaction type is required' }];
  }
  if (!VALID_TYPES.includes(type)) {
    return [{
      field: 'type',
      message: `Invalid transaction type. Valid types: ${VALID_TYPES.join(', ')}`
    }];
  }
  return [];
};

/**
 * Validate account field
 * @param {string} account - Account to validate
 * @param {string} fieldName - Field name for error message
 * @param {string} transactionType - Type of transaction
 * @returns {Array} Array of error objects
 */
const validateAccount = (account, fieldName, transactionType) => {
  if (!account) {
    return [{ field: fieldName, message: `${fieldName} is required for ${transactionType}s` }];
  }
  if (!ACCOUNT_REGEX.test(account)) {
    return [{ field: fieldName, message: `${fieldName} must follow format ACC-XXXXX (5 alphanumeric characters)` }];
  }
  return [];
};

/**
 * Validate accounts based on transaction type
 * @param {Object} data - Transaction data
 * @returns {Array} Array of error objects
 */
const validateAccountsByType = ({ type, fromAccount, toAccount }) => {
  const errors = [];

  switch (type) {
    case 'deposit':
      errors.push(...validateAccount(toAccount, 'toAccount', 'deposit'));
      break;

    case 'withdrawal':
      errors.push(...validateAccount(fromAccount, 'fromAccount', 'withdrawal'));
      break;

    case 'transfer':
      errors.push(...validateAccount(fromAccount, 'fromAccount', 'transfer'));
      errors.push(...validateAccount(toAccount, 'toAccount', 'transfer'));

      // Check that fromAccount and toAccount are different
      if (fromAccount && toAccount && fromAccount === toAccount) {
        errors.push({ field: 'toAccount', message: 'fromAccount and toAccount must be different' });
      }
      break;
  }

  return errors;
};

/**
 * Validate a transaction request
 * @param {Object} data - Transaction data to validate
 * @returns {Object} Validation result with isValid and errors array
 */
export const validateTransaction = (data) => {
  const errors = [
    ...validateAmount(data.amount),
    ...validateCurrency(data.currency),
    ...validateType(data.type),
    ...validateAccountsByType(data)
  ];

  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Validate account ID format
 * @param {string} accountId - Account ID to validate
 * @returns {boolean} True if valid
 */
export const isValidAccountId = (accountId) => ACCOUNT_REGEX.test(accountId);
