package com.Ecommerce.ShippingService.Exception;

public class ShippingOptionNotFoundException extends RuntimeException {

    public ShippingOptionNotFoundException(String message) {
        super(message);
    }

    public ShippingOptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
