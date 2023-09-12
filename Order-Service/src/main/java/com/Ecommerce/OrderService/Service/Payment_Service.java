package com.Ecommerce.OrderService.Service;

import com.Ecommerce.OrderService.Dto.MessageResponse;
import com.Ecommerce.OrderService.Entity.Order;
import com.Ecommerce.OrderService.Entity.OrderPayment.BillingAddress;
import com.Ecommerce.OrderService.Entity.OrderPayment.OrderPayment;
import com.Ecommerce.OrderService.Entity.OrderPayment.PaymentDetail;
import com.Ecommerce.OrderService.Enum.OrderStatus;
import com.Ecommerce.OrderService.Exception.CustomerOwnershipValidationException;
import com.Ecommerce.OrderService.Exception.OrderNotFoundException;
import com.Ecommerce.OrderService.Repository.BillingAddressRepository;
import com.Ecommerce.OrderService.Repository.OrderPaymentRepository;
import com.Ecommerce.OrderService.Repository.OrderRepository;
import com.Ecommerce.OrderService.Repository.PaymentDetailRepository;
import com.Ecommerce.OrderService.Request.OrderPaymentDataRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class Payment_Service {

    private final OrderPaymentRepository orderPaymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final BillingAddressRepository billingAddressRepository;
    private final OrderRepository orderRepository;

    public Payment_Service(OrderPaymentRepository orderPaymentRepository, PaymentDetailRepository paymentDetailRepository, BillingAddressRepository billingAddressRepository, OrderRepository orderRepository) {
        this.orderPaymentRepository = orderPaymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.billingAddressRepository = billingAddressRepository;
        this.orderRepository = orderRepository;
    }

    //Order Payment
    public MessageResponse orderPayment(String customerId, OrderPaymentDataRequest orderPaymentDetails) {
        Order order = findOrderById(orderPaymentDetails.getOrderId());

        validateCustomerOwnership(order, customerId);

        order.setOrderStatus(OrderStatus.PAID);

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

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found"));
    }

    private void validateCustomerOwnership(Order order, String customerId) {
        if (!Objects.equals(customerId, order.getCustomers().getConsumerId())) {
            throw new CustomerOwnershipValidationException("Customer ID didn't match the Order Customer ID");
        }
    }
}
