/**
 * Amount validation
 */

interface ValidationError {
  field: string;
  message: string;
}

/**
 * Validates transaction amount
 * - Must be a number type (no strings)
 * - Must be positive
 * - Must have maximum 2 decimal places
 * 
 * @param amount - The amount to validate
 * @returns null if valid, error object if invalid
 */
export function validateAmount(amount: any): ValidationError | null {
  // Check if amount is a number type
  if (typeof amount !== 'number') {
    return {
      field: 'amount',
      message: `Amount must be a number, received: ${typeof amount}`
    };
  }

  // Check if amount is positive
  if (amount <= 0) {
    return {
      field: 'amount',
      message: `Amount must be a positive number, received: ${amount}`
    };
  }

  // Check if amount has maximum 2 decimal places
  // Multiply by 100 and check if it's an integer
  if (!Number.isInteger(amount * 100)) {
    return {
      field: 'amount',
      message: `Amount must have maximum 2 decimal places, received: ${amount}`
    };
  }

  return null;
}
