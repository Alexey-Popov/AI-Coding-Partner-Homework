package com.banking.api.config;

import com.banking.api.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for the Banking Transactions API.
 * 
 * <p>This configuration class handles:
 * <ul>
 *   <li>CORS (Cross-Origin Resource Sharing) settings</li>
 *   <li>Web MVC customizations</li>
 * </ul>
 * 
 * <p><b>CORS Configuration:</b>
 * For development purposes, this configuration allows all origins.
 * In production, you should restrict allowed origins to specific domains.
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;
    
    /**
     * Configures CORS mappings to allow cross-origin requests.
     * 
     * <p><b>Current Configuration (Development):</b>
     * <ul>
     *   <li>Allowed Origins: All (*)</li>
     *   <li>Allowed Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH</li>
     *   <li>Allowed Headers: All (*)</li>
     *   <li>Exposed Headers: Custom rate-limiting headers</li>
     *   <li>Credentials: Allowed</li>
     *   <li>Max Age: 3600 seconds (1 hour)</li>
     * </ul>
     * 
     * <p><b>Production Recommendation:</b>
     * Replace "*" with specific allowed origins:
     * <pre>
     * .allowedOrigins("https://yourdomain.com", "https://app.yourdomain.com")
     * </pre>
     * 
     * @param registry The CORS registry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // Allow all origins (development)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight response for 1 hour
    }
    
    /**
     * Registers interceptors for request processing.
     * 
     * <p>Currently registers:
     * <ul>
     *   <li>RateLimitInterceptor - Limits requests to 100 per minute per IP</li>
     * </ul>
     * 
     * @param registry The interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/actuator/**");
    }
}
