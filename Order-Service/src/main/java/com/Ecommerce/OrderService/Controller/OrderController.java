package com.Ecommerce.OrderService.Controller;

import com.Ecommerce.OrderService.Exception.InsufficientProductQuantityException;
import com.Ecommerce.OrderService.Exception.OrderNotFoundException;
import com.Ecommerce.OrderService.Exception.ProductsNotFoundException;
import com.Ecommerce.OrderService.Request.CustomerInfo;
import com.Ecommerce.OrderService.Request.OrderRequest;
import com.Ecommerce.OrderService.Service.Order_Service;
import com.Ecommerce.OrderService.Service.WebclientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/order")
public class OrderController {
    private final Order_Service orderService;
    private final WebclientService tokenDecodeService;

    public OrderController(Order_Service orderService, WebclientService tokenDecodeService) {
        this.orderService = orderService;
        this.tokenDecodeService = tokenDecodeService;
    }

    //Create Order
    @PostMapping("/add-order")
    public ResponseEntity<?> addOrder(@RequestBody OrderRequest orderRequest, @RequestHeader("Authorization") String bearerToken) {
        try{
            CustomerInfo customerInfo = tokenDecodeService.getUserInfo(bearerToken);
            return ResponseEntity.ok(orderService.addOrder(orderRequest, customerInfo, bearerToken));
        }catch(InsufficientProductQuantityException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }catch(ProductsNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find Order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
       try{
           return ResponseEntity.ok(orderService.getOrderById(orderId));
       }catch (OrderNotFoundException e){
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
       }catch(Exception e){
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
       }

    }
}

