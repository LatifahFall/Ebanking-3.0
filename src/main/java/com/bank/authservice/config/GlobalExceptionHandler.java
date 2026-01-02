package com.bank.authservice.config;

import com.bank.authservice.exception.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /* ================= VALIDATION ================= */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(error(
                "VALIDATION_ERROR",
                "Invalid request payload"
        ));
    }

    /* ================= AUTH ================= */

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(
                "AUTHENTICATION_FAILED",
                ex.getMessage()
        ));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handleJwt(JwtException ex) {
        log.warn("JWT error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(
                "INVALID_TOKEN",
                "Token is invalid or expired"
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(
                "ACCESS_DENIED",
                "You are not allowed to access this resource"
        ));
    }

    /* ================= BAD REQUEST ================= */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(error(
                "BAD_REQUEST",
                ex.getMessage()
        ));
    }

    /* ================= FALLBACK ================= */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(
                "INTERNAL_ERROR",
                "An unexpected error occurred"
        ));
    }

    /* ================= UTIL ================= */

    private Map<String, Object> error(String code, String message) {
        return Map.of(
                "error", code,
                "message", message,
                "timestamp", java.time.Instant.now().toString()
        );
    }
}
