package com.Ecommerce.ProductService.Service;

import com.Ecommerce.ProductService.Entity.Product;
import com.Ecommerce.ProductService.Entity.Review;
import com.Ecommerce.ProductService.Exception.ProductAlreadyExistsException;
import com.Ecommerce.ProductService.Exception.ProductNotFoundException;
import com.Ecommerce.ProductService.Exception.ProductsNotFoundException;
import com.Ecommerce.ProductService.Repository.ProductRepository;
import com.Ecommerce.ProductService.Repository.ReviewRepository;
import com.Ecommerce.ProductService.Request.ProductRequest;
import com.Ecommerce.ProductService.Request.ReviewRequest;
import com.Ecommerce.ProductService.Response.MessageResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class Product_Service {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final ModelMapper modelMapper;
    @Autowired
    public Product_Service(ProductRepository productRepository, ReviewRepository reviewRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.modelMapper = modelMapper;
    }

    //Creating product
    public MessageResponse createProduct(ProductRequest productRequest) {
        if (productRepository.existsByProductName(productRequest.getProductName())) {
            throw new ProductAlreadyExistsException("Product with name " + productRequest.getProductName() + " already exists");
        }
        Product product = modelMapper.map(productRequest, Product.class);
        productRepository.save(product);

        return new MessageResponse("Product Added Successfully!");
    }

    //Creating Review of products
    public MessageResponse createProductReview(Long productId, ReviewRequest reviewRequest){
        productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product with ID " + productId + " not found."));
        Review review = modelMapper.map(reviewRequest, Review.class);
        review.setProductId(productId);
        reviewRepository.save(review);

        return new MessageResponse("Review Added Successfully!");
    }

    //Get Product by ID
    public Product getProductById(Long productId){
        Optional<Product> products = productRepository.findById(productId);
        return products.orElseThrow(() -> new ProductNotFoundException("Product with ID " + productId + " not found."));
    }

    //Get Products by IDs
    public List<Product> getProductsByIds(List<Long> productIds) {
        List<Product> products = new ArrayList<>();
        List<Long> missingProductIds = new ArrayList<>();

        for (Long productId : productIds) {
            Optional<Product> product = productRepository.findById(productId);
            if (product.isPresent()) {
                products.add(product.get());
            } else {
                missingProductIds.add(productId);
            }
        }

        if (!missingProductIds.isEmpty()) {
            throw new ProductsNotFoundException(missingProductIds);
        }

        return products;
    }



    //List All Products
    public List<Product> AllProducts(){
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            throw new ProductNotFoundException("No Products found.");
        }
        return products;
    }

    //Get All Reviews by ID
    public List<Review> getAllReviewsById(long productId){
        List<Review> reviews = reviewRepository.findByProductId(productId);
        if (reviews.isEmpty()) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found.");
        }
        return reviews;
    }

    //List All Products by Category
    public List<Product> AllProductsByCategory(String category){
        List<Product> products = productRepository.findByCategory(category);
        if (products.isEmpty()) {
            throw new ProductNotFoundException("Product with Category " + category + " not found.");
        }
        return products;
    }

    //Delete Product
    public void deleteProduct(Long productId){
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found.");
        }
        productRepository.deleteById(productId);
    }

    //Update Products
    public void updateProduct(long id, ProductRequest productRequest) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));

        modelMapper.map(productRequest, existingProduct);
        productRepository.save(existingProduct);
    }
}
