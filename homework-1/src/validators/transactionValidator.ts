import { CreateTransactionDTO, TransactionType } from '../models/transaction';
import { ErrorResponse } from '../models/common';
import { validateAmount } from './amountValidator';
import { validateAccountFormat } from './accountValidator';
import { validateCurrency } from './currencyValidator';

/**
 * Validate a transaction creation request
 * Returns null if valid, or an ErrorResponse if invalid
 * Stops at first validation error (fail-fast approach)
 */
export function validateTransaction(dto: CreateTransactionDTO): ErrorResponse | null {
  // Validate amount first (type, positive, max 2 decimals)
  const amountError = validateAmount(dto.amount);
  if (amountError) {
    return {
      error: 'Validation failed',
      details: [amountError]
    };
  }

  // Validate currency (ISO 4217)
  const currencyError = validateCurrency(dto.currency);
  if (currencyError) {
    return {
      error: 'Validation failed',
      details: [currencyError]
    };
  }

  // Validate transaction type and required accounts
  if (!dto.type || !Object.values(TransactionType).includes(dto.type)) {
    return {
      error: 'Validation failed',
      details: [{
        field: 'type',
        message: `Type must be one of: ${Object.values(TransactionType).join(', ')}`
      }]
    };
  }

  // Validate accounts based on transaction type
  switch (dto.type) {
    case TransactionType.DEPOSIT:
      if (!dto.toAccount) {
        return {
          error: 'Validation failed',
          details: [{ field: 'toAccount', message: 'Deposit requires toAccount' }]
        };
      }
      if (dto.fromAccount) {
        return {
          error: 'Validation failed',
          details: [{ field: 'fromAccount', message: 'Deposit should not have fromAccount' }]
        };
      }
      // Validate toAccount format
      const depositAccountError = validateAccountFormat(dto.toAccount, 'toAccount');
      if (depositAccountError) {
        return {
          error: 'Validation failed',
          details: [depositAccountError]
        };
      }
      break;

    case TransactionType.WITHDRAWAL:
      if (!dto.fromAccount) {
        return {
          error: 'Validation failed',
          details: [{ field: 'fromAccount', message: 'Withdrawal requires fromAccount' }]
        };
      }
      if (dto.toAccount) {
        return {
          error: 'Validation failed',
          details: [{ field: 'toAccount', message: 'Withdrawal should not have toAccount' }]
        };
      }
      // Validate fromAccount format
      const withdrawalAccountError = validateAccountFormat(dto.fromAccount, 'fromAccount');
      if (withdrawalAccountError) {
        return {
          error: 'Validation failed',
          details: [withdrawalAccountError]
        };
      }
      break;

    case TransactionType.TRANSFER:
      if (!dto.fromAccount) {
        return {
          error: 'Validation failed',
          details: [{ field: 'fromAccount', message: 'Transfer requires fromAccount' }]
        };
      }
      if (!dto.toAccount) {
        return {
          error: 'Validation failed',
          details: [{ field: 'toAccount', message: 'Transfer requires toAccount' }]
        };
      }
      // Validate both account formats
      const transferFromAccountError = validateAccountFormat(dto.fromAccount, 'fromAccount');
      if (transferFromAccountError) {
        return {
          error: 'Validation failed',
          details: [transferFromAccountError]
        };
      }
      const transferToAccountError = validateAccountFormat(dto.toAccount, 'toAccount');
      if (transferToAccountError) {
        return {
          error: 'Validation failed',
          details: [transferToAccountError]
        };
      }
      break;
  }

  return null;
}
