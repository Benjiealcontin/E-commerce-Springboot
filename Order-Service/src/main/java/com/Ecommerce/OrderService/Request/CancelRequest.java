package com.Ecommerce.OrderService.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelRequest {
    @NotBlank(message = "Cancel reason cannot be blank")
    @Size(min = 1, max = 255, message = "Cancel reason must be between 1 and 255 characters")
    private String cancelReason;
}
