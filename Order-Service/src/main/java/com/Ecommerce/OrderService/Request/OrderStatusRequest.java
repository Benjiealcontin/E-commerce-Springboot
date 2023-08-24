package com.Ecommerce.OrderService.Request;

import com.Ecommerce.OrderService.Enum.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusRequest {
    private OrderStatus newOrderStatus;

}

