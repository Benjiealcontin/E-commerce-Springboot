package com.Ecommerce.CustomerService.Service;

import com.Ecommerce.CustomerService.Dto.MessageResponse;
import com.Ecommerce.CustomerService.Dto.TokenResponse;
import com.Ecommerce.CustomerService.Exception.AddCustomerConflictException;
import com.Ecommerce.CustomerService.Request.AddCustomer;
import com.Ecommerce.CustomerService.Request.Login;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class Customer_Service {

    private final WebClient.Builder webClientBuilder;

    public Customer_Service(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    //Login


    //Add Customer
    public MessageResponse Add_Customer(AddCustomer customer) {
        try{
            WebClient webClient = webClientBuilder.baseUrl("http://Keycloak-Service").build();

            webClient.post()
                    .uri("/api/keycloak/add-customer")
                    .body(Mono.just(customer), AddCustomer.class)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return new MessageResponse("Customer Added Successfully.");

        }catch (WebClientResponseException.Conflict e) {
            String responseBody = e.getResponseBodyAsString();
            throw new AddCustomerConflictException(responseBody);
        }
    }
}
