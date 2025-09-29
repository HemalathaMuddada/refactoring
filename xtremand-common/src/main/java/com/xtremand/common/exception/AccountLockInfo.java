package com.xtremand.common.exception;

/**
 * Provides lockout details for authentication-related exceptions.
 */
public interface AccountLockInfo {
    boolean isUserLocked();
    boolean isIpLocked();
}
