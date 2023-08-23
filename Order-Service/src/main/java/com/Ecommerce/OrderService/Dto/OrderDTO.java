package com.Ecommerce.OrderService.Dto;

import com.Ecommerce.OrderService.Enum.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class OrderDTO {
    private Long id;

    private CustomerDTO customer;
    private List<OrderItemDTO> orderItems;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private double totalAmount;
    private ShippingAddressDTO shippingAddress;
}

