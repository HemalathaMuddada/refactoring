package com.xtremand.auth.util;

/**
 * Thrown when a hex token cannot be decoded.
 */
public class TokenDecodeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TokenDecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
