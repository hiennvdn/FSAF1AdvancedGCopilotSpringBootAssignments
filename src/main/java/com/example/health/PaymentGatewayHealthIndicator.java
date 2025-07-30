package com.example.health;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Custom Health Check for Payment Gateway
 * In a real application, this would ping the actual payment gateway's status endpoint
 * For simulation purposes, it randomly reports UP or DOWN status
 */
@Component
public class PaymentGatewayHealthIndicator {
    
    private final Random random = new Random();
    
    /**
     * Simulates checking payment gateway health
     * @return Map containing health status details
     */
    public Map<String, Object> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Simulate random payment gateway status
        // In production, this would make an actual HTTP call to the payment provider
        boolean isPaymentGatewayUp = random.nextBoolean();
        
        health.put("status", isPaymentGatewayUp ? "UP" : "DOWN");
        health.put("gateway", "PayPal");
        health.put("connection_status", isPaymentGatewayUp ? "Connected" : "Connection Failed");
        health.put("response_time", isPaymentGatewayUp ? "120ms" : "Timeout after 5000ms");
        health.put("last_check", LocalDateTime.now().toString());
        
        if (!isPaymentGatewayUp) {
            health.put("error", "Unable to connect to payment gateway");
        }
        
        return health;
    }
}
