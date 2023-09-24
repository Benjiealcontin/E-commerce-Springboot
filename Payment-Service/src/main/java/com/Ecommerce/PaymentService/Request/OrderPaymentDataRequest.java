package com.Ecommerce.PaymentService.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPaymentDataRequest {
    @NotNull(message = "orderId is required")
    private Long orderId;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod;

    private String shippingMethod;

    @Valid
    private PaymentDetailsRequest paymentDetail;

    @DecimalMin(value = "0.01", message = "amount must be greater than or equal to 0.01")
    @NotNull(message = "amount is required")
    private BigDecimal amount;
}
