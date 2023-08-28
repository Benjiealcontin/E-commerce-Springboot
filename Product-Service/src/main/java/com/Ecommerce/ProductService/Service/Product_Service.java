package com.Ecommerce.ProductService.Service;

import com.Ecommerce.ProductService.Dto.ProductWithImageDTO;
import com.Ecommerce.ProductService.Entity.Image;
import com.Ecommerce.ProductService.Entity.Product;
import com.Ecommerce.ProductService.Entity.Review;
import com.Ecommerce.ProductService.Exception.*;
import com.Ecommerce.ProductService.Repository.ImageRepository;
import com.Ecommerce.ProductService.Repository.ProductRepository;
import com.Ecommerce.ProductService.Repository.ReviewRepository;
import com.Ecommerce.ProductService.Request.ProductRequest;
import com.Ecommerce.ProductService.Request.ReviewRequest;
import com.Ecommerce.ProductService.Request.StockQuantityRequest;
import com.Ecommerce.ProductService.Dto.MessageResponse;
import com.Ecommerce.ProductService.Utils.ImageUtility;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class Product_Service {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;
    private final ModelMapper modelMapper;
    @Autowired
    public Product_Service(ProductRepository productRepository, ReviewRepository reviewRepository, ImageRepository imageRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.imageRepository = imageRepository;
        this.modelMapper = modelMapper;
    }

    //Creating product
    public MessageResponse createProduct(ProductRequest productRequest, MultipartFile file) throws IOException {
        String productName = productRequest.getProductName();

        if (productRepository.existsByProductName(productName)) {
            throw new ProductAlreadyExistsException("Product with name " + productName + " already exists");
        }

        Product product = mapProductRequestToProduct(productRequest);
        productRepository.save(product);

        Image image = createImageEntity(file, product);
        imageRepository.save(image);

        return new MessageResponse("Product Added Successfully!");
    }

    private Product mapProductRequestToProduct(ProductRequest productRequest) {
        return modelMapper.map(productRequest, Product.class);
    }

    private Image createImageEntity(MultipartFile file, Product product) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        byte[] compressedImage = ImageUtility.compressImage(file.getBytes());

        return Image.builder()
                .name(originalFilename)
                .type(contentType)
                .image(compressedImage)
                .product(product)
                .build();
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
    public ProductWithImageDTO getProductWithImageDetails(long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        Image image = imageRepository.findByProductId(product.getId())
                .orElseThrow(() -> new ImageNotFoundException("Image not found for Product with ID: " + productId));

        ProductWithImageDTO productWithImageDTO = modelMapper.map(product, ProductWithImageDTO.class);
        productWithImageDTO.setImageName(image.getName());
        productWithImageDTO.setImageType(image.getType());

        return productWithImageDTO;
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
    public List<ProductWithImageDTO> getAllProductsWithImageDetails() {
        List<Product> products = productRepository.findAll();
        List<ProductWithImageDTO> productWithImageDTOList = new ArrayList<>();

        for (Product product : products) {
            Image image = imageRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new ImageNotFoundException("Image not found for Product with ID: " + product.getId()));

            ProductWithImageDTO productWithImageDTO = modelMapper.map(product, ProductWithImageDTO.class);
            productWithImageDTO.setImageName(image.getName());
            productWithImageDTO.setImageType(image.getType());


            productWithImageDTOList.add(productWithImageDTO);
        }

        return productWithImageDTOList;
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

    //Update Stock quantity of Product
    public void updateQuantityOfProduct(long id, StockQuantityRequest stockQuantityRequest) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));

        int requestedSubtraction = stockQuantityRequest.getSubtractionAmount();
        int currentStock = existingProduct.getStockQuantity();

        if (currentStock < requestedSubtraction) {
            throw new InsufficientStockException("Insufficient stock for product with id " + id);
        }

        existingProduct.setStockQuantity(currentStock - requestedSubtraction);
        productRepository.save(existingProduct);

        // Debugging output
        System.out.println("Updated Stock: " + existingProduct.getStockQuantity());
    }

}
