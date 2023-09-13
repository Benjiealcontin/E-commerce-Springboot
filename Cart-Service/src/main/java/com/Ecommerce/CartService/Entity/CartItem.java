package com.Ecommerce.CartService.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "CartItems")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_cart_id")
    private UserCart userCart;

    @OneToOne(mappedBy = "cartItem", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Product product;
}
