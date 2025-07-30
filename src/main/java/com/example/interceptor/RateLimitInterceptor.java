package com.example.interceptor;

import com.example.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Rate Limiting Interceptor
 * Applies rate limiting to specific endpoints based on IP address
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    @Autowired
    private RateLimitConfig rateLimitConfig;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIpAddress(request);
        String requestUri = request.getRequestURI();
        
        // Determine bucket type based on endpoint
        String bucketType = isLoginEndpoint(requestUri) ? "login" : "general";
        
        // Get bucket for this IP and endpoint type
        Bucket bucket = rateLimitConfig.getBucket(clientIp, bucketType);
        
        // Try to consume a token
        if (bucket.tryConsume(1)) {
            // Request allowed
            return true;
        } else {
            // Rate limit exceeded
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}");
            return false;
        }
    }
    
    /**
     * Check if the request is for a login endpoint
     */
    private boolean isLoginEndpoint(String uri) {
        return uri != null && (uri.contains("/login") || uri.contains("/auth"));
    }
    
    /**
     * Extract client IP address from request
     * Handles common proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get the first IP in the chain
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
