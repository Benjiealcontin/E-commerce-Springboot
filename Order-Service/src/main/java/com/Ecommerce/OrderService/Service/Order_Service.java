package com.Ecommerce.OrderService.Service;


import com.Ecommerce.OrderService.Entity.Order;
import com.Ecommerce.OrderService.Entity.OrderItem;
import com.Ecommerce.OrderService.Repository.OrderRepository;

import com.Ecommerce.OrderService.Request.OrderRequest;
import com.Ecommerce.OrderService.Request.Product;
import com.Ecommerce.OrderService.Response.MessageResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Order_Service {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    public Order_Service(OrderRepository orderRepository, WebClient.Builder webClientBuilder) {
        this.orderRepository = orderRepository;
        this.webClientBuilder = webClientBuilder;
    }

    public MessageResponse addOrder(OrderRequest orderRequest, String bearerToken) {
        List<Long> productIds = orderRequest.getOrderItems().stream()
                .map(OrderRequest.OrderItemRequest::getProductId)
                .toList();

        String commaSeparatedIds = productIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        List<Product> products = webClientBuilder.build().get()
                .uri("http://Product-Service/api/product/getByIds?productIds={productIds}", commaSeparatedIds)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Product>>() {})
                .block();

        if (products != null && !products.isEmpty()) {
            Order order = new Order();
            List<OrderItem> orderItems = new ArrayList<>();

            for (Product product : products) {
                int totalQuantity = 0;
                for (OrderRequest.OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
                    if (itemRequest.getProductId().equals(product.getId())) {
                        totalQuantity += itemRequest.getQuantity();
                    }
                }

                if (totalQuantity > 0) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setQuantity(totalQuantity);
                    orderItem.setTotalPrice(product.getPrice() * totalQuantity); // Calculate total price based on product price and combined quantity
                    orderItem.setProductId(product.getId());
                    orderItem.setOrder(order); // Associate the order item with the order
                    orderItems.add(orderItem);

                    System.out.println("Product Name: " + product.getProductName());
                    // Process each product here
                }
            }

            order.setOrderItems(orderItems);

            // Save the order to the repository if needed
            orderRepository.save(order);
        }

        return new MessageResponse("Order created successfully");
    }









    public MessageResponse createOrder(OrderRequest orderRequest,String bearerToken) {
        Order order = new Order();
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderRequest.OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setTotalPrice(0.0); // Calculate total price based on product price and quantity
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setOrder(order); // Associate the order item with the order
            orderItems.add(orderItem);
        }


        order.setOrderItems(orderItems);

//        Order savedOrder = orderRepository.save(order);
        return new MessageResponse("Order created successfully");
    }


}