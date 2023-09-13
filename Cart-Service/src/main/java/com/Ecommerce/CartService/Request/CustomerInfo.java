package com.Ecommerce.CartService.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInfo {
    private String fullName;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String consumerId;
    private String streetAddress;
    private String locality;
    private String region;
    private String postalCode;
    private String country;
}
