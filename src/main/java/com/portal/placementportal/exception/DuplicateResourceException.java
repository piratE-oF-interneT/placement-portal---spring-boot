package com.portal.placementportal.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BaseApiException {
    public DuplicateResourceException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
