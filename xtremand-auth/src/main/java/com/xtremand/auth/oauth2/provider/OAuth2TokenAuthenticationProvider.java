package com.xtremand.auth.oauth2.provider;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import com.xtremand.auth.oauth2.constants.OAuth2CustomProperties;
import com.xtremand.auth.oauth2.repository.OAuth2ExtendedAuthorizationRepository;
import com.xtremand.auth.oauth2.token.OAuth2TokenGrantAuthenticationToken;

public final class OAuth2TokenAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private OAuth2ExtendedAuthorizationRepository oAuth2AuthorizationExtRepository;

	private final RegisteredClientRepository registeredClientRepository;
	private final OAuth2AuthorizationService authorizationService;
	private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

	public OAuth2TokenAuthenticationProvider(RegisteredClientRepository registeredClientRepository,
			OAuth2AuthorizationService authorizationService,
			OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {

		this.registeredClientRepository = registeredClientRepository;
		this.authorizationService = authorizationService;
		this.tokenGenerator = tokenGenerator;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		OAuth2TokenGrantAuthenticationToken tokenRequest = (OAuth2TokenGrantAuthenticationToken) authentication;

		if (!ClientAuthenticationMethod.NONE.equals(tokenRequest.getClientAuthenticationMethod())) {
			return null;
		}

		String clientId = tokenRequest.getPrincipal().toString();
		String refreshTokenOrAuthorizationCode = tokenRequest.getRefreshTokenOrAuthorizationCode();
		String grantType = tokenRequest.getGrantType();

		RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
		if (registeredClient == null) {
			throw new OAuth2AuthenticationException(
					new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT, "invalid client", null));
		}

		if (!registeredClient.getClientAuthenticationMethods().contains(tokenRequest.getClientAuthenticationMethod())) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT,
					"authentication_method is not registered with the client", null));
		}

		OAuth2TokenType tokenType = switch (grantType) {
		case "refresh_token" -> OAuth2TokenType.REFRESH_TOKEN;
		case "authorization_code" -> new OAuth2TokenType("code");
		default -> throw new OAuth2AuthenticationException("Unsupported grant type: " + grantType);
		};

		OAuth2Authorization authorization = authorizationService.findByToken(refreshTokenOrAuthorizationCode,
				tokenType);
		if (authorization == null) {
			throw new OAuth2AuthenticationException(
					new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT, "invalid grant", null));
		}

		if (!authorization.getRegisteredClientId().equals(registeredClient.getId())) {
			throw new OAuth2AuthenticationException(
					new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT, "client mismatch", null));
		}

		OAuth2Authorization.Builder builder = OAuth2Authorization.from(authorization);
		if (tokenRequest.getDeviceId() != null)
			builder.attribute(OAuth2CustomProperties.DEVICE_ID, tokenRequest.getDeviceId());

		if (tokenRequest.getDeviceType() != null)
			builder.attribute(OAuth2CustomProperties.DEVICE_TYPE, tokenRequest.getDeviceType());

		if (tokenRequest.getIpAddress() != null)
			builder.attribute(OAuth2CustomProperties.IP_ADDRESS, tokenRequest.getIpAddress());

		if (tokenRequest.getInternetServiceProvider() != null)
			builder.attribute(OAuth2CustomProperties.INTERNET_SERVICE_PROVIDER,
					tokenRequest.getInternetServiceProvider());

		if (tokenRequest.getNetworkOrganization() != null)
			builder.attribute(OAuth2CustomProperties.NETWORK_ORGANIZATION, tokenRequest.getNetworkOrganization());

		if (tokenRequest.getCity() != null)
			builder.attribute(OAuth2CustomProperties.CITY, tokenRequest.getCity());

		if (tokenRequest.getRegionName() != null)
			builder.attribute(OAuth2CustomProperties.REGION_NAME, tokenRequest.getRegionName());

		if (tokenRequest.getCountry() != null)
			builder.attribute(OAuth2CustomProperties.COUNTRY, tokenRequest.getCountry());

		if (tokenRequest.getTimezone() != null)
			builder.attribute(OAuth2CustomProperties.TIME_ZONE, tokenRequest.getTimezone());

		if (tokenRequest.getZip() != null)
			builder.attribute(OAuth2CustomProperties.ZIP, tokenRequest.getZip());

		if (tokenRequest.getUserAgent() != null)
			builder.attribute(OAuth2CustomProperties.USER_AGENT, tokenRequest.getUserAgent());

		if (tokenRequest.getBrowser() != null)
			builder.attribute(OAuth2CustomProperties.BROWSER, tokenRequest.getBrowser());

		if (tokenRequest.getOperatingSystem() != null)
			builder.attribute(OAuth2CustomProperties.OPERATING_SYSTEM, tokenRequest.getOperatingSystem());

		if (tokenRequest.getAutonomousSystemNumber() != null) {
			builder.attribute(OAuth2CustomProperties.ASN, tokenRequest.getAutonomousSystemNumber());
		}

		if (tokenRequest.getLatitude() != null) {
			builder.attribute(OAuth2CustomProperties.LATITUDE, tokenRequest.getLatitude());
		}

		if (tokenRequest.getLongitude() != null) {
			builder.attribute(OAuth2CustomProperties.LONGITUDE, tokenRequest.getLongitude());
		}

		OAuth2TokenContext accessTokenContext = DefaultOAuth2TokenContext.builder().principal(authentication)
				.tokenType(OAuth2TokenType.ACCESS_TOKEN)
				.authorizationGrantType(new AuthorizationGrantType(tokenRequest.getGrantType()))
				.registeredClient(registeredClient).authorizedScopes(authorization.getAuthorizedScopes())
				.authorization(builder.build()).build();

		OAuth2Token token = tokenGenerator.generate(accessTokenContext);
		if (!(token instanceof OAuth2AccessToken accessToken)) {
			throw new OAuth2AuthenticationException("Failed to generate access token");
		}

		builder.accessToken(accessToken);
		OAuth2Authorization updatedAuthorization = builder.build();

		authorizationService.save(updatedAuthorization);

		oAuth2AuthorizationExtRepository.updateDeviceMetadata(updatedAuthorization.getId(), tokenRequest);

		return new OAuth2TokenGrantAuthenticationToken(registeredClient, tokenRequest.getDeviceId(),
				tokenRequest.getDeviceType(), accessToken.getTokenValue(), grantType, tokenRequest.getIpAddress(),
				tokenRequest.getInternetServiceProvider(), tokenRequest.getNetworkOrganization(),
				tokenRequest.getCity(), tokenRequest.getRegionName(), tokenRequest.getCountry(),
				tokenRequest.getTimezone(), tokenRequest.getZip(), tokenRequest.getUserAgent(),
				tokenRequest.getBrowser(), tokenRequest.getOperatingSystem(), tokenRequest.getLoginTime(),
				tokenRequest.getAutonomousSystemNumber(), tokenRequest.getLatitude(), tokenRequest.getLongitude());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuth2TokenGrantAuthenticationToken.class.isAssignableFrom(authentication);
	}

}