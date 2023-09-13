package com.Ecommerce.CartService.Dto;

import com.Ecommerce.CartService.Entity.CartItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserCartDTO {
    private Long id;

    private String customerId;

    private List<CartItemDTO> cartItems = new ArrayList<>();
}
