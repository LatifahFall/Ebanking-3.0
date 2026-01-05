package com.bank.graphql_gateway.security;

import graphql.schema.DataFetchingEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Security utility to extract and forward authentication headers from GraphQL context.
 * This class does NOT implement authentication logic - it only propagates tokens to microservices.
 */
@Component
public class SecurityContext {

    /**
     * Extracts the Authorization header from the GraphQL context.
     * The header should be set by the GraphQL infrastructure from the HTTP request.
     * 
     * @param environment DataFetchingEnvironment containing the GraphQL context
     * @return Authorization header value (e.g., "Bearer eyJhbGc...") or null if not present
     */
    public String getAuthorizationHeader(DataFetchingEnvironment environment) {
        try {
            // Extract Authorization from GraphQL context (set from HTTP headers)
            return environment.getGraphQlContext().get(HttpHeaders.AUTHORIZATION);
        } catch (Exception e) {
            // Return null if Authorization header is not present
            return null;
        }
    }

    /**
     * Checks if an Authorization header is present in the GraphQL context.
     * 
     * @param environment DataFetchingEnvironment containing the GraphQL context
     * @return true if Authorization header exists, false otherwise
     */
    public boolean hasAuthorizationHeader(DataFetchingEnvironment environment) {
        String auth = getAuthorizationHeader(environment);
        return auth != null && !auth.isEmpty();
    }
}
