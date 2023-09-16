package com.Ecommerce.OrderService.Service;


import com.Ecommerce.OrderService.Dto.TokenDTO;
import com.Ecommerce.OrderService.Exception.InvalidTokenException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

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
