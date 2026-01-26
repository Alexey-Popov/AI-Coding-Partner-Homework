// Valid currency codes (ISO 4217)
const VALID_CURRENCIES = ['USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'CNY'];

// Valid transaction types
const VALID_TYPES = ['deposit', 'withdrawal', 'transfer'];

// Account format: ACC-XXXXX (X is alphanumeric uppercase)
const ACCOUNT_REGEX = /^ACC-[A-Z0-9]{5}$/;

/**
 * Validate amount
 * - Must be a positive number
 * - Maximum 2 decimal places
 */
function validateAmount(amount) {
  if (typeof amount !== 'number' || isNaN(amount)) {
    return 'Amount must be a number';
  }
  if (amount <= 0) {
    return 'Amount must be a positive number';
  }
  // Check for max 2 decimal places
  const decimalPart = amount.toString().split('.')[1];
  if (decimalPart && decimalPart.length > 2) {
    return 'Amount must have at most 2 decimal places';
  }
  return null;
}

/**
 * Validate account ID format
 */
function validateAccount(accountId, fieldName) {
  if (!accountId) {
    return null; // Optional field
  }
  if (typeof accountId !== 'string') {
    return `${fieldName} must be a string`;
  }
  if (!ACCOUNT_REGEX.test(accountId)) {
    return `${fieldName} must be in format ACC-XXXXX (5 uppercase alphanumeric characters)`;
  }
  return null;
}

/**
 * Validate currency code
 */
function validateCurrency(currency) {
  if (!currency) {
    return 'Currency is required';
  }
  if (typeof currency !== 'string') {
    return 'Currency must be a string';
  }
  if (!VALID_CURRENCIES.includes(currency.toUpperCase())) {
    return `Invalid currency code. Valid codes: ${VALID_CURRENCIES.join(', ')}`;
  }
  return null;
}

/**
 * Validate transaction type
 */
function validateType(type) {
  if (!type) {
    return 'Type is required';
  }
  if (typeof type !== 'string') {
    return 'Type must be a string';
  }
  if (!VALID_TYPES.includes(type.toLowerCase())) {
    return `Invalid type. Valid types: ${VALID_TYPES.join(', ')}`;
  }
  return null;
}

/**
 * Validate entire transaction request
 * Returns array of errors or empty array if valid
 */
function validateTransaction(data) {
  const errors = [];

  // Validate amount
  const amountError = validateAmount(data.amount);
  if (amountError) {
    errors.push({ field: 'amount', message: amountError });
  }

  // Validate currency
  const currencyError = validateCurrency(data.currency);
  if (currencyError) {
    errors.push({ field: 'currency', message: currencyError });
  }

  // Validate type
  const typeError = validateType(data.type);
  if (typeError) {
    errors.push({ field: 'type', message: typeError });
  }

  // Validate accounts based on type
  const type = data.type?.toLowerCase();

  if (type === 'deposit') {
    // Deposit requires toAccount
    if (!data.toAccount) {
      errors.push({ field: 'toAccount', message: 'toAccount is required for deposits' });
    } else {
      const toAccountError = validateAccount(data.toAccount, 'toAccount');
      if (toAccountError) {
        errors.push({ field: 'toAccount', message: toAccountError });
      }
    }
  } else if (type === 'withdrawal') {
    // Withdrawal requires fromAccount
    if (!data.fromAccount) {
      errors.push({ field: 'fromAccount', message: 'fromAccount is required for withdrawals' });
    } else {
      const fromAccountError = validateAccount(data.fromAccount, 'fromAccount');
      if (fromAccountError) {
        errors.push({ field: 'fromAccount', message: fromAccountError });
      }
    }
  } else if (type === 'transfer') {
    // Transfer requires both accounts
    if (!data.fromAccount) {
      errors.push({ field: 'fromAccount', message: 'fromAccount is required for transfers' });
    } else {
      const fromAccountError = validateAccount(data.fromAccount, 'fromAccount');
      if (fromAccountError) {
        errors.push({ field: 'fromAccount', message: fromAccountError });
      }
    }
    if (!data.toAccount) {
      errors.push({ field: 'toAccount', message: 'toAccount is required for transfers' });
    } else {
      const toAccountError = validateAccount(data.toAccount, 'toAccount');
      if (toAccountError) {
        errors.push({ field: 'toAccount', message: toAccountError });
      }
    }
  } else {
    // Validate any provided accounts even if type is invalid
    const fromAccountError = validateAccount(data.fromAccount, 'fromAccount');
    if (fromAccountError) {
      errors.push({ field: 'fromAccount', message: fromAccountError });
    }
    const toAccountError = validateAccount(data.toAccount, 'toAccount');
    if (toAccountError) {
      errors.push({ field: 'toAccount', message: toAccountError });
    }
  }

  return errors;
}

/**
 * Validate account ID format (for route params)
 */
function validateAccountId(accountId) {
  if (!accountId) {
    return 'Account ID is required';
  }
  if (!ACCOUNT_REGEX.test(accountId)) {
    return 'Invalid account ID format. Must be ACC-XXXXX (5 uppercase alphanumeric characters)';
  }
  return null;
}

/**
 * Validate interest calculation parameters
 */
function validateInterestParams(rate, days) {
  const errors = [];

  if (rate !== undefined) {
    const rateNum = parseFloat(rate);
    if (isNaN(rateNum)) {
      errors.push({ field: 'rate', message: 'Rate must be a number' });
    } else if (rateNum < 0 || rateNum > 1) {
      errors.push({ field: 'rate', message: 'Rate must be between 0 and 1' });
    }
  }

  if (days !== undefined) {
    const daysNum = parseInt(days);
    if (isNaN(daysNum)) {
      errors.push({ field: 'days', message: 'Days must be a number' });
    } else if (daysNum < 1) {
      errors.push({ field: 'days', message: 'Days must be at least 1' });
    }
  }

  return errors;
}

module.exports = {
  validateTransaction,
  validateAccountId,
  validateInterestParams,
  VALID_CURRENCIES,
  VALID_TYPES
};
