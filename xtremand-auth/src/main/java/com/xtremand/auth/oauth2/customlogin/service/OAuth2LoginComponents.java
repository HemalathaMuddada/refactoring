package com.xtremand.auth.oauth2.customlogin.service;

import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.stereotype.Component;

import com.xtremand.auth.oauth2.repository.OAuth2ExtendedAuthorizationRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Component
@RequiredArgsConstructor
public class OAuth2LoginComponents {
	private final RegisteredClientService registeredClientService;
	private final OAuth2AttributeBuilder attributeBuilder;
	private final OAuth2TokenService tokenService;
	private final OAuth2AuthorizationBuilder authorizationBuilder;
	private final OAuth2AuthorizationService authorizationService;
	private final OAuth2ExtendedAuthorizationRepository oAuth2AuthorizationExtRepository;
	private final TokenResponseService tokenResponseService;
}
