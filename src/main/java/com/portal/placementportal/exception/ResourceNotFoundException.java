package com.portal.placementportal.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseApiException {
    public ResourceNotFoundException(String resource, Object id) {
        super(HttpStatus.NOT_FOUND, resource + " not found with identifier: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
