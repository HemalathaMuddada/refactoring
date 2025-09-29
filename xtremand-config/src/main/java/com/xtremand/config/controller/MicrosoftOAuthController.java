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
public class MicrosoftOAuthController {

    @Value("${microsoft.client.id}")
    private String microsoftClientId;

    @Value("${microsoft.client.secret}")
    private String microsoftClientSecret;

    @Value("${microsoft.oauth.redirect-uri}")
    private String microsoftRedirectUri;

    private final WebClient webClient = WebClient.create();

    private final MailConfigService mailConfigService;

    private final AuthenticationFacade authenticationFacade;

    private final UserRepository userRepository;

    public MicrosoftOAuthController(MailConfigService mailConfigService, AuthenticationFacade authenticationFacade, UserRepository userRepository) {
        this.mailConfigService = mailConfigService;
        this.authenticationFacade = authenticationFacade;
        this.userRepository = userRepository;
    }

    // Redirect to Microsoft authorization endpoint
    @PostMapping("/connect/microsoft")
    public ResponseEntity<Map<String, String>> redirectToMicrosoftOAuth(@RequestBody List<String> importantMails) {
        Authentication authentication = authenticationFacade.getAuthentication();
        String username = authentication.getName();

        // Combine username and important mails as single string
        String importantMailsJoined = String.join(";", importantMails);
        String combinedState = username + "|" + importantMailsJoined;
        String encodedState = URLEncoder.encode(combinedState, StandardCharsets.UTF_8);

        String url = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize" +
                "?client_id=" + URLEncoder.encode(microsoftClientId, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&redirect_uri=" + URLEncoder.encode(microsoftRedirectUri, StandardCharsets.UTF_8) +
                "&response_mode=query" +
                "&scope=" + URLEncoder.encode("https://graph.microsoft.com/Mail.ReadWrite offline_access user.read", StandardCharsets.UTF_8) +
                "&state=" + encodedState +
                "&prompt=select_account";

        Map<String, String> response = new HashMap<String, String>();
        response.put("authUrl", url);
        return ResponseEntity.ok(response);
    }

    // OAuth2 callback from Microsoft
    @GetMapping("/oauth2/callback/microsoft")
    public Mono<String> handleMicrosoftCallback(@RequestParam("code") String code, @RequestParam("state") String state) {
        try {
            String decodedState = URLDecoder.decode(state, StandardCharsets.UTF_8);
            String[] parts = decodedState.split("\\|", 2);
            if (parts.length == 0) {
                return Mono.error(new RuntimeException("Invalid state parameter"));
            }
            String username = parts[0];
            List<String> importantMails = (parts.length > 1 && !parts[1].isEmpty())
                    ? Arrays.asList(parts[1].split(";"))
                    : Collections.emptyList();

            System.out.println("OAuth callback called with code=" + code + " and state=" + state);
            System.out.println("Parsed username = " + username);
            System.out.println("Important mails = " + importantMails);

            User user = userRepository.fetchByUsername(username);
            if (user == null) {
                return Mono.error(new RuntimeException("User not found for username: " + username));
            }

            return exchangeCodeForTokens(code)
                    .flatMap(tokens -> fetchUserEmail(tokens.getAccessToken())
                            .flatMap(email -> {
                                MailConfigInput input = new MailConfigInput();
                                input.setEmail(email);
                                input.setConfigType(EmailConfigType.OFFICE);
                                input.setOauthToken(tokens.getAccessToken());
                                input.setOauthRefreshToken(tokens.getRefreshToken());
                                input.setTokenExpiry(LocalDateTime.now().plusDays(30));
                                input.setUsername(email);
                                input.setCreatedBy(user);
                                input.setImportantEmail(importantMails);

                                return Mono.fromCallable(() -> mailConfigService.saveOrUpdateMailConfig(input, null, user))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .thenReturn("Microsoft Office 365 account " + email + " connected and saved successfully!");
                            }));
        } catch (Exception e) {
            e.printStackTrace();
            return Mono.error(new RuntimeException("Error processing OAuth callback: " + e.getMessage(), e));
        }
    }

    // Exchange auth code for tokens
    private Mono<OAuthTokenResponse> exchangeCodeForTokens(String code) {
        return webClient.post()
                .uri("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", microsoftClientId)
                        .with("scope", "https://graph.microsoft.com/Mail.ReadWrite offline_access user.read")
                        .with("code", code)
                        .with("redirect_uri", microsoftRedirectUri)
                        .with("grant_type", "authorization_code")
                        .with("client_secret", microsoftClientSecret))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response ->
                        response.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new RuntimeException(
                                        "Failed token exchange: HTTP " + response.statusCode() + " Response: " + body))))
                .bodyToMono(OAuthTokenResponse.class);
    }

    // Fetch user email from Microsoft Graph API
    private Mono<String> fetchUserEmail(String accessToken) {
        return webClient.get()
                .uri("https://graph.microsoft.com/v1.0/me")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Object.class)
                .map(userInfo -> {
                    @SuppressWarnings("unchecked")
                    var map = (java.util.Map<String, Object>) userInfo;
                    return (String) map.get("userPrincipalName"); // userPrincipalName is the email
                });
    }
}
