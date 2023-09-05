package com.Ecommerce.CustomerService.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class AddCustomer {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private boolean enabled;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private Address attributes;

    private List<String> groups;

    @Valid
    private List<Credentials> credentials;

    public AddCustomer() {
        this.groups = new ArrayList<>();
        this.groups.add("Consumer"); // Set the default value
    }


}
