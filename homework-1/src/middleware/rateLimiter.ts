import { Request, Response, NextFunction } from 'express';

interface RateLimitEntry {
  count: number;
  windowStart: number;
}

interface RateLimiterOptions {
  windowMs: number;
  maxRequests: number;
}

const DEFAULT_OPTIONS: RateLimiterOptions = {
  windowMs: 60 * 1000, // 1 minute
  maxRequests: 100
};

const requestCounts: Map<string, RateLimitEntry> = new Map();

function getClientIp(req: Request): string {
  const forwardedFor = req.headers['x-forwarded-for'];
  if (forwardedFor) {
    const ips = Array.isArray(forwardedFor) ? forwardedFor[0] : forwardedFor;
    return ips.split(',')[0].trim();
  }
  return req.ip || req.socket.remoteAddress || 'unknown';
}

export function createRateLimiter(options: Partial<RateLimiterOptions> = {}) {
  const config = { ...DEFAULT_OPTIONS, ...options };

  return (req: Request, res: Response, next: NextFunction): void => {
    const clientIp = getClientIp(req);
    const now = Date.now();

    const entry = requestCounts.get(clientIp);

    if (!entry || now - entry.windowStart >= config.windowMs) {
      requestCounts.set(clientIp, { count: 1, windowStart: now });
      next();
      return;
    }

    if (entry.count >= config.maxRequests) {
      const retryAfter = Math.ceil((entry.windowStart + config.windowMs - now) / 1000);
      res.set('Retry-After', String(retryAfter));
      res.status(429).json({
        error: 'Too Many Requests',
        message: `Rate limit exceeded. Maximum ${config.maxRequests} requests per ${config.windowMs / 1000} seconds.`,
        retryAfter
      });
      return;
    }

    entry.count++;
    next();
  };
}

export function resetRateLimiter(): void {
  requestCounts.clear();
}

export const rateLimiter = createRateLimiter();
