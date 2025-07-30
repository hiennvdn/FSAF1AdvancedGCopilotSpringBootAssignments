package com.example.config;

import com.example.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Configuration
 * Registers interceptors and other web-related configurations
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply rate limiting to specific endpoints
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/auth/**", "/api/orders/**") // Login and sensitive endpoints
                .excludePathPatterns("/api/health/**", "/actuator/**"); // Exclude health checks
    }
}
