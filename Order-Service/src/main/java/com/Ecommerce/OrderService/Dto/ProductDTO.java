package com.Ecommerce.OrderService.Dto;

import lombok.Data;

@Data
public class ProductDTO {
    private Long productId;
    private String productName;
    private double price;
}
