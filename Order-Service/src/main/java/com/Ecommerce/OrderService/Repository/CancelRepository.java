package com.Ecommerce.OrderService.Repository;

import com.Ecommerce.OrderService.Entity.CancelOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancelRepository extends JpaRepository<CancelOrder, Long> {
}
