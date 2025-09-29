package com.xtremand.email.verification.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.xtremand.domain.entity.XtremandResponse;
import com.xtremand.email.verification.dto.EmailVerifierInput;
import com.xtremand.email.verification.dto.EmailVerifierOutput;
import com.xtremand.email.verification.rule.EmailValidationStatus;
import com.xtremand.email.verification.rule.EmailValidator;
import com.xtremand.email.verification.rule.SmtpChecker;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EmailVerificationService {

	private final EmailValidator validator = new EmailValidator();
	private final SmtpChecker smtpChecker = new SmtpChecker();

	@Value("#{'${disposable.email.list}'}")
	private final Set<String> disposableDomains;
	private final Set<String> allowlist;
	@Value("#{'${disposable.email.block.list}'}")
	private final Set<String> blocklist;
	@Value("#{'${disposable.email.blocked.tld.list}'}")
	private final Set<String> blockedTlds;

	public EmailVerificationService(Set<String> disposableDomains, Set<String> allowlist, Set<String> blocklist,
			Set<String> blockedTlds) {
		this.disposableDomains = Collections.unmodifiableSet(new HashSet<>(disposableDomains));
		this.allowlist = Collections.unmodifiableSet(new HashSet<>(allowlist));
		this.blocklist = Collections.unmodifiableSet(new HashSet<>(blocklist));
		this.blockedTlds = Collections.unmodifiableSet(new HashSet<>(blockedTlds));
	}

	public XtremandResponse<EmailVerifierOutput> verify(EmailVerifierInput input) {
		Set<String> emailSet = input.getEmailSet();
		for (String email : emailSet) {
			String masked = EmailValidator.maskEmail(email);
			System.out.println("Verifying " + masked);
			if (!validator.isValidSyntax(email))
				return new XtremandResponse<>(200, null,
						new EmailVerifierOutput(email, EmailValidationStatus.INVALID_SYNTAX, "Invalid syntax"));
			String domain = validator.extractDomain(email);
			String tld = domain.contains(".") ? domain.substring(domain.lastIndexOf('.') + 1) : "";
			if (!allowlist.isEmpty() && !allowlist.contains(domain))
				return new XtremandResponse<>(200, null, new EmailVerifierOutput(email,
						EmailValidationStatus.BLOCKED_DOMAIN, "Domain not in allowlist"));
			if (blocklist.contains(domain) || blockedTlds.contains(tld))
				return new XtremandResponse<>(200, null,
						new EmailVerifierOutput(email, EmailValidationStatus.BLOCKED_DOMAIN, "Domain or TLD blocked"));
			String mxRecord = validator.domainHasMxOrARecord(domain);
			if (!StringUtils.hasText(mxRecord))
				return new XtremandResponse<>(200, null,
						new EmailVerifierOutput(email, EmailValidationStatus.DOMAIN_NOT_FOUND, "No MX or A record"));
			if (disposableDomains.contains(domain))
				return new XtremandResponse<>(200, null, new EmailVerifierOutput(email,
						EmailValidationStatus.DISPOSABLE_EMAIL, "Disposable email domain"));
			EmailValidationStatus smtp = smtpChecker.ping(email, mxRecord);
			if (smtp != EmailValidationStatus.VALID)
				return new XtremandResponse<>(200, null,
						new EmailVerifierOutput(email, smtp, "SMTP verification failed"));
			return new XtremandResponse<>(200, null,
					new EmailVerifierOutput(email, EmailValidationStatus.VALID, "Email is valid"));
		}
		return null;
	}
}
