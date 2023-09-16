package com.Ecommerce.CartService.Controller;

import com.Ecommerce.CartService.Dto.TokenDTO;
import com.Ecommerce.CartService.Exception.CartItemNotFoundException;
import com.Ecommerce.CartService.Exception.CartNotFoundException;
import com.Ecommerce.CartService.Exception.ProductNotFoundException;
import com.Ecommerce.CartService.Exception.ServiceUnavailableException;
import com.Ecommerce.CartService.Request.CartRequest;
import com.Ecommerce.CartService.Service.Cart_Service;
import com.Ecommerce.CartService.Service.TokenDecodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/cart")
public class CartController {
    private final Cart_Service cartService;
    private final TokenDecodeService tokenDecodeService;

    public CartController(Cart_Service cartService, TokenDecodeService tokenDecodeService) {
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

            String token = tokenDecodeService.extractToken(bearerToken);
            TokenDTO customerId = tokenDecodeService.decodeUserToken(token);

            return ResponseEntity.ok(cartService.addToCart(customerId, cartRequest, bearerToken));
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

    //Get Cart of User by Customer ID
    @GetMapping("/user-carts")
    public ResponseEntity<?> getCartByCustomer(@RequestHeader("Authorization") String bearerToken) {
        try {
            String token = tokenDecodeService.extractToken(bearerToken);
            TokenDTO customerId = tokenDecodeService.decodeUserToken(token);
            return ResponseEntity.ok(cartService.getUserCartWithDTOByCustomerId(customerId));
        } catch (CartNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Remove cart Item
    @DeleteMapping("/remove-item/{cartId}")
    public ResponseEntity<?> RemoveCart(@PathVariable Long cartId, @RequestHeader("Authorization") String bearerToken) {
        try {
            cartService.deleteCartItemWithProduct(cartId);
            return ResponseEntity.ok("Remove Successfully.");
        } catch (CartItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
