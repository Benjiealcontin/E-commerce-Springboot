package com.Ecommerce.CartService.Repository;

import com.Ecommerce.CartService.Entity.UserCart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCartRepository extends JpaRepository<UserCart, Long> {

    UserCart findByCustomerId(String customerId);
}
