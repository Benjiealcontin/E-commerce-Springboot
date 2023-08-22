package com.Ecommerce.OrderService.Service;

import com.Ecommerce.OrderService.Request.Customer;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TokenDecodeService {
    private final WebClient.Builder webClientBuilder;

    public TokenDecodeService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Customer getUserInfo(String bearerToken) {
        return webClientBuilder.build()
                .get()
                .uri("http://Keycloak-Service/api/keycloak/userInfo")
                .header("Authorization", bearerToken)
                .retrieve()
                .bodyToMono(Customer.class)
                .block();
    }
}
