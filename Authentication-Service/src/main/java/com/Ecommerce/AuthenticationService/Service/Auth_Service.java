package com.Ecommerce.AuthenticationService.Service;

import com.Ecommerce.AuthenticationService.Dto.MessageResponse;
import com.Ecommerce.AuthenticationService.Dto.TokenDetails;
import com.Ecommerce.AuthenticationService.Dto.TokenResponse;
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
}
