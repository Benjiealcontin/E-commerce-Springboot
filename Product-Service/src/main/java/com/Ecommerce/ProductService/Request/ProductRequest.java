package com.Ecommerce.ProductService.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String productName;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private double price;

    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    private int stockQuantity;

}
