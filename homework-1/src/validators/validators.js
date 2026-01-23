/**
 * Validation functions for transaction data
 */

// Valid ISO 4217 currency codes
const VALID_CURRENCIES = [
  'USD', 'EUR', 'GBP', 'JPY', 'AUD', 'CAD', 'CHF', 'CNY', 'SEK',
  'NZD', 'MXN', 'SGD', 'HKD', 'NOK', 'KRW', 'TRY', 'RUB', 'INR',
  'BRL', 'ZAR'
];

/**
 * Validate amount is positive and has max 2 decimal places
 * @param {number} amount - Transaction amount to validate
 * @returns {{valid: boolean, error?: string}} Validation result
 */
function validateAmount(amount) {
  if (typeof amount !== 'number' || isNaN(amount)) {
    return { valid: false, error: 'Amount must be a number' };
  }

  if (amount <= 0) {
    return { valid: false, error: 'Amount must be positive' };
  }

  // Check decimal places
  const decimalPlaces = (amount.toString().split('.')[1] || '').length;
  if (decimalPlaces > 2) {
    return { valid: false, error: 'Amount can have maximum 2 decimal places' };
  }

  return { valid: true };
}

/**
 * Validate account format (ACC-XXXXX)
 * @param {string} account - Account identifier to validate
 * @returns {{valid: boolean, error?: string}} Validation result
 */
function validateAccountFormat(account) {
  if (typeof account !== 'string') {
    return { valid: false, error: 'Account must be a string' };
  }

  const accountRegex = /^ACC-[A-Za-z0-9]{5}$/;
  if (!accountRegex.test(account)) {
    return { valid: false, error: 'Account must be in format ACC-XXXXX (alphanumeric)' };
  }

  return { valid: true };
}

/**
 * Validate currency is ISO 4217
 * @param {string} currency - Currency code to validate
 * @returns {{valid: boolean, error?: string}} Validation result
 */
function validateCurrency(currency) {
  if (typeof currency !== 'string') {
    return { valid: false, error: 'Currency must be a string' };
  }

  if (!VALID_CURRENCIES.includes(currency.toUpperCase())) {
    return { valid: false, error: `Currency must be a valid ISO 4217 code (e.g., ${VALID_CURRENCIES.slice(0, 5).join(', ')})` };
  }

  return { valid: true };
}

/**
 * Validate transaction type
 * @param {string} type - Transaction type
 * @returns {{valid: boolean, error?: string}} Validation result
 */
function validateType(type) {
  const validTypes = ['deposit', 'withdrawal', 'transfer'];
  if (!validTypes.includes(type)) {
    return { valid: false, error: `Type must be one of: ${validTypes.join(', ')}` };
  }

  return { valid: true };
}

/**
 * Validate transaction type matches account requirements
 * @param {string} type - Transaction type
 * @param {string} fromAccount - Source account
 * @param {string} toAccount - Destination account
 * @returns {{valid: boolean, error?: string}} Validation result
 */
function validateTypeAccountConsistency(type, fromAccount, toAccount) {
  if (type === 'deposit' && fromAccount) {
    return { valid: false, error: 'Deposits should not have a fromAccount (only toAccount)' };
  }
  
  if (type === 'withdrawal' && toAccount) {
    return { valid: false, error: 'Withdrawals should not have a toAccount (only fromAccount)' };
  }
  
  if (type === 'transfer' && (!fromAccount || !toAccount)) {
    return { valid: false, error: 'Transfers must have both fromAccount and toAccount' };
  }
  
  return { valid: true };
}

/**
 * Validate date string format
 * @param {string} dateStr - Date string to validate
 * @returns {{valid: boolean, error?: string}} Validation result
 */
function validateDateFormat(dateStr) {
  if (typeof dateStr !== 'string') {
    return { valid: false, error: 'Date must be a string' };
  }
  
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) {
    return { valid: false, error: 'Invalid date format. Use ISO 8601 format (e.g., 2024-01-01)' };
  }
  
  return { valid: true };
}

/**
 * Validate complete transaction data
 * @param {Object} data - Transaction data to validate
 * @param {string} [data.fromAccount] - Source account
 * @param {string} [data.toAccount] - Destination account
 * @param {number} data.amount - Transaction amount
 * @param {string} data.currency - Currency code
 * @param {string} data.type - Transaction type
 * @returns {{valid: boolean, errors: Object}} Validation result with error details
 */
function validateTransactionData(data) {
  const errors = {};

  // Validate type first
  if (!data.type) {
    errors.type = 'type is required';
  } else {
    const typeValidation = validateType(data.type);
    if (!typeValidation.valid) {
      errors.type = typeValidation.error;
    }
  }

  // Validate accounts based on transaction type
  const shouldHaveFromAccount = data.type === 'withdrawal' || data.type === 'transfer';
  const shouldHaveToAccount = data.type === 'deposit' || data.type === 'transfer';

  if (shouldHaveFromAccount && !data.fromAccount) {
    errors.fromAccount = `fromAccount is required for ${data.type} transactions`;
  } else if (data.fromAccount) {
    const fromValidation = validateAccountFormat(data.fromAccount);
    if (!fromValidation.valid) {
      errors.fromAccount = fromValidation.error;
    }
  }

  if (shouldHaveToAccount && !data.toAccount) {
    errors.toAccount = `toAccount is required for ${data.type} transactions`;
  } else if (data.toAccount) {
    const toValidation = validateAccountFormat(data.toAccount);
    if (!toValidation.valid) {
      errors.toAccount = toValidation.error;
    }
  }

  // Validate type-account consistency
  if (data.type && !errors.type) {
    const consistencyValidation = validateTypeAccountConsistency(data.type, data.fromAccount, data.toAccount);
    if (!consistencyValidation.valid) {
      errors.transaction = consistencyValidation.error;
    }
  }

  // Validate amount
  const amountValidation = validateAmount(data.amount);
  if (!amountValidation.valid) {
    errors.amount = amountValidation.error;
  }

  // Validate currency
  if (!data.currency) {
    errors.currency = 'currency is required';
  } else {
    const currencyValidation = validateCurrency(data.currency);
    if (!currencyValidation.valid) {
      errors.currency = currencyValidation.error;
    }
  }

  return {
    valid: Object.keys(errors).length === 0,
    errors
  };
}

module.exports = {
  validateAmount,
  validateAccountFormat,
  validateCurrency,
  validateType,
  validateTypeAccountConsistency,
  validateDateFormat,
  validateTransactionData,
  VALID_CURRENCIES
};
