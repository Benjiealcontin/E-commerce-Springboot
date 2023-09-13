package com.Ecommerce.CartService.Request;

import lombok.Data;

@Data
public class ProductRequest {
    private Long id;
    private String productName;
    private String description;
    private String category;
    private double price;
    private int stockQuantity;
}
