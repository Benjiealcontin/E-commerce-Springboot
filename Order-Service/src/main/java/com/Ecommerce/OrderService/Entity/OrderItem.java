package com.Ecommerce.OrderService.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "OrderItems")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @OneToMany(mappedBy = "orderItem", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Product> product;

    private int quantity;
    private double totalPrice;

}