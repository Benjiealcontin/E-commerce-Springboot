package com.Ecommerce.CustomerService.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    public static final String ADMIN = "client_admin";
    public static final String CONSUMER = "client_consumer";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.GET,"/actuator/**").permitAll();
                    auth.requestMatchers(HttpMethod.POST,"/api/customer/add-customer").permitAll();
                    auth.requestMatchers(HttpMethod.GET,"/api/customer/customerDetails/*").hasRole(ADMIN);
                    auth.requestMatchers(HttpMethod.DELETE,"/api/customer/delete-customer/*").hasRole(ADMIN);
                    auth.requestMatchers(HttpMethod.PUT,"/api/customer/update-customer/*").hasAnyRole(ADMIN,CONSUMER);
                    auth.anyRequest().authenticated();
                })
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
