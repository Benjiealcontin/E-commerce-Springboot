package com.Ecommerce.OrderService.Service;


import com.Ecommerce.OrderService.Dto.*;
import com.Ecommerce.OrderService.Entity.*;
import com.Ecommerce.OrderService.Enum.OrderStatus;
import com.Ecommerce.OrderService.Exception.DeliveredOrdersNotFoundException;
import com.Ecommerce.OrderService.Exception.InsufficientProductQuantityException;
import com.Ecommerce.OrderService.Exception.OrderNotFoundException;
import com.Ecommerce.OrderService.Exception.ProductsNotFoundException;
import com.Ecommerce.OrderService.Repository.*;

import com.Ecommerce.OrderService.Request.CustomerInfo;
import com.Ecommerce.OrderService.Request.OrderRequest;
import com.Ecommerce.OrderService.Request.ProductRequest;
import com.Ecommerce.ProductService.Request.StockQuantityRequest;
import org.modelmapper.ModelMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
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
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final WebClient.Builder webClientBuilder;
    private final ModelMapper modelMapper;

    public Order_Service(OrderRepository orderRepository, CustomerRepository customerRepository, ShippingAddressRepository shippingAddressRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository, WebClient.Builder webClientBuilder, ModelMapper modelMapper) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.shippingAddressRepository = shippingAddressRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.webClientBuilder = webClientBuilder;
        this.modelMapper = modelMapper;
    }

    private static final String PRODUCT_SERVICE_URL = "http://Product-Service/api/product";
    double totalAmount = 0.0;

    //Add Order
    public MessageResponse addOrder(OrderRequest orderRequest, CustomerInfo customerInfo, String bearerToken) {
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

                // Process each product and order item
                for (ProductRequest product : products) {
                    int totalQuantity = 0;

                    for (OrderRequest.OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
                        if (itemRequest.getProductId().equals(product.getId())) {
                            totalQuantity += itemRequest.getQuantity();
                        }
                    }

                    if (totalQuantity > 0) {
                        if (totalQuantity > product.getStockQuantity()) {
                            throw new InsufficientProductQuantityException("Insufficient quantity for product: " + product.getProductName());
                        }

                        // Create a new order
                        Order order = new Order();
                        order.setOrderStatus(OrderStatus.PENDING);
                        List<OrderItem> orderItems = new ArrayList<>();
                        Order savedOrder = orderRepository.save(order);

                        // Calculate total price based on product price and combined quantity
                        double itemTotalPrice = product.getPrice() * totalQuantity;

                        // Create an order item and set its attributes
                        OrderItem orderItem = new OrderItem();
                        orderItem.setQuantity(totalQuantity);
                        orderItem.setTotalPrice(itemTotalPrice); // Use the calculated item total price

                        // Save the order item to obtain its ID
                        OrderItem savedOrderItem = orderItemRepository.save(orderItem);

                        // Associate the order item with the saved order
                        savedOrderItem.setOrder(savedOrder);

                        // Save the order item again with the updated association
                        orderItemRepository.save(savedOrderItem);

                        // Create a product and associate it with the order item
                        Product productEntity = createProductEntityFromRequest(product);
                        productEntity.setOrderItem(savedOrderItem); // Associate with the saved order item
                        productRepository.save(productEntity);

                        // Call the function to update the product's stock quantity
                        updateProductStockQuantity(product.getId(), totalQuantity, bearerToken);

                        // Accumulate the item's total price to the totalAmount
                        totalAmount += itemTotalPrice;

                        order.setTotalAmount(totalAmount);

                        // Associate order items with the order
                        order.setOrderItems(orderItems);

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
                }
            }
            return new MessageResponse("Order created successfully");
        }catch (WebClientResponseException.NotFound ex) {
            String responseBody = ex.getResponseBodyAsString();
            throw new ProductsNotFoundException(responseBody);
        }
    }

    private Customer createCustomerFromRequest(CustomerInfo customerRequest) {
        Customer customer = new Customer();
        customer.setFullName(customerRequest.getFullName());
        customer.setFirstName(customerRequest.getFirstName());
        customer.setLastName(customerRequest.getLastName());
        customer.setEmail(customerRequest.getEmail());
        customer.setPhoneNumber(customerRequest.getPhoneNumber());
        customer.setConsumerId(customerRequest.getConsumerId());
        return customer;
    }
    private ShippingAddress createShippingAddressFromRequest(CustomerInfo customerRequest) {
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setCountry(customerRequest.getCountry());
        shippingAddress.setStreetAddress(customerRequest.getStreetAddress());
        shippingAddress.setRegion(customerRequest.getRegion());
        shippingAddress.setPostalCode(customerRequest.getPostalCode());
        shippingAddress.setLocality(customerRequest.getLocality());
        return shippingAddress;
    }
    private Product createProductEntityFromRequest(ProductRequest productRequest){
        Product product = new Product();
        product.setProductId(productRequest.getId());
        product.setProductName(productRequest.getProductName());
        product.setPrice(productRequest.getPrice());
        return product;
    }

    private void updateProductStockQuantity(Long productId, int quantity, String bearerToken) {
        // Make a PUT request to update stock quantity in the Product Service
        webClientBuilder.build().put()
                .uri(PRODUCT_SERVICE_URL + "/update/quantity/{id}", productId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(BodyInserters.fromValue(new StockQuantityRequest(quantity)))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    //Find Order by ID
    public OrderDTO getOrderById(Long orderId) {
        // Find the order by ID or throw an exception if not found
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        // Map the Order entity to an OrderDTO
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);

        // Map OrderItems
        List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(orderItem -> {
                    // Map each OrderItem to an OrderItemDTO
                    OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);

                    // Map associated Product if present
                    Product product = orderItem.getProduct();
                    if (product != null) {
                        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
                        orderItemDTO.setProduct(productDTO);
                    }
                    return orderItemDTO;
                })
                .collect(Collectors.toList());

        // Set the mapped OrderItemDTOs to the OrderDTO
        orderDTO.setOrderItems(orderItemDTOs);

        // Map associated Customer if present
        Customer customer = order.getCustomers();
        if (customer != null) {
            CustomerDTO customerDTO = modelMapper.map(customer, CustomerDTO.class);
            orderDTO.setCustomer(customerDTO);
        }

        // Map associated ShippingAddress if present
        ShippingAddress shippingAddress = order.getShippingAddresses();
        if (shippingAddress != null) {
            ShippingAddressDTO shippingAddressDTO = modelMapper.map(shippingAddress, ShippingAddressDTO.class);
            orderDTO.setShippingAddress(shippingAddressDTO);
        }

        // Return the fully mapped OrderDTO
        return orderDTO;
    }

    //Find Customer Order by ConsumerId
    public List<OrderDTO> getOrdersByConsumerId(String consumerId) {
        List<Order> orders = orderRepository.findByCustomers_ConsumerId(consumerId);
        if (orders.isEmpty()) {
            throw new OrderNotFoundException("No orders found with consumerId: " + consumerId);
        }
        return orders.stream()
                .map(this::mapOrderToOrderDTO)
                .collect(Collectors.toList());
    }

    //Find All Orders by OrderStatus
    public List<OrderDTO> getOrdersByOrderStatus(OrderStatus orderStatus) {
        List<Order> orders = orderRepository.findByOrderStatus(orderStatus);

        if (orders.isEmpty()) {
            throw new OrderNotFoundException("No orders found with status: " + orderStatus);
        }
        return orders.stream()
                .map(this::mapOrderToOrderDTO)
                .collect(Collectors.toList());
    }

    //Find All Order
    public List<OrderDTO> getAllOrders() {
        try {
            // Retrieve all orders from the database
            List<Order> orders = orderRepository.findAll();
            List<OrderDTO> orderDTOs = new ArrayList<>();

            // Iterate through each order and map it to an OrderDTO
            for (Order order : orders) {
                OrderDTO orderDTO = mapOrderToOrderDTO(order);
                orderDTOs.add(orderDTO);
            }

            // Return the list of OrderDTOs
            return orderDTOs;
        } catch (Exception e) {
            // If an exception occurs during the process
            e.printStackTrace();
            throw new OrderNotFoundException("Error while retrieving orders");
        }
    }

    //Find All History Order of customer
    public List<OrderDTO> getDeliveredOrdersByConsumerId(String consumerId) {
        List<Order> deliveredOrders = orderRepository.findByCustomers_ConsumerIdAndOrderStatus(consumerId, OrderStatus.DELIVERED);

        if (deliveredOrders.isEmpty()) {
            throw new DeliveredOrdersNotFoundException("No delivered orders found for consumer: " + consumerId);
        }

        return deliveredOrders.stream()
                .map(this::mapOrderToOrderDTO)
                .collect(Collectors.toList());
    }

    // Helper method to map an Order to an OrderDTO
    private OrderDTO mapOrderToOrderDTO(Order order) {
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);

        // Map OrderItem data
        List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(orderItem -> {
                    OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);

                    // Map associated Product data to ProductDTO
                    Product product = orderItem.getProduct();
                    if (product != null) {
                        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
                        orderItemDTO.setProduct(productDTO);
                    }

                    return orderItemDTO;
                })
                .collect(Collectors.toList());

        orderDTO.setOrderItems(orderItemDTOs);

        // Map associated Customer data to CustomerDTO
        Customer customer = order.getCustomers();
        if (customer != null) {
            CustomerDTO customerDTO = modelMapper.map(customer, CustomerDTO.class);
            orderDTO.setCustomer(customerDTO);
        }

        // Map associated ShippingAddress data to ShippingAddressDTO
        ShippingAddress shippingAddress = order.getShippingAddresses();
        if (shippingAddress != null) {
            ShippingAddressDTO shippingAddressDTO = modelMapper.map(shippingAddress, ShippingAddressDTO.class);
            orderDTO.setShippingAddress(shippingAddressDTO);
        }

        return orderDTO;
    }

    //Delete Order
    public void deleteOrder(Long orderId){
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException("Order with ID " + orderId + " not found.");
        }
        orderRepository.deleteById(orderId);
    }

    //Update Order Details
    public void updateOrderDetails(Long orderId, OrderDTO updatedOrderDTO) {
        Order existingOrder = orderRepository.findById(orderId).orElse(null);

        if (existingOrder != null) {
            // Update fields from the updatedOrderDTO to the existingOrder
            existingOrder.setOrderDate(updatedOrderDTO.getOrderDate());
            existingOrder.setOrderStatus(updatedOrderDTO.getOrderStatus());
            existingOrder.setTotalAmount(updatedOrderDTO.getTotalAmount());

            // Update Customer details if provided in the updatedOrderDTO
            if (updatedOrderDTO.getCustomer() != null) {
                CustomerDTO updatedCustomerDTO = updatedOrderDTO.getCustomer();
                Customer existingCustomer = existingOrder.getCustomers();

                if (existingCustomer != null) {
                    // Update customer fields from the updatedCustomerDTO
                    existingCustomer.setFullName(updatedCustomerDTO.getFullName());
                    existingCustomer.setFirstName(updatedCustomerDTO.getFirstName());
                    existingCustomer.setLastName(updatedCustomerDTO.getLastName());
                    existingCustomer.setEmail(updatedCustomerDTO.getEmail());
                    existingCustomer.setPhoneNumber(updatedCustomerDTO.getPhoneNumber());
                }
            }

            // Update Order Items details if provided in the updatedOrderDTO
            if (updatedOrderDTO.getOrderItems() != null) {
                List<OrderItemDTO> updatedOrderItemDTOs = updatedOrderDTO.getOrderItems();

                for (int i = 0; i < updatedOrderItemDTOs.size(); i++) {
                    OrderItemDTO updatedOrderItemDTO = updatedOrderItemDTOs.get(i);
                    OrderItem existingOrderItem = existingOrder.getOrderItems().get(i);

                    if (existingOrderItem != null) {
                        // Update order item fields from the updatedOrderItemDTO
                        existingOrderItem.setQuantity(updatedOrderItemDTO.getQuantity());
                        existingOrderItem.setTotalPrice(updatedOrderItemDTO.getTotalPrice());

                        // Update Product details if provided in the updatedOrderItemDTO
                        if (updatedOrderItemDTO.getProduct() != null) {
                            ProductDTO updatedProductDTO = updatedOrderItemDTO.getProduct();
                            Product existingProduct = existingOrderItem.getProduct();

                            if (existingProduct != null) {
                                // Update product fields from the updatedProductDTO
                                existingProduct.setProductName(updatedProductDTO.getProductName());
                                existingProduct.setPrice(updatedProductDTO.getPrice());
                            }
                        }
                    }
                }
            }

            // Save the updated order and return the updated OrderDTO
            Order updatedOrder = orderRepository.save(existingOrder);
            modelMapper.map(updatedOrder, OrderDTO.class);
        } else {
            throw new OrderNotFoundException("Order with ID " + orderId + " not found");
        }

    }




}