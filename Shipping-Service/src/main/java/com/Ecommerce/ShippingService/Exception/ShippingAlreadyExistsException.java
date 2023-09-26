package com.Ecommerce.ShippingService.Exception;

public class ShippingAlreadyExistsException extends RuntimeException{

    public ShippingAlreadyExistsException(String message) {
        super(message);
    }
}
