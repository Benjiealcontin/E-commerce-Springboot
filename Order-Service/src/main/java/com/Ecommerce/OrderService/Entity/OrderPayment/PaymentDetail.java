package com.Ecommerce.OrderService.Entity.OrderPayment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PaymentDetails")
public class PaymentDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardNumber;
    private String expirationDate;
    private String cvv;

    @OneToOne(mappedBy = "paymentDetail", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private BillingAddress billingAddress;

    @OneToOne
    @MapsId
    @JoinColumn(name = "orderPayment_id")
    private OrderPayment orderPayment;
}
