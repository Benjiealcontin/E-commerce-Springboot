package com.Ecommerce.PaymentService.Request;

import lombok.Data;

@Data
public class ProductTotalAmountRequest {
    private String shoppingMethod;
    private double totalAmount;
}
