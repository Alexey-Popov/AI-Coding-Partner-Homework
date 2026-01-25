/**
 * Convert an array of objects to CSV format
 * @param {Array} data - Array of objects to convert
 * @returns {string} CSV string
 */
function toCSV(data) {
  if (!data || data.length === 0) {
    return '';
  }

  // Get headers from the first object
  const headers = Object.keys(data[0]);

  // Create CSV header row
  const csvRows = [headers.join(',')];

  // Add data rows
  for (const row of data) {
    const values = headers.map(header => {
      const value = row[header];
      // Escape values that contain commas, quotes, or newlines
      if (typeof value === 'string' && (value.includes(',') || value.includes('"') || value.includes('\n'))) {
        return `"${value.replace(/"/g, '""')}"`;
      }
      return value;
    });
    csvRows.push(values.join(','));
  }

  return csvRows.join('\n');
}

/**
 * Parse date string to Date object, returns null if invalid
 * @param {string} dateStr - Date string to parse
 * @returns {Date|null} Parsed date or null
 */
function parseDate(dateStr) {
  if (!dateStr) return null;
  const date = new Date(dateStr);
  return isNaN(date.getTime()) ? null : date;
}

/**
 * Format error response
 * @param {string} message - Error message
 * @param {Array} details - Optional array of detail objects
 * @returns {Object} Formatted error object
 */
function formatError(message, details = null) {
  const error = { error: message };
  if (details) {
    error.details = details;
  }
  return error;
}

/**
 * Format success response
 * @param {*} data - Response data
 * @param {string} message - Optional message
 * @returns {Object} Formatted response object
 */
function formatSuccess(data, message = null) {
  const response = { data };
  if (message) {
    response.message = message;
  }
  return response;
}

module.exports = {
  toCSV,
  parseDate,
  formatError,
  formatSuccess
};
