package com.xtremand.config.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.xtremand.config.MailConfigService;
import com.xtremand.domain.dto.MailConfigInput;
import com.xtremand.domain.dto.OAuthTokenResponse;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.enums.EmailConfigType;
import com.xtremand.user.repository.UserRepository;
import com.xtremand.auth.oauth2.service.AuthenticationFacade;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/social")
public class OutlookOAuthController {

	@Value("${microsoft.client.id}")
	private String outlookClientId;

	@Value("${microsoft.client.secret}")
	private String outlookClientSecret;

	@Value("${outlook.oauth.redirect-uri}")
	private String outlookRedirectUri;

	private final WebClient webClient = WebClient.create();

	private final MailConfigService mailConfigService;

	private final AuthenticationFacade authenticationFacade;

	private final UserRepository userRepository;

	public OutlookOAuthController(MailConfigService mailConfigService, AuthenticationFacade authenticationFacade,
			UserRepository userRepository) {
		this.mailConfigService = mailConfigService;
		this.authenticationFacade = authenticationFacade;
		this.userRepository = userRepository;
	}

	// Redirect to Outlook authorization endpoint
	@PostMapping("/connect/outlook")
	public ResponseEntity<Map<String, String>> redirectToOutlookOAuth(@RequestBody List<String> importantMails) {
		Authentication authentication = authenticationFacade.getAuthentication();
		String username = authentication.getName();

		String importantMailsJoined = String.join(";", importantMails);
		String combinedState = username + "|" + importantMailsJoined;
		String encodedState = URLEncoder.encode(combinedState, StandardCharsets.UTF_8);

		String url = "https://login.live.com/oauth20_authorize.srf" + "?client_id="
				+ URLEncoder.encode(outlookClientId, StandardCharsets.UTF_8) + "&response_type=code" + "&redirect_uri="
				+ URLEncoder.encode(outlookRedirectUri, StandardCharsets.UTF_8) + "&scope="
				+ URLEncoder.encode("wl.signin wl.offline_access wl.imap wl.basic wl.emails wl.contacts_emails",
						StandardCharsets.UTF_8)
				+ "&state=" + encodedState + "&prompt=select_account";

		Map<String, String> response = new HashMap<String, String>();
        response.put("authUrl", url);
        return ResponseEntity.ok(response);
	}

	// Outlook OAuth callback
	@GetMapping("/oauth2/callback/outlook")
	public Mono<String> handleOutlookCallback(@RequestParam("code") String code, @RequestParam("state") String state) {
		try {
			String decodedState = URLDecoder.decode(state, StandardCharsets.UTF_8);
			String[] parts = decodedState.split("\\|", 2);
			if (parts.length == 0) {
				return Mono.error(new RuntimeException("Invalid state parameter"));
			}
			String username = parts[0];
			List<String> importantMails = (parts.length > 1 && !parts[1].isEmpty()) ? Arrays.asList(parts[1].split(";"))
					: Collections.emptyList();

			User user = userRepository.fetchByUsername(username);
			if (user == null) {
				return Mono.error(new RuntimeException("User not found for username: " + username));
			}

			return exchangeCodeForTokens(code)
					.flatMap(tokens -> fetchUserEmail(tokens.getAccessToken()).flatMap(email -> {
						MailConfigInput input = new MailConfigInput();
						input.setEmail(email);
						input.setConfigType(EmailConfigType.OUTLOOK);
						input.setOauthToken(tokens.getAccessToken());
						input.setOauthRefreshToken(tokens.getRefreshToken());
						input.setTokenExpiry(LocalDateTime.now().plusDays(30));
						input.setUsername(email);
						input.setCreatedBy(user);
						input.setImportantEmail(importantMails);
						return Mono.fromCallable(() -> mailConfigService.saveOrUpdateMailConfig(input, null, user))
								.subscribeOn(Schedulers.boundedElastic())
								.thenReturn("Outlook account " + email + " connected and saved successfully!");
					}));
		} catch (Exception e) {
			e.printStackTrace();
			return Mono.error(new RuntimeException("Error processing Outlook OAuth callback: " + e.getMessage(), e));
		}
	}

	// Exchange auth code for tokens with Outlook token endpoint
	private Mono<OAuthTokenResponse> exchangeCodeForTokens(String code) {
		return webClient.post().uri("https://login.live.com/oauth20_token.srf")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(BodyInserters.fromFormData("client_id", outlookClientId)
						.with("client_secret", outlookClientSecret).with("code", code)
						.with("redirect_uri", outlookRedirectUri).with("grant_type", "authorization_code")
						.with("scope", "wl.signin wl.offline_access wl.imap wl.basic wl.emails wl.contacts_emails"))
				.retrieve()
				.onStatus(status -> !status.is2xxSuccessful(),
						response -> response.bodyToMono(String.class)
								.flatMap(body -> Mono.error(new RuntimeException("Failed Outlook token exchange: HTTP "
										+ response.statusCode() + " Response: " + body))))
				.bodyToMono(OAuthTokenResponse.class);
	}

	// Fetch user email from Outlook API
	private Mono<String> fetchUserEmail(String accessToken) {
		return webClient.get().uri("https://apis.live.net/v5.0/me")
			    .headers(headers -> headers.setBearerAuth(accessToken))
			    .retrieve()
			    .bodyToMono(Object.class)
			    .map(userInfo -> {
			        @SuppressWarnings("unchecked")
			        var map = (java.util.Map<String, Object>) userInfo;

			        Object emailsObj = map.get("emails");
			        if (emailsObj instanceof java.util.Map) {
			            @SuppressWarnings("unchecked")
			            var emailsMap = (java.util.Map<String,Object>) emailsObj;
			            // For example, get preferred email
			            Object preferredEmail = emailsMap.get("preferred");
			            if (preferredEmail instanceof String) {
			                return (String) preferredEmail;
			            }
			        }
			        // fallback or error
			        throw new RuntimeException("Email not found in Outlook response");
			    });

	}
}
