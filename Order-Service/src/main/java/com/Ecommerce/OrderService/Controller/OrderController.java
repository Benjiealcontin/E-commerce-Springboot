package com.Ecommerce.OrderService.Controller;

import com.Ecommerce.OrderService.Dto.MessageResponse;
import com.Ecommerce.OrderService.Dto.OrderDTO;
import com.Ecommerce.OrderService.Enum.OrderStatus;
import com.Ecommerce.OrderService.Exception.*;
import com.Ecommerce.OrderService.Request.CancelRequest;
import com.Ecommerce.OrderService.Request.CustomerInfo;
import com.Ecommerce.OrderService.Request.OrderRequest;
import com.Ecommerce.OrderService.Service.Order_Service;
import com.Ecommerce.OrderService.Service.WebclientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
    public ResponseEntity<?> addOrder(@RequestBody @Valid OrderRequest orderRequest,
                                      @RequestHeader("Authorization") String bearerToken,
                                      BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }

            CustomerInfo customerInfo = tokenDecodeService.getUserInfo(bearerToken);
            return ResponseEntity.ok(orderService.addOrder(orderRequest, customerInfo, bearerToken));
        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        } catch (InsufficientProductQuantityException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (ProductsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Cancel Order
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId,
                                         @RequestBody CancelRequest cancelReason,
                                         @RequestHeader("Authorization") String bearerToken,
                                         BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }

            CustomerInfo customerInfo = tokenDecodeService.getUserInfo(bearerToken);
            orderService.cancelOrder(orderId, customerInfo.getConsumerId(), cancelReason);
            return ResponseEntity.ok("Order Cancel Successfully.");
        }catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }  catch (CustomBadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find Order by ID
    @GetMapping("/getOrder/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(orderService.getOrderById(orderId));
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Get Order of Consumer
    @GetMapping("/customerOrder")
    public ResponseEntity<?> getOrderByConsumerId(@RequestHeader("Authorization") String bearerToken) {
        try {
            CustomerInfo customerInfo = tokenDecodeService.getUserInfo(bearerToken);
            return ResponseEntity.ok(orderService.getOrdersByConsumerId(customerInfo.getConsumerId()));
        } catch (WebClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find Orders by Order Status
    @GetMapping("/status/{orderStatus}")
    public ResponseEntity<?> getOrderByConsumerId(@PathVariable OrderStatus orderStatus) {
        try {
            return ResponseEntity.ok(orderService.getOrdersByOrderStatus(orderStatus));
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Delivered history of customer
    @GetMapping("/customer/history")
    public ResponseEntity<?> getDeliveredOrdersByConsumerId(@RequestHeader("Authorization") String bearerToken) {
        try {
            CustomerInfo customerInfo = tokenDecodeService.getUserInfo(bearerToken);
            return ResponseEntity.ok(orderService.getDeliveredOrdersByConsumerId(customerInfo.getConsumerId()));
        } catch (DeliveredOrdersNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Orders
    @GetMapping("/getAllOrders")
    public ResponseEntity<?> getAllOrders() {
        try {
            return ResponseEntity.ok(orderService.getAllOrders());
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Delete Order
    @DeleteMapping("/deleteOrder/{orderId}")
    public ResponseEntity<?> deleteOrders(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.ok(new MessageResponse("Order Delete Successfully!"));
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Update Order Details
    @PutMapping("/updateOrder/{orderId}")
    public ResponseEntity<?> updateOrdersDetails(@PathVariable Long orderId,
                                                 @RequestBody OrderDTO updatedOrderDTO) {
        try {
            orderService.updateOrderDetails(orderId, updatedOrderDTO);
            return ResponseEntity.ok(new MessageResponse("Order Updated Successfully!"));
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Update Order Status
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus newStatus) {
        try {
            orderService.updateOrderStatus(orderId, newStatus);
            return ResponseEntity.ok("Order Status Successfully Updated.");
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}

