package com.xtremand.auth.login.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountUnapprovedException extends AuthenticationException {
    public AccountUnapprovedException(String msg) {
        super(msg);
    }
}
