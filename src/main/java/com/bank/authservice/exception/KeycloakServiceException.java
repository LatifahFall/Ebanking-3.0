package com.bank.authservice.exception;

public class KeycloakServiceException extends RuntimeException {

    public KeycloakServiceException(String message) {
        super(message);
    }

    public KeycloakServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
