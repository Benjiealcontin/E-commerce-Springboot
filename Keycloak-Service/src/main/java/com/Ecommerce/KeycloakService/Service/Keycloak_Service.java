package com.Ecommerce.KeycloakService.Service;

import com.Ecommerce.KeycloakService.Request.Address;
import com.Ecommerce.KeycloakService.Request.Customer;
import com.Ecommerce.KeycloakService.Request.UserTokenData;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class Keycloak_Service {
    private final WebClient.Builder webClientBuilder;
    public Keycloak_Service(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Customer decodeUserToken(String bearerToken) {
        UserTokenData userTokenData = webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/realms/E-commerce/protocol/openid-connect/userinfo")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(UserTokenData.class)
                .block();

        assert userTokenData != null;

        Address address = userTokenData.getAddress();
        return new Customer(
                userTokenData.getName(),
                userTokenData.getGiven_name(),
                userTokenData.getFamily_name(),
                userTokenData.getEmail(),
                userTokenData.getPhone_number(),
                userTokenData.getSub(),
                address.getStreet_address(),
                address.getLocality(),
                address.getRegion(),
                address.getPostal_code(),
                address.getCountry()
        );
    }
}
