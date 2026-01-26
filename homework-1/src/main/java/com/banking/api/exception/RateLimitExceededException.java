package com.banking.api.exception;

/**
 * Exception thrown when rate limit is exceeded
 */
public class RateLimitExceededException extends RuntimeException {
    
    private final long retryAfterSeconds;
    private final int limit;
    private final int remaining;
    private final long resetTime;

    public RateLimitExceededException(String message, long retryAfterSeconds, int limit, int remaining, long resetTime) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
        this.limit = limit;
        this.remaining = remaining;
        this.resetTime = resetTime;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public int getLimit() {
        return limit;
    }

    public int getRemaining() {
        return remaining;
    }

    public long getResetTime() {
        return resetTime;
    }
}
