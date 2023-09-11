package com.Ecommerce.CustomerService.Controller;

import com.Ecommerce.CustomerService.Exception.*;
import com.Ecommerce.CustomerService.Request.Customer;
import com.Ecommerce.CustomerService.Service.Customer_Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final Customer_Service customerService;

    public CustomerController(Customer_Service customerService) {
        this.customerService = customerService;
    }

    //Add Customer
    @PostMapping("/add-customer")
    public ResponseEntity<?> addCustomer(@Valid @RequestBody Customer customer, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                // If there are validation errors, return a bad request response with error details
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            return ResponseEntity.ok(customerService.Add_Customer(customer));
        } catch (AddCustomerConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Get Customer Details by ID
    @GetMapping("/customerDetails/{customerId}")
    public ResponseEntity<?> CustomerDetails(@PathVariable String customerId, @RequestHeader("Authorization") String bearerToken) {
        try {
            return ResponseEntity.ok(customerService.CustomerDetails(customerId, bearerToken));
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Delete Customer
    @DeleteMapping("/delete-customer/{customerId}")
    public ResponseEntity<?> deleteCustomer(@PathVariable String customerId, @RequestHeader("Authorization") String bearerToken) {
        try {
            customerService.deleteCustomer(customerId, bearerToken);
            return ResponseEntity.ok("Customer Deleted Successfully.");
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Update Customer Details
    @PutMapping("/update-customer/{customerId}")
    public ResponseEntity<?> updateCustomerDetails(@PathVariable String customerId,
                                                   Customer customer,
                                                   @RequestHeader("Authorization") String bearerToken) {
        try {
            customerService.updateCustomer(customerId, customer, bearerToken);
            return ResponseEntity.ok("Customer Updated Successfully");
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Customer Order list
    @GetMapping("/customer-order")
    public ResponseEntity<?> getCustomerOrder(@RequestHeader("Authorization") String bearerToken) {
        try {
            return ResponseEntity.ok(customerService.getCustomerOrders(bearerToken));
        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
