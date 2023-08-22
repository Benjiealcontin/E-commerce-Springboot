package com.Ecommerce.OrderService.Service;


import com.Ecommerce.OrderService.Entity.Order;
import com.Ecommerce.OrderService.Entity.OrderItem;
import com.Ecommerce.OrderService.Entity.ShippingAddress;
import com.Ecommerce.OrderService.Enum.OrderStatus;
import com.Ecommerce.OrderService.Exception.ProductsNotFoundException;
import com.Ecommerce.OrderService.Repository.CustomerRepository;
import com.Ecommerce.OrderService.Repository.OrderRepository;

import com.Ecommerce.OrderService.Entity.Customer;
import com.Ecommerce.OrderService.Repository.ShippingAddressRepository;
import com.Ecommerce.OrderService.Request.CustomerRequest;
import com.Ecommerce.OrderService.Request.OrderRequest;
import com.Ecommerce.OrderService.Request.ProductRequest;
import com.Ecommerce.OrderService.Response.MessageResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Order_Service {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final WebClient.Builder webClientBuilder;
    public Order_Service(OrderRepository orderRepository, CustomerRepository customerRepository, ShippingAddressRepository shippingAddressRepository, WebClient.Builder webClientBuilder) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.shippingAddressRepository = shippingAddressRepository;
        this.webClientBuilder = webClientBuilder;
    }

    private static final String PRODUCT_SERVICE_URL = "http://Product-Service/api/product";

    //Add Order
    public MessageResponse addOrder(OrderRequest orderRequest, CustomerRequest customerInfo, String bearerToken) {
        try {
            // Extract product IDs from the order request
            List<Long> productIds = orderRequest.getOrderItems().stream()
                    .map(OrderRequest.OrderItemRequest::getProductId)
                    .toList();

            // Convert product IDs to a comma-separated string
            String commaSeparatedIds = productIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            // Fetch product details from the Product Service
            List<ProductRequest> products = webClientBuilder.build().get()
                    .uri(PRODUCT_SERVICE_URL + "/getByIds?productIds={productIds}", commaSeparatedIds)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ProductRequest>>() {})
                    .block();

            if (products != null && !products.isEmpty()) {
                // Create a new order
                Order order = new Order();
                order.setOrderStatus(OrderStatus.PENDING);
                List<OrderItem> orderItems = new ArrayList<>();

                // Process each product and order item
                for (ProductRequest product : products) {
                    int totalQuantity = 0;
                    for (OrderRequest.OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
                        if (itemRequest.getProductId().equals(product.getId())) {
                            totalQuantity += itemRequest.getQuantity();
                        }
                    }

                    if (totalQuantity > 0) {
                        // Create an order item and calculate total price
                        OrderItem orderItem = new OrderItem();
                        orderItem.setQuantity(totalQuantity);
                        orderItem.setTotalPrice(product.getPrice() * totalQuantity); // Calculate total price based on product price and combined quantity
                        orderItem.setOrder(order); // Associate the order item with the order
                        orderItems.add(orderItem);
                    }
                }

                // Associate order items with the order
                order.setOrderItems(orderItems);

                // Save the order
                Order savedOrder = orderRepository.save(order);

                // Create and save customer
                Customer customer = createCustomerFromRequest(customerInfo);
                customer.setOrder(savedOrder);
                customerRepository.save(customer);

                // Create and save shipping address
                ShippingAddress shipping = createShippingAddressFromRequest(customerInfo);
                shipping.setOrder(savedOrder);
                shippingAddressRepository.save(shipping);

                // Save the order to the repository if needed
                orderRepository.save(order);
            }
            return new MessageResponse("Order created successfully");
        }catch (WebClientResponseException.NotFound ex) {
            String responseBody = ex.getResponseBodyAsString();
            throw new ProductsNotFoundException(responseBody);
        }
    }
    private Customer createCustomerFromRequest(CustomerRequest customerRequest) {
        Customer customer = new Customer();
        customer.setFullName(customerRequest.getFullName());
        customer.setFirstName(customerRequest.getFirstName());
        customer.setLastName(customerRequest.getLastName());
        customer.setEmail(customerRequest.getEmail());
        customer.setPhoneNumber(customerRequest.getPhoneNumber());
        customer.setConsumerId(customerRequest.getConsumerId());
        return customer;
    }
    private ShippingAddress createShippingAddressFromRequest(CustomerRequest customerRequest) {
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setCountry(customerRequest.getCountry());
        shippingAddress.setStreetAddress(customerRequest.getStreetAddress());
        shippingAddress.setRegion(customerRequest.getRegion());
        shippingAddress.setPostalCode(customerRequest.getPostalCode());
        shippingAddress.setLocality(customerRequest.getLocality());
        return shippingAddress;
    }
}