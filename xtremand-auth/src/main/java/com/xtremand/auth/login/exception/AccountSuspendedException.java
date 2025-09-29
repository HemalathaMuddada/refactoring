package com.xtremand.auth.login.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountSuspendedException extends AuthenticationException {
    public AccountSuspendedException(String msg) {
        super(msg);
    }
}
