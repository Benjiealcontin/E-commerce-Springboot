package com.Ecommerce.KeycloakService.Controller;

import com.Ecommerce.KeycloakService.Dto.TokenResponse;
import com.Ecommerce.KeycloakService.Exception.AddCustomerConflictException;
import com.Ecommerce.KeycloakService.Exception.LoginException;
import com.Ecommerce.KeycloakService.Exception.LogoutException;
import com.Ecommerce.KeycloakService.Exception.TokenException;
import com.Ecommerce.KeycloakService.Request.AddCustomer;
import com.Ecommerce.KeycloakService.Request.CustomerForGetById;
import com.Ecommerce.KeycloakService.Request.Login;
import com.Ecommerce.KeycloakService.Service.Keycloak_Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/keycloak")
public class KeycloakController {

    private final Keycloak_Service keycloakService;

    public KeycloakController(Keycloak_Service keycloakService) {
        this.keycloakService = keycloakService;
    }

    //Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login login) {
        try {
            return ResponseEntity.ok( keycloakService.CustomerLogin(login));
        }catch (LoginException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Logout
    @PostMapping("/logout")
    public ResponseEntity<?> CustomerLogout(@RequestParam String refresh_token) {
        try {
            return ResponseEntity.ok(keycloakService.CustomerLogout(refresh_token));
        } catch (LogoutException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Add Customer
    @PostMapping("/add-customer")
    public ResponseEntity<String> createCustomer(@RequestBody AddCustomer customer) {
        try {
            keycloakService.createCustomer(customer);
            return ResponseEntity.ok("Customer created successfully");
        } catch (AddCustomerConflictException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Get Customer Info
    @GetMapping("/userInfo")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String bearerToken) {
        try{
            return ResponseEntity.ok(keycloakService.decodeUserToken(bearerToken));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Get customer Info By Id
    @GetMapping("/getById/{sub}")
    public ResponseEntity<?> getUser(@PathVariable String sub, @RequestHeader("Authorization") String bearerToken) {
        CustomerForGetById customer = keycloakService.getCustomerInfo(sub,bearerToken);
        if (customer != null) {
            return ResponseEntity.ok(customer);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
