package com.xtremand.common.exception;

/**
 * Thrown when a domain/business rule is violated.
 * 
 * Example: "User already exists", "Cannot delete active campaign", etc.
 */
public class BusinessException extends RuntimeException implements SuppressDebugTrace {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
