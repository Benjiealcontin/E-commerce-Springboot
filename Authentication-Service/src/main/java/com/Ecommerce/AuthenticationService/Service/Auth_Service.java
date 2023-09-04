package com.Ecommerce.AuthenticationService.Service;

import com.Ecommerce.AuthenticationService.Dto.MessageResponse;
import com.Ecommerce.AuthenticationService.Dto.TokenDetails;
import com.Ecommerce.AuthenticationService.Exception.LoginException;
import com.Ecommerce.AuthenticationService.Exception.LogoutException;
import com.Ecommerce.AuthenticationService.Request.Login;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class Auth_Service {

    private final WebClient.Builder webClientBuilder;

    public Auth_Service(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    private static final String KEYCLOAK_SERVICE_URL = "http://localhost:8081/realms/E-commerce";

    public TokenDetails CustomerLogin(Login login) {
        String clientId = "Consumer-clients";
        String clientSecret = "p8Q6W5YMegeVcAVToJfvi1BCEIRPT7x0";
        String grantType = "password";

        try {
            return webClientBuilder.build()
                    .post()
                    .uri(KEYCLOAK_SERVICE_URL + "/protocol/openid-connect/token")
                    .body(BodyInserters
                            .fromFormData("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("grant_type", grantType)
                            .with("username", login.getUsername())
                            .with("password", login.getPassword()))
                    .retrieve()
                    .bodyToMono(TokenDetails.class)
                    .block();

        } catch (WebClientResponseException.Unauthorized | WebClientResponseException.BadRequest e) {
            String responseBody = e.getResponseBodyAsString();
            throw new LoginException(responseBody);
        }
    }

    //Logout
    public MessageResponse CustomerLogout(String refresh_token) {
        String clientId = "Consumer-clients";
        String clientSecret = "p8Q6W5YMegeVcAVToJfvi1BCEIRPT7x0";

        try{
            webClientBuilder.build()
                    .post()
                    .uri(KEYCLOAK_SERVICE_URL + "/protocol/openid-connect/logout")
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
}
