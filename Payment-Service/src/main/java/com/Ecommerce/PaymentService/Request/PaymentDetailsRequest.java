package com.Ecommerce.PaymentService.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailsRequest {
    @NotBlank(message = "cardNumber is required")
    private String cardNumber;

    @NotBlank(message = "expirationDate is required")
    private String expirationDate;

    @NotBlank(message = "cvv is required")
    private String cvv;

    @Valid
    @NotNull(message = "billingAddress is required")
    private BillingAddressRequest billingAddress;
}
