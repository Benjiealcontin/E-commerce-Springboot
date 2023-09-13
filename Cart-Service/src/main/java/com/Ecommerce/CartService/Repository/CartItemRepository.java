package com.Ecommerce.CartService.Repository;

import com.Ecommerce.CartService.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
