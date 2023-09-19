package com.Ecommerce.InventoryService.Service;

import com.Ecommerce.InventoryService.Entity.Product;
import com.Ecommerce.InventoryService.Exception.InsufficientStockException;
import com.Ecommerce.InventoryService.Exception.ProductNotFoundException;
import com.Ecommerce.InventoryService.Request.StockQuantityRequest;
import com.Ecommerce.InventoryService.Repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class Inventory_Service {

    private final ProductRepository productRepository;

    public Inventory_Service(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getProductById(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        return productOptional.orElseThrow(() -> new ProductNotFoundException("Product with ID " + productId + " not found."));
    }

    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();

        if (products.isEmpty()) {
            throw new ProductNotFoundException("No products found.");
        }

        return products;
    }

    //Low Inventory
    public List<Product> AllProductsByLowInventory() {
        List<Product> products = productRepository.findByStockQuantityLessThanEqual(10);

        if (products.isEmpty()) {
            throw new ProductNotFoundException("No products with low inventory (stock quantity <= 10) were found.");
        }

        return products;
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
}
