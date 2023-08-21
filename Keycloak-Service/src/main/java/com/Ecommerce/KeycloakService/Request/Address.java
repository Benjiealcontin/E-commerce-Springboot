package com.Ecommerce.KeycloakService.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String street_address;
    private String locality;
    private String region;
    private String postal_code;
    private String country;
}
