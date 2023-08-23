package com.Ecommerce.OrderService.Repository;

import com.Ecommerce.OrderService.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
