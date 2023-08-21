package com.Ecommerce.KeycloakService.Controller;

import com.Ecommerce.KeycloakService.Service.Keycloak_Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
