package com.Ecommerce.InventoryService.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockQuantityRequest {
    @Min(value = 1, message = "Stock Quantity must be at least 1")
    @Max(value = 5, message = "Stock Quantity must be at most 5")
    private int quantityAmount;
}
