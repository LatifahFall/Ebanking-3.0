package com.banking.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Security configuration for API Gateway with conditional Keycloak integration.
 * Supports both dev mode (no auth) and prod mode (OAuth2 JWT with Keycloak).
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${keycloak.enabled:false}")
    private boolean keycloakEnabled;

    /**
     * Production Security Configuration with OAuth2 JWT (Keycloak)
     * Activated when keycloak.enabled=true
     */
    @Bean
    @ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true")
    public SecurityWebFilterChain securityWebFilterChainProduction(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints - No authentication required
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/auth/**").permitAll()
                .pathMatchers("/fallback/**").permitAll()
                
                // Protected endpoints - Require authentication
                .pathMatchers("/api/**").authenticated()
                .pathMatchers("/ws/**").authenticated()
                
                // Deny all other requests
                .anyExchange().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
            );

        return http.build();
    }

    /**
     * Development Security Configuration - No authentication
     * Activated when keycloak.enabled=false (default)
     */
    @Bean
    @ConditionalOnProperty(name = "keycloak.enabled", havingValue = "false", matchIfMissing = true)
    public SecurityWebFilterChain securityWebFilterChainDev(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll()
            );

        return http.build();
    }

    /**
     * JWT Authentication Converter - Extracts roles from Keycloak token
     * Converts realm_access.roles to Spring Security authorities with ROLE_ prefix
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}
