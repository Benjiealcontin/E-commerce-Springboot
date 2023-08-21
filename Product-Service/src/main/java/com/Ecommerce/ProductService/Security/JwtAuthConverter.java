package com.Ecommerce.ProductService.Security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    @Value("${jwt.auth.converter[0].resource-id}")
    private String resourceId0;
    @Value("${jwt.auth.converter[0].principle-attribute}")
    private String principleAttribute0;

    @Value("${jwt.auth.converter[1].resource-id}")
    private String resourceId1;
    @Value("${jwt.auth.converter[1].principle-attribute}")
    private String principleAttribute1;

    @Value("${jwt.auth.converter[2].resource-id}")
    private String resourceId2;
    @Value("${jwt.auth.converter[2].principle-attribute}")
    private String principleAttribute2;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractResourceRoles(jwt).stream()
        ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(
                jwt,
                authorities,
                getPrincipleClaimName(jwt)
        );
    }

    private String getPrincipleClaimName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;
        if (principleAttribute0 != null) {
            claimName = principleAttribute0;
        } else if (principleAttribute1 != null) {
            claimName = principleAttribute1;
        } else if (principleAttribute2 != null) {
            claimName = principleAttribute2;
        }
        return jwt.getClaim(claimName);
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess;
        Map<String, Object> resource;
        Collection<String> resourceRoles;

        if (jwt.getClaim("resource_access") == null) {
            return Set.of();
        }

        resourceAccess = jwt.getClaim("resource_access");

        if (resourceAccess.get(resourceId0) == null &&
                resourceAccess.get(resourceId1) == null &&
                resourceAccess.get(resourceId2) == null) {
            return Set.of();
        }

        if (resourceAccess.get(resourceId0) != null) {
            resource = (Map<String, Object>) resourceAccess.get(resourceId0);
        } else if (resourceAccess.get(resourceId1) != null) {
            resource = (Map<String, Object>) resourceAccess.get(resourceId1);
        } else {
            resource = (Map<String, Object>) resourceAccess.get(resourceId2);
        }

        resourceRoles = (Collection<String>) resource.get("roles");
        return resourceRoles
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }

}
