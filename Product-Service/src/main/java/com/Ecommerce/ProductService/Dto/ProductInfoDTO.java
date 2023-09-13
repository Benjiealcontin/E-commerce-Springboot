package com.Ecommerce.ProductService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfoDTO {
    private Long id;
    private String productName;
    private double price;
    private int stockQuantity;
    private String description;
    private String category;
}
