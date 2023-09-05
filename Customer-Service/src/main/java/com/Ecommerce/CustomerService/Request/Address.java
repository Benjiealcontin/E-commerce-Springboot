package com.Ecommerce.CustomerService.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private List<String> locality;
    private List<String> country;
    private List<String> phoneNumber;
    private List<String> postalCode;
    private List<String> region;
    private List<String> street;
}
