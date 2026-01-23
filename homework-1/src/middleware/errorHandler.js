/**
 * Global error handler middleware
 */

function errorHandler(err, req, res, next) {
  console.error('Error:', err);

  const statusCode = err.statusCode || 500;
  const message = err.message || 'Internal Server Error';

  res.status(statusCode).json({
    error: {
      status: statusCode,
      message: message,
      ...(err.details && { details: err.details })
    }
  });
}

module.exports = errorHandler;
