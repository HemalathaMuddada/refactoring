package com.xtremand.auth.oauth2.handler;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.auth.oauth2.constants.OAuth2CustomGrantType;
import com.xtremand.auth.oauth2.repository.JdbcClientRegistrationRepository;
import com.xtremand.auth.storage.TemporaryAuthStorage;
import com.xtremand.auth.util.TokenUtils;
import com.xtremand.common.identity.ExternalIdentityUserService;
import com.xtremand.common.identity.ExternalUserDto;
import com.xtremand.common.identity.UserLookupService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final ExternalIdentityUserService externalIdentityUserService;
	private final OAuth2AuthorizationService authorizationService;

	private final TemporaryAuthStorage temporaryAuthStorage;

	private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

	private final JdbcClientRegistrationRepository jdbcClientRegistrationRepository;

	private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

	public CustomOAuth2SuccessHandler(ExternalIdentityUserService externalIdentityUserService,
			OAuth2AuthorizationService authorizationService, TemporaryAuthStorage temporaryAuthStorage,
			JdbcClientRegistrationRepository jdbcClientRegistrationRepository,
			OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
			OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
		this.externalIdentityUserService = externalIdentityUserService;
		this.authorizationService = authorizationService;
		this.temporaryAuthStorage = temporaryAuthStorage;
		this.jdbcClientRegistrationRepository = jdbcClientRegistrationRepository;
		this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
		this.tokenGenerator = tokenGenerator;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {

		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
		String registrationId = oauthToken.getAuthorizedClientRegistrationId(); // e.g. "github"

		OAuth2User oauthUser = oauthToken.getPrincipal();

		// 1. Extract GitHub user info
		String username = oauthUser.getAttribute("login");
		String email = oauthUser.getAttribute("email");

		ExternalUserDto user = externalIdentityUserService.findOrCreateUserByExternalLogin(username, email);

		// 2. Get GitHub access token
		OAuth2AuthorizedClient client = oAuth2AuthorizedClientService.loadAuthorizedClient(registrationId,
				oauthToken.getName());

		if (client == null) {
			throw new IllegalStateException("Authorized client not found for registrationId: " + registrationId);
		}

		String githubAccessToken = client.getAccessToken().getTokenValue();

		// 3. Get GitHub profile (via REST API)
		JsonNode githubProfile = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			String decodedToken = TokenUtils.decodeHexToken(githubAccessToken);
			headers.setBearerAuth(decodedToken);
			HttpEntity<Void> entity = new HttpEntity<>(headers);

			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> githubResponse = restTemplate.exchange("https://api.github.com/user", HttpMethod.GET,
					entity, String.class);

			githubProfile = new ObjectMapper().readTree(githubResponse.getBody());
		} catch (Exception e) {
			e.printStackTrace(); // Optional: handle error
		}

		RegisteredClient registeredClient = jdbcClientRegistrationRepository
				.findRegisteredClientByRegistrationId(registrationId);

		if (registeredClient == null) {
			throw new IllegalStateException("Client not registered: " + registrationId);
		}

		OAuth2TokenContext accessTokenContext = DefaultOAuth2TokenContext.builder().principal(authentication)
				.tokenType(OAuth2TokenType.ACCESS_TOKEN)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).registeredClient(registeredClient)
				.authorizedScopes(registeredClient.getScopes()).build();

		OAuth2AccessToken accessToken = (OAuth2AccessToken) tokenGenerator.generate(accessTokenContext);

		OAuth2RefreshToken refreshToken = getRefreshToken(authentication, registeredClient);
		if (refreshToken == null) {
			throw new IllegalStateException("Refresh Token Not Found");
		}

		Set<String> scopes = new HashSet<>();
		scopes.add("read");
		scopes.add("write");
		scopes.add("trust");

		Map<String, Object> customAttributes = new HashMap<>();
		customAttributes.put("user_id", user.getId());

		OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
				.principalName(user.getUsername()).authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizedScopes(scopes).accessToken(accessToken).refreshToken(refreshToken)
				.attributes(a -> a.putAll(customAttributes)).attribute(Principal.class.getName(), authentication)
				.build();

		authorizationService.save(authorization);

		String tokenKey = UUID.randomUUID().toString();
		temporaryAuthStorage.save(tokenKey, Map.of("access_token", accessToken, "refresh_token", refreshToken,
				"github_profile", githubProfile, "username", user.getUsername()));

		System.err.println("✅ Stored in tempAuthStorage for key=" + tokenKey);

		// 6. Redirect to Angular with key
		response.sendRedirect("http://localhost:4200/callback?key=" + tokenKey);
	}

	private OAuth2RefreshToken getRefreshToken(Authentication authResult, RegisteredClient registeredClient) {
		OAuth2TokenContext refreshTokenContext = DefaultOAuth2TokenContext.builder().principal(authResult)
				.tokenType(OAuth2TokenType.REFRESH_TOKEN).authorizationGrantType(OAuth2CustomGrantType.LOGIN)
				.registeredClient(registeredClient).authorizedScopes(Set.of("read", "write")).build();

		return (OAuth2RefreshToken) tokenGenerator.generate(refreshTokenContext);
	}

}
