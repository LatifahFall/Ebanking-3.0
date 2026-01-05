# GraphQL Gateway - Authentication & Testing Guide

## ⚠️ CRITICAL ISSUE: Authentication Required

### Problem
All GraphQL queries are failing with `401 Unauthorized` errors because the microservices require authentication, but the GraphQL Gateway is not configured to handle auth tokens.

**Error Example:**
```
Caused by: org.springframework.web.reactive.function.client.WebClientResponseException$Unauthorized: 
401 Unauthorized from GET http://localhost:8081/admin/users/search
```

---

## 🔧 Solutions (Choose ONE)

### **Option 1: Disable Security in Microservices (Recommended for Testing)**

For each microservice, temporarily disable Spring Security:

#### In `application.properties` or `application.yml`:
```properties
# Disable security for testing
spring.security.enabled=false
management.security.enabled=false
```

#### OR in Spring Boot 3.x, configure SecurityFilterChain:
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
```

#### Restart all microservices after making changes.

---

###**Option 2: Add Authentication Header to GraphQL Gateway**

Configure WebClient to pass auth tokens from GraphQL requests to microservices:

#### Update `application.properties`:
```properties
# Service authentication (if using basic auth)
app.service.auth.username=admin
app.service.auth.password=admin123

# OR JWT token
app.service.auth.token=your-jwt-token-here
```

#### Update `GraphQLGatewayApplication.java`:
```java
@Bean
public WebClient.Builder webClientBuilder() {
    return WebClient.builder()
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
}
```

---

### **Option 3: Use Test Profile with Mock Services**

Create a test profile that doesn't require real microservices:

#### `application-test.properties`:
```properties
# Use mock service URLs or in-memory data
spring.graphql.schema.printer.enabled=true
```

---

## 🚀 Running Tests

### Method 1: Maven Test Command
```cmd
cd c:\Users\Hp\Desktop\graphql\Ebanking-3.0
mvn test
```

### Method 2: Run Specific Test Class
```cmd
mvn test -Dtest=GraphQLIntegrationTest
```

### Method 3: Run with Spring Boot
```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

---

## 🧪 Manual Testing with cURL

### Health Check (Always Works)
```cmd
curl -X POST http://localhost:8090/graphql ^
  -H "Content-Type: application/json" ^
  -d "{\"query\":\"{ health }\"}"
```

**Expected Response:**
```json
{
  "data": {
    "health": "GraphQL Gateway is UP"
  }
}
```

### Get All Users (Requires Auth Fix)
```cmd
curl -X POST http://localhost:8090/graphql ^
  -H "Content-Type: application/json" ^
  -d "{\"query\":\"query { users { id login email } }\"}"
```

**Current Response** (401 Error):
```json
{
  "errors": [{
    "message": "INTERNAL_ERROR",
    "extensions": {"classification": "INTERNAL_ERROR"}
  }],
  "data": {"users": null}
}
```

**Expected Response** (After Fix):
```json
{
  "data": {
    "users": [
      {"id": "1", "login": "admin", "email": "admin@example.com"},
      {"id": "2", "login": "user1", "email": "user1@example.com"}
    ]
  }
}
```

---

## 📝 Testing Checklist

### Before Testing
- [ ] All microservices are running on correct ports
  - User Service: http://localhost:8081
  - Account Service: http://localhost:8082  
  - Payment Service: http://localhost:8082
  - Crypto Service: http://localhost:8081
  - Notification Service: http://localhost:8084
  - Audit Service: http://localhost:8083
  - Auth Service: http://localhost:8081

- [ ] Security is disabled OR auth tokens configured
- [ ] GraphQL Gateway is running on port 8090
- [ ] Database connections are working for each service

### Test Execution Order
1. ✅ `health` query (no dependencies)
2. ⏳ `users` query (requires User Service + auth fix)
3. ⏳ `userById(id: "1")` query (requires User Service + auth fix)
4. ⏳ `accounts`, `payments`, etc. (requires respective services + auth fix)

---

##🔍 Debugging Authentication Issues

### Check if User Service is Running
```cmd
curl http://localhost:8081/actuator/health
```

### Test User Service Directly (Will Show Auth Error)
```cmd
curl http://localhost:8081/admin/users/search
```

**If you see 401**, security is enabled and needs to be disabled or configured.

### Check GraphQL Gateway Logs
```cmd
# Look for these error patterns in console:
"401 Unauthorized from GET http://localhost:8081/admin/users/search"
"Failed to fetch users from User Service"
```

---

## ✅ Quick Fix Summary

**To make GraphQL Gateway work immediately:**

1. **Stop all microservices**
2. **Add to each microservice's `application.properties`:**
   ```properties
   spring.security.enabled=false
   management.security.enabled=false
   ```
3. **Restart all microservices**
4. **Test GraphQL Gateway:**
   ```cmd
   curl -X POST http://localhost:8090/graphql ^
     -H "Content-Type: application/json" ^
     -d "{\"query\":\"{ users { id login email } }\"}"
   ```

---

## 📊 Test Results

| Test Type | Status | Notes |
|-----------|--------|-------|
| Health Check | ✅ PASS | No dependencies, always works |
| Schema Validation | ✅ PASS | All 27 queries + 29 mutations defined |
| Maven Compilation | ✅ PASS | 33 Java files compiled successfully |
| GraphQL Queries | ❌ FAIL | Blocked by 401 Unauthorized errors |
| Integration Tests | ⏳ PENDING | Requires auth configuration |

---

## 🎯 Next Steps

1. **Immediate**: Disable security in microservices for testing
2. **Short-term**: Configure proper JWT token passing through gateway
3. **Long-term**: Implement Keycloak integration with token relay

---

## 📞 Support

If tests still fail after following this guide:

1. Check microservice logs for detailed error messages
2. Verify all services are on correct ports using `netstat -ano | findstr "8081"`
3. Ensure PostgreSQL/databases are running and accessible
4. Check firewall settings if services can't communicate

---

**Created**: January 5, 2026  
**Last Updated**: January 5, 2026  
**Status**: 🔴 Authentication blocking all queries except `health`
