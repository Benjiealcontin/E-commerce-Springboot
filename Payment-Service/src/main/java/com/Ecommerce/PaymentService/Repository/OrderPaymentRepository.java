package com.Ecommerce.PaymentService.Repository;


import com.Ecommerce.PaymentService.Entity.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
}
