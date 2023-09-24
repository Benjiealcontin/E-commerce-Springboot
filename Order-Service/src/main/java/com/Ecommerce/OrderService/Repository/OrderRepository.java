package com.Ecommerce.OrderService.Repository;

import com.Ecommerce.OrderService.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomers_ConsumerId(String consumerId);

    List<Order> findByOrderStatus(String orderStatus);

    List<Order> findByOrderStatusAndCustomers_ConsumerId(String orderStatus, String consumerId);

    List<Order> findByCustomers_ConsumerIdAndOrderStatus(String consumerId, String orderStatus);

}
