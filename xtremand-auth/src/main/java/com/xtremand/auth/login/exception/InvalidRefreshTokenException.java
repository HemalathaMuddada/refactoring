package com.xtremand.auth.login.exception;

import com.xtremand.common.constants.ErrorCodes;

import com.xtremand.common.exception.ApiException;

/**
 * Thrown when the provided refresh token is invalid or expired.
 */
public class InvalidRefreshTokenException extends ApiException {

    private static final long serialVersionUID = 1L;
    public static final String ERROR_CODE = ErrorCodes.INVALID_REFRESH_TOKEN;
    public static final String DEFAULT_MESSAGE = "Invalid or expired refresh token";

    public InvalidRefreshTokenException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public InvalidRefreshTokenException(String message) {
        super(message, ERROR_CODE);
    }
}
