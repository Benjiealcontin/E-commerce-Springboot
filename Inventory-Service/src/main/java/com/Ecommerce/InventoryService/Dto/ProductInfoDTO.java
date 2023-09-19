package com.Ecommerce.InventoryService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ProductInfoDTO {
    private long id;
    private String productName;
    private String description;
    private String category;
    private double price;
    private int stockQuantity;
}
