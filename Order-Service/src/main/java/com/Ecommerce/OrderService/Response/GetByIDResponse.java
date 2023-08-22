package com.Ecommerce.OrderService.Response;

import com.Ecommerce.OrderService.Enum.OrderStatus;
import com.Ecommerce.OrderService.Request.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetByIDResponse {
    private Long id;
    private Customer customer;
    private List<OrderItemResponse> items;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private double totalAmount;
    private ShippingAddress shippingAddress;

}
