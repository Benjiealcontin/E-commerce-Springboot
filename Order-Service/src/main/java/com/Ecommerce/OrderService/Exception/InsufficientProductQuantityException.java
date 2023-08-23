package com.Ecommerce.OrderService.Exception;

public class InsufficientProductQuantityException extends RuntimeException {
    public InsufficientProductQuantityException(String message) {
        super(message);
    }
}

