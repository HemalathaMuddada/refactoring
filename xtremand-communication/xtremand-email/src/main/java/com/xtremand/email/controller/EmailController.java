package com.xtremand.email.controller;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.xtremand.auth.oauth2.service.AuthenticationFacade;
import com.xtremand.domain.dto.*;
import com.xtremand.domain.entity.EmailAnalytics;
import com.xtremand.domain.enums.EmailConfigType;
import com.xtremand.email.service.EmailService;
import com.xtremand.email.service.EmailAnalyticsService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/emails")
@Tag(name = "Email Sending and Tracking", description = "APIs for sending emails and tracking their engagement")
public class EmailController {
	private final EmailService emailService;
	private final EmailAnalyticsService analyticsService;
	private final AuthenticationFacade authenticationFacade;

	public EmailController(EmailService emailService, EmailAnalyticsService analyticsService,
			AuthenticationFacade authenticationFacade) {
		this.emailService = emailService;
		this.analyticsService = analyticsService;
		this.authenticationFacade = authenticationFacade;
	}

	@Operation(summary = "Send a bulk email", description = "Sends an email to a list of contacts.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Mail sent successfully"),
			@ApiResponse(responseCode = "404", description = "Not Found - A contact was not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.xtremand.common.dto.ErrorResponse.class))) })
	@PostMapping("/send")
	public ResponseEntity<Response> sendEmail(@RequestBody SendEmailRequest request) {
		Authentication auth = authenticationFacade.getAuthentication();
		emailService.sendBulkEmail(request, null, auth);
		return ResponseEntity.ok(new Response("Mail sent successfully"));
	}

	// This endpoint may not need user context, but you can optionally log it for
	// auditing
	@Hidden
	@GetMapping("/track/click/{trackingId}")
	public void trackClick(@PathVariable UUID trackingId, @RequestParam("redirect") String redirect,
			HttpServletResponse response) throws IOException {
		analyticsService.logClicked(trackingId);
		response.sendRedirect(redirect);
	}

	@Operation(summary = "Get email analytics summary", description = "Retrieves a summary of email performance analytics.")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved analytics summary")
	@GetMapping("/analytics/summary")
	public ResponseEntity<EmailAnalyticsSummaryDto> getEmailAnalyticsSummary() {
		Authentication auth = authenticationFacade.getAuthentication();
		EmailAnalyticsSummaryDto summary = analyticsService.getEmailAnalyticsSummaryForUser(auth);
		return ResponseEntity.ok(summary);
	}

	@Operation(summary = "Get email history", description = "Retrieves a history of all sent emails.")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved email history")
	@GetMapping("/history")
	public ResponseEntity<List<EmailHistoryDto>> getEmailHistory() {
		Authentication auth = authenticationFacade.getAuthentication();
		return ResponseEntity.ok(analyticsService.getAllEmailHistoryForUser(auth));
	}

	@Operation(summary = "Email Preview", description = "Retrieves a history of sent email By its id.")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved email history")
	@GetMapping("/email-history/{id}")
	public ResponseEntity<EmailHistoryDto> getEmailHistoryById(@PathVariable Long id) {
		Authentication auth = authenticationFacade.getAuthentication();
		EmailHistoryDto dto = analyticsService.getEmailHistoryByIdForUser(id, auth);
		if (dto != null) {
			return ResponseEntity.ok(dto);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/sent-with-reply-chains/{id}")
	public List<SentEmailWithReplyFlagDto> getSentWithReplyChains(@PathVariable("id") Long id) {
		Authentication auth = authenticationFacade.getAuthentication();
		return analyticsService.getSentEmailsWithReplyStatusForUser(id, auth);
	}

	@DeleteMapping("/delete-mail")
	public ResponseEntity<Void> deleteMail(@RequestParam String messageId) throws Exception {
		Authentication auth = authenticationFacade.getAuthentication();
		emailService.deleteEmailByMessageId(auth,messageId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/reply-mail")
	public ResponseEntity<Void> replyMail(@RequestBody ReplyRequest request) throws Exception {
		Authentication auth = authenticationFacade.getAuthentication();
		emailService.replyToEmail(auth,request.getMessageId(), request.getReplyBody(),request.getConfigType());
		return ResponseEntity.ok().build();
	}

	@PostMapping("/star-mail")
	public ResponseEntity<Void> starMail(@RequestParam String messageId, @RequestParam boolean star) throws Exception {
		Authentication auth = authenticationFacade.getAuthentication();
		emailService.starEmail(auth,messageId, star);
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/importantEmails")
	@Operation(summary = "Fetch important emails filtered by target email")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Important emails fetched successfully"),
			@ApiResponse(responseCode = "404", description = "No important emails found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<List<SentEmailWithReplyFlagDto>> getImportantEmails(
			@RequestParam("targetEmail") String targetEmail, @RequestParam("configId") Long configId) {
		Authentication auth = authenticationFacade.getAuthentication();
		try {
			List<SentEmailWithReplyFlagDto> importantEmails = emailService.fetchImportantEmailsByTargetEmail(auth,
					targetEmail,configId);
			if (importantEmails.isEmpty()) {
				return ResponseEntity.status(404).body(Collections.emptyList());
			}
			return ResponseEntity.ok(importantEmails);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(Collections.emptyList());
		}
	}

	@GetMapping("/importantEmailsWithIds")
    @Operation(summary = "Fetch mail config IDs with their important email addresses for the authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Important emails fetched successfully"),
        @ApiResponse(responseCode = "404", description = "No important emails found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<MailConfigImportantEmailsDto>> getImportantEmailsWithIds( @RequestParam("configId") Long configId) {
        Authentication auth = authenticationFacade.getAuthentication();
        return emailService.fetchConfiguredEmailAddresses(auth, configId);
    }


}
