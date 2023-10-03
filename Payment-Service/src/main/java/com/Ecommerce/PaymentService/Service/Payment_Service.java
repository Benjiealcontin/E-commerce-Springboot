package com.Ecommerce.PaymentService.Service;

import com.Ecommerce.PaymentService.Dto.*;
import com.Ecommerce.PaymentService.Entity.*;
import com.Ecommerce.PaymentService.Exception.*;
import com.Ecommerce.PaymentService.Repository.BillingAddressRepository;
import com.Ecommerce.PaymentService.Repository.OrderPaymentRepository;
import com.Ecommerce.PaymentService.Repository.PaymentDetailRepository;
import com.Ecommerce.PaymentService.Repository.ShippingOptionRepository;
import com.Ecommerce.PaymentService.Request.OrderPaymentDataRequest;
import com.Ecommerce.PaymentService.Request.OrderStatusRequest;
import com.Ecommerce.PaymentService.Request.ProductTotalAmountRequest;
import com.Ecommerce.PaymentService.Request.ShippingMethodRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.text.DecimalFormat;
import java.util.Objects;


@Service
@Slf4j
public class Payment_Service {

    private final OrderPaymentRepository orderPaymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final BillingAddressRepository billingAddressRepository;
    private final ShippingOptionRepository shippingOptionRepository;
    private final WebClient.Builder webClientBuilder;


    public Payment_Service(OrderPaymentRepository orderPaymentRepository, PaymentDetailRepository paymentDetailRepository, BillingAddressRepository billingAddressRepository, ShippingOptionRepository shippingOptionRepository, WebClient.Builder webClientBuilder) {
        this.orderPaymentRepository = orderPaymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.billingAddressRepository = billingAddressRepository;
        this.shippingOptionRepository = shippingOptionRepository;

        this.webClientBuilder = webClientBuilder;
    }

    private static final String ORDER_SERVICE_URL = "http://Order-Service/api/order";
    private static final String SHIPPING_SERVICE_URL = "http://Shipping-Service/api/shipping";


    //Get Total Amount with Shipping Fee
    public MessageResponse getTotalAmountWithShippingFee(Long orderId, ShippingMethodRequest shippingMethod, String bearerToken) {
        try {

            OrderDTO orderDTO = webClientBuilder.build()
                    .get()
                    .uri(ORDER_SERVICE_URL + "/getOrder/{orderId}", orderId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(OrderDTO.class)
                    .block();

            ProductTotalAmountRequest productTotalAmountRequest = new ProductTotalAmountRequest();
            productTotalAmountRequest.setShoppingMethod(shippingMethod.getShoppingMethod());

            assert orderDTO != null;
            productTotalAmountRequest.setTotalAmount(orderDTO.getTotalAmount());

            TotalAmountAlongShippingAndTotalAmountDto totalAmount = webClientBuilder.build()
                    .post()
                    .uri(SHIPPING_SERVICE_URL + "/calculateTotalCost")
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .bodyValue(productTotalAmountRequest)
                    .retrieve()
                    .bodyToMono(TotalAmountAlongShippingAndTotalAmountDto.class)
                    .block();

            // Format the totalPrice to two decimal places
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            assert totalAmount != null;
            double formattedTotalPrice = Double.parseDouble(decimalFormat.format(totalAmount.getTotalAmount()));

            return new MessageResponse("Total amount: " + formattedTotalPrice);

        } catch (WebClientResponseException.NotFound e) {
            throw new ShippingMethodNotFoundException(e.getResponseBodyAsString());
        } catch (WebClientResponseException.ServiceUnavailable e) {
            String responseBody = "Shipping service is currently unavailable. Please try again later.";
            throw new ServiceUnavailableException(responseBody);
        }
    }

    //Order Payment
    @CircuitBreaker(name = "orderPayment", fallbackMethod = "orderPaymentFallback")
    public MessageResponse orderPayment(String bearerToken, String customerId, OrderPaymentDataRequest orderPaymentDetails) {
        // Get the order based on bearerToken and orderPaymentDetails
        OrderDTO order = getOrderDetailsById(bearerToken, orderPaymentDetails);

        // Validate if the customer owns the order
        validateCustomerOwnership(order.getCustomer().getConsumerId(), customerId);

        // Update the order status to DELIVERED
        UpdateOrderStatus(bearerToken, orderPaymentDetails);

        ShippingOptionDTO shippingOptionDTO = getShippingOptionDTO(orderPaymentDetails, bearerToken);

        // Save the order payment
        OrderPayment orderPayment = saveOrderPayment(orderPaymentDetails, shippingOptionDTO);

        double TotalAmountToBePay = order.getTotalAmount() + shippingOptionDTO.getPrice();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        double formattedTotalPrice = Double.parseDouble(decimalFormat.format(TotalAmountToBePay));


        if (Double.compare(formattedTotalPrice, orderPaymentDetails.getAmount()) == 0) {

            // Save the order payment in the repository
            orderPaymentRepository.save(orderPayment);

            // Return a success message
            return new MessageResponse("Payment Successfully.");
        } else {
            throw new AmountMismatchException("Total amount and payment amount do not match.");
        }
    }

    public OrderPayment saveOrderPayment(OrderPaymentDataRequest request, ShippingOptionDTO shippingOptionDTO) {
        // Create an OrderPayment entity from the request data
        OrderPayment orderPayment = new OrderPayment();
        orderPayment.setOrderId(request.getOrderId());
        orderPayment.setPaymentMethod(request.getPaymentMethod());
        orderPayment.setShippingMethod(request.getShippingMethod());
        orderPayment.setAmount(request.getAmount());

        // Create a PaymentDetail entity
        PaymentDetail paymentDetail = createPaymentDetail(request);
        paymentDetail.setOrderPayment(orderPayment);

        // Create a BillingAddress entity
        BillingAddress billingAddress = createBillingAddress(request);
        billingAddress.setPaymentDetail(paymentDetail);

        ShippingOption shippingOption = createShippingOption(shippingOptionDTO);
        shippingOption.setOrderPayment(orderPayment);

        // Save PaymentDetail and BillingAddress entities in the database
        paymentDetailRepository.save(paymentDetail);
        billingAddressRepository.save(billingAddress);
        shippingOptionRepository.save(shippingOption);

        // Save the OrderPayment entity
        return orderPaymentRepository.save(orderPayment);
    }

    private PaymentDetail createPaymentDetail(OrderPaymentDataRequest paymentDetailData) {
        // Create a PaymentDetail entity from paymentDetailData
        PaymentDetail paymentDetail = new PaymentDetail();
        paymentDetail.setCardNumber(paymentDetailData.getPaymentDetail().getCardNumber());
        paymentDetail.setExpirationDate(paymentDetailData.getPaymentDetail().getExpirationDate());
        paymentDetail.setCvv(paymentDetailData.getPaymentDetail().getCvv());
        return paymentDetail;
    }

    private BillingAddress createBillingAddress(OrderPaymentDataRequest billingAddressData) {
        // Create a BillingAddress entity from billingAddressData
        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setStreet(billingAddressData.getPaymentDetail().getBillingAddress().getStreet());
        billingAddress.setCity(billingAddressData.getPaymentDetail().getBillingAddress().getCity());
        billingAddress.setState(billingAddressData.getPaymentDetail().getBillingAddress().getState());
        billingAddress.setPostalCode(billingAddressData.getPaymentDetail().getBillingAddress().getPostalCode());
        billingAddress.setCountry(billingAddressData.getPaymentDetail().getBillingAddress().getCountry());
        return billingAddress;
    }

    private ShippingOption createShippingOption(ShippingOptionDTO shippingOptionDTO) {
        ShippingOption shippingOption = new ShippingOption();
        shippingOption.setShippingName(shippingOptionDTO.getShippingName());
        shippingOption.setDescription(shippingOptionDTO.getDescription());
        shippingOption.setPrice(shippingOptionDTO.getPrice());
        shippingOption.setEstimatedDeliveryTimeInDays(shippingOptionDTO.getEstimatedDeliveryTimeInDays());
        return shippingOption;
    }

    public ShippingOptionDTO getShippingOptionDTO(OrderPaymentDataRequest request, String bearerToken) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(SHIPPING_SERVICE_URL + "/shippingOption/{shippingName}", request.getShippingMethod())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(ShippingOptionDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ShippingMethodNotFoundException(e.getResponseBodyAsString());
        } catch (WebClientResponseException.ServiceUnavailable e) {
            String responseBody = "Shipping service is currently unavailable. Please try again later.";
            throw new ServiceUnavailableException(responseBody);
        }
    }

    public void UpdateOrderStatus(String bearerToken, OrderPaymentDataRequest orderPaymentDataRequest) {
        // Define the new order status as DELIVERED
        try {
            // Use WebClient to update the order status
            webClientBuilder.build()
                    .put()
                    .uri(ORDER_SERVICE_URL + "/status/{orderId}", orderPaymentDataRequest.getOrderId())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(new OrderStatusRequest("TO_SHIP")))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException.ServiceUnavailable e) {
            String responseBody = "Order Service is temporarily unavailable. Please try again later.";
            throw new ServiceUnavailableException(responseBody);
        } catch (WebClientResponseException.NotFound e) {
            throw new OrderNotFoundException(e.getResponseBodyAsString());
        }
    }

    public OrderDTO getOrderDetailsById(String bearerToken, OrderPaymentDataRequest orderPaymentDataRequest) {
        try {
            // Use WebClient to get the order details based on orderId
            return webClientBuilder.build()
                    .get()
                    .uri(ORDER_SERVICE_URL + "/getOrder/{orderId}", orderPaymentDataRequest.getOrderId())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(OrderDTO.class)
                    .block();

        } catch (WebClientResponseException.NotFound e) {
            // Handle the case where the order is not found
            String responseBody = e.getResponseBodyAsString();
            throw new OrderNotFoundException(responseBody);
        }
    }

    private void validateCustomerOwnership(String customerIdFromOrder, String customerId) {
        // Validate if the customer ID matches the order's customer ID
        if (!Objects.equals(customerId, customerIdFromOrder)) {
            throw new CustomerOwnershipValidationException("Customer ID didn't match the Order Customer ID");
        }
    }

    // Fallback method to handle circuit open state
    public MessageResponse orderPaymentFallback(String bearerToken, String customerId, OrderPaymentDataRequest orderPaymentDetails, Throwable t) {
        log.warn("Circuit breaker fallback: Unable to create payment. Error: {}", t.getMessage());
        return new MessageResponse("Payment is temporarily unavailable. Please try again later.");
    }

    //Get Payment by ID
    public OrderPaymentDTO getOrderPayment(Long paymentId) {
        OrderPayment orderPayment = orderPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new OrderNotFoundException("Payment not found with ID: " + paymentId));

        return convertToDTO(orderPayment);
    }

    public OrderPaymentDTO convertToDTO(OrderPayment orderPayment) {
        OrderPaymentDTO orderPaymentDTO = new OrderPaymentDTO();
        orderPaymentDTO.setOrderId(orderPayment.getOrderId());
        orderPaymentDTO.setPaymentMethod(orderPayment.getPaymentMethod());
        orderPaymentDTO.setAmount(orderPayment.getAmount());

        PaymentDetailDTO paymentDetailDTO = new PaymentDetailDTO();
        paymentDetailDTO.setCardNumber(orderPayment.getPaymentDetail().getCardNumber());
        paymentDetailDTO.setExpirationDate(orderPayment.getPaymentDetail().getExpirationDate());
        paymentDetailDTO.setCvv(orderPayment.getPaymentDetail().getCvv());

        BillingAddressDTO billingAddressDTO = new BillingAddressDTO();
        billingAddressDTO.setStreet(orderPayment.getPaymentDetail().getBillingAddress().getStreet());
        billingAddressDTO.setCity(orderPayment.getPaymentDetail().getBillingAddress().getCity());
        billingAddressDTO.setState(orderPayment.getPaymentDetail().getBillingAddress().getState());
        billingAddressDTO.setPostalCode(orderPayment.getPaymentDetail().getBillingAddress().getPostalCode());
        billingAddressDTO.setCountry(orderPayment.getPaymentDetail().getBillingAddress().getCountry());

        ShippingOptionDTO shippingOptionDTO = new ShippingOptionDTO();
        shippingOptionDTO.setShippingName(orderPayment.getShippingOption().getShippingName());
        shippingOptionDTO.setDescription(orderPayment.getShippingOption().getDescription());
        shippingOptionDTO.setPrice(orderPayment.getShippingOption().getPrice());
        shippingOptionDTO.setEstimatedDeliveryTimeInDays(orderPayment.getShippingOption().getEstimatedDeliveryTimeInDays());

        paymentDetailDTO.setBillingAddress(billingAddressDTO);
        orderPaymentDTO.setPaymentDetail(paymentDetailDTO);
        orderPaymentDTO.setShippingOption(shippingOptionDTO);
        return orderPaymentDTO;
    }

}
