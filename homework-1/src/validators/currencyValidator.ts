/**
 * Currency validation using ISO 4217 standard
 */

import { codes } from 'currency-codes';

interface ValidationError {
  field: string;
  message: string;
}

/**
 * Validates currency code against ISO 4217 standard
 * 
 * @param currency - The currency code to validate (e.g., USD, EUR, GBP)
 * @returns null if valid, error object if invalid
 */
export function validateCurrency(currency: string | undefined): ValidationError | null {
  if (!currency) {
    return {
      field: 'currency',
      message: 'Currency is required'
    };
  }

  // Check if currency code exists in ISO 4217 standard
  const validCurrency = codes().find(c => c === currency);

  if (!validCurrency) {
    return {
      field: 'currency',
      message: `Invalid currency code (ISO 4217), received: ${currency}`
    };
  }

  return null;
}
