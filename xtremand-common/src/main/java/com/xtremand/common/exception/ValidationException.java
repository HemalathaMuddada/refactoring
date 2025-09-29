package com.xtremand.common.exception;

import java.util.List;
import java.util.Map;

public class ValidationException extends RuntimeException implements SuppressDebugTrace {
	private static final long serialVersionUID = 1L;

	private final String errorCode;
	private final transient Map<String, Object> details;

	public ValidationException(String errorCode, String message, String field) {
		super(message);
		this.errorCode = errorCode;
		this.details = Map.of("fields",
				List.of(Map.of("field", field, "errorMessage", message, "errorCode", errorCode)));
	}

	public ValidationException(String errorCode, String message, Map<String, Object> details) {
		super(message);
		this.errorCode = errorCode;
		this.details = details;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public Map<String, Object> getDetails() {
		return details;
	}
}
