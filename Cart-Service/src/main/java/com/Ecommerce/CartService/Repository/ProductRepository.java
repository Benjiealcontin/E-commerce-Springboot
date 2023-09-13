package com.Ecommerce.CartService.Repository;

import com.Ecommerce.CartService.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
