package com.Ecommerce.KeycloakService.Service;

import com.Ecommerce.KeycloakService.Dto.*;
import com.Ecommerce.KeycloakService.Exception.AddCustomerConflictException;
import com.Ecommerce.KeycloakService.Exception.LoginException;
import com.Ecommerce.KeycloakService.Exception.LogoutException;
import com.Ecommerce.KeycloakService.Request.AddCustomer;
import com.Ecommerce.KeycloakService.Request.CustomerForGetById;
import com.Ecommerce.KeycloakService.Request.Login;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;


import java.util.Objects;

@Service
public class Keycloak_Service {
    private final WebClient.Builder webClientBuilder;
    public Keycloak_Service(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }


    //Login
    public TokenResponse CustomerLogin(Login login) {
        String clientId = "Consumer-clients";
        String clientSecret = "p8Q6W5YMegeVcAVToJfvi1BCEIRPT7x0";
        String grantType = "password";

        try {
            TokenDetails tokenDetails = webClientBuilder.baseUrl("http://localhost:8081/realms/E-commerce")
                    .build()
                    .post()
                    .uri("/protocol/openid-connect/token")
                    .body(BodyInserters
                            .fromFormData("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("grant_type", grantType)
                            .with("username", login.getUsername())
                            .with("password", login.getPassword()))
                    .retrieve()
                    .bodyToMono(TokenDetails.class)
                    .block(); // Block to get the result

            if (tokenDetails != null) {
                TokenResponse tokenResponse = new TokenResponse();
                tokenResponse.setAccess_token(tokenDetails.getAccess_token());
                tokenResponse.setRefresh_token(tokenDetails.getRefresh_token());
                tokenResponse.setExpires_in(tokenDetails.getExpires_in());
                tokenResponse.setRefresh_expires_in(tokenDetails.getRefresh_expires_in());
                return tokenResponse;
            } else {
                throw new LoginException("Token retrieval failed");
            }
        } catch (WebClientResponseException.Unauthorized e) {
            String responseBody = e.getResponseBodyAsString();
            throw new LoginException(responseBody);
        }
    }


    //Logout
    public MessageResponse CustomerLogout(String refresh_token) {
        String clientId = "Consumer-clients";
        String clientSecret = "p8Q6W5YMegeVcAVToJfvi1BCEIRPT7x0";

        try{
            webClientBuilder.baseUrl("http://localhost:8081/realms/E-commerce")
                    .build()
                    .post()
                    .uri("/protocol/openid-connect/logout")
                    .body(BodyInserters
                            .fromFormData("refresh_token", refresh_token)
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret))
                    .retrieve()
                    .bodyToMono(TokenDetails.class)
                    .block();
            return new MessageResponse("Logout Successfully.");
        }catch (WebClientResponseException.BadRequest e) {
            String responseBody = e.getResponseBodyAsString();
            throw new LogoutException(responseBody);
        }
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

    //Get Customer Info
    public CustomerForGetById getCustomerInfo(String sub, String bearerToken) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/admin/realms/E-commerce/users/{id}",sub)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(CustomerForGetById.class).block();
    }

    //Get by ID
//    public void getById(String bearerToken){
//        String example = "96a6bd26-7cc0-48dd-9ec3-a5cafa3c56d7";
//
//        UserTokenDataForGetById customer = webClientBuilder.build()
//                .get()
//                .uri("http://localhost:8081/admin/realms/E-commerce/users/{id}", example)
//                .header(HttpHeaders.AUTHORIZATION, bearerToken)
//                .retrieve()
//                .bodyToMono(UserTokenDataForGetById.class)
//                .block();
//
//        assert customer != null;
//        String givenName = customer.getFirstName();
//        String familyName = customer.getLastName();
//        String email = customer.getEmail();
//        Map<String, Object> patientAttributes = customer.getAttributes();
//        String[] keys = {"street", "locality", "region", "postal_code", "country"};
//        String[] addresses = new String[keys.length];
//
//        for (int i = 0; i < keys.length; i++) {
//            Object value = patientAttributes.get(keys[i]);
//            addresses[i] = value.toString().replaceAll("[\\[\\]]", "");
//        }
//
//        System.out.println(givenName);
//        System.out.println(familyName);
//        System.out.println(email);
//        System.out.println(addresses[0]);
//        System.out.println(addresses[1]);
//        System.out.println(addresses[2]);
//        System.out.println(addresses[3]);
//        System.out.println(addresses[4]);
//
//    }

    //Add Customer
    public void createCustomer(AddCustomer customer) {
        try {
            String accessToken = obtainAccessToken();
            sendCreateCustomerRequest(customer, accessToken);
        } catch (WebClientResponseException e) {
            handleWebClientResponseException(e);
        }
    }

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

    private void handleWebClientResponseException(WebClientResponseException e) {
        if (e.getStatusCode() == HttpStatus.CONFLICT) {
            String responseBody = e.getResponseBodyAsString();
            throw new AddCustomerConflictException(responseBody);
        } else {
            throw e;
        }
    }

}
