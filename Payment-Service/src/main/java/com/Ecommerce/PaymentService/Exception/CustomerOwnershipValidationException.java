package com.Ecommerce.PaymentService.Exception;

public class CustomerOwnershipValidationException extends RuntimeException{

    public CustomerOwnershipValidationException(String message) {
        super(message);
    }
}
