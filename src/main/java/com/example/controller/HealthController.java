package com.example.controller;

import com.example.health.PaymentGatewayHealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health Controller - Exposes custom health checks
 * Accessible at /api/health/payment-gateway
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private PaymentGatewayHealthIndicator paymentGatewayHealthIndicator;

    @GetMapping("/payment-gateway")
    public ResponseEntity<Map<String, Object>> getPaymentGatewayHealth() {
        Map<String, Object> healthStatus = paymentGatewayHealthIndicator.checkHealth();
        
        // Return appropriate HTTP status based on health
        String status = (String) healthStatus.get("status");
        if ("UP".equals(status)) {
            return ResponseEntity.ok(healthStatus);
        } else {
            return ResponseEntity.status(503).body(healthStatus); // Service Unavailable
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllHealthChecks() {
        Map<String, Object> allHealth = new HashMap<>();
        
        // Payment Gateway Health
        Map<String, Object> paymentHealth = paymentGatewayHealthIndicator.checkHealth();
        allHealth.put("paymentGateway", paymentHealth);
        
        // Overall status
        String paymentStatus = (String) paymentHealth.get("status");
        boolean allHealthy = "UP".equals(paymentStatus);
        
        allHealth.put("status", allHealthy ? "UP" : "DOWN");
        allHealth.put("timestamp", java.time.LocalDateTime.now().toString());
        
        if (allHealthy) {
            return ResponseEntity.ok(allHealth);
        } else {
            return ResponseEntity.status(503).body(allHealth);
        }
    }
}
