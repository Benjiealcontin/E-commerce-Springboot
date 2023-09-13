package com.Ecommerce.CartService.Dto;

import lombok.Data;

@Data
public class ProductDTO {
    private long id;
    private String productName;
    private String description;
    private String category;
    private double price;
    private int stockQuantity;
}
