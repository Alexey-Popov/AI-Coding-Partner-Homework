/**
 * Transaction Validation Module
 * Validates all transaction data according to business rules
 * Built with AI assistance (Claude Code)
 */

// Valid ISO 4217 currency codes supported by the system
const VALID_CURRENCIES = ['USD', 'EUR', 'GBP', 'JPY', 'CHF', 'AUD', 'CAD'];

// Valid transaction types
const VALID_TYPES = ['deposit', 'withdrawal', 'transfer'];

// Account format regex: ACC-XXXXX where X is alphanumeric
const ACCOUNT_FORMAT_REGEX = /^ACC-[A-Z0-9]{5}$/i;

/**
 * Validate transaction amount
 * Rules: Must be positive, maximum 2 decimal places
 * @param {number} amount - The transaction amount
 * @returns {object|null} Error object if invalid, null if valid
 */
function validateAmount(amount) {
  // Check if amount exists
  if (amount === undefined || amount === null) {
    return {
      field: 'amount',
      message: 'Amount is required'
    };
  }

  // Convert to number if string
  const numAmount = typeof amount === 'string' ? parseFloat(amount) : amount;

  // Check if it's a valid number
  if (isNaN(numAmount)) {
    return {
      field: 'amount',
      message: 'Amount must be a valid number'
    };
  }

  // Check if positive
  if (numAmount <= 0) {
    return {
      field: 'amount',
      message: 'Amount must be a positive number'
    };
  }

  // Check maximum 2 decimal places
  const decimalPlaces = (numAmount.toString().split('.')[1] || '').length;
  if (decimalPlaces > 2) {
    return {
      field: 'amount',
      message: 'Amount must have maximum 2 decimal places'
    };
  }

  return null; // Valid
}

/**
 * Validate account format
 * Rules: Must match pattern ACC-XXXXX where X is alphanumeric
 * @param {string} account - The account identifier
 * @param {string} fieldName - Name of the field (fromAccount or toAccount)
 * @returns {object|null} Error object if invalid, null if valid
 */
function validateAccountFormat(account, fieldName = 'account') {
  // Check if account exists
  if (!account) {
    return {
      field: fieldName,
      message: `${fieldName} is required`
    };
  }

  // Check if it's a string
  if (typeof account !== 'string') {
    return {
      field: fieldName,
      message: `${fieldName} must be a string`
    };
  }

  // Check format ACC-XXXXX
  if (!ACCOUNT_FORMAT_REGEX.test(account)) {
    return {
      field: fieldName,
      message: `${fieldName} must follow format ACC-XXXXX (where X is alphanumeric)`
    };
  }

  return null; // Valid
}

/**
 * Validate currency code
 * Rules: Must be a valid ISO 4217 currency code from supported list
 * @param {string} currency - The currency code
 * @returns {object|null} Error object if invalid, null if valid
 */
function validateCurrency(currency) {
  // Check if currency exists
  if (!currency) {
    return {
      field: 'currency',
      message: 'Currency is required'
    };
  }

  // Check if it's a string
  if (typeof currency !== 'string') {
    return {
      field: 'currency',
      message: 'Currency must be a string'
    };
  }

  // Convert to uppercase for case-insensitive check
  const upperCurrency = currency.toUpperCase();

  // Check if it's in the valid list
  if (!VALID_CURRENCIES.includes(upperCurrency)) {
    return {
      field: 'currency',
      message: `Invalid currency code. Supported currencies: ${VALID_CURRENCIES.join(', ')}`
    };
  }

  return null; // Valid
}

/**
 * Validate transaction type
 * Rules: Must be one of: deposit, withdrawal, transfer
 * @param {string} type - The transaction type
 * @returns {object|null} Error object if invalid, null if valid
 */
function validateTransactionType(type) {
  // Check if type exists
  if (!type) {
    return {
      field: 'type',
      message: 'Transaction type is required'
    };
  }

  // Check if it's a string
  if (typeof type !== 'string') {
    return {
      field: 'type',
      message: 'Transaction type must be a string'
    };
  }

  // Convert to lowercase for case-insensitive check
  const lowerType = type.toLowerCase();

  // Check if it's in the valid list
  if (!VALID_TYPES.includes(lowerType)) {
    return {
      field: 'type',
      message: `Invalid transaction type. Supported types: ${VALID_TYPES.join(', ')}`
    };
  }

  return null; // Valid
}

/**
 * Validate complete transaction data
 * Runs all validators and returns array of errors
 * @param {object} transactionData - The transaction data to validate
 * @returns {array} Array of validation errors (empty if valid)
 */
function validateTransaction(transactionData) {
  const errors = [];

  // Validate fromAccount
  const fromAccountError = validateAccountFormat(
    transactionData.fromAccount,
    'fromAccount'
  );
  if (fromAccountError) {
    errors.push(fromAccountError);
  }

  // Validate toAccount
  const toAccountError = validateAccountFormat(
    transactionData.toAccount,
    'toAccount'
  );
  if (toAccountError) {
    errors.push(toAccountError);
  }

  // Validate amount
  const amountError = validateAmount(transactionData.amount);
  if (amountError) {
    errors.push(amountError);
  }

  // Validate currency
  const currencyError = validateCurrency(transactionData.currency);
  if (currencyError) {
    errors.push(currencyError);
  }

  // Validate type
  const typeError = validateTransactionType(transactionData.type);
  if (typeError) {
    errors.push(typeError);
  }

  // Additional business logic validations
  // Check that fromAccount and toAccount are different
  if (transactionData.fromAccount && transactionData.toAccount) {
    if (transactionData.fromAccount === transactionData.toAccount) {
      errors.push({
        field: 'accounts',
        message: 'fromAccount and toAccount must be different'
      });
    }
  }

  return errors;
}

/**
 * Create validation error response
 * @param {array} errors - Array of validation errors
 * @returns {object} Formatted error response
 */
function createValidationErrorResponse(errors) {
  return {
    error: 'Validation failed',
    details: errors
  };
}

module.exports = {
  validateAmount,
  validateAccountFormat,
  validateCurrency,
  validateTransactionType,
  validateTransaction,
  createValidationErrorResponse,
  VALID_CURRENCIES,
  VALID_TYPES
};
