package com.Ecommerce.KeycloakService.Controller;

import com.Ecommerce.KeycloakService.Dto.Customer;
import com.Ecommerce.KeycloakService.Exception.AddCustomerConflictException;
import com.Ecommerce.KeycloakService.Exception.CustomerNotFoundException;
import com.Ecommerce.KeycloakService.Request.AddCustomer;
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

    //Get Customer Info for Save the Customer Info in Order Service
    @GetMapping("/userInfo")
    public ResponseEntity<?> getCustomerInfoByToken(@RequestHeader("Authorization") String bearerToken) {
        try{
            return ResponseEntity.ok(keycloakService.decodeUserToken(bearerToken));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Get customer Info By ID
    @GetMapping("/getCustomerById/{sub}")
    public ResponseEntity<?> getCustomerInfoByID(@PathVariable String sub, @RequestHeader("Authorization") String bearerToken) {
        try{
            return ResponseEntity.ok(keycloakService.getCustomerInfoById(sub,bearerToken));
        }catch (CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

}
