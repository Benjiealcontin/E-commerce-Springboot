package com.Ecommerce.CartService.Service;


import com.Ecommerce.CartService.Dto.TokenDTO;
import com.Ecommerce.CartService.Exception.InvalidTokenException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nimbusds.jose.shaded.gson.JsonObject;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

@Service
public class TokenDecodeService {

    public String extractToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid Bearer token");
        }
        return bearerToken.substring(7);
    }

    public TokenDTO decodeUserToken(String token) {
        if (token != null) {
            // Decode the JWT token
            DecodedJWT decodedJWT = JWT.decode(token);

            // Extract user information from the JWT payload
            String sub = decodedJWT.getSubject();

            return new TokenDTO(sub);
        } else {
            throw new IllegalArgumentException("Token is null");
        }
    }
}
