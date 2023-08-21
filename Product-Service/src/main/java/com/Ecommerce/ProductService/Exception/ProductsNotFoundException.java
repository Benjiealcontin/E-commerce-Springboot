package com.Ecommerce.ProductService.Exception;

import java.util.List;

public class ProductsNotFoundException extends RuntimeException {
    private final List<Long> missingProductIds;

    public ProductsNotFoundException(List<Long> missingProductIds) {
        super("Products with IDs " + missingProductIds + " not found.");
        this.missingProductIds = missingProductIds;
    }

    public List<Long> getMissingProductIds() {
        return missingProductIds;
    }
}

