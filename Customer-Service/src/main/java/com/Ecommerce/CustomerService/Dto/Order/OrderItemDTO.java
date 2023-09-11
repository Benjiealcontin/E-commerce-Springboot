package com.Ecommerce.CustomerService.Dto.Order;

import lombok.Data;

@Data
public class OrderItemDTO {
    private ProductDTO product;
    private int quantity;
    private double totalPrice;
}
