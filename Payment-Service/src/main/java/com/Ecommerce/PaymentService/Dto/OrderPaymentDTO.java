package com.Ecommerce.PaymentService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPaymentDTO {
    private Long orderId;
    private String paymentMethod;
    private PaymentDetailDTO paymentDetail;
    private double amount;
}
