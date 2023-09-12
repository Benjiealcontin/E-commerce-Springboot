package com.Ecommerce.PaymentService.Repository;


import com.Ecommerce.PaymentService.Entity.BillingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingAddressRepository extends JpaRepository<BillingAddress, Long> {
}
