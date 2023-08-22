package com.Ecommerce.OrderService.Repository;

import com.Ecommerce.OrderService.Entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
}
