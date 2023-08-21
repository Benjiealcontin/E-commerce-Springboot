package com.Ecommerce.GatewayService;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("keycloak", r -> r.path("/realms/**")
//                        .uri("http://localhost:8081"))
//                // Add other routes for your APIs here
//                .build();
//    }
}

