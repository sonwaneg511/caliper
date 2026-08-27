package com.caliper.utils.exception.customException;

import org.springframework.security.core.AuthenticationException;

public class EmailNotVerifiedException extends AuthenticationException {

	private static final long serialVersionUID = 1L;

	public EmailNotVerifiedException(String message) {
		super(message);
	}
}
