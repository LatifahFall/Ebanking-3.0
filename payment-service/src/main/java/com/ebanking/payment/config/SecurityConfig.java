package com.ebanking.payment.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité avec support Keycloak conditionnel
 * En développement : pas d'authentification requise (service auth non disponible)
 * En production : Keycloak activé (quand le service auth sera disponible)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * Configuration Keycloak - active uniquement si keycloak.enabled=true
     * Sera utilisée en production quand le service auth sera disponible
     */
    @Configuration
    @KeycloakConfiguration
    @ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true", matchIfMissing = false)
    @Import(KeycloakSpringBootConfigResolver.class)
    static class KeycloakSecurityConfig {
        
        @Bean
        public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
            return new KeycloakSpringBootConfigResolver();
        }

        @Bean
        public SecurityFilterChain keycloakSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    .requestMatchers("/actuator/**").hasRole("ADMIN")
                    .requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/payments/**").hasAnyRole("USER", "ADMIN", "AGENT")
                    .anyRequest().authenticated()
                );
            return http.build();
        }
    }

    /**
     * Configuration de développement - pas d'authentification requise
     * Utilisée quand keycloak.enabled=false (par défaut en dev)
     * Permet de tester sans le service auth
     */
    @Configuration
    @ConditionalOnProperty(name = "keycloak.enabled", havingValue = "false", matchIfMissing = true)
    static class DevSecurityConfig {

        @Bean
        public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    // En développement, toutes les routes sont accessibles sans authentification
                    // Cela permet de tester sans dépendre du service auth
                    .anyRequest().permitAll()
                );
            return http.build();
        }
    }
}
