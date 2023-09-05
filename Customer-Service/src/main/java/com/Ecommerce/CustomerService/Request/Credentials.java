package com.Ecommerce.CustomerService.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Credentials {
    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Value is required")
    @Size(min = 8, message = "Value must be at least 8 characters long")
    private String value;

    @NotNull(message = "Temporary is required")
    private boolean temporary;
}
