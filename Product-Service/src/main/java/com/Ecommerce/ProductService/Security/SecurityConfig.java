package com.Ecommerce.ProductService.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    public static final String ADMIN = "client_admin";
    public static final String CONSUMER = "client_consumer";
    public static final String SELLER = "client_seller";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET,"/actuator/**",
                                "/api/product/getById/*",
                                "/api/product/getByIds",
                                "/api/product/allProducts",
                                "/api/product/reviews/*",
                                "/api/product/category/*").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/product/add-product").hasAnyRole(ADMIN,SELLER)
                        .requestMatchers(HttpMethod.POST,"/api/product/add-review/*").hasRole(CONSUMER)
                        .requestMatchers(HttpMethod.DELETE,"/api/product/delete/*").hasAnyRole(ADMIN,SELLER)
                        .requestMatchers(HttpMethod.PUT,"/api/product/update/*").hasAnyRole(ADMIN,SELLER)
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthConverter)
                        )
                )
                .sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
