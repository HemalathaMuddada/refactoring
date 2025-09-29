package com.xtremand.common.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountAlreadyActivatedException extends AuthenticationException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1023190604554041572L;

	public AccountAlreadyActivatedException(String msg) {
        super(msg);
    }
}
