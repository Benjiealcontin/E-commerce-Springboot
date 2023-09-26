package com.Ecommerce.PaymentService.Exception;

public class ShippingMethodNotFoundException extends RuntimeException{
    public ShippingMethodNotFoundException(String message) {
        super(message);
    }
}
