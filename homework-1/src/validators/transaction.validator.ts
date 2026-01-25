import { ValidationError, TransactionType, CreateTransactionInput } from '../models';
import { ValidationException } from '../errors';

const VALID_TRANSACTION_TYPES: TransactionType[] = ['deposit', 'withdrawal', 'transfer'];
const VALID_CURRENCIES = ['USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'NZD', 'CNY', 'INR', 'PLN', 'UAH'];
const ACCOUNT_PATTERN = /^ACC-[A-Z0-9]{5}$/;

export interface RawTransactionInput {
  fromAccount?: unknown;
  toAccount?: unknown;
  amount?: unknown;
  currency?: unknown;
  type?: unknown;
  description?: unknown;
}

export function validateTransactionInput(input: RawTransactionInput): CreateTransactionInput {
  const errors: ValidationError[] = [];

  // Validate amount
  if (!input.amount || typeof input.amount !== 'number' || input.amount <= 0) {
    errors.push({ field: 'amount', message: 'Amount must be a positive number' });
  } else {
    // Check max 2 decimal places
    const decimalPart = String(input.amount).split('.')[1];
    if (decimalPart && decimalPart.length > 2) {
      errors.push({ field: 'amount', message: 'Amount must have at most 2 decimal places' });
    }
  }

  // Validate currency
  if (!input.currency || typeof input.currency !== 'string') {
    errors.push({ field: 'currency', message: 'Currency is required' });
  } else if (!VALID_CURRENCIES.includes(input.currency.toUpperCase())) {
    errors.push({ field: 'currency', message: 'Invalid currency code. Allowed: ' + VALID_CURRENCIES.join(', ') });
  }

  // Validate type
  if (!input.type || !VALID_TRANSACTION_TYPES.includes(input.type as TransactionType)) {
    errors.push({ field: 'type', message: 'Type must be deposit, withdrawal, or transfer' });
  }

  // Validate accounts based on type
  const type = input.type as TransactionType;

  if (type === 'deposit' && !input.toAccount) {
    errors.push({ field: 'toAccount', message: 'toAccount is required for deposits' });
  }

  if (type === 'withdrawal' && !input.fromAccount) {
    errors.push({ field: 'fromAccount', message: 'fromAccount is required for withdrawals' });
  }

  if (type === 'transfer' && (!input.fromAccount || !input.toAccount)) {
    errors.push({ field: 'accounts', message: 'Both fromAccount and toAccount are required for transfers' });
  }

  // Validate account format (ACC-XXXXX where X is alphanumeric)
  if (input.fromAccount && typeof input.fromAccount === 'string' && !ACCOUNT_PATTERN.test(input.fromAccount)) {
    errors.push({ field: 'fromAccount', message: 'Account must match format ACC-XXXXX (5 alphanumeric characters)' });
  }
  if (input.toAccount && typeof input.toAccount === 'string' && !ACCOUNT_PATTERN.test(input.toAccount)) {
    errors.push({ field: 'toAccount', message: 'Account must match format ACC-XXXXX (5 alphanumeric characters)' });
  }

  if (errors.length > 0) {
    throw new ValidationException(errors);
  }

  return {
    fromAccount: input.fromAccount as string | undefined,
    toAccount: input.toAccount as string | undefined,
    amount: input.amount as number,
    currency: input.currency as string,
    type: type,
    description: input.description as string | undefined
  };
}
