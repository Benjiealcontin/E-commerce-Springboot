package com.Ecommerce.KeycloakService.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenData {
    private String sub;
    private Address address;
    private boolean email_verified;
    private String name;
    private String phone_number;
    private String preferred_username;
    private String given_name;
    private String family_name;
    private String email;
}
