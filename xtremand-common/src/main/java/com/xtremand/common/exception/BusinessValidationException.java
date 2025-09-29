package com.xtremand.common.exception;

import java.util.Map;

/**
 * Thrown when a business validation rule is violated.
 */
public class BusinessValidationException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public BusinessValidationException(String errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
