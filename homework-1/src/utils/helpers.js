/**
 * Convert an array of objects to CSV format
 * @param {Array} data - Array of objects to convert
 * @returns {string} CSV string
 */
export const toCSV = (data) => {
  if (!data?.length) {
    return '';
  }

  const headers = Object.keys(data[0]);
  const csvRows = [headers.join(',')];

  for (const row of data) {
    const values = headers.map((header) => {
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
};

/**
 * Parse date string to Date object, returns null if invalid
 * @param {string} dateStr - Date string to parse
 * @returns {Date|null} Parsed date or null
 */
export const parseDate = (dateStr) => {
  if (!dateStr) return null;
  const date = new Date(dateStr);
  return Number.isNaN(date.getTime()) ? null : date;
};

/**
 * Format error response
 * @param {string} message - Error message
 * @param {Array} details - Optional array of detail objects
 * @returns {Object} Formatted error object
 */
export const formatError = (message, details = null) => ({
  error: message,
  ...(details && { details })
});

/**
 * Format success response
 * @param {*} data - Response data
 * @param {string} message - Optional message
 * @returns {Object} Formatted response object
 */
export const formatSuccess = (data, message = null) => ({
  data,
  ...(message && { message })
});
