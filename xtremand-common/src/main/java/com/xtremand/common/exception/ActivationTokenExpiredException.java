package com.xtremand.common.exception;

import org.springframework.security.core.AuthenticationException;

public class ActivationTokenExpiredException extends AuthenticationException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1354074112383014841L;

	public ActivationTokenExpiredException(String msg) {
        super(msg);
    }
}
