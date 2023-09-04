package com.Ecommerce.AuthenticationService.Dto;

import lombok.Data;

@Data
public class TokenDetails {
    private String access_token;
    private long expires_in;
    private long refresh_expires_in;
    private String refresh_token;
    private String token_type;
    private long not_before_policy;
    private String session_state;
    private String scope;
}
