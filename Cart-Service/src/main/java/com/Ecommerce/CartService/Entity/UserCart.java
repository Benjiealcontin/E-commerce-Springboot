package com.Ecommerce.CartService.Entity;

import com.Ecommerce.CartService.Enum.CartStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "UserCarts")
public class UserCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerId;
    private CartStatus status;
    @OneToMany(mappedBy = "userCart", cascade = CascadeType.ALL)
    private List<CartItem> cartItems = new ArrayList<>();
}
