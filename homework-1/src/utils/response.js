/**
 * Helper functions for consistent API responses
 */

/**
 * Send a successful response
 * @param {Object} res - Express response object
 * @param {*} data - Response data
 * @param {number} [statusCode=200] - HTTP status code
 * @param {string|null} [message=null] - Optional success message
 */
function successResponse(res, data, statusCode = 200, message = null) {
  const response = {
    success: true,
    ...(message && { message }),
    data
  };
  res.status(statusCode).json(response);
}

/**
 * Send an error response
 * @param {Object} res - Express response object
 * @param {string} message - Error message
 * @param {number} [statusCode=400] - HTTP status code
 * @param {Object|null} [details=null] - Optional error details
 */
function errorResponse(res, message, statusCode = 400, details = null) {
  const response = {
    success: false,
    error: {
      message,
      ...(details && { details })
    }
  };
  res.status(statusCode).json(response);
}

module.exports = {
  successResponse,
  errorResponse
};
