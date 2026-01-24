/**
 * Account validation
 */

import { ACCOUNT_NUMBER_REGEX } from './accountConstants';

interface ValidationError {
  field: string;
  message: string;
}

/**
 * Validates account number format
 * Format: ACC-XXXXX where XXXXX is exactly 5 alphanumeric characters
 * Case-sensitive
 * 
 * @param account - The account number to validate
 * @param fieldName - The field name (fromAccount or toAccount) for error reporting
 * @returns null if valid, error object if invalid
 */
export function validateAccountFormat(account: string | undefined, fieldName: string): ValidationError | null {
  if (!account) {
    return {
      field: fieldName,
      message: `${fieldName} is required`
    };
  }

  if (!ACCOUNT_NUMBER_REGEX.test(account)) {
    return {
      field: fieldName,
      message: `${fieldName} must follow format ACC-XXXXX (5 alphanumeric characters), received: ${account}`
    };
  }

  return null;
}
