package com.Ecommerce.CustomerService.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Credentials {
    private String type;
    private String value;
    private boolean temporary;
}
