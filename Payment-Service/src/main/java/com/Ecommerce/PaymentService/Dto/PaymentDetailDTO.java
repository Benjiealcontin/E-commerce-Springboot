package com.Ecommerce.PaymentService.Dto;

import com.Ecommerce.PaymentService.Entity.BillingAddress;
import com.Ecommerce.PaymentService.Entity.OrderPayment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailDTO {
    private String cardNumber;
    private String expirationDate;
    private String cvv;
    private BillingAddressDTO billingAddress;
    private ShippingOptionDTO shippingOption;
}
