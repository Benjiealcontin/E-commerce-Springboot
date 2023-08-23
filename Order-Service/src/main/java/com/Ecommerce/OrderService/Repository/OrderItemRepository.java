package com.Ecommerce.OrderService.Repository;

import com.Ecommerce.OrderService.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
