package com.Ecommerce.OrderService.Repository;

import com.Ecommerce.OrderService.Entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
