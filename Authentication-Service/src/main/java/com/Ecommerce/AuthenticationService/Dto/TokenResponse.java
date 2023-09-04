package com.Ecommerce.AuthenticationService.Dto;

import lombok.Data;

@Data
public class TokenResponse {
    private String access_token;
    private long expires_in;
    private long refresh_expires_in;
    private String refresh_token;
}
