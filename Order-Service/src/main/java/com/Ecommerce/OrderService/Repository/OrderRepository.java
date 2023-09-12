package com.Ecommerce.OrderService.Repository;

import com.Ecommerce.OrderService.Entity.Order;
import com.Ecommerce.OrderService.Enum.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomers_ConsumerId(String consumerId);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    List<Order> findByOrderStatusAndCustomers_ConsumerId(OrderStatus orderStatus, String consumerId);

    List<Order> findByCustomers_ConsumerIdAndOrderStatus(String consumerId, OrderStatus orderStatus);

}
