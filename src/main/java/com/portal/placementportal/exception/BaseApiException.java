package com.portal.placementportal.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for all application-defined exceptions. Carries an HTTP status so
 * the global handler can translate the exception into an appropriate response
 * without relying on Java's built-in exception types.
 */
public abstract class BaseApiException extends RuntimeException {

    private final HttpStatus status;

    protected BaseApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
