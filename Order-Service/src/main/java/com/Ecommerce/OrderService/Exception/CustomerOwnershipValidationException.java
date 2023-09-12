package com.Ecommerce.OrderService.Exception;

public class CustomerOwnershipValidationException extends RuntimeException{

    public CustomerOwnershipValidationException(String message) {
        super(message);
    }
}
