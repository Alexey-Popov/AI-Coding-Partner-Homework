/**
 * Generate a unique transaction ID
 * Format: txn-XXXXXXXX (8 random hex characters)
 */
function generateTransactionId() {
  const chars = '0123456789abcdef';
  let id = 'txn-';
  for (let i = 0; i < 8; i++) {
    id += chars[Math.floor(Math.random() * chars.length)];
  }
  return id;
}

/**
 * Parse date string to Date object
 * Returns null if invalid
 */
function parseDate(dateString) {
  if (!dateString) return null;
  const date = new Date(dateString);
  return isNaN(date.getTime()) ? null : date;
}

/**
 * Check if a date falls within a range
 */
function isDateInRange(date, from, to) {
  const timestamp = new Date(date).getTime();
  if (from && timestamp < new Date(from).getTime()) return false;
  if (to && timestamp > new Date(to).getTime()) return false;
  return true;
}

/**
 * Round a number to 2 decimal places
 */
function roundToTwoDecimals(num) {
  return Math.round(num * 100) / 100;
}

module.exports = {
  generateTransactionId,
  parseDate,
  isDateInRange,
  roundToTwoDecimals
};
