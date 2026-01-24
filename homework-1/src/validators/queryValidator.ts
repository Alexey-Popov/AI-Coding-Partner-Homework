import { TransactionType } from '../models/transaction';

/**
 * Validate date string format (YYYY-MM-DD or ISO datetime)
 * @param dateStr - Date string to validate
 * @returns Error message if invalid, null if valid or empty
 */
export function validateDateFormat(dateStr: string | undefined): string | null {
  if (!dateStr || dateStr.trim() === '') {
    return null; // Empty strings are ignored
  }

  // Try to parse the date
  const date = new Date(dateStr);
  
  // Check if date is valid
  if (isNaN(date.getTime())) {
    return `Invalid date format: ${dateStr}. Expected YYYY-MM-DD or ISO datetime format.`;
  }

  return null;
}

/**
 * Validate transaction type (case-insensitive)
 * @param type - Transaction type to validate
 * @returns Error message if invalid, null if valid or empty
 */
export function validateTransactionType(type: string | undefined): string | null {
  if (!type || type.trim() === '') {
    return null; // Empty strings are ignored
  }

  const normalizedType = type.toLowerCase();
  const validTypes = Object.values(TransactionType).map(t => t.toLowerCase());

  if (!validTypes.includes(normalizedType)) {
    return `Invalid transaction type: ${type}. Expected one of: ${Object.values(TransactionType).join(', ')}`;
  }

  return null;
}

/**
 * Validate all query parameters for transaction filtering
 * @param params - Query parameters object
 * @returns Array of validation errors (empty if all valid)
 */
export function validateQueryParameters(params: {
  accountId?: string;
  type?: string;
  from?: string;
  to?: string;
}): string[] {
  const errors: string[] = [];

  // Validate dates
  const fromError = validateDateFormat(params.from);
  if (fromError) errors.push(fromError);

  const toError = validateDateFormat(params.to);
  if (toError) errors.push(toError);

  // Validate transaction type
  const typeError = validateTransactionType(params.type);
  if (typeError) errors.push(typeError);

  return errors;
}
