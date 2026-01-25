import { CreateTransactionInput, TransactionType, ValidationError } from '../models/transaction';

/**
 * ISO 4217 currency codes supported by the system
 */
const VALID_CURRENCIES = [
  'USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'NZD',
  'CNY', 'INR', 'BRL', 'ZAR', 'SEK', 'NOK', 'DKK', 'PLN',
  'RUB', 'MXN', 'SGD', 'HKD', 'KRW', 'TRY', 'AED', 'SAR'
];

/**
 * Account number format: ACC-XXXXX where X is alphanumeric
 */
const ACCOUNT_FORMAT_REGEX = /^ACC-[A-Z0-9]{5}$/;

/**
 * Validates a transaction input and returns validation errors if any
 */
export function validateTransaction(input: CreateTransactionInput): ValidationError[] {
  const errors: ValidationError[] = [];

  // Validate amount
  if (input.amount === undefined || input.amount === null) {
    errors.push({
      field: 'amount',
      message: 'Amount is required'
    });
  } else if (typeof input.amount !== 'number' || isNaN(input.amount)) {
    errors.push({
      field: 'amount',
      message: 'Amount must be a valid number'
    });
  } else if (input.amount <= 0) {
    errors.push({
      field: 'amount',
      message: 'Amount must be a positive number'
    });
  } else if (!isValidDecimalPlaces(input.amount, 2)) {
    errors.push({
      field: 'amount',
      message: 'Amount must have maximum 2 decimal places'
    });
  }

  // Validate fromAccount
  if (!input.fromAccount) {
    errors.push({
      field: 'fromAccount',
      message: 'From account is required'
    });
  } else if (!isValidAccountFormat(input.fromAccount)) {
    errors.push({
      field: 'fromAccount',
      message: 'From account must follow format ACC-XXXXX (where X is alphanumeric)'
    });
  }

  // Validate toAccount
  if (!input.toAccount) {
    errors.push({
      field: 'toAccount',
      message: 'To account is required'
    });
  } else if (!isValidAccountFormat(input.toAccount)) {
    errors.push({
      field: 'toAccount',
      message: 'To account must follow format ACC-XXXXX (where X is alphanumeric)'
    });
  }

  // Validate currency
  if (!input.currency) {
    errors.push({
      field: 'currency',
      message: 'Currency is required'
    });
  } else if (!isValidCurrency(input.currency)) {
    errors.push({
      field: 'currency',
      message: `Invalid currency code. Must be one of: ${VALID_CURRENCIES.join(', ')}`
    });
  }

  // Validate type
  if (!input.type) {
    errors.push({
      field: 'type',
      message: 'Transaction type is required'
    });
  } else if (!isValidTransactionType(input.type)) {
    errors.push({
      field: 'type',
      message: `Transaction type must be one of: ${Object.values(TransactionType).join(', ')}`
    });
  }

  // Business logic validation
  if (input.type === TransactionType.DEPOSIT && input.fromAccount !== 'ACC-00000') {
    // For deposits, fromAccount should typically be a system account
    // This is a soft validation - we'll allow it but could be stricter
  }

  if (input.type === TransactionType.WITHDRAWAL && input.toAccount !== 'ACC-00000') {
    // For withdrawals, toAccount should typically be a system account
    // This is a soft validation - we'll allow it but could be stricter
  }

  return errors;
}

/**
 * Validates that a number has at most the specified decimal places
 */
function isValidDecimalPlaces(value: number, maxDecimals: number): boolean {
  const decimalPart = value.toString().split('.')[1];
  return !decimalPart || decimalPart.length <= maxDecimals;
}

/**
 * Validates account format: ACC-XXXXX
 */
function isValidAccountFormat(account: string): boolean {
  return ACCOUNT_FORMAT_REGEX.test(account);
}

/**
 * Validates ISO 4217 currency code
 */
function isValidCurrency(currency: string): boolean {
  return VALID_CURRENCIES.includes(currency.toUpperCase());
}

/**
 * Validates transaction type
 */
function isValidTransactionType(type: string): boolean {
  return Object.values(TransactionType).includes(type as TransactionType);
}

/**
 * Validates date format (ISO 8601)
 */
export function isValidDateFormat(dateString: string): boolean {
  const date = new Date(dateString);
  return !isNaN(date.getTime());
}

