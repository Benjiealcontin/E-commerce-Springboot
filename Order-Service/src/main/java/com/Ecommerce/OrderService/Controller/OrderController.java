package com.Ecommerce.OrderService.Controller;

import com.Ecommerce.OrderService.Exception.OrderCreationException;
import com.Ecommerce.OrderService.Exception.ProductsNotFoundException;
import com.Ecommerce.OrderService.Request.Customer;
import com.Ecommerce.OrderService.Request.OrderRequest;
import com.Ecommerce.OrderService.Service.Order_Service;
import com.Ecommerce.OrderService.Service.TokenDecodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/order")
public class OrderController {
    private final Order_Service orderService;
    private final TokenDecodeService tokenDecodeService;

    public OrderController(Order_Service orderService, TokenDecodeService tokenDecodeService) {
        this.orderService = orderService;
        this.tokenDecodeService = tokenDecodeService;
    }

    @GetMapping("/add-order")
    public ResponseEntity<?> addOrder(@RequestBody OrderRequest orderRequest, @RequestHeader("Authorization") String bearerToken) {
        try{
            Customer customer = tokenDecodeService.getUserInfo(bearerToken);
            return ResponseEntity.ok(orderService.addOrder(orderRequest, bearerToken, customer));
        }catch(ProductsNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

}

