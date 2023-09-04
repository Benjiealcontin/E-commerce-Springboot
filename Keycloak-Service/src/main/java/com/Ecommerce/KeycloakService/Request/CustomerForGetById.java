package com.Ecommerce.KeycloakService.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerForGetById {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private Map<String, Object> attributes;
}
