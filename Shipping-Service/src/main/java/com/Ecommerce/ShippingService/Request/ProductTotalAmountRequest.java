package com.Ecommerce.ShippingService.Request;

import lombok.Data;

@Data
public class ProductTotalAmountRequest {
    private String shoppingMethod;
    private double totalAmount;
}
