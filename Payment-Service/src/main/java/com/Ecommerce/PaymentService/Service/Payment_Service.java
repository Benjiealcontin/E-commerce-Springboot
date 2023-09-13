package com.Ecommerce.PaymentService.Service;

import com.Ecommerce.PaymentService.Dto.*;
import com.Ecommerce.PaymentService.Entity.BillingAddress;
import com.Ecommerce.PaymentService.Entity.OrderPayment;
import com.Ecommerce.PaymentService.Entity.PaymentDetail;
import com.Ecommerce.PaymentService.Enum.OrderStatus;
import com.Ecommerce.PaymentService.Exception.CustomerOwnershipValidationException;
import com.Ecommerce.PaymentService.Exception.OrderNotFoundException;
import com.Ecommerce.PaymentService.Repository.BillingAddressRepository;
import com.Ecommerce.PaymentService.Repository.OrderPaymentRepository;
import com.Ecommerce.PaymentService.Repository.PaymentDetailRepository;
import com.Ecommerce.PaymentService.Request.OrderPaymentDataRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Objects;


@Service
public class Payment_Service {

    private final OrderPaymentRepository orderPaymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final BillingAddressRepository billingAddressRepository;
    private final WebClient.Builder webClientBuilder;


    public Payment_Service(OrderPaymentRepository orderPaymentRepository, PaymentDetailRepository paymentDetailRepository, BillingAddressRepository billingAddressRepository, WebClient.Builder webClientBuilder) {
        this.orderPaymentRepository = orderPaymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.billingAddressRepository = billingAddressRepository;

        this.webClientBuilder = webClientBuilder;
    }

    private static final String ORDER_SERVICE_URL = "http://Order-Service/api/order";


    //Order Payment
    public MessageResponse orderPayment(String bearerToken, String customerId, OrderPaymentDataRequest orderPaymentDetails) {
        // Get the order based on bearerToken and orderPaymentDetails
        OrderDTO order = getCustomerId(bearerToken, orderPaymentDetails);

        // Validate if the customer owns the order
        validateCustomerOwnership(order.getCustomer().getConsumerId(), customerId);

        // Update the order status to DELIVERED
        UpdateOrderStatus(bearerToken, orderPaymentDetails);

        // Save the order payment
        OrderPayment orderPayment = saveOrderPayment(orderPaymentDetails);

        // Save the order payment in the repository
        orderPaymentRepository.save(orderPayment);

        // Return a success message
        return new MessageResponse("Payment Successfully.");
    }

    public OrderPayment saveOrderPayment(OrderPaymentDataRequest request) {
        // Create an OrderPayment entity from the request data
        OrderPayment orderPayment = new OrderPayment();
        orderPayment.setOrderId(request.getOrderId());
        orderPayment.setPaymentMethod(request.getPaymentMethod());
        orderPayment.setAmount(request.getAmount());

        // Create a PaymentDetail entity
        PaymentDetail paymentDetail = createPaymentDetail(request);
        paymentDetail.setOrderPayment(orderPayment);

        // Create a BillingAddress entity
        BillingAddress billingAddress = createBillingAddress(request);
        billingAddress.setPaymentDetail(paymentDetail);

        // Save PaymentDetail and BillingAddress entities in the database
        paymentDetailRepository.save(paymentDetail);
        billingAddressRepository.save(billingAddress);

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

    public void UpdateOrderStatus(String bearerToken, OrderPaymentDataRequest orderPaymentDataRequest) {
        // Define the new order status as DELIVERED
        OrderStatus newStatus = OrderStatus.DELIVERED;
        try {
            // Use WebClient to update the order status
            webClientBuilder.build()
                    .put()
                    .uri(ORDER_SERVICE_URL + "/{orderId}/status", orderPaymentDataRequest.getOrderId())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(newStatus))
                    .retrieve()
                    .bodyToMono(String.class);
        } catch (WebClientResponseException.NotFound e) {
            // Handle the case where the order is not found
            String responseBody = e.getResponseBodyAsString();
            throw new OrderNotFoundException(responseBody);
        }
    }

    public OrderDTO getCustomerId(String bearerToken, OrderPaymentDataRequest orderPaymentDataRequest) {
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

    //Get Payment by ID
    public OrderPaymentDTO getOrderPayment(Long paymentId) {
        OrderPayment orderPayment = orderPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new OrderNotFoundException("Payment not found with ID: " + paymentId));

        return convertToDTO(orderPayment);
    }


    public OrderPaymentDTO convertToDTO(OrderPayment orderPayment) {
        OrderPaymentDTO orderPaymentDTO = new OrderPaymentDTO();
        orderPaymentDTO.setOrderId(orderPayment.getId());
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

        paymentDetailDTO.setBillingAddress(billingAddressDTO);
        orderPaymentDTO.setPaymentDetail(paymentDetailDTO);

        return orderPaymentDTO;
    }

}
