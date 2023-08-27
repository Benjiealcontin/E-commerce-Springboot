package com.Ecommerce.OrderService.Repository;

import com.Ecommerce.OrderService.Entity.Order;
import com.Ecommerce.OrderService.Enum.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomers_ConsumerId(String consumerId);
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    List<Order> findByCustomers_ConsumerIdAndOrderStatus(String consumerId, OrderStatus orderStatus);

}
