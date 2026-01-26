// Valid ISO 4217 currency codes
const VALID_CURRENCIES = [
  'USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'NZD',
  'CNY', 'INR', 'BRL', 'MXN', 'KRW', 'SGD', 'HKD', 'SEK',
  'NOK', 'DKK', 'PLN', 'CZK', 'HUF', 'RUB', 'ZAR', 'TRY'
];

// Valid transaction types
const VALID_TYPES = ['deposit', 'withdrawal', 'transfer'];

/**
 * Validates account number format: ACC-XXXXX (X is alphanumeric)
 */
function isValidAccountNumber(account) {
  const pattern = /^ACC-[A-Za-z0-9]{5}$/;
  return pattern.test(account);
}

/**
 * Validates amount: positive number with max 2 decimal places
 */
function isValidAmount(amount) {
  if (typeof amount !== 'number' || isNaN(amount)) {
    return false;
  }
  if (amount <= 0) {
    return false;
  }
  // Check max 2 decimal places
  const decimalPlaces = (amount.toString().split('.')[1] || '').length;
  return decimalPlaces <= 2;
}

/**
 * Validates currency code (ISO 4217)
 */
function isValidCurrency(currency) {
  return VALID_CURRENCIES.includes(currency);
}

/**
 * Validates transaction type
 */
function isValidType(type) {
  return VALID_TYPES.includes(type);
}

/**
 * Validates a transaction object and returns array of errors
 */
function validateTransaction(transaction) {
  const errors = [];

  // Validate amount
  if (transaction.amount === undefined || transaction.amount === null) {
    errors.push({ field: 'amount', message: 'Amount is required' });
  } else if (!isValidAmount(transaction.amount)) {
    if (typeof transaction.amount !== 'number') {
      errors.push({ field: 'amount', message: 'Amount must be a number' });
    } else if (transaction.amount <= 0) {
      errors.push({ field: 'amount', message: 'Amount must be a positive number' });
    } else {
      errors.push({ field: 'amount', message: 'Amount must have maximum 2 decimal places' });
    }
  }

  // Validate fromAccount
  if (!transaction.fromAccount) {
    errors.push({ field: 'fromAccount', message: 'fromAccount is required' });
  } else if (!isValidAccountNumber(transaction.fromAccount)) {
    errors.push({ field: 'fromAccount', message: 'fromAccount must follow format ACC-XXXXX (X is alphanumeric)' });
  }

  // Validate toAccount
  if (!transaction.toAccount) {
    errors.push({ field: 'toAccount', message: 'toAccount is required' });
  } else if (!isValidAccountNumber(transaction.toAccount)) {
    errors.push({ field: 'toAccount', message: 'toAccount must follow format ACC-XXXXX (X is alphanumeric)' });
  }

  // Validate currency
  if (!transaction.currency) {
    errors.push({ field: 'currency', message: 'Currency is required' });
  } else if (!isValidCurrency(transaction.currency)) {
    errors.push({ field: 'currency', message: 'Invalid currency code. Must be a valid ISO 4217 code' });
  }

  // Validate type
  if (!transaction.type) {
    errors.push({ field: 'type', message: 'Transaction type is required' });
  } else if (!isValidType(transaction.type)) {
    errors.push({ field: 'type', message: 'Type must be one of: deposit, withdrawal, transfer' });
  }

  return errors;
}

module.exports = {
  validateTransaction,
  isValidAccountNumber,
  isValidAmount,
  isValidCurrency,
  isValidType,
  VALID_CURRENCIES,
  VALID_TYPES
};
