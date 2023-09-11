package com.Ecommerce.CustomerService.Service;


import com.Ecommerce.CustomerService.Dto.CustomerDetails;
import com.Ecommerce.CustomerService.Dto.MessageResponse;
import com.Ecommerce.CustomerService.Dto.Order.OrderDTO;
import com.Ecommerce.CustomerService.Exception.*;
import com.Ecommerce.CustomerService.Request.Customer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
public class Customer_Service {

    private final WebClient.Builder webClientBuilder;

    public Customer_Service(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    private static final String KEYCLOAK_SERVICE_URL = "http://Keycloak-Service/api/keycloak";
    private static final String ORDER_SERVICE_URL = "http://Order-Service/api/order";

    //Add Customer
    public MessageResponse Add_Customer(Customer customer) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(KEYCLOAK_SERVICE_URL + "/add-customer")
                    .body(Mono.just(customer), Customer.class)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return new MessageResponse("Customer Added Successfully.");
        } catch (WebClientResponseException.Conflict e) {
            String responseBody = e.getResponseBodyAsString();
            throw new AddCustomerConflictException(responseBody);
        }catch (WebClientResponseException.ServiceUnavailable e) {
            String responseBody = "Keycloak service is currently unavailable. Please try again later.";
            throw new ServiceUnavailableException(responseBody);
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
        }catch (WebClientResponseException.ServiceUnavailable e) {
            String responseBody = "Keycloak service is currently unavailable. Please try again later.";
            throw new ServiceUnavailableException(responseBody);
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
        }catch (WebClientResponseException.ServiceUnavailable e) {
            String responseBody = "Keycloak service is currently unavailable. Please try again later.";
            throw new ServiceUnavailableException(responseBody);
        }
    }

    //Update Customer
    public void updateCustomer(String customerId, Customer customer, String bearerToken) {
        try {
            webClientBuilder.build()
                    .put()
                    .uri(KEYCLOAK_SERVICE_URL + "/update/{customerId}", customerId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .body(BodyInserters.fromValue(customer))
                    .retrieve()
                    .bodyToMono(Customer.class)
                    .block();
        } catch (WebClientResponseException.BadRequest e) {
            String responseBody = e.getResponseBodyAsString();
            throw new BadRequestException(responseBody);
        } catch (WebClientResponseException.NotFound | WebClientResponseException.InternalServerError e) {
            String responseBody = e.getResponseBodyAsString();
            throw new CustomerNotFoundException(responseBody);
        }
    }

    //Get Customer Order from Order Service
    public List<OrderDTO> getCustomerOrders(String bearerToken) {
        try {
            Flux<OrderDTO> orderFlux = webClientBuilder.build()
                    .get()
                    .uri(ORDER_SERVICE_URL + "/customerOrder") // Replace with the actual API endpoint for customer orders
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToFlux(OrderDTO.class);

            return orderFlux.collectList().block();
        } catch (WebClientResponseException.NotFound e) {
            String responseBody = e.getResponseBodyAsString();
            throw new OrderNotFoundException(responseBody);
        }catch (WebClientResponseException.ServiceUnavailable e) {
            String responseBody = "Keycloak service is currently unavailable. Please try again later.";
            throw new ServiceUnavailableException(responseBody);
        }
    }
}
