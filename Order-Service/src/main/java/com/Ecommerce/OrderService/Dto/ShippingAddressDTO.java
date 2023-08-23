package com.Ecommerce.OrderService.Dto;

import lombok.Data;

@Data
public class ShippingAddressDTO {
    private String streetAddress;
    private String locality;
    private String region;
    private String postalCode;
    private String country;
}
