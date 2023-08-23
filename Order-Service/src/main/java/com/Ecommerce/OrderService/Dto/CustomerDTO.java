package com.Ecommerce.OrderService.Dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private String consumerId;
    private String fullName;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}
