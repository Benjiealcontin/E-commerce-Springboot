package com.Ecommerce.OrderService.Repository;

import com.Ecommerce.OrderService.Entity.OrderPayment.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {
}
