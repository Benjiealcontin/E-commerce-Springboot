package com.Ecommerce.ProductService.Repository;

import com.Ecommerce.ProductService.Entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByProductId(long productId);

    Optional<Image> findByName(String name);
}
