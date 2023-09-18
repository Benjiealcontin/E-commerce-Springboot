package com.Ecommerce.ProductService.Service;

import com.Ecommerce.ProductService.Dto.ImageResponseDTO;
import com.Ecommerce.ProductService.Dto.MessageResponse;
import com.Ecommerce.ProductService.Dto.ProductInfoDTO;
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
import com.Ecommerce.ProductService.Utils.ImageUtility;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @CircuitBreaker(name = "createProduct", fallbackMethod = "createProductFallback")
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

    //Map the ProductRequest and Product
    private Product mapProductRequestToProduct(ProductRequest productRequest) {
        return modelMapper.map(productRequest, Product.class);
    }

    //Create a Image
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

    // Fallback method to handle circuit open state
    public MessageResponse createProductFallback(ProductRequest productRequest, MultipartFile file, Throwable t) {
        return new MessageResponse("Product creation is temporarily unavailable. Please try again later.");
    }

    //Creating Review of products
    public MessageResponse createProductReview(Long productId, ReviewRequest reviewRequest) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID " + productId + " not found."));
        Review review = modelMapper.map(reviewRequest, Review.class);
        review.setProductId(productId);
        reviewRepository.save(review);

        return new MessageResponse("Review Added Successfully!");
    }

    //Get the Image and Decode
    public ImageResponseDTO getImageByName(String name) throws ImageNotFoundException {
        final Optional<Image> dbImage = imageRepository.findByName(name);

        if (dbImage.isPresent()) {
            Image image = dbImage.get();
            byte[] imageData = ImageUtility.decompressImage(image.getImage());
            MediaType contentType = MediaType.valueOf(image.getType());

            return new ImageResponseDTO(imageData, contentType);
        } else {
            throw new ImageNotFoundException("Image not found with the given name");
        }
    }

    //Get Product by ID in Image
    public ProductWithImageDTO getProductWithImageDetails(long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        Image image = imageRepository.findByProductId(product.getId())
                .orElseThrow(() -> new ImageNotFoundException("Image not found for Product with ID: " + productId));

        ProductWithImageDTO productWithImageDTO = modelMapper.map(product, ProductWithImageDTO.class);
        productWithImageDTO.setImageName(image.getName());
        productWithImageDTO.setImageType(image.getType());
        productWithImageDTO.setImageData(image.getImage());
        return productWithImageDTO;
    }

    //Get Product by ID
    public Product getProductById(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        return productOptional.orElseThrow(() -> new ProductNotFoundException("Product with ID " + productId + " not found."));
    }

    //Get Products by IDs
    public List<ProductInfoDTO> getProductsInfoByIds(List<Long> productIds) {
        List<ProductInfoDTO> productsInfo = new ArrayList<>();
        List<Long> missingProductIds = new ArrayList<>();

        for (Long productId : productIds) {
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                ProductInfoDTO productInfoDTO = new ProductInfoDTO(
                        product.getId(),
                        product.getProductName(),
                        product.getPrice(),
                        product.getStockQuantity(),
                        product.getDescription(),
                        product.getCategory()
                );
                productsInfo.add(productInfoDTO);
            } else {
                missingProductIds.add(productId);
            }
        }

        if (!missingProductIds.isEmpty()) {
            throw new ProductsNotFoundException(missingProductIds);
        }

        return productsInfo;
    }

    //List All Products
    public List<ProductWithImageDTO> getAllProductsWithImageDetails() {
        List<Product> products = productRepository.findAll();
        List<ProductWithImageDTO> productWithImageDTOList = new ArrayList<>();

        for (Product product : products) {
            Image image = imageRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new ImageNotFoundException("Image not found for Product with ID: " + product.getId()));

            ProductWithImageDTO productWithImageDTO = mapProductToDTO(product, image);
            productWithImageDTOList.add(productWithImageDTO);
        }

        return productWithImageDTOList;
    }

    //Get All Reviews by ID
    public List<Review> getAllReviewsById(long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        if (reviews.isEmpty()) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found.");
        }
        return reviews;
    }

    //List All Products by Category
    public List<ProductWithImageDTO> getAllProductsByCategory(String category) {
        List<Product> products = productRepository.findByCategory(category);

        if (products.isEmpty()) {
            throw new ProductNotFoundException("Products with category '" + category + "' not found.");
        }

        List<ProductWithImageDTO> productWithImageDTOList = new ArrayList<>(products.size());

        for (Product product : products) {
            Image image = imageRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new ImageNotFoundException("Image not found for Product with ID: " + product.getId()));

            ProductWithImageDTO productWithImageDTO = mapProductToDTO(product, image);
            productWithImageDTOList.add(productWithImageDTO);
        }

        return productWithImageDTOList;
    }

    private ProductWithImageDTO mapProductToDTO(Product product, Image image) {
        ProductWithImageDTO productWithImageDTO = modelMapper.map(product, ProductWithImageDTO.class);
        productWithImageDTO.setImageName(image.getName());
        productWithImageDTO.setImageType(image.getType());
        productWithImageDTO.setImageData(image.getImage());

        return productWithImageDTO;
    }


    //Low Inventory
    public List<ProductWithImageDTO> AllProductsByLowInventory() {
        List<Product> products = productRepository.findByStockQuantityLessThanEqual(10);

        if (products.isEmpty()) {
            throw new ProductNotFoundException("No products with low inventory (stock quantity <= 10) were found.");
        }

        return products.stream()
                .map(product -> {
                    Image image = imageRepository.findByProductId(product.getId())
                            .orElseThrow(() -> new ImageNotFoundException("Image not found for Product with ID: " + product.getId()));

                    ProductWithImageDTO productWithImageDTO = modelMapper.map(product, ProductWithImageDTO.class);
                    productWithImageDTO.setImageName(image.getName());
                    productWithImageDTO.setImageType(image.getType());
                    productWithImageDTO.setImageData(image.getImage());

                    return productWithImageDTO;
                })
                .collect(Collectors.toList());
    }

    //Delete Product
    public void deleteProduct(Long productId) {
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

    //Decrement Product when order in place
    public void updateQuantityOfProduct(long id, StockQuantityRequest stockQuantityRequest) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));

        int requestedSubtraction = stockQuantityRequest.getQuantityAmount();
        int currentStock = existingProduct.getStockQuantity();

        if (currentStock < requestedSubtraction) {
            throw new InsufficientStockException("Insufficient stock for product with id " + id);
        }

        existingProduct.setStockQuantity(currentStock - requestedSubtraction);
        productRepository.save(existingProduct);

    }

    //Increment Product when restock
    public void restockOfProduct(long id, StockQuantityRequest stockQuantityRequest) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));

        int requestedSubtraction = stockQuantityRequest.getQuantityAmount();
        int currentStock = existingProduct.getStockQuantity();

        existingProduct.setStockQuantity(currentStock + requestedSubtraction);
        productRepository.save(existingProduct);

    }

    //Update Image
    public void updateProductImage(Product product, MultipartFile newImageFile) throws IOException {
        Image existingImage = product.getImage();

        if (existingImage != null) {
            // Delete the existing image from both the product and the database
            product.setImage(null);
            imageRepository.delete(existingImage);
        }

        // Create and save the new image entity
        Image newImage = createImageEntity(newImageFile, product);
        imageRepository.save(newImage);

        // Update the product's image reference
        product.setImage(newImage);
    }

}
