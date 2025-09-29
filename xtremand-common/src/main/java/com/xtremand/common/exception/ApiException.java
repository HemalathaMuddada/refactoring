package com.xtremand.common.exception;

/**
 * Base unchecked exception carrying an error code for API error responses.
 */
public class ApiException extends RuntimeException implements SuppressDebugTrace {

    private static final long serialVersionUID = 1L;
    private final String errorCode;

    public ApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
