package com.xtremand.config.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.view.RedirectView;

import com.xtremand.auth.oauth2.service.AuthenticationFacade;
import com.xtremand.config.MailConfigService;
import com.xtremand.domain.dto.GoogleUserInfo;
import com.xtremand.domain.dto.MailConfigInput;
import com.xtremand.domain.dto.OAuthTokenResponse;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.enums.EmailConfigType;
import com.xtremand.user.repository.UserRepository;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/social")
public class GoogleOAuthController {

	@Value("${google.client.id}")
	private String googleClientId;

	@Value("${google.client.secret}")
	private String googleClientSecret;

	@Value("${google.oauth.redirect-uri}")
	private String googleRedirectUri;

	private final WebClient webClient = WebClient.create();

	private final MailConfigService mailConfigService;

	private final AuthenticationFacade authenticationFacade;

	private final UserRepository userRepository;

	@Autowired
	public GoogleOAuthController(MailConfigService mailConfigService, AuthenticationFacade authenticationFacade,
			UserRepository userRepository) {
		this.mailConfigService = mailConfigService;
		this.authenticationFacade = authenticationFacade;
		this.userRepository = userRepository;
	}

	// Redirect user to Google OAuth consent screen
	@PostMapping("/connect/google")
    public ResponseEntity<Map<String, String>> redirectToGoogleOAuth(@RequestBody List<String> importantMails) {
        Authentication authentication = authenticationFacade.getAuthentication();
        String username = authentication.getName();
        String importantMailsJoined = String.join(";", importantMails);  // use a separator unlikely in emails
        String combinedState = username + "|" + importantMailsJoined;
        String encodedState = URLEncoder.encode(combinedState, StandardCharsets.UTF_8);
        String joinedMails = String.join(",", importantMails);
        String encodedMails = URLEncoder.encode(joinedMails, StandardCharsets.UTF_8);
        String url = "https://accounts.google.com/o/oauth2/v2/auth" + "?client_id="
                + URLEncoder.encode(googleClientId, StandardCharsets.UTF_8) + "&redirect_uri="
                + URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8) + "&response_type=code" + "&scope="
                + URLEncoder.encode("https://mail.google.com/ https://www.googleapis.com/auth/userinfo.email",
                        StandardCharsets.UTF_8)
                + "&access_type=offline" + "&prompt=consent" + "&state=" + encodedState;

        Map<String, String> response = new HashMap<String, String>();
        response.put("authUrl", url);
        return ResponseEntity.ok(response);
    }

	@GetMapping("/oauth2/callback/google")
	public Mono<String> handleGoogleCallback(@RequestParam("code") String code, @RequestParam("state") String state) {
		String decodedState = URLDecoder.decode(state, StandardCharsets.UTF_8);
		String[] parts = decodedState.split("\\|", 2);
		String username = parts[0];
		User user = userRepository.fetchByUsername(username);
		List<String> importantMails = (parts.length > 1 && !parts[1].isEmpty())
		    ? Arrays.asList(parts[1].split(";"))
		    : Collections.emptyList();
		System.out.println("Callback with code = " + code + ", state = " + state);
		if (user == null) {
			return Mono.error(new RuntimeException("User not found for username: " + username));
		}
		return exchangeCodeForTokens(code).flatMap(tokens -> fetchUserEmail(tokens.getAccessToken()).flatMap(email -> {
			MailConfigInput input = new MailConfigInput();
			input.setEmail(email);
			input.setConfigType(EmailConfigType.OAUTH_CONFIG);
			input.setOauthToken(tokens.getAccessToken());
			input.setOauthRefreshToken(tokens.getRefreshToken());
			input.setTokenExpiry(LocalDateTime.now().plusDays(30));
			input.setUsername(email);
			input.setCreatedBy(user);
			input.setImportantEmail(importantMails);
			return Mono.fromCallable(() -> mailConfigService.saveOrUpdateMailConfig(input, null, user))
					.subscribeOn(Schedulers.boundedElastic())
					.thenReturn("Google account " + email + " connected and saved successfully!");
		})).onErrorResume(e -> {
		    e.printStackTrace();  // Print full stack trace to logs
		    return Mono.error(new RuntimeException("Failed to connect Google account: " + e.getMessage(), e));
		});

	}

	// Exchange authorization code for tokens with error handling
	private Mono<OAuthTokenResponse> exchangeCodeForTokens(String code) {
		return webClient.post().uri("https://oauth2.googleapis.com/token")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(BodyInserters.fromFormData("code", code).with("client_id", googleClientId)
						.with("client_secret", googleClientSecret).with("redirect_uri", googleRedirectUri)
						.with("grant_type", "authorization_code"))
				.retrieve()
				.onStatus(status -> !status.is2xxSuccessful(), response -> response.bodyToMono(String.class).flatMap(
						body -> Mono.error(new RuntimeException("Failed to exchange code for tokens: " + body))))
				.bodyToMono(OAuthTokenResponse.class);
	}

	// Fetch user email from Google's userinfo endpoint
	private Mono<String> fetchUserEmail(String accessToken) {
		return webClient.get().uri("https://www.googleapis.com/oauth2/v2/userinfo")
				.headers(headers -> headers.setBearerAuth(accessToken)).retrieve().bodyToMono(GoogleUserInfo.class)
				.map(GoogleUserInfo::getEmail);
	}
}
