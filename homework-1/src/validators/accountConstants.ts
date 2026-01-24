/**
 * Account validation constants
 */

/**
 * Account number format: ACC-XXXXX
 * Where XXXXX is exactly 5 alphanumeric characters (A-Z, a-z, 0-9)
 * Case-sensitive format
 */
export const ACCOUNT_NUMBER_REGEX = /^ACC-[A-Za-z0-9]{5}$/;
