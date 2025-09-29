package com.xtremand.email.verification.rule;

public enum EmailValidationStatus {
	VALID, INVALID_SYNTAX, DOMAIN_NOT_FOUND, DISPOSABLE_EMAIL, SMTP_PING_FAILED, MAILBOX_NOT_FOUND, BLOCKED_DOMAIN
}
