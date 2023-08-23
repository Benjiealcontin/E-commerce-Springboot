package com.Ecommerce.OrderService.Utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperUtil {

    @Bean
    public org.modelmapper.ModelMapper modelMapper() {
        return new org.modelmapper.ModelMapper();
    }
}
