package com.Ecommerce.KeycloakService.Service;

import com.Ecommerce.KeycloakService.Dto.*;
import com.Ecommerce.KeycloakService.Exception.AddCustomerConflictException;
import com.Ecommerce.KeycloakService.Exception.CustomerNotFoundException;
import com.Ecommerce.KeycloakService.Request.AddCustomer;
import com.Ecommerce.KeycloakService.Request.CustomerForGetById;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;


import java.util.Map;
import java.util.Objects;

@Service
public class Keycloak_Service {
    private final WebClient.Builder webClientBuilder;
    public Keycloak_Service(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    //User Info
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

    //Get Customer Info by ID
    public Customer getCustomerInfoById(String customerId, String bearerToken) {
        try {
            CustomerForGetById customerInfo = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8081/admin/realms/E-commerce/users/{id}", customerId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(CustomerForGetById.class)
                    .block();

            assert customerInfo != null;

            Customer customer = new Customer();
            customer.setFullName(customerInfo.getFirstName() + " " + customerInfo.getLastName());
            customer.setFirstName(customerInfo.getFirstName());
            customer.setLastName(customerInfo.getLastName());
            customer.setConsumerId(customerInfo.getId());
            customer.setEmail(customerInfo.getEmail());

            Map<String, Object> patientAttributes = customerInfo.getAttributes();
            String[] keys = {"street", "locality", "region", "postal_code", "country", "phoneNumber"};
            String[] attributes = new String[keys.length];

            for (int i = 0; i < keys.length; i++) {
                Object value = patientAttributes.get(keys[i]);
                attributes[i] = value != null ? value.toString().replaceAll("[\\[\\]]", "") : null;
            }

            customer.setPhoneNumber(attributes[5]);
            customer.setStreetAddress(attributes[0]);
            customer.setLocality(attributes[1]);
            customer.setRegion(attributes[2]);
            customer.setPostalCode(attributes[3]);
            customer.setCountry(attributes[4]);

            return customer;
        } catch (WebClientResponseException.NotFound e) {
            String responseBody = e.getResponseBodyAsString();
            throw new CustomerNotFoundException(responseBody);

        }
    }

    //Add Customer
    public void createCustomer(AddCustomer customer) {
        try {
            String accessToken = obtainAccessToken();
            sendCreateCustomerRequest(customer, accessToken);
        } catch (WebClientResponseException e) {
            handleWebClientResponseException(e);
        }
    }

    //To obtain access token
    private String obtainAccessToken() {
        String clientId = "Consumer-clients";
        String clientSecret = "p8Q6W5YMegeVcAVToJfvi1BCEIRPT7x0";
        String grantType = "client_credentials";

        TokenDetails token = webClientBuilder.build()
                .post()
                .uri("http://localhost:8081/realms/E-commerce/protocol/openid-connect/token")
                .body(BodyInserters
                        .fromFormData("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("grant_type", grantType))
                .retrieve()
                .bodyToMono(TokenDetails.class)
                .block();

        return Objects.requireNonNull(token).getAccess_token();
    }

    //To send the data to Keycloak
    private void sendCreateCustomerRequest(AddCustomer customer, String bearerToken) {
        Mono<Void> response = webClientBuilder.build()
                .post()
                .uri("http://localhost:8081/admin/realms/E-commerce/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .body(BodyInserters.fromValue(customer))
                .retrieve()
                .bodyToMono(Void.class);

        response.block(); // This blocks until the request completes
    }

    //Exception Handler
    private void handleWebClientResponseException(WebClientResponseException e) {
        if (e.getStatusCode() == HttpStatus.CONFLICT) {
            String responseBody = e.getResponseBodyAsString();
            throw new AddCustomerConflictException(responseBody);
        } else {
            throw e;
        }
    }
}
