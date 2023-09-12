package com.Ecommerce.OrderService.Repository;

import com.Ecommerce.OrderService.Entity.OrderPayment.BillingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingAddressRepository extends JpaRepository<BillingAddress, Long> {
}
