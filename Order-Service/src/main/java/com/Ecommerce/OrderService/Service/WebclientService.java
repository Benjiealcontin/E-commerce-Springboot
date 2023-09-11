package com.Ecommerce.OrderService.Service;

import com.Ecommerce.OrderService.Exception.ServiceUnavailableException;
import com.Ecommerce.OrderService.Exception.WebClientException;
import com.Ecommerce.OrderService.Request.CustomerInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class WebclientService {
    private final WebClient.Builder webClientBuilder;

    public WebclientService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    //User Info
    public CustomerInfo getUserInfo(String bearerToken) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://Keycloak-Service/api/keycloak/userInfo")
                    .header("Authorization", bearerToken)
                    .retrieve()
                    .bodyToMono(CustomerInfo.class)
                    .block();
        } catch (WebClientResponseException.BadRequest e) {
            String responseBody = e.getResponseBodyAsString();
            throw new WebClientException(responseBody);
        }catch (WebClientResponseException.ServiceUnavailable e) {
            String responseBody = "Keycloak service is currently unavailable. Please try again later.";
            throw new ServiceUnavailableException(responseBody);
        }
    }
}
