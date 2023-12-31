package com.Ecommerce.PaymentService.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "OrderPayments")
public class OrderPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private String paymentMethod;
    private String shippingMethod;
    @OneToOne(mappedBy = "orderPayment", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private ShippingOption shippingOption;
    @OneToOne(mappedBy = "orderPayment", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private PaymentDetail paymentDetail;
    private double amount;
}
