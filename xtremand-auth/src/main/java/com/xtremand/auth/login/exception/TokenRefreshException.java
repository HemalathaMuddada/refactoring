package com.xtremand.auth.login.exception;

import com.xtremand.common.constants.ErrorCodes;


import com.xtremand.common.exception.ApiException;
import com.xtremand.common.exception.SuppressDebugTrace;

/**
 * Thrown when an unexpected error occurs while refreshing tokens.
 */
public class TokenRefreshException extends ApiException implements SuppressDebugTrace {

    private static final long serialVersionUID = 1L;
    public static final String ERROR_CODE = ErrorCodes.TOKEN_REFRESH;
    public static final String DEFAULT_MESSAGE = "Token refresh failed";

    public TokenRefreshException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public TokenRefreshException(String message) {
        super(message, ERROR_CODE);
    }

    public TokenRefreshException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }
}
