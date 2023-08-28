package com.Ecommerce.ProductService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithImageDTO {
    private long id;
    private String productName;
    private String description;
    private String category;
    private double price;
    private int stockQuantity;

    private String imageName;
    private String imageType;
}
