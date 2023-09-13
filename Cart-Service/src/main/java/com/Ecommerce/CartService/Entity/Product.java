package com.Ecommerce.CartService.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String productName;
    private String description;
    private String category;
    private double price;
    private int stockQuantity;

    @OneToOne
    @MapsId
    @JoinColumn(name = "cartItem_id")
    private CartItem cartItem;
}
