package com.Ecommerce.ProductService.Controller;

import com.Ecommerce.ProductService.Dto.*;
import com.Ecommerce.ProductService.Entity.Product;
import com.Ecommerce.ProductService.Entity.Review;
import com.Ecommerce.ProductService.Exception.*;
import com.Ecommerce.ProductService.Request.ProductRequest;
import com.Ecommerce.ProductService.Request.ReviewRequest;
import com.Ecommerce.ProductService.Request.StockQuantityRequest;
import com.Ecommerce.ProductService.Service.Product_Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/product")
public class ProductController {

    private final Product_Service productService;

    public ProductController(Product_Service productService) {
        this.productService = productService;
    }

    //Add product
    @PostMapping("/add-product")
    public ResponseEntity<?> createProduct(@ModelAttribute @Valid ProductRequest productRequest,
                                           @RequestParam("image") MultipartFile image,
                                           BindingResult bindingResult){

        if (image.isEmpty()) {
            return ResponseEntity.badRequest().body("Image file is required");
        }

        if (bindingResult.hasErrors()) {
            // If there are validation errors, return a bad request response with error details
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        try {
            return ResponseEntity.ok(productService.createProduct(productRequest, image));
        } catch (ProductAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Add Review
    @PostMapping("/add-review/{productId}")
    public ResponseEntity<?> createReview(@PathVariable Long productId,
                                          @RequestBody @Valid ReviewRequest reviewRequest,
                                          BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            // If there are validation errors, return a bad request response with error details
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        try{
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
        }catch (ProductNotFoundException | ImageNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find products by IDs
    @GetMapping("/getByIds")
    public ResponseEntity<?> getProductsByIds(@RequestParam List<Long> productIds) {
        try {
            List<ProductInfoDTO> products = productService.getProductsInfoByIds(productIds);
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

    //Display Image
    @GetMapping("/get/image/{name}")
    public ResponseEntity<ImageResponseDTO> getImage(@PathVariable("name") String name) {
        try {
            ImageResponseDTO imageResponse = productService.getImageByName(name);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(imageResponse.getContentType());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageResponse);
        } catch (ImageNotFoundException e) {
            return ResponseEntity.notFound().build();
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

    //Update Quantity of Product
    @PutMapping("/update-quantity/{id}")
    public ResponseEntity<?> updateQuantityOfProduct(@PathVariable Long id, @RequestBody StockQuantityRequest stockQuantityRequest) {
        try {
            productService.updateQuantityOfProduct(id, stockQuantityRequest);
            return ResponseEntity.ok(new MessageResponse("Product Stock Quantity Update Successfully."));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Update Image
    @PutMapping("/update-image/{productId}")
    public ResponseEntity<?> updateProductImage(
            @PathVariable Long productId,
            @RequestParam("image") MultipartFile imageFile)
    {
        try {
            Product product = productService.getProductById(productId);

            productService.updateProductImage(product, imageFile);

            return ResponseEntity.ok(new MessageResponse("Product image updated successfully."));
        }catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
