package com.example.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String productName, Integer available, Integer requested) {
        super("Insufficient stock for product: " + productName + 
              ". Available: " + available + ", Requested: " + requested);
    }
    
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
