package com.Ecommerce.OrderService.Exception;

public class DeliveredOrdersNotFoundException extends RuntimeException {
    public DeliveredOrdersNotFoundException(String message) {
        super(message);
    }
}

