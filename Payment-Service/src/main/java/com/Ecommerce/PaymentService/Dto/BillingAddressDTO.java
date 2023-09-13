package com.Ecommerce.PaymentService.Dto;

import com.Ecommerce.PaymentService.Entity.PaymentDetail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillingAddressDTO {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
