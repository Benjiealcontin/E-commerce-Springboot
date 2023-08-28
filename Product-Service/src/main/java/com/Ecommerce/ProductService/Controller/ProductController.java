package com.Ecommerce.ProductService.Controller;

import com.Ecommerce.ProductService.Dto.ProductWithImageDTO;
import com.Ecommerce.ProductService.Entity.Image;
import com.Ecommerce.ProductService.Entity.Product;
import com.Ecommerce.ProductService.Entity.Review;
import com.Ecommerce.ProductService.Exception.InsufficientStockException;
import com.Ecommerce.ProductService.Exception.ProductAlreadyExistsException;
import com.Ecommerce.ProductService.Exception.ProductNotFoundException;
import com.Ecommerce.ProductService.Exception.ProductsNotFoundException;
import com.Ecommerce.ProductService.Repository.ImageRepository;
import com.Ecommerce.ProductService.Request.ProductRequest;
import com.Ecommerce.ProductService.Request.ReviewRequest;
import com.Ecommerce.ProductService.Request.StockQuantityRequest;
import com.Ecommerce.ProductService.Dto.MessageResponse;
import com.Ecommerce.ProductService.Dto.ValidationErrorResponse;
import com.Ecommerce.ProductService.Service.Product_Service;
import com.Ecommerce.ProductService.Service.ValidationService;
import com.Ecommerce.ProductService.Utils.ImageUtility;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/product")
public class ProductController {

    private final Product_Service productService;
    private final ValidationService validationService;
    private final ImageRepository imageRepository;

    public ProductController(Product_Service productService, ValidationService validationService, ImageRepository imageRepository) {
        this.productService = productService;
        this.validationService = validationService;
        this.imageRepository = imageRepository;
    }

    //Add product
    @PostMapping("/add-product")
    public ResponseEntity<?> createProduct(@RequestParam("image") MultipartFile file,
                                           @RequestParam("productName") String productName,
                                           @RequestParam("description") String description,
                                           @RequestParam("category") String category,
                                           @RequestParam("price") double price,
                                           @RequestParam("stockQuantity") int stockQuantity){
        try{
            // Validate the ProductRequest using the validation service
//            if (bindingResult.hasErrors()) {
//                ValidationErrorResponse validationErrorResponse = validationService.buildValidationErrorResponse(bindingResult);
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrorResponse);
//            }
            ProductRequest productRequest = new ProductRequest(productName, description, category, price, stockQuantity);
            return ResponseEntity.ok(productService.createProduct(productRequest, file));
        }catch (ProductAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Add Review
    @PostMapping("/add-review/{productId}")
    public ResponseEntity<?> createReview(@PathVariable Long productId, @RequestBody @Valid ReviewRequest reviewRequest, BindingResult bindingResult){
        try{
            // Validate the ProductRequest using the validation service
            if (bindingResult.hasErrors()) {
                ValidationErrorResponse validationErrorResponse = validationService.buildValidationErrorResponse(bindingResult);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrorResponse);
            }
            return ResponseEntity.ok(productService.createProductReview(productId,reviewRequest));
        }catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find product by ID
    @GetMapping("/getById/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId){
        try{
            return ResponseEntity.ok(productService.getProductWithImageDetails(productId));
        }catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find products by IDs
    @GetMapping("/getByIds")
    public ResponseEntity<?> getProductsByIds(@RequestParam List<Long> productIds) {
        try {
            List<Product> products = productService.getProductsByIds(productIds);
            return ResponseEntity.ok(products);
        } catch (ProductsNotFoundException e) {
            List<Long> missingProductIds = e.getMissingProductIds();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Products not found for IDs: " + missingProductIds);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All Products
    @GetMapping("/allProducts")
    public ResponseEntity<?> allProducts(){
        try{
            List<ProductWithImageDTO> allProducts = productService.getAllProductsWithImageDetails();
            return ResponseEntity.ok(allProducts);
        }catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping(path = {"/get/image/{name}"})
    public ResponseEntity<byte[]> getImage(@PathVariable("name") String name) throws IOException {
        final Optional<Image> dbImage = imageRepository.findByName(name);

        if (dbImage.isPresent()) {
            Image image = dbImage.get();
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.valueOf(image.getType()))
                    .body(ImageUtility.decompressImage(image.getImage()));
        } else {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }


    //Find All Review by Product ID
    @GetMapping("/reviews/{productId}")
    public ResponseEntity<?> allReviews(@PathVariable Long productId){
        try{
            List<Review> allReviews = productService.getAllReviewsById(productId);
            return ResponseEntity.ok(allReviews);
        }catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All products by Category
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getAllProductsByCategory(@PathVariable String category){
        try{
            List<Product> allReviews = productService.AllProductsByCategory(category);
            return ResponseEntity.ok(allReviews);
        }catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Delete Product
    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId){
        try{
            productService.deleteProduct(productId);
            return ResponseEntity.ok(new MessageResponse("Product Delete Successfully!"));
        }catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Update Product
    @PutMapping("/update/{Id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long Id,@RequestBody ProductRequest productRequest){
        try{
            productService.updateProduct(Id,productRequest);
            return ResponseEntity.ok(new MessageResponse("Product Update Successfully!"));
        }catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/update/quantity/{id}")
    public ResponseEntity<?> updateQuantityOfProduct(@PathVariable Long id, @RequestBody StockQuantityRequest stockQuantityRequest) {
        try {
            productService.updateQuantityOfProduct(id, stockQuantityRequest);
            return ResponseEntity.ok(new MessageResponse("Product Stock Quantity Update Successfully!"));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
