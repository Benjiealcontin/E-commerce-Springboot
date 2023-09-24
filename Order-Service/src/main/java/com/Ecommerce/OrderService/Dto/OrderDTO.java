package com.Ecommerce.OrderService.Dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private CustomerDTO customer;
    private List<OrderItemDTO> orderItems;
    private LocalDateTime orderDate;
    private String orderStatus;
    private double totalAmount;
    private ShippingAddressDTO shippingAddress;
}

