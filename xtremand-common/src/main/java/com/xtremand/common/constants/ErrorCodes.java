package com.xtremand.common.constants;

/**
 * Central location for API error code constants.
 */
public class ErrorCodes {

	private ErrorCodes() {
		// Prevent instantiation
	}

	public static final String DUPLICATE_MAPPING = "ERR_DUPLICATE_MAPPING";
	public static final String SUPPORT_USER_OWNERSHIP_CONFLICT = "ERR_SUPPORT_USER_OWNERSHIP_CONFLICT";
	public static final String DUPLICATE_EMAIL = "ERR_DUPLICATE_EMAIL";

	// Generic validation and input errors
	public static final String INVALID_INPUT = "ERR_INVALID_INPUT";
	public static final String INVALID_FILE = "ERR_INVALID_FILE";
	public static final String INVALID_FILE_TYPE = "ERR_INVALID_FILE_TYPE";
        public static final String FILE_TOO_LARGE = "ERR_FILE_TOO_LARGE";

        // Generic codes shared across modules
        public static final String VALIDATION = "ERR_VALIDATION";
        public static final String VALIDATION_UNKNOWN = "ERR_VALIDATION_UNKNOWN";
        public static final String INVALID_JSON = "ERR_INVALID_JSON";
        public static final String INVALID_UUID = "ERR_INVALID_UUID";
        public static final String INVALID_FIELD = "ERR_INVALID_FIELD";
        public static final String MISSING_PARAM = "ERR_MISSING_PARAM";
        public static final String MISSING_HEADER = "ERR_MISSING_HEADER";
        public static final String BUSINESS_RULE = "ERR_BUSINESS_RULE";
        public static final String ILLEGAL_ARGUMENT = "ERR_ILLEGAL_ARGUMENT";
        public static final String NOT_FOUND = "ERR_NOT_FOUND";
        public static final String INTERNAL_ERROR = "ERR_INTERNAL_ERROR";
        public static final String UNKNOWN = "ERR_UNKNOWN";

        // Authentication and authorization errors
	public static final String INVALID_TOKEN = "ERR_INVALID_TOKEN";
	public static final String INVALID_CREDENTIALS = "ERR_INVALID_CREDENTIALS";
	public static final String ACCESS_DENIED = "ERR_ACCESS_DENIED";
	public static final String INVALID_REFRESH_TOKEN = "ERR_INVALID_REFRESH_TOKEN";
	public static final String TOKEN_REFRESH = "ERR_TOKEN_REFRESH";
	public static final String TOKEN_GENERATION = "ERR_TOKEN_GENERATION";
	public static final String ACCOUNT_LOCKED = "ERR_ACCOUNT_LOCKED";
	public static final String IP_BLOCKED = "ERR_IP_BLOCKED";

        // Account management errors
        public static final String ACTIVATION_TOKEN = "ERR_ACTIVATION_TOKEN";
        public static final String INVALID_ACTIVATION_TOKEN = "ERR_INVALID_ACTIVATION_TOKEN";
        public static final String SUPERADMIN_EXISTS = "ERR_SUPERADMIN_EXISTS";

	// Common API errors
	public static final String DUPLICATE_RESOURCE = "ERR_DUPLICATE_RESOURCE";
        public static final String UNSUPPORTED_MEDIA_TYPE = "ERR_UNSUPPORTED_MEDIA_TYPE";
        public static final String INVALID_PATH_PARAM = "ERR_INVALID_PATH_PARAM";
        public static final String INVALID_JSON_FIELD = "ERR_INVALID_JSON_FIELD";
        public static final String MALFORMED_REQUEST_BODY = "ERR_MALFORMED_REQUEST_BODY";
        public static final String VALIDATION_FAILED = "ERR_VALIDATION_FAILED";
        public static final String DUPLICATE_COMPANY = "ERR_DUPLICATE_COMPANY";
        public static final String DUPLICATE_COMPANY_NAME = "ERR_DUPLICATE_COMPANY_NAME";
        public static final String DUPLICATE_USERNAME = "ERR_DUPLICATE_USERNAME";
        public static final String DUPLICATE_ACCOUNT = "ERR_DUPLICATE_ACCOUNT";
        public static final String DUPLICATE_ACCOUNT_NUMBER = "ERR_DUPLICATE_ACCOUNT_NUMBER";
        public static final String REQUEST_BODY_MISSING = "ERR_REQUEST_BODY_MISSING";
        public static final String MISSING_AWS_CONFIG = "ERR_MISSING_AWS_CONFIG";
        public static final String RESOURCE_NOT_FOUND = "ERR_RESOURCE_NOT_FOUND";
        public static final String EMAIL_NOT_FOUND = "ERR_EMAIL_NOT_FOUND";
        public static final String METHOD_NOT_ALLOWED = "ERR_METHOD_NOT_ALLOWED";
	public static final String UNAUTHORIZED_ACCESS = "ERR_UNAUTHORIZED_ACCESS";
	public static final String INTERNAL_SERVER = "ERR_INTERNAL_SERVER";
	public static final String ILLEGAL_STATE = "ERR_ILLEGAL_STATE";

	// Support assignment errors
	public static final String MAPPING_NOT_FOUND = "ERR_MAPPING_NOT_FOUND";
	public static final String USER_ALREADY_ASSIGNED = "ERR_USER_ALREADY_ASSIGNED";
	public static final String CANNOT_ASSIGN_SELF_COMPANY = "ERR_CANNOT_ASSIGN_SELF_COMPANY";
        public static final String DUPLICATE_SUPPORT_ASSIGNMENT = "ERR_DUPLICATE_SUPPORT_ASSIGNMENT";
        public static final String DUPLICATE_SUPPORT_USER_COMPANY = "ERR_DUPLICATE_SUPPORT_USER_COMPANY";
        public static final String INVALID_MODULE_KEYS = "ERR_INVALID_MODULE_KEYS";
        public static final String MODULE_NAME_FORMAT = "ERR_MODULE_NAME_FORMAT";
        public static final String MODULE_KEY_FORMAT = "ERR_MODULE_KEY_FORMAT";
        public static final String MODULE_ASSIGN_CONFLICT = "ERR_MODULE_ASSIGN_CONFLICT";
        public static final String MODULE_GROUP_HEADER_CONFLICT = "ERR_MODULE_GROUP_HEADER_CONFLICT";
        public static final String DUPLICATE_GROUP = "ERR_DUPLICATE_GROUP";
        public static final String GROUP_NOT_FOUND = "ERR_GROUP_NOT_FOUND";
        public static final String DUPLICATE_PRIVILEGE_KEY = "ERR_DUPLICATE_PRIVILEGE_KEY";
        public static final String PRIVILEGE_KEY_FORMAT = "ERR_PRIVILEGE_KEY_FORMAT";
}
