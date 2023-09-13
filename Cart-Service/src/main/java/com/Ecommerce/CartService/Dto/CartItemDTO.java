package com.Ecommerce.CartService.Dto;

import com.Ecommerce.CartService.Entity.Product;
import lombok.Data;

@Data
public class CartItemDTO {
    private ProductDTO product;
}
