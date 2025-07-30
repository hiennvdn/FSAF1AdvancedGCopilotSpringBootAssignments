package com.example.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Rate Limiting Configuration using Bucket4j
 * Implements token bucket algorithm for rate limiting
 */
@Component
public class RateLimitConfig {
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    /**
     * Creates a rate-limited bucket for login endpoint
     * 10 requests per minute per IP address
     */
    public Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.simple(10, Duration.ofMinutes(1));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    /**
     * Creates a rate-limited bucket for general API endpoints
     * 100 requests per minute per IP address
     */
    public Bucket createGeneralApiBucket() {
        Bandwidth limit = Bandwidth.simple(100, Duration.ofMinutes(1));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    /**
     * Get or create a bucket for the given key
     * @param key Usually the IP address or user identifier
     * @param bucketType "login" or "general"
     * @return Bucket for rate limiting
     */
    public Bucket getBucket(String key, String bucketType) {
        String bucketKey = bucketType + ":" + key;
        return buckets.computeIfAbsent(bucketKey, k -> {
            if ("login".equals(bucketType)) {
                return createLoginBucket();
            } else {
                return createGeneralApiBucket();
            }
        });
    }
    
    /**
     * Clean up old buckets to prevent memory leaks
     * In production, this should be called periodically or use a cache with TTL
     */
    public void cleanup() {
        // Simple cleanup - in production use more sophisticated approach
        if (buckets.size() > 10000) {
            buckets.clear();
        }
    }
}
