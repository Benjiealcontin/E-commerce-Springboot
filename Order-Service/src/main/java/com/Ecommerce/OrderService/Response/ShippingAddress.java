package com.Ecommerce.OrderService.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
