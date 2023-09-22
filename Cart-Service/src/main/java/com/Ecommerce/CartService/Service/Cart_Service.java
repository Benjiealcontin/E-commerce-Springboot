package com.Ecommerce.CartService.Service;

import com.Ecommerce.CartService.Dto.*;
import com.Ecommerce.CartService.Entity.CartItem;
import com.Ecommerce.CartService.Entity.Product;
import com.Ecommerce.CartService.Entity.UserCart;
import com.Ecommerce.CartService.Exception.CartItemNotFoundException;
import com.Ecommerce.CartService.Exception.CartNotFoundException;
import com.Ecommerce.CartService.Exception.ProductNotFoundException;
import com.Ecommerce.CartService.Repository.CartItemRepository;
import com.Ecommerce.CartService.Repository.ProductRepository;
import com.Ecommerce.CartService.Repository.UserCartRepository;
import com.Ecommerce.CartService.Request.CartRequest;
import com.Ecommerce.CartService.Request.ProductRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class Cart_Service {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserCartRepository userCartRepository;
    private final WebClient.Builder webClientBuilder;

    private static final String PRODUCT_SERVICE_URL = "http://Product-Service/api/product";

    public Cart_Service(CartItemRepository cartItemRepository, ProductRepository productRepository, UserCartRepository userCartRepository, WebClient.Builder webClientBuilder) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userCartRepository = userCartRepository;
        this.webClientBuilder = webClientBuilder;
    }

    //Add to Cart
    @CircuitBreaker(name = "addToCartCircuit", fallbackMethod = "addToCartFallback")
    public MessageResponse addToCart(TokenDTO tokenDTO, CartRequest cartRequest, String bearerToken) {
        // Extract product IDs from the order request
        List<Long> productIds = extractProductIds(cartRequest);

        // Create a new UserCart for the customer
        UserCart user = new UserCart();
        user.setCustomerId(tokenDTO.getSub());
        userCartRepository.save(user);

        // Fetch product details based on the product IDs
        List<ProductRequest> products = fetchProducts(productIds, bearerToken);

        // Create cart items and associate them with the user's cart
        createCartItems(products, user);

        return new MessageResponse("Cart Added Successfully.");
    }

    // Create cart items and associate them with the user's cart
    private void createCartItems(List<ProductRequest> products, UserCart userCart) {
        List<CartItem> cartItems = new ArrayList<>();
        for (ProductRequest productRequest : products) {
            CartItem cartItem = new CartItem();

            // Set the userCart for the cartItem
            cartItem.setUserCart(userCart);

            // Create a Product instance and set its properties
            Product product = new Product();
            product.setProductName(productRequest.getProductName());
            product.setDescription(productRequest.getDescription());
            product.setCategory(productRequest.getCategory());
            product.setPrice(productRequest.getPrice());
            product.setStockQuantity(productRequest.getStockQuantity());

            // Set the cartItem for the product
            product.setCartItem(cartItem);

            // Save the product in the database
            productRepository.save(product);

            cartItems.add(cartItem);
        }

        // Save all cart items in the database
        cartItemRepository.saveAll(cartItems);
    }

    //Get All the Product Ids from the Order Request
    private List<Long> extractProductIds(CartRequest cartRequest) {
        return cartRequest.getCartItems().stream()
                .map(CartRequest.CartItemRequest::getProductId)
                .collect(Collectors.toList());
    }

    //Get Product Details
    private List<ProductRequest> fetchProducts(List<Long> productIds, String bearerToken) {
        try {
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
        } catch (WebClientResponseException.NotFound e) {
            String responseBody = e.getResponseBodyAsString();
            throw new ProductNotFoundException(responseBody);
        } catch (WebClientResponseException.ServiceUnavailable e) {
            String responseBody = "Product is currently unavailable. Please try again later.";
            throw new ProductNotFoundException(responseBody);
        }
    }

    // Fallback method to handle circuit open state
    public MessageResponse addToCartFallback(TokenDTO tokenDTO, CartRequest cartRequest, String bearerToken, Throwable t) {
        return new MessageResponse("Cart addition is temporarily unavailable. Please try again later.");
    }

    //Get Order Cart of the customer by ID
    public UserCartDTO getUserCartWithDTOs(Long cartId) {
        // Retrieve the UserCart entity from the repository (you may need to adjust this part)
        UserCart userCart = userCartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart with ID " + cartId + " not found"));
        // Create a UserCartDTO
        UserCartDTO userCartDTO = new UserCartDTO();
        userCartDTO.setId(userCart.getId());
        userCartDTO.setCustomerId(userCart.getCustomerId());

        // Create CartItemDTOs with ProductDTOs
        List<CartItemDTO> cartItemDTOs = userCart.getCartItems().stream()
                .map(cartItem -> {
                    CartItemDTO cartItemDTO = new CartItemDTO();
                    Product product = cartItem.getProduct();

                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setId(product.getId());
                    productDTO.setProductName(product.getProductName());
                    productDTO.setDescription(product.getDescription());
                    productDTO.setCategory(product.getCategory());
                    productDTO.setPrice(product.getPrice());
                    productDTO.setStockQuantity(product.getStockQuantity());

                    cartItemDTO.setProduct(productDTO);
                    return cartItemDTO;
                })
                .collect(Collectors.toList());

        userCartDTO.setCartItems(cartItemDTOs);
        return userCartDTO;
    }

    //Get Order Cart of the customer by ID
    public UserCartDTO getUserCartWithDTOByCustomerId(TokenDTO tokenDTO) {
        // Retrieve the UserCart entity from the repository (you may need to adjust this part)
        UserCart userCart = userCartRepository.findByCustomerId(tokenDTO.getSub());

        if (userCart == null) {
            throw new CartNotFoundException("Customer Cart not found for customerId: " + tokenDTO.getSub());
        }

        // Create a UserCartDTO
        UserCartDTO userCartDTO = new UserCartDTO();
        userCartDTO.setId(userCart.getId());
        userCartDTO.setCustomerId(userCart.getCustomerId());

        // Create CartItemDTOs with ProductDTOs
        List<CartItemDTO> cartItemDTOs = userCart.getCartItems().stream()
                .map(cartItem -> {
                    CartItemDTO cartItemDTO = new CartItemDTO();
                    Product product = cartItem.getProduct();

                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setId(product.getId());
                    productDTO.setProductName(product.getProductName());
                    productDTO.setDescription(product.getDescription());
                    productDTO.setCategory(product.getCategory());
                    productDTO.setPrice(product.getPrice());
                    productDTO.setStockQuantity(product.getStockQuantity());

                    cartItemDTO.setProduct(productDTO);
                    return cartItemDTO;
                })
                .collect(Collectors.toList());

        userCartDTO.setCartItems(cartItemDTOs);
        return userCartDTO;
    }

    //Remove the Item from the Cart
    public void deleteCartItemWithProduct(Long cartItemId) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemId);

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            Product associatedProduct = cartItem.getProduct();

            // Delete the cart item
            cartItemRepository.deleteById(cartItemId);

            // Delete the associated product
            if (associatedProduct != null) {
                productRepository.deleteById(associatedProduct.getId());
            }
        } else {
            throw new CartItemNotFoundException("Cart Item not found.");
        }
    }
}
