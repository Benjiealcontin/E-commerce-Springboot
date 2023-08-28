package com.Ecommerce.OrderService.Security;

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
        http.
                authorizeHttpRequests(auth ->
                {
                    auth.requestMatchers(HttpMethod.GET,
                   "/actuator/**",
                            "/api/order/getOrder/*",
                            "").permitAll();
                    auth.requestMatchers(HttpMethod.GET,
                   "/api/order/customerOrder",
                            "/api/order/customer/history").hasAnyRole(CONSUMER);
                    auth.requestMatchers(HttpMethod.GET,
                   "/api/order/getAllOrders",
                            "/api/order/status/*").hasAnyRole(ADMIN);
                    auth.requestMatchers(HttpMethod.POST, "/api/order/add-order").hasRole(ADMIN);
                    auth.requestMatchers(HttpMethod.DELETE, "/api/order/deleteOrder/*").hasRole(ADMIN);
                    auth.requestMatchers(HttpMethod.PUT, "/api/order/updateOrder/*").hasRole(ADMIN);
                    auth.anyRequest().authenticated();
                });

        http.
                oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)
                ));

        http.
                sessionManagement((session) ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
