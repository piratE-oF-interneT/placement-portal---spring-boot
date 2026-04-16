package com.portal.placementportal.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a user attempts to access or modify a resource belonging to a
 * college other than their own.
 */
public class CrossCollegeAccessException extends BaseApiException {
    public CrossCollegeAccessException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
