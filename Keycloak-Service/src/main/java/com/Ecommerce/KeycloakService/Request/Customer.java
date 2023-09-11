package com.Ecommerce.KeycloakService.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Customer {
    private String username;
    private String email;
    private boolean enabled;
    private String firstName;
    private String lastName;
    private Address attributes;
    private List<String> groups;
    private List<Credentials> credentials;

    public Customer() {
        this.groups = new ArrayList<>();
        this.groups.add("Consumer"); // Set the default value
    }
}
