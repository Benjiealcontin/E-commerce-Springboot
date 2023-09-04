package com.Ecommerce.CustomerService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetails {
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
