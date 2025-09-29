package com.xtremand.auth.login.exception;

import com.xtremand.common.constants.ErrorCodes;

import com.xtremand.common.exception.ApiException;

/**
 * Thrown when access or refresh token generation fails.
 */
public class TokenGenerationException extends ApiException {

    private static final long serialVersionUID = 1L;
    public static final String ERROR_CODE = ErrorCodes.TOKEN_GENERATION;
    public static final String DEFAULT_MESSAGE = "Token generation failed";

    public TokenGenerationException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }

    public TokenGenerationException(String message) {
        super(message, ERROR_CODE);
    }
}
