// ========================================
// SwaggerConfig.java
// ========================================
package com.banking.audit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Audit Service API")
                        .version("V11__create_audit_partitioning.sql.0.0")
                        .description("API pour la gestion des événements d'audit et conformité")
                        .contact(new Contact()
                                .name("Banking 3.0 Team")
                                .email("support@banking3.com")
                                .url("https://banking3.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token from Keycloak")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
