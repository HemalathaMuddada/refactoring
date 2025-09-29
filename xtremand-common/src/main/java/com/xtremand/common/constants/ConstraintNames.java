package com.xtremand.common.constants;

/**
 * Centralized constraint name constants used for mapping database constraint
 * violations to user-friendly messages.
 */
public class ConstraintNames {

	private ConstraintNames() {
		// Prevent instantiation
	}

	public static final String UNIQUE_COMPANY_PROFILE_NAME = "idx_xa_company_profile_name_unique";
	public static final String UNIQUE_EMAIL = "uq_xa_user_email"; // ✅ updated
	public static final String UNIQUE_USERNAME = "uq_xa_user_username"; // ✅ updated
	public static final String UNIQUE_ACCOUNT_NUMBER = "uq_account_number";
	public static final String UNIQUE_COMPANY_NAME = "uq_xa_company_normalized_name"; // ✅ NEW
        public static final String UNIQUE_SUPPORT_USER_COMPANY = "uq_support_user_company";

        // Foreign key validations for account creation
        public static final String FK_ACCOUNT_ROLE_KEY_INVALID = "fk_account_role_key_invalid";
        public static final String FK_ACCOUNT_MODULE_KEYS_INVALID = "fk_account_module_keys_invalid";

}