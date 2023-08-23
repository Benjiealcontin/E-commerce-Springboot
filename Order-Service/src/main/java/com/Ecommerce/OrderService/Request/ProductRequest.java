package com.Ecommerce.OrderService.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    private Long id;
    private String productName;
    private double price;
    private int stockQuantity;
}
