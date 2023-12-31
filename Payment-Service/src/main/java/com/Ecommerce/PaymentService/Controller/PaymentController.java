package com.Ecommerce.PaymentService.Controller;


import com.Ecommerce.PaymentService.Exception.*;
import com.Ecommerce.PaymentService.Request.CustomerInfo;
import com.Ecommerce.PaymentService.Request.OrderPaymentDataRequest;
import com.Ecommerce.PaymentService.Request.ShippingMethodRequest;
import com.Ecommerce.PaymentService.Service.Payment_Service;
import com.Ecommerce.PaymentService.Service.WebclientService;
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

    //Calculate the TotalAmount with Shipping fee
    @GetMapping("/calculateTotalCost/{orderId}")
    public ResponseEntity<?> calculateTotalCost(@PathVariable long orderId,
                                                @RequestBody ShippingMethodRequest shippingMethod,
                                                @RequestHeader("Authorization") String bearerToken) {
        try {
            return ResponseEntity.ok(paymentService.getTotalAmountWithShippingFee(orderId, shippingMethod, bearerToken));
        } catch (ShippingMethodNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
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
            return ResponseEntity.ok(paymentService.orderPayment(bearerToken, customerInfo.getConsumerId(), orderPaymentRequest));
        } catch (OrderNotFoundException | ShippingMethodNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CustomerOwnershipValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (AmountMismatchException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }  catch (ServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Get Order Payment by ID
    @GetMapping("/order-payment/{paymentId}")
    public ResponseEntity<?> getOrderPaymentById(@PathVariable Long paymentId) {
        try {
            return ResponseEntity.ok(paymentService.getOrderPayment(paymentId));
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
