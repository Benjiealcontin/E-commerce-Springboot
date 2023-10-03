package com.Ecommerce.ShippingService.Controller;

import com.Ecommerce.ShippingService.Exception.ShippingAlreadyExistsException;
import com.Ecommerce.ShippingService.Exception.ShippingMethodNotFoundException;
import com.Ecommerce.ShippingService.Exception.ShippingOptionNotFoundException;
import com.Ecommerce.ShippingService.Request.ProductTotalAmountRequest;
import com.Ecommerce.ShippingService.Request.ShippingOptionRequest;
import com.Ecommerce.ShippingService.Service.Shipping_Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/shipping")
public class ShippingController {
    private final Shipping_Service shippingService;

    public ShippingController(Shipping_Service shippingService) {
        this.shippingService = shippingService;
    }

    //Add Shipping option
    @PostMapping("/add-shippingOption")
    public ResponseEntity<?> addShippingOption(@RequestBody ShippingOptionRequest shippingOptionRequest,
                                               BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                // If there are validation errors, return a bad request response with error details
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            return ResponseEntity.ok(shippingService.addShippingOption(shippingOptionRequest));
        } catch (ShippingAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Get All the Shipping Option
    @GetMapping("/getAllShippingOptions")
    public ResponseEntity<?> getShippingOption() {
        try {
            return ResponseEntity.ok(shippingService.getShippingOption());
        } catch (ShippingOptionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Get Shipping Option by ShippingName
    @GetMapping("/shippingOption/{shippingName}")
    public ResponseEntity<?> getShippingOptionByShippingName(@PathVariable String shippingName) {
        try {
            return ResponseEntity.ok(shippingService.getShippingOptionByShippingName(shippingName));
        } catch (ShippingOptionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Calculate the TotalAmount with Shipping fee
    @PostMapping("/calculateTotalCost")
    public ResponseEntity<?> calculateTotalCost(@RequestBody ProductTotalAmountRequest request) {
        try {
            return ResponseEntity.ok(shippingService.calculateTotalCost(request));
        } catch (ShippingMethodNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Delete Shipping Method
    @DeleteMapping("/delete-shippingMethod/{id}")
    public ResponseEntity<?> deleteShippingMethod(@PathVariable long id) {
        try {
            shippingService.deleteShippingMethod(id);
            return ResponseEntity.ok("Shipping Method Deleted Successfully.");
        } catch (ShippingOptionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Update Shipping Method Details
    @PutMapping("/update-shippingMethod/{id}")
    public ResponseEntity<?> updateShippingMethod(@PathVariable long id, @RequestBody ShippingOptionRequest shippingOptionRequest) {
        try {
            shippingService.updateShippingOption(id, shippingOptionRequest);
            return ResponseEntity.ok("Shipping Method Updated Successfully!");
        } catch (ShippingOptionNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
