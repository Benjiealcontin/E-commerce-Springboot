package com.Ecommerce.PaymentService.Repository;


import com.Ecommerce.PaymentService.Entity.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {
}
