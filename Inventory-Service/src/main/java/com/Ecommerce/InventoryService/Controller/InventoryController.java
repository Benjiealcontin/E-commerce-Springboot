package com.Ecommerce.InventoryService.Controller;

import com.Ecommerce.InventoryService.Dto.MessageResponse;
import com.Ecommerce.InventoryService.Entity.Product;
import com.Ecommerce.InventoryService.Exception.InsufficientStockException;
import com.Ecommerce.InventoryService.Exception.ProductNotFoundException;
import com.Ecommerce.InventoryService.Request.StockQuantityRequest;
import com.Ecommerce.InventoryService.Service.Inventory_Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/inventory")
public class InventoryController {

    private final Inventory_Service inventoryService;

    public InventoryController(Inventory_Service inventoryService) {
        this.inventoryService = inventoryService;
    }

    //Get Product by ID
    @GetMapping("/getProduct/{productId}")
    public ResponseEntity<?> getProductByID(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(inventoryService.getProductById(productId));
        }catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Get All Products
    @GetMapping("/getAllProduct")
    public ResponseEntity<?> getAllProduct() {
        try {
            return ResponseEntity.ok(inventoryService.getAllProducts());
        }catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Find All low Inventory
    @GetMapping("/low-inventory")
    public ResponseEntity<?> getAllProductsWithLowInventory() {
        try {
            List<Product> lowInventory = inventoryService.AllProductsByLowInventory();
            return ResponseEntity.ok(lowInventory);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Decrement Product when order in place
    @PutMapping("/decrement/{id}")
    public ResponseEntity<?> updateQuantityOfProduct(@PathVariable Long id, @RequestBody StockQuantityRequest stockQuantityRequest) {
        try {
            inventoryService.updateQuantityOfProduct(id, stockQuantityRequest);
            return ResponseEntity.ok(new MessageResponse("Product Stock Quantity Update Successfully."));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Increment Product when restock
    @PutMapping("/product-restock/{id}")
    public ResponseEntity<?> productRestock(@PathVariable Long id, @RequestBody StockQuantityRequest stockQuantityRequest) {
        try {
            inventoryService.restockOfProduct(id, stockQuantityRequest);
            return ResponseEntity.ok(new MessageResponse("Product Restock Update Successfully."));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}
