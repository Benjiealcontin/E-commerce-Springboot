package com.Ecommerce.PaymentService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class OrderDTO {
    private double totalAmount;
    private CustomerDTO customer;
}
