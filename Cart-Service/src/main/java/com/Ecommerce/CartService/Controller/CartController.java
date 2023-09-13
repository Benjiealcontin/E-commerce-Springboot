package com.Ecommerce.CartService.Controller;

import com.Ecommerce.CartService.Exception.CartNotFoundException;
import com.Ecommerce.CartService.Exception.ProductNotFoundException;
import com.Ecommerce.CartService.Exception.ServiceUnavailableException;
import com.Ecommerce.CartService.Request.CartRequest;
import com.Ecommerce.CartService.Request.CustomerInfo;
import com.Ecommerce.CartService.Service.Cart_Service;
import com.Ecommerce.CartService.Service.WebclientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/cart")
public class CartController {
    private final Cart_Service cartService;
    private final WebclientService tokenDecodeService;

    public CartController(Cart_Service cartService, WebclientService tokenDecodeService) {
        this.cartService = cartService;
        this.tokenDecodeService = tokenDecodeService;
    }

    //Add To Cart
    @PostMapping("/add-cart")
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartRequest cartRequest,
                                       @RequestHeader("Authorization") String bearerToken,
                                       BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }

            CustomerInfo customerInfo = tokenDecodeService.getUserInfo(bearerToken);
            return ResponseEntity.ok(cartService.addToCart(customerInfo.getConsumerId(), cartRequest, bearerToken));
        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Get Cart Order By ID
    @GetMapping("/carts/{cartId}")
    public ResponseEntity<?> getCartById(@PathVariable Long cartId) {
        try {
            return ResponseEntity.ok(cartService.getUserCartWithDTOs(cartId));
        } catch (CartNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/user-carts")
    public ResponseEntity<?> getCartByCustomer(@RequestHeader("Authorization") String bearerToken) {
        try {
            CustomerInfo customerInfo = tokenDecodeService.getUserInfo(bearerToken);
            return ResponseEntity.ok(cartService.getUserCartWithDTOByCustomerId(customerInfo.getConsumerId()));
        } catch (CartNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
