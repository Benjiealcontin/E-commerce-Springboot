package com.Ecommerce.KeycloakService.Service;

import com.Ecommerce.KeycloakService.Request.Address;
import com.Ecommerce.KeycloakService.Request.Customer;
import com.Ecommerce.KeycloakService.Request.CustomerForGetById;
import com.Ecommerce.KeycloakService.Request.UserTokenData;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

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
}
