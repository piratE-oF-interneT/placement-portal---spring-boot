package com.portal.placementportal.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseApiException {
    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    public InvalidCredentialsException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
