package com.Ecommerce.KeycloakService.Controller;

import com.Ecommerce.KeycloakService.Request.CustomerForGetById;
import com.Ecommerce.KeycloakService.Service.Keycloak_Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/keycloak")
public class KeycloakController {

    private final Keycloak_Service keycloakService;

    public KeycloakController(Keycloak_Service keycloakService) {
        this.keycloakService = keycloakService;
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
