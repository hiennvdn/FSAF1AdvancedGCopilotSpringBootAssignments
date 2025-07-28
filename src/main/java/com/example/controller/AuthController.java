package com.example.controller;

import com.example.dto.UserRegistrationDTO;
import com.example.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @PostMapping("/register")
    public String registerUser(@Valid @RequestBody UserRegistrationDTO userDto) {
        // Fixed: Added proper input validation
        validateRegistrationInput(userDto);
        
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword())); // Fixed: Password encryption
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        
        return "User registered successfully";
    }

    private void validateRegistrationInput(UserRegistrationDTO userDto) {
        if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        
        if (userDto.getUsername().length() < 3 || userDto.getUsername().length() > 50) {
            throw new RuntimeException("Username must be between 3 and 50 characters");
        }
        
        if (userDto.getEmail() == null || !EMAIL_PATTERN.matcher(userDto.getEmail()).matches()) {
            throw new RuntimeException("Valid email is required");
        }
        
        if (userDto.getPassword() == null || userDto.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }
        
        if (!userDto.getPassword().matches(".*[A-Z].*") || 
            !userDto.getPassword().matches(".*[a-z].*") ||
            !userDto.getPassword().matches(".*[0-9].*")) {
            throw new RuntimeException("Password must contain at least one uppercase letter, one lowercase letter, and one number");
        }
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        // Simplified login logic
        return "Login successful";
    }

    static class LoginRequest {
        private String username;
        private String password;
        
        // getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
