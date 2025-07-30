package com.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * For demonstration purposes - shows rate limiting in action
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Mock login endpoint for testing rate limiting
     * This endpoint is rate-limited to 10 requests per minute per IP
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        // Mock authentication logic
        if ("admin".equals(username) && "password".equals(password)) {
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", "mock-jwt-token-" + System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Mock registration endpoint 
     * Also rate-limited by the interceptor
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> userData) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User registered successfully");
        response.put("userId", Math.abs(userData.hashCode()));
        return ResponseEntity.ok(response);
    }

    /**
     * Status endpoint to check rate limiting
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "active");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        response.put("message", "Authentication service is running");
        return ResponseEntity.ok(response);
    }
}
