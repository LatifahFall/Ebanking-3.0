package com.bank.graphql_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.http.HttpHeaders;

/**
 * GraphQL configuration to intercept HTTP requests and propagate headers to GraphQL context.
 * This allows resolvers to access HTTP headers (like Authorization) from the GraphQL context.
 */
@Configuration
public class GraphQLSecurityConfig {

    /**
     * Intercepts GraphQL requests to extract and store HTTP headers in GraphQL context.
     * The Authorization header is stored so it can be forwarded to microservices.
     */
    @Bean
    public WebGraphQlInterceptor authorizationInterceptor() {
        return (request, chain) -> {
            // Extract Authorization header from HTTP request
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            // Store in GraphQL context for access in resolvers
            if (authHeader != null && !authHeader.isEmpty()) {
                request.configureExecutionInput((executionInput, builder) -> {
                    // Put Authorization header in GraphQL context
                    return builder.graphQLContext(context -> 
                        context.put(HttpHeaders.AUTHORIZATION, authHeader)
                    ).build();
                });
            }
            
            return chain.next(request);
        };
    }
}
