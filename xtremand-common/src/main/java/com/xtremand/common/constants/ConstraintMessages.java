package com.xtremand.common.constants;

import java.util.Map;

public class ConstraintMessages {

	public static final Map<String, ConstraintFieldMessage> MAPPING = Map.of(
			ConstraintNames.UNIQUE_COMPANY_PROFILE_NAME,
                        new ConstraintFieldMessage("companyProfileName", "Company profile name already exists.",
                                        ErrorCodes.DUPLICATE_COMPANY),

			ConstraintNames.UNIQUE_COMPANY_NAME,
                        new ConstraintFieldMessage("companyName", "Company name already exists.", ErrorCodes.DUPLICATE_COMPANY_NAME),

			ConstraintNames.UNIQUE_EMAIL,
                        new ConstraintFieldMessage("emailAddress", "Email already registered.", ErrorCodes.DUPLICATE_EMAIL),

			ConstraintNames.UNIQUE_USERNAME,
                        new ConstraintFieldMessage("username", "Username already taken.", ErrorCodes.DUPLICATE_USERNAME),

			ConstraintNames.UNIQUE_ACCOUNT_NUMBER,
                        new ConstraintFieldMessage("accountNumber", "Account number must be unique.", ErrorCodes.DUPLICATE_ACCOUNT),

                        ConstraintNames.UNIQUE_SUPPORT_USER_COMPANY,
                        new ConstraintFieldMessage(
                                        "supportUserId",
                                        ErrorMessages.USER_ALREADY_ASSIGNED,
                                        ErrorCodes.DUPLICATE_SUPPORT_ASSIGNMENT));

	public record ConstraintFieldMessage(String field, String message, String errorCode) {
	}
}
