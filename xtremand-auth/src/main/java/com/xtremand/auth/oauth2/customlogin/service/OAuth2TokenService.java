package com.xtremand.auth.oauth2.customlogin.service;

import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Service;

import com.xtremand.auth.oauth2.constants.OAuth2CustomGrantType;

@Service
public class OAuth2TokenService {

	private static final AuthorizationGrantType LOGIN = OAuth2CustomGrantType.LOGIN;

	private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

	public OAuth2TokenService(OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
		this.tokenGenerator = tokenGenerator;
	}

	public OAuth2AccessToken generateAccessToken(Authentication auth, RegisteredClient client) {
		OAuth2TokenContext context = DefaultOAuth2TokenContext.builder().principal(auth)
				.tokenType(OAuth2TokenType.ACCESS_TOKEN).authorizationGrantType(LOGIN).registeredClient(client)
				.authorizedScopes(Set.of("read", "write", "trust")).build();

		return (OAuth2AccessToken) tokenGenerator.generate(context);
	}

	public OAuth2RefreshToken generateRefreshToken(Authentication auth, RegisteredClient client) {
		OAuth2TokenContext context = DefaultOAuth2TokenContext.builder().principal(auth)
				.tokenType(OAuth2TokenType.REFRESH_TOKEN).authorizationGrantType(LOGIN).registeredClient(client)
				.build();

		return (OAuth2RefreshToken) tokenGenerator.generate(context);
	}

	public OAuth2AccessToken generateAccessTokenFromRefresh(Authentication principal, RegisteredClient client,
			OAuth2Authorization authorization) {
		OAuth2TokenContext context = DefaultOAuth2TokenContext.builder().principal(principal)
				.tokenType(OAuth2TokenType.ACCESS_TOKEN).authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.registeredClient(client).authorizedScopes(authorization.getAuthorizedScopes()).build();

		return (OAuth2AccessToken) tokenGenerator.generate(context);
	}

	public OAuth2RefreshToken generateRefreshTokenFromRefresh(Authentication principal, RegisteredClient client,
			OAuth2Authorization authorization) {
		OAuth2TokenContext context = DefaultOAuth2TokenContext.builder().principal(principal)
				.tokenType(OAuth2TokenType.REFRESH_TOKEN).authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.registeredClient(client).authorizedScopes(authorization.getAuthorizedScopes()).build();

		return (OAuth2RefreshToken) tokenGenerator.generate(context);
	}

}
