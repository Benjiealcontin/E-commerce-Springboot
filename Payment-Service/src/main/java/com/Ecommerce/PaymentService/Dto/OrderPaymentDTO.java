package com.Ecommerce.PaymentService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPaymentDTO {
    private Long orderId;
    private String paymentMethod;
    private PaymentDetailDTO paymentDetail;
    private ShippingOptionDTO shippingOption;
    private double amount;
}
