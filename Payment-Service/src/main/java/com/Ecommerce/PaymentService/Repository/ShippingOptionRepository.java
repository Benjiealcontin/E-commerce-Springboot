package com.Ecommerce.PaymentService.Repository;

import com.Ecommerce.PaymentService.Entity.ShippingOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingOptionRepository extends JpaRepository<ShippingOption, Long> {
}
