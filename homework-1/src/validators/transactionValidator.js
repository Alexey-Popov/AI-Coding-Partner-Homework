// Valid ISO 4217 currency codes
const VALID_CURRENCIES = ['USD', 'EUR', 'GBP', 'JPY', 'AUD', 'CAD', 'CHF', 'CNY', 'SEK', 'NZD'];

// Valid transaction types
const VALID_TYPES = ['deposit', 'withdrawal', 'transfer'];

/**
 * Validates the account number format (ACC-XXXXX)
 */
function validateAccountNumber(accountNumber) {
  const accountRegex = /^ACC-[A-Z0-9]{5}$/;
  return accountRegex.test(accountNumber);
}

/**
 * Validates the amount (positive number with max 2 decimal places)
 */
function validateAmount(amount) {
  if (typeof amount !== 'number' || amount <= 0) {
    return false;
  }
  // Check for max 2 decimal places
  const decimalPlaces = (amount.toString().split('.')[1] || '').length;
  return decimalPlaces <= 2;
}

/**
 * Validates currency code against ISO 4217
 */
function validateCurrency(currency) {
  return VALID_CURRENCIES.includes(currency.toUpperCase());
}

/**
 * Validates transaction type
 */
function validateType(type) {
  return VALID_TYPES.includes(type.toLowerCase());
}

/**
 * Main transaction validation function
 */
function validateTransaction(transaction) {
  const errors = [];

  // Validate amount
  if (!transaction.amount) {
    errors.push({ field: 'amount', message: 'Amount is required' });
  } else if (!validateAmount(transaction.amount)) {
    errors.push({ field: 'amount', message: 'Amount must be a positive number with maximum 2 decimal places' });
  }

  // Validate currency
  if (!transaction.currency) {
    errors.push({ field: 'currency', message: 'Currency is required' });
  } else if (!validateCurrency(transaction.currency)) {
    errors.push({ field: 'currency', message: 'Invalid currency code. Must be a valid ISO 4217 code (e.g., USD, EUR, GBP)' });
  }

  // Validate type
  if (!transaction.type) {
    errors.push({ field: 'type', message: 'Type is required' });
  } else if (!validateType(transaction.type)) {
    errors.push({ field: 'type', message: 'Type must be one of: deposit, withdrawal, transfer' });
  }

  // Validate fromAccount
  if (transaction.type !== 'deposit') {
    if (!transaction.fromAccount) {
      errors.push({ field: 'fromAccount', message: 'From account is required for withdrawal/transfer' });
    } else if (!validateAccountNumber(transaction.fromAccount)) {
      errors.push({ field: 'fromAccount', message: 'From account must follow format ACC-XXXXX (where X is alphanumeric)' });
    }
  }

  // Validate toAccount
  if (transaction.type !== 'withdrawal') {
    if (!transaction.toAccount) {
      errors.push({ field: 'toAccount', message: 'To account is required for deposit/transfer' });
    } else if (!validateAccountNumber(transaction.toAccount)) {
      errors.push({ field: 'toAccount', message: 'To account must follow format ACC-XXXXX (where X is alphanumeric)' });
    }
  }

  return errors;
}

module.exports = {
  validateTransaction,
  validateAccountNumber,
  validateAmount,
  validateCurrency,
  validateType,
  VALID_CURRENCIES,
  VALID_TYPES
};
