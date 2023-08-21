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

    public void getUserInfo(String bearerToken) {
        Customer customer = webClientBuilder.build()
                .get()
                .uri("http://Keycloak-Service/api/keycloak/userInfo")
                .header("Authorization", bearerToken)
                .retrieve()
                .bodyToMono(Customer.class)
                .block();


        assert customer != null;
        System.out.println(customer.getConsumerId());
        System.out.println(customer.getCountry());
    }

}
