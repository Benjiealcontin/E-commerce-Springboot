package com.Ecommerce.OrderService.Repository;

import com.Ecommerce.OrderService.Entity.OrderPayment.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
}
