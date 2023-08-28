package com.Ecommerce.ProductService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponseDTO {
    private byte[] imageData;
    private MediaType contentType;
}
