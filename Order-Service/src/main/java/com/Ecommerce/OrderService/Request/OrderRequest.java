package com.Ecommerce.OrderService.Request;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private List<OrderItemRequest> orderItems;
    @Data
    public static class OrderItemRequest {
        private Long productId;
        private int quantity;
    }
}