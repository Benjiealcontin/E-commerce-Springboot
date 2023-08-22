package com.Ecommerce.OrderService.Service;

import com.Ecommerce.OrderService.Request.CustomerRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WebclientService {
    private final WebClient.Builder webClientBuilder;

    public WebclientService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    //User Info
    public CustomerRequest getUserInfo(String bearerToken) {
        return webClientBuilder.build()
                .get()
                .uri("http://Keycloak-Service/api/keycloak/userInfo")
                .header("Authorization", bearerToken)
                .retrieve()
                .bodyToMono(CustomerRequest.class)
                .block();
    }
}
