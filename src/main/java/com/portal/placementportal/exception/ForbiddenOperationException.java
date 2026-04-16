package com.portal.placementportal.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenOperationException extends BaseApiException {
    public ForbiddenOperationException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
