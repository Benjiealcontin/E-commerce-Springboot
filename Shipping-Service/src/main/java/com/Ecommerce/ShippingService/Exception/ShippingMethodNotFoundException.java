package com.Ecommerce.ShippingService.Exception;

public class ShippingMethodNotFoundException extends RuntimeException{
    public ShippingMethodNotFoundException(String message) {
        super(message);
    }
}
