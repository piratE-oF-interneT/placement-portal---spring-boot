package com.portal.placementportal.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a student fails to meet the eligibility rules of a company drive
 * (CGPA, backlogs, branch, deadline).
 */
public class EligibilityViolationException extends BaseApiException {
    public EligibilityViolationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
