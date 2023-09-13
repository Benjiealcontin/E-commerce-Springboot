package com.Ecommerce.CartService.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CartRequest {
    @NotEmpty(message = "Order items are required")
    @Valid
    private List<CartItemRequest> cartItems;

    @Data
    public static class CartItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;
    }
}
