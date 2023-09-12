package com.Ecommerce.OrderService.Controller;

import com.Ecommerce.OrderService.Exception.CustomerOwnershipValidationException;
import com.Ecommerce.OrderService.Exception.OrderNotFoundException;
import com.Ecommerce.OrderService.Request.CustomerInfo;
import com.Ecommerce.OrderService.Request.OrderPaymentDataRequest;
import com.Ecommerce.OrderService.Service.Payment_Service;
import com.Ecommerce.OrderService.Service.WebclientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/payment")
public class PaymentController {

    private final Payment_Service paymentService;
    private final WebclientService tokenDecodeService;

    public PaymentController(Payment_Service paymentService, WebclientService tokenDecodeService) {
        this.paymentService = paymentService;
        this.tokenDecodeService = tokenDecodeService;
    }

    //Order Payment
    @PostMapping("/order-payment")
    public ResponseEntity<?> orderPayment(@RequestBody @Valid OrderPaymentDataRequest orderPaymentRequest,
                                          @RequestHeader("Authorization") String bearerToken,
                                          BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            CustomerInfo customerInfo = tokenDecodeService.getUserInfo(bearerToken);
            return ResponseEntity.ok(paymentService.orderPayment(customerInfo.getConsumerId(), orderPaymentRequest));
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CustomerOwnershipValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
