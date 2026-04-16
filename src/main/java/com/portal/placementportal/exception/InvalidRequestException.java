package com.portal.placementportal.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when business-level request validation fails (i.e. request is
 * syntactically valid but semantically rejected).
 */
public class InvalidRequestException extends BaseApiException {
    public InvalidRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
