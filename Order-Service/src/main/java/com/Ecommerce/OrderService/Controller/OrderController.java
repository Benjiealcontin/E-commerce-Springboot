package com.Ecommerce.OrderService.Controller;

import com.Ecommerce.OrderService.Request.OrderRequest;
import com.Ecommerce.OrderService.Response.MessageResponse;
import com.Ecommerce.OrderService.Service.Order_Service;
import com.Ecommerce.OrderService.Service.TokenDecodeService;
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
    public MessageResponse addOrder(@RequestBody OrderRequest orderRequest, @RequestHeader("Authorization") String bearerToken) {
        return orderService.addOrder(orderRequest, bearerToken);
    }

}

