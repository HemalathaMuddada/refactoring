package com.xtremand.common.exception;

import com.xtremand.common.dto.Rfc7807ErrorResponse;

/**
 * Runtime wrapper used to ensure Spring transactions rollback when
 * validation errors are mapped from database constraints.
 */
public class RollbackValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Rfc7807ErrorResponse errorResponse;

    public RollbackValidationException(Rfc7807ErrorResponse errorResponse) {
        super(errorResponse.getDetail());
        this.errorResponse = errorResponse;
    }

    public Rfc7807ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
