package com.Ecommerce.OrderService.Service;


import com.Ecommerce.OrderService.Dto.*;
import com.Ecommerce.OrderService.Entity.*;
import com.Ecommerce.OrderService.Exception.*;
import com.Ecommerce.OrderService.Repository.*;
import com.Ecommerce.OrderService.Request.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private static final String INVENTORY_SERVICE_URL = "http://Inventory-Service/api/inventory";

    //Add Order

    public MessageResponse addOrder(OrderRequest orderRequest, CustomerInfo customerInfo, String bearerToken) {
        try {
            // Extract product IDs from the order request
            List<Long> productIds = extractProductIds(orderRequest);

            // Fetch product details based on extracted product IDs
            List<ProductRequest> products = fetchProducts(productIds, bearerToken);

            // Proceed with order processing if products are available
            if (!products.isEmpty()) {
                processOrderItems(orderRequest, products, customerInfo, bearerToken);
            }

            return new MessageResponse("Order created successfully");
        } catch (WebClientResponseException.NotFound ex) {
            // If products are not found, throw a custom exception with the response body
            String responseBody = ex.getResponseBodyAsString();
            throw new ProductsNotFoundException(responseBody);
        } catch (WebClientResponseException.ServiceUnavailable ex) {
            String responseBody = "Product service is currently unavailable. Please try again later.";
            throw new ServiceUnavailableException(responseBody);
        }
    }

    //Get All the Product Ids from the Order Request
    private List<Long> extractProductIds(OrderRequest orderRequest) {
        return orderRequest.getOrderItems().stream()
                .map(OrderRequest.OrderItemRequest::getProductId)
                .collect(Collectors.toList());
    }

    private List<ProductRequest> fetchProducts(List<Long> productIds, String bearerToken) {
        // Create a comma-separated string of product IDs
        String commaSeparatedIds = productIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // Use WebClient to send a GET request to the product service
        return webClientBuilder.build().get()
                .uri(PRODUCT_SERVICE_URL + "/getByIds?productIds={productIds}", commaSeparatedIds)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                // Convert the response body to a list of ProductRequest objects
                .bodyToMono(new ParameterizedTypeReference<List<ProductRequest>>() {
                })
                // Block and wait for the response
                .block();
    }

    private void processOrderItems(OrderRequest orderRequest, List<ProductRequest> products, CustomerInfo customerInfo, String bearerToken) {
        double totalAmount = 0.0;

        // Create and save a new order
        Order savedOrder = new Order();
        savedOrder.setOrderStatus("PENDING");

        // Iterate through each product to process order items
        // Count how many quantity customer Order
        for (ProductRequest product : products) {
            int totalQuantity = calculateTotalQuantity(orderRequest, product);

            if (totalQuantity > 0 && totalQuantity <= product.getStockQuantity()) {
                double itemTotalPrice = product.getPrice() * totalQuantity;

                updateProductStockQuantity(product.getId(), totalQuantity, bearerToken);

                // Attempt to save the order first
                savedOrder = orderRepository.save(savedOrder);

                OrderItem savedOrderItem = createAndSaveOrderItem(savedOrder, totalQuantity, itemTotalPrice);
                associateOrderItemWithProduct(savedOrderItem, product);

                totalAmount += itemTotalPrice;
            } else {
                throw new InsufficientProductQuantityException("Insufficient quantity for product: " + product.getProductName());
            }
        }

        savedOrder.setTotalAmount(totalAmount);
        orderRepository.save(savedOrder); // Update the order with totalAmount
        createAndSaveCustomerAndShipping(savedOrder, customerInfo);
    }

    // Calculates the total quantity of a given product in the order
    private int calculateTotalQuantity(OrderRequest orderRequest, ProductRequest product) {
        return orderRequest.getOrderItems().stream()
                .filter(item -> item.getProductId().equals(product.getId()))
                .mapToInt(OrderRequest.OrderItemRequest::getQuantity)
                .sum();
    }

    // Creates and saves an order item associated with an order
    private OrderItem createAndSaveOrderItem(Order order, int totalQuantity, double itemTotalPrice) {
        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(totalQuantity);
        orderItem.setTotalPrice(itemTotalPrice);
        orderItem.setOrder(order);
        return orderItemRepository.save(orderItem);
    }

    // Associates an order item with a product entity and saves it
    private void associateOrderItemWithProduct(OrderItem orderItem, ProductRequest product) {
        Product productEntity = createProductEntityFromRequest(product);
        productEntity.setOrderItem(orderItem);
        productRepository.save(productEntity);
    }

    // Creates a product entity based on the product request
    private Product createProductEntityFromRequest(ProductRequest productRequest) {
        Product product = new Product();
        product.setProductId(productRequest.getId());
        product.setProductName(productRequest.getProductName());
        product.setPrice(productRequest.getPrice());
        return product;
    }

    // Updates the stock quantity of a product using WebClient PUT request
    private void updateProductStockQuantity(Long productId, int quantity, String bearerToken) {
        try {
            webClientBuilder.build().put()
                    .uri(INVENTORY_SERVICE_URL + "/decrement/{id}", productId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .body(BodyInserters.fromValue(new StockQuantityRequest(quantity)))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException.ServiceUnavailable e) {
            String responseBody = "Inventory service is currently unavailable. Please try again later.";
            throw new ServiceUnavailableException(responseBody);
        }
    }

    // Creates and saves customer and shipping information associated with an order
    private void createAndSaveCustomerAndShipping(Order order, CustomerInfo customerInfo) {
        Customer customer = createCustomerFromRequest(customerInfo);
        customer.setOrder(order);
        customerRepository.save(customer);

        ShippingAddress shipping = createShippingAddressFromRequest(customerInfo);
        shipping.setOrder(order);
        shippingAddressRepository.save(shipping);
    }

    // Creates a customer entity based on customer information
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

    // Creates a shipping address entity based on customer information
    private ShippingAddress createShippingAddressFromRequest(CustomerInfo customerRequest) {
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setCountry(customerRequest.getCountry());
        shippingAddress.setStreetAddress(customerRequest.getStreetAddress());
        shippingAddress.setRegion(customerRequest.getRegion());
        shippingAddress.setPostalCode(customerRequest.getPostalCode());
        shippingAddress.setLocality(customerRequest.getLocality());
        return shippingAddress;
    }

    // Fallback method to handle circuit open state
    public MessageResponse addOrderFallback(OrderRequest orderRequest, CustomerInfo customerInfo, String bearerToken, Throwable t) {
        return new MessageResponse("Order creation is temporarily unavailable. Please try again later.");
    }

    //Cancel Order
    public void cancelOrder(Long orderId, String customerId) {
        Order order = findOrderById(orderId);

        validateCustomerOwnership(order, customerId);

        // Update the order status
        order.setOrderStatus("CANCELLED");

        orderRepository.save(order);
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
    public List<OrderDTO> getOrdersByOrderStatus(String orderStatus) {
        List<Order> orders = orderRepository.findByOrderStatus(orderStatus);

        if (orders.isEmpty()) {
            throw new OrderNotFoundException("No orders found with status: " + orderStatus);
        }
        return orders.stream()
                .map(this::mapOrderToOrderDTO)
                .collect(Collectors.toList());
    }

    //Find All Orders of Costumer By OrderStatus
    public List<OrderDTO> getOrdersOfCustomerByOrderStatus(String costumerId, String orderStatus) {
        List<Order> orders = orderRepository.findByOrderStatusAndCustomers_ConsumerId(orderStatus, costumerId);

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
        List<Order> deliveredOrders = orderRepository.findByCustomers_ConsumerIdAndOrderStatus(consumerId, "DELIVERED");

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
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException("Order with ID " + orderId + " not found.");
        }
        orderRepository.deleteById(orderId);
    }

    //Update Order Details
    @Transactional
    public void updateOrderDetails(Long orderId, OrderDTO updatedOrderDTO) {
        // Find the existing order
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found"));

        // Update order fields from the updatedOrderDTO
        BeanUtils.copyProperties(updatedOrderDTO, existingOrder, "id");

        // Update Customer details if provided in the updatedOrderDTO
        if (updatedOrderDTO.getCustomer() != null) {
            CustomerDTO updatedCustomerDTO = updatedOrderDTO.getCustomer();
            Customer existingCustomer = existingOrder.getCustomers();

            if (existingCustomer != null) {
                // Update customer fields from the updatedCustomerDTO
                BeanUtils.copyProperties(updatedCustomerDTO, existingCustomer);
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
                    BeanUtils.copyProperties(updatedOrderItemDTO, existingOrderItem, "id");

                    // Update Product details if provided in the updatedOrderItemDTO
                    if (updatedOrderItemDTO.getProduct() != null) {
                        ProductDTO updatedProductDTO = updatedOrderItemDTO.getProduct();
                        Product existingProduct = existingOrderItem.getProduct();

                        if (existingProduct != null) {
                            // Update product fields from the updatedProductDTO
                            BeanUtils.copyProperties(updatedProductDTO, existingProduct);
                        }
                    }
                }
            }
        }
        // Save the updated order
        orderRepository.save(existingOrder);
    }

    //Update Order Status
    public void updateOrderStatus(Long orderId, OrderStatusRequest newStatus) {
        // Find the order by its ID
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            // Update the order status
            order.setOrderStatus(newStatus.getOrderStatus());

            // Save the updated order
            orderRepository.save(order);
        } else {
            // Handle the case where the order with the given ID is not found
            throw new OrderNotFoundException("Order with ID " + orderId + " not found");
        }
    }
}