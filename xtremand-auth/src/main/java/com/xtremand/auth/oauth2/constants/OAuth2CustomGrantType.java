package com.xtremand.auth.oauth2.constants;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

public class OAuth2CustomGrantType {
	public static final AuthorizationGrantType LOGIN = new AuthorizationGrantType("custom_login");

	private OAuth2CustomGrantType() {

	}
}
