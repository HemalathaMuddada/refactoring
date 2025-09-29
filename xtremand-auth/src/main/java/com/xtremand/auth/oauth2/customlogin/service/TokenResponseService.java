package com.xtremand.auth.oauth2.customlogin.service;

import java.time.Duration;

import java.time.Instant;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;

import com.xtremand.auth.login.dto.TokenResponse;

@Service
public class TokenResponseService {

	public TokenResponse build(OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken) {
		long expiresIn = Duration.between(Instant.now(), accessToken.getExpiresAt()).getSeconds();
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setAccessToken(accessToken.getTokenValue());
		tokenResponse.setRefreshToken(refreshToken.getTokenValue());
		tokenResponse.setTokenType(accessToken.getTokenType().getValue());
		tokenResponse.setExpiresIn(expiresIn);
		return tokenResponse;
	}

	public TokenResponse getTokenDetails(OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken) {
		long expiresIn = Duration.between(Instant.now(), accessToken.getExpiresAt()).getSeconds();
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setAccessToken(accessToken.getTokenValue());
		tokenResponse.setRefreshToken(refreshToken.getTokenValue());
		tokenResponse.setExpiresIn(expiresIn);
		tokenResponse.setTokenType("Bearer");
		return tokenResponse;
	}

}
