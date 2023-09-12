package com.Ecommerce.PaymentService.Service;

import com.Ecommerce.PaymentService.Dto.MessageResponse;
import com.Ecommerce.PaymentService.Dto.OrderDTO;
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
import reactor.core.publisher.Mono;

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
    public MessageResponse orderPayment(String bearerToken,String customerId, OrderPaymentDataRequest orderPaymentDetails) {
        OrderDTO order = getCustomerId(bearerToken, orderPaymentDetails);

        validateCustomerOwnership(order.getCustomer().getConsumerId(), customerId);

        UpdateOrderStatus(bearerToken, orderPaymentDetails);

        OrderPayment orderPayment = saveOrderPayment(orderPaymentDetails);

        orderPaymentRepository.save(orderPayment);

        return new MessageResponse("Payment Successfully.");
    }

    public OrderPayment saveOrderPayment(OrderPaymentDataRequest request) {
        // Create entities from the request data
        OrderPayment orderPayment = new OrderPayment();
        orderPayment.setOrderId(request.getOrderId());
        orderPayment.setPaymentMethod(request.getPaymentMethod());
        orderPayment.setAmount(request.getAmount());

        PaymentDetail paymentDetail = createPaymentDetail(request);
        paymentDetail.setOrderPayment(orderPayment);

        BillingAddress billingAddress = createBillingAddress(request);
        billingAddress.setPaymentDetail(paymentDetail);

        // Save entities in the database
        paymentDetailRepository.save(paymentDetail);
        billingAddressRepository.save(billingAddress);
        return orderPaymentRepository.save(orderPayment);
    }

    private PaymentDetail createPaymentDetail(OrderPaymentDataRequest paymentDetailData) {
        PaymentDetail paymentDetail = new PaymentDetail();
        paymentDetail.setCardNumber(paymentDetailData.getPaymentDetail().getCardNumber());
        paymentDetail.setExpirationDate(paymentDetailData.getPaymentDetail().getExpirationDate());
        paymentDetail.setCvv(paymentDetailData.getPaymentDetail().getCvv());
        return paymentDetail;
    }

    private BillingAddress createBillingAddress(OrderPaymentDataRequest billingAddressData) {
        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setStreet(billingAddressData.getPaymentDetail().getBillingAddress().getStreet());
        billingAddress.setCity(billingAddressData.getPaymentDetail().getBillingAddress().getCity());
        billingAddress.setState(billingAddressData.getPaymentDetail().getBillingAddress().getState());
        billingAddress.setPostalCode(billingAddressData.getPaymentDetail().getBillingAddress().getPostalCode());
        billingAddress.setCountry(billingAddressData.getPaymentDetail().getBillingAddress().getCountry());
        return billingAddress;
    }

    public void UpdateOrderStatus(String bearerToken, OrderPaymentDataRequest orderPaymentDataRequest) {
        OrderStatus newStatus = OrderStatus.DELIVERED;
        try{
            webClientBuilder.build()
                    .put()
                    .uri(ORDER_SERVICE_URL + "/{orderId}/status", orderPaymentDataRequest.getOrderId())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(newStatus))
                    .retrieve()
                    .bodyToMono(String.class);
        }catch (WebClientResponseException.NotFound e) {
            String responseBody = e.getResponseBodyAsString();
            throw new OrderNotFoundException(responseBody);
        }
    }

    public OrderDTO getCustomerId(String bearerToken, OrderPaymentDataRequest orderPaymentDataRequest){
        try{
            return webClientBuilder.build()
                    .get()
                    .uri(ORDER_SERVICE_URL + "/getOrder/{orderId}" , orderPaymentDataRequest.getOrderId())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(OrderDTO.class)
                    .block();

        }catch (WebClientResponseException.NotFound e) {
            String responseBody = e.getResponseBodyAsString();
            throw new OrderNotFoundException(responseBody);
        }
    }

    private void validateCustomerOwnership(String customerIdFromOrder, String customerId) {
        if (!Objects.equals(customerId, customerIdFromOrder)) {
            throw new CustomerOwnershipValidationException("Customer ID didn't match the Order Customer ID");
        }
    }
}
