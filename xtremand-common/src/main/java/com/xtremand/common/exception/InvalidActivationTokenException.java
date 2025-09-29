package com.xtremand.common.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidActivationTokenException extends AuthenticationException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8749200365691604665L;

	public InvalidActivationTokenException(String msg) {
        super(msg);
    }
}
