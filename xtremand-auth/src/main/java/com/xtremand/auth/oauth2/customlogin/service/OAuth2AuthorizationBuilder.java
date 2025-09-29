package com.xtremand.auth.oauth2.customlogin.service;

import java.util.Map;


import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;

import com.xtremand.auth.oauth2.constants.OAuth2CustomGrantType;
import com.xtremand.auth.userdetails.CustomUserDetails;
import com.xtremand.common.identity.AuthUserDto;
import com.xtremand.common.identity.UserLookupService;

@Service
public class OAuth2AuthorizationBuilder {

	private final UserLookupService userLookupService;

	public OAuth2AuthorizationBuilder(UserLookupService userLookupService) {
		this.userLookupService = userLookupService;
	}

	public OAuth2Authorization build(RegisteredClient client, Authentication principal, OAuth2AccessToken accessToken,
			OAuth2RefreshToken refreshToken, Map<String, Object> attributes) {
		Long userId = null;
		Object p = principal.getPrincipal();
		if (p instanceof CustomUserDetails cud) {
			userId = cud.getUserId();
		} else {
			AuthUserDto dto = userLookupService.findByEmail(principal.getName()).orElse(null);
			if (dto != null) {
				userId = dto.getId();
			}
		}

		if (userId != null) {
			attributes.put("user_id", userId);
		}
		return OAuth2Authorization.withRegisteredClient(client).principalName(principal.getName())
				.authorizationGrantType(OAuth2CustomGrantType.LOGIN).accessToken(accessToken).refreshToken(refreshToken)
				.attributes(attrs -> attrs.putAll(attributes)).authorizedScopes(client.getScopes()).build();
	}
}
