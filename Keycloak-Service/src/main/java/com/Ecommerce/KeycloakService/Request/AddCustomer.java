package com.Ecommerce.KeycloakService.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCustomer {
    private String username;
    private String email;
    private boolean enabled;
    private String firstName;
    private String lastName;
    private Map<String, Object> attributes;
    private List<String> groups;
    private List<Credentials> credentials;
}
