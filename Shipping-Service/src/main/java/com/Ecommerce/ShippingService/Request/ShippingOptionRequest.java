package com.Ecommerce.ShippingService.Request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ShippingOptionRequest {
    @NotBlank(message = "Shipping name is required")
    @Size(max = 255, message = "Shipping name must not exceed 255 characters")
    private String shippingName;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be a positive value")
    private double price;

    @NotNull(message = "Estimated delivery time is required")
    @PositiveOrZero(message = "Estimated delivery time must be a positive or zero value")
    private int estimatedDeliveryTimeInDays;
}
