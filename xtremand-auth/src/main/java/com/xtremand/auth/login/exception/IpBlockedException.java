package com.xtremand.auth.login.exception;

import com.xtremand.common.constants.ErrorCodes;

import com.xtremand.common.exception.ApiException;
import com.xtremand.common.exception.SuppressDebugTrace;

/**
 * Thrown when login attempts from an IP address exceed the configured threshold.
 */
public class IpBlockedException extends ApiException implements SuppressDebugTrace {

    private static final long serialVersionUID = 1L;
    public static final String ERROR_CODE = ErrorCodes.IP_BLOCKED;
    public static final String DEFAULT_MESSAGE = "Too many failed login attempts from this IP address.";

    public IpBlockedException() {
        super(DEFAULT_MESSAGE, ERROR_CODE);
    }
}
