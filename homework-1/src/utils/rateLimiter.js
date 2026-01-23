/**
 * Rate Limiting Middleware
 * Limits requests to 100 per minute per IP address
 * Built with AI assistance (Claude Code)
 */

// Store request counts: Map<IP, { count: number, resetTime: number }>
const requestCounts = new Map();

// Rate limit configuration
const MAX_REQUESTS = 100; // Maximum requests allowed
const WINDOW_MS = 60 * 1000; // Time window in milliseconds (1 minute)

/**
 * Clean up expired entries periodically
 * Removes entries where reset time has passed
 */
function cleanupExpiredEntries() {
  const now = Date.now();
  for (const [ip, data] of requestCounts.entries()) {
    if (now > data.resetTime) {
      requestCounts.delete(ip);
    }
  }
}

// Run cleanup every 30 seconds
setInterval(cleanupExpiredEntries, 30000);

/**
 * Rate limiting middleware
 * @param {object} req - Express request object
 * @param {object} res - Express response object
 * @param {function} next - Express next function
 */
function rateLimiter(req, res, next) {
  // Get client IP address
  // Try different headers for IP (in case behind proxy)
  const ip = req.headers['x-forwarded-for'] ||
             req.headers['x-real-ip'] ||
             req.connection.remoteAddress ||
             req.socket.remoteAddress;

  const now = Date.now();

  // Get or create request count entry for this IP
  if (!requestCounts.has(ip)) {
    // First request from this IP
    requestCounts.set(ip, {
      count: 1,
      resetTime: now + WINDOW_MS
    });
  } else {
    const data = requestCounts.get(ip);

    // Check if window has expired
    if (now > data.resetTime) {
      // Reset the counter
      data.count = 1;
      data.resetTime = now + WINDOW_MS;
    } else {
      // Increment counter
      data.count++;
    }
  }

  const data = requestCounts.get(ip);
  const remaining = Math.max(0, MAX_REQUESTS - data.count);
  const resetIn = Math.ceil((data.resetTime - now) / 1000); // seconds

  // Add rate limit headers to response
  res.setHeader('X-RateLimit-Limit', MAX_REQUESTS);
  res.setHeader('X-RateLimit-Remaining', remaining);
  res.setHeader('X-RateLimit-Reset', data.resetTime);

  // Check if limit exceeded
  if (data.count > MAX_REQUESTS) {
    res.setHeader('Retry-After', resetIn);
    return res.status(429).json({
      error: 'Too Many Requests',
      message: `Rate limit exceeded. Maximum ${MAX_REQUESTS} requests per minute allowed.`,
      retryAfter: resetIn,
      resetTime: new Date(data.resetTime).toISOString()
    });
  }

  // Allow request to proceed
  next();
}

/**
 * Get current rate limit status for an IP
 * Useful for testing and monitoring
 * @param {string} ip - IP address
 * @returns {object} Rate limit status
 */
function getRateLimitStatus(ip) {
  const data = requestCounts.get(ip);
  if (!data) {
    return {
      count: 0,
      remaining: MAX_REQUESTS,
      resetTime: null
    };
  }

  const now = Date.now();
  if (now > data.resetTime) {
    return {
      count: 0,
      remaining: MAX_REQUESTS,
      resetTime: null
    };
  }

  return {
    count: data.count,
    remaining: Math.max(0, MAX_REQUESTS - data.count),
    resetTime: new Date(data.resetTime).toISOString()
  };
}

/**
 * Clear all rate limit data
 * Useful for testing
 */
function clearRateLimits() {
  requestCounts.clear();
}

module.exports = {
  rateLimiter,
  getRateLimitStatus,
  clearRateLimits,
  MAX_REQUESTS,
  WINDOW_MS
};
