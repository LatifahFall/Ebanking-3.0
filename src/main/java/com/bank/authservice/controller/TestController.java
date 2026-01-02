package com.bank.authservice.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public Map<String, Object> test(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from secured endpoint!");
        response.put("username", jwt.getClaim("preferred_username"));
        response.put("email", jwt.getClaim("email"));
        response.put("subject", jwt.getSubject());

        // Extract roles from realm_access
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            response.put("roles", realmAccess.get("roles"));
        }

        return response;
    }

    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        return Map.of(
                "message", "This is a public endpoint",
                "timestamp", java.time.Instant.now().toString()
        );
    }

    @GetMapping("/user-info")
    public Map<String, Object> userInfo(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", jwt.getSubject());
        userInfo.put("preferred_username", jwt.getClaim("preferred_username"));
        userInfo.put("email", jwt.getClaim("email"));
        userInfo.put("email_verified", jwt.getClaim("email_verified"));
        userInfo.put("name", jwt.getClaim("name"));
        userInfo.put("given_name", jwt.getClaim("given_name"));
        userInfo.put("family_name", jwt.getClaim("family_name"));

        return userInfo;
    }
}