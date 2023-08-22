package com.Ecommerce.OrderService.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private ProductResponse product;
    private int quantity;
    private double totalPrice;
}
