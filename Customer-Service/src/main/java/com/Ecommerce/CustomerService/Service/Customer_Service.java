package com.Ecommerce.CustomerService.Service;


import com.Ecommerce.CustomerService.Dto.CustomerDetails;
import com.Ecommerce.CustomerService.Dto.MessageResponse;
import com.Ecommerce.CustomerService.Exception.AddCustomerConflictException;
import com.Ecommerce.CustomerService.Exception.CustomerNotFoundException;
import com.Ecommerce.CustomerService.Exception.ForbiddenException;
import com.Ecommerce.CustomerService.Request.AddCustomer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;


@Service
public class Customer_Service {

    private final WebClient.Builder webClientBuilder;

    public Customer_Service(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    private static final String KEYCLOAK_SERVICE_URL = "http://Keycloak-Service/api/keycloak";

    //Add Customer
    public MessageResponse Add_Customer(AddCustomer customer) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(KEYCLOAK_SERVICE_URL + "/add-customer")
                    .body(Mono.just(customer), AddCustomer.class)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return new MessageResponse("Customer Added Successfully.");
        } catch (WebClientResponseException.Conflict e) {
            String responseBody = e.getResponseBodyAsString();
            throw new AddCustomerConflictException(responseBody);
        }
    }

    //Get the Customer Details by ID
    public CustomerDetails CustomerDetails(String customerId, String bearerToken) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(KEYCLOAK_SERVICE_URL + "/getCustomerById/{id}", customerId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(CustomerDetails.class)
                    .block();
        } catch (WebClientResponseException.NotFound | WebClientResponseException.BadRequest e) {
            String responseBody = e.getResponseBodyAsString();
            throw new CustomerNotFoundException(responseBody);
        }
    }

    //Delete Customer
    public void deleteCustomer(String customerId, String bearerToken) {
        try {
            webClientBuilder.build()
                    .delete()
                    .uri(KEYCLOAK_SERVICE_URL + "/delete/{customerId}", customerId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException.Forbidden e) {
            String responseBody = e.getResponseBodyAsString();
            throw new ForbiddenException(responseBody);
        } catch (WebClientResponseException.NotFound e) {
            String responseBody = e.getResponseBodyAsString();
            throw new CustomerNotFoundException(responseBody);
        }
    }
}
