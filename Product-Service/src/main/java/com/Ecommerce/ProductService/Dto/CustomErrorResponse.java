package com.Ecommerce.ProductService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomErrorResponse {
    private int status;
    private String message;
    private List<String> errors;
}
