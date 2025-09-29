package com.xtremand.auth.handler;

import java.time.Instant;
import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.xtremand.auth.forgotpassword.exception.InvalidResetTokenException;
import com.xtremand.auth.handler.dto.ErrorResponse;
import com.xtremand.auth.login.exception.AccountDeletedException;
import com.xtremand.auth.login.exception.AccountLockedException;
import com.xtremand.auth.login.exception.AccountSuspendedException;
import com.xtremand.auth.login.exception.AccountUnapprovedException;
import com.xtremand.common.exception.AccountAlreadyActivatedException;
import com.xtremand.common.exception.ActivationTokenExpiredException;
import com.xtremand.common.exception.DuplicateResourceException;
import com.xtremand.common.exception.InvalidActivationTokenException;

@RestControllerAdvice
public class RestExceptionHandler {

    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception ex, HttpStatus status, String errorCode, String message, WebRequest request) {
        ErrorResponse.Detail detail = ErrorResponse.Detail.builder()
                .type(ex.getClass().getSimpleName())
                .message(ex.getMessage())
                .build();

        ErrorResponse.DebugInfo debugInfo = ErrorResponse.DebugInfo.builder()
                .exceptionType(ex.getClass().getName())
                .build();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .errorCode(errorCode)
                .message(message)
                .details(Collections.singletonList(detail))
                .path(request.getDescription(false).substring(4)) // remove "uri="
                .debug(debugInfo)
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler({
            AccountUnapprovedException.class,
            AccountSuspendedException.class,
            AccountDeletedException.class,
            AccountLockedException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationExceptions(Exception ex, WebRequest request) {
        String errorCode = "ERR_AUTH_FAILURE";
        if (ex instanceof BadCredentialsException) {
            errorCode = "ERR_INVALID_CREDENTIALS";
        } else if (ex instanceof AccountSuspendedException) {
            errorCode = "ERR_ACCOUNT_SUSPENDED";
        } else if (ex instanceof AccountDeletedException) {
            errorCode = "ERR_ACCOUNT_DELETED";
        } else if (ex instanceof AccountLockedException) {
            errorCode = "ERR_ACCOUNT_LOCKED";
        } else if (ex instanceof AccountUnapprovedException) {
            errorCode = "ERR_ACCOUNT_UNAPPROVED";
        }
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, errorCode, ex.getMessage(), request);
    }

    @ExceptionHandler({
            InvalidActivationTokenException.class,
            AccountAlreadyActivatedException.class,
            ActivationTokenExpiredException.class,
            InvalidResetTokenException.class
    })
    public ResponseEntity<ErrorResponse> handleTokenExceptions(Exception ex, WebRequest request) {
        String errorCode = "ERR_INVALID_TOKEN";
        if (ex instanceof AccountAlreadyActivatedException) {
            errorCode = "ERR_ALREADY_ACTIVATED";
        } else if (ex instanceof ActivationTokenExpiredException) {
            errorCode = "ERR_TOKEN_EXPIRED";
        }
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, errorCode, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, "ERR_DUPLICATE_RESOURCE", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "ERR_INTERNAL_SERVER_ERROR", "An unexpected error occurred.", request);
    }
}
