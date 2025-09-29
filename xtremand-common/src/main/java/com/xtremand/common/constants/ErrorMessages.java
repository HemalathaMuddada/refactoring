package com.xtremand.common.constants;

/**
 * Shared error message constants used across modules.
 */
public class ErrorMessages {
	private ErrorMessages() {
	}

	public static final String DUPLICATE_MAPPING = "Support user is already assigned to this company. Duplicate mappings are not allowed.";
	public static final String USER_ALREADY_ASSIGNED = "Support user is already assigned to this company.";
	public static final String CANNOT_ASSIGN_SELF_COMPANY = "A user cannot be assigned as a support user for their own company.";
}
