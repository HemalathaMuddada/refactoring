package com.xtremand.common.exception;

/**
 * Base class for resource-not-found errors across the domain.
 */
public abstract class BaseNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BaseNotFoundException(String message) {
        super(message);
    }

    public BaseNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
