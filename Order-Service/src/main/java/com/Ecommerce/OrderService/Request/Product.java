package com.Ecommerce.OrderService.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private long id;
    private String productName;
    private String description;
    private String category;
    private double price;
    private int stockQuantity;
}
