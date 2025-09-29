package com.xtremand.email.verification.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.domain.entity.XtremandResponse;
import com.xtremand.email.verification.dto.EmailVerifierInput;
import com.xtremand.email.verification.dto.EmailVerifierOutput;
import com.xtremand.email.verification.service.EmailVerificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/email-verifier")
@Tag(name = "Email verifier", description = "Operations related to email verification.")
public class EmailVerificationController {

	private final EmailVerificationService emailVerificationService;

	public EmailVerificationController(EmailVerificationService emailVerificationService) {
		this.emailVerificationService = emailVerificationService;
	}

	@PostMapping
	@Operation(summary = "Verify email", responses = {
			@ApiResponse(responseCode = "200", description = "Found the user"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public XtremandResponse<EmailVerifierOutput> validateEmail(@Valid @RequestBody EmailVerifierInput input) {
		return emailVerificationService.verify(input);
	}

}
