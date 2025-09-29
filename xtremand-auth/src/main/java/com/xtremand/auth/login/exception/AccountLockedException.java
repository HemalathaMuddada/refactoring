package com.xtremand.auth.login.exception;

import com.xtremand.common.constants.ErrorCodes;

import com.xtremand.common.exception.AccountLockInfo;
import com.xtremand.common.exception.ApiException;
import com.xtremand.common.exception.SuppressDebugTrace;

/**
 * Thrown when a user account is temporarily locked due to excessive failed login attempts.
 */
public class AccountLockedException extends ApiException implements SuppressDebugTrace, AccountLockInfo {

    private static final long serialVersionUID = 1L;
    public static final String ERROR_CODE = ErrorCodes.ACCOUNT_LOCKED;
    public static final String DEFAULT_MESSAGE = "Too many failed login attempts. Please try again later.";

    private final boolean userLocked;
    private final boolean ipLocked;

    public AccountLockedException() {
        this(true, false);
    }

    public AccountLockedException(String message) {
        this(true, false, message);
    }

    public AccountLockedException(boolean userLocked, boolean ipLocked) {
        super(DEFAULT_MESSAGE, ERROR_CODE);
        this.userLocked = userLocked;
        this.ipLocked = ipLocked;
    }

    public AccountLockedException(boolean userLocked, boolean ipLocked, String message) {
        super(message, ERROR_CODE);
        this.userLocked = userLocked;
        this.ipLocked = ipLocked;
    }

    public boolean isUserLocked() {
        return userLocked;
    }

    public boolean isIpLocked() {
        return ipLocked;
    }
}
