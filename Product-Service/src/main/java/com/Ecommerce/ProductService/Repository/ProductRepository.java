package com.Ecommerce.ProductService.Repository;

import com.Ecommerce.ProductService.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    List<Product> findByStockQuantityLessThanEqual(int stockQuantity);

    boolean  existsByProductName(String productName);
}
