package com.Ecommerce.KeycloakService.Controller;

import com.Ecommerce.KeycloakService.Exception.*;
import com.Ecommerce.KeycloakService.Request.Customer;
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
    public ResponseEntity<String> createCustomer(@RequestBody Customer customer) {
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
        try {
            return ResponseEntity.ok(keycloakService.decodeUserToken(bearerToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Get customer Info By ID
    @GetMapping("/getCustomerById/{customerId}")
    public ResponseEntity<?> getCustomerInfoByID(@PathVariable String customerId, @RequestHeader("Authorization") String bearerToken) {
        try {
            return ResponseEntity.ok(keycloakService.getCustomerInfoById(customerId, bearerToken));
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Delete Customer
    @DeleteMapping("/delete/{customerId}")
    public ResponseEntity<?> DeleteCustomer(@PathVariable String customerId, @RequestHeader("Authorization") String bearerToken) {
        try {
            keycloakService.deleteCustomer(customerId, bearerToken);
            return ResponseEntity.ok("Customer Deleted Successfully.");
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Update Customer Details
    @PutMapping("/update/{customerId}")
    public ResponseEntity<?> UpdateCustomer(@PathVariable String customerId, @RequestBody Customer customer, @RequestHeader("Authorization") String bearerToken) {
        try {
            keycloakService.updateCustomerDetails(customerId, customer, bearerToken);
            return ResponseEntity.ok("Customer Updated Successfully.");
        } catch (UpdateCustomerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
