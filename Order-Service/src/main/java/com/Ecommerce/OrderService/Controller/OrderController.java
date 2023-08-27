package com.Ecommerce.OrderService.Controller;

import com.Ecommerce.OrderService.Dto.OrderDTO;
import com.Ecommerce.OrderService.Enum.OrderStatus;
import com.Ecommerce.OrderService.Exception.DeliveredOrdersNotFoundException;
import com.Ecommerce.OrderService.Exception.InsufficientProductQuantityException;
import com.Ecommerce.OrderService.Exception.OrderNotFoundException;
import com.Ecommerce.OrderService.Exception.ProductsNotFoundException;
import com.Ecommerce.OrderService.Request.CustomerInfo;
import com.Ecommerce.OrderService.Request.OrderRequest;
import com.Ecommerce.OrderService.Request.OrderStatusRequest;
import com.Ecommerce.OrderService.Service.Order_Service;
import com.Ecommerce.OrderService.Service.WebclientService;
import com.Ecommerce.ProductService.Response.MessageResponse;
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

    //Find Order of Consumer
    @GetMapping("/customerOrder")
    public ResponseEntity<?> getOrderByConsumerId(@RequestHeader("Authorization") String bearerToken) {
        try{
            CustomerInfo customerInfo = tokenDecodeService.getUserInfo(bearerToken);
            return ResponseEntity.ok(orderService.getOrdersByConsumerId(customerInfo.getConsumerId()));
        }catch (OrderNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find Orders by Order Status
    @GetMapping("/status/{orderStatus}")
    public ResponseEntity<?> getOrderByConsumerId(@PathVariable OrderStatus orderStatus) {
        try{
            return ResponseEntity.ok(orderService.getOrdersByOrderStatus(orderStatus));
        }catch (OrderNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Delivered history of customer
    @GetMapping("/customer/history")
    public ResponseEntity<?> getDeliveredOrdersByConsumerId(@RequestHeader("Authorization") String bearerToken) {
        try{
            CustomerInfo customerInfo = tokenDecodeService.getUserInfo(bearerToken);
            return ResponseEntity.ok(orderService.getDeliveredOrdersByConsumerId(customerInfo.getConsumerId()));
        }catch (DeliveredOrdersNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Orders
    @GetMapping("/getAllOrders")
    public ResponseEntity<?> getAllOrders() {
        try{
            return ResponseEntity.ok(orderService.getAllOrders());
        }catch (OrderNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Delete Order
    @DeleteMapping("/deleteOrder/{orderId}")
    public ResponseEntity<?> deleteOrders(@PathVariable Long orderId) {
        try{
            orderService.deleteOrder(orderId);
            return ResponseEntity.ok(new MessageResponse("Order Delete Successfully!"));
        }catch (OrderNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Update Order Details
    @PutMapping("/updateOrder/{orderId}")
    public ResponseEntity<?> updateOrdersDetails(@PathVariable Long orderId,@RequestBody OrderDTO updatedOrderDTO) {
        try{
            orderService.updateOrderDetails(orderId, updatedOrderDTO);
            return ResponseEntity.ok(new MessageResponse("Order Updated Successfully!"));
        }catch (OrderNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}

