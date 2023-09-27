package com.Ecommerce.PaymentService.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ShippingOptions")
public class ShippingOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String shippingName;
    private String description;
    private double price;
    private int estimatedDeliveryTimeInDays;
    @OneToOne
    @MapsId
    @JoinColumn(name = "orderPayment_id")
    private OrderPayment orderPayment;
}
