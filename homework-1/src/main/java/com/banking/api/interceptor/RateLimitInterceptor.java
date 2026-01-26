package com.banking.api.interceptor;

import com.banking.api.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Interceptor for rate limiting API requests
 * Implements sliding window algorithm
 * Limit: 100 requests per minute per IP address
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long WINDOW_SIZE_MILLIS = 60_000; // 1 minute

    // Store request timestamps per IP address
    private final Map<String, ConcurrentLinkedQueue<Long>> requestTimestamps = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws RateLimitExceededException {
        
        String clientIp = getClientIp(request);
        long currentTime = System.currentTimeMillis();
        
        // Get or create queue for this IP
        ConcurrentLinkedQueue<Long> timestamps = requestTimestamps.computeIfAbsent(
                clientIp, k -> new ConcurrentLinkedQueue<>());
        
        // Remove timestamps outside the window (older than 1 minute)
        timestamps.removeIf(timestamp -> currentTime - timestamp > WINDOW_SIZE_MILLIS);
        
        // Check if limit is exceeded
        int currentCount = timestamps.size();
        
        if (currentCount >= MAX_REQUESTS_PER_MINUTE) {
            // Calculate retry after time
            Long oldestTimestamp = timestamps.peek();
            long retryAfterMillis = oldestTimestamp != null 
                    ? (oldestTimestamp + WINDOW_SIZE_MILLIS - currentTime) 
                    : WINDOW_SIZE_MILLIS;
            long retryAfterSeconds = (retryAfterMillis / 1000) + 1;
            
            long resetTime = oldestTimestamp != null 
                    ? (oldestTimestamp + WINDOW_SIZE_MILLIS) 
                    : (currentTime + WINDOW_SIZE_MILLIS);
            
            throw new RateLimitExceededException(
                    "Rate limit exceeded. Maximum " + MAX_REQUESTS_PER_MINUTE + " requests per minute allowed.",
                    retryAfterSeconds,
                    MAX_REQUESTS_PER_MINUTE,
                    0,
                    resetTime
            );
        }
        
        // Add current request timestamp
        timestamps.add(currentTime);
        
        // Add rate limit headers to response
        response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(MAX_REQUESTS_PER_MINUTE - currentCount - 1));
        
        Long oldestTimestamp = timestamps.peek();
        long resetTime = oldestTimestamp != null 
                ? (oldestTimestamp + WINDOW_SIZE_MILLIS) 
                : (currentTime + WINDOW_SIZE_MILLIS);
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime / 1000));
        
        return true;
    }

    /**
     * Extract client IP address from request
     * Considers X-Forwarded-For header for proxy scenarios
     *
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Cleanup method to prevent memory leaks
     * Removes IP addresses with no recent requests
     */
    public void cleanup() {
        long currentTime = System.currentTimeMillis();
        requestTimestamps.entrySet().removeIf(entry -> {
            ConcurrentLinkedQueue<Long> timestamps = entry.getValue();
            timestamps.removeIf(timestamp -> currentTime - timestamp > WINDOW_SIZE_MILLIS);
            return timestamps.isEmpty();
        });
    }
}
