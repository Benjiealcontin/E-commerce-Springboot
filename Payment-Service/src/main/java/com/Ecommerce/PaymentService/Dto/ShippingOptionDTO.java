package com.Ecommerce.PaymentService.Dto;

import lombok.Data;

@Data
public class ShippingOptionDTO {
    private String shippingName;
    private String description;
    private double price;
    private int estimatedDeliveryTimeInDays;
}
