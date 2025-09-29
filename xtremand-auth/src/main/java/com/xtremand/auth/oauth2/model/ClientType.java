package com.xtremand.auth.oauth2.model;

public enum ClientType {

	EXTERNAL_LOGIN("Third-party OAuth provider like GitHub, Google, etc."),
	PUBLIC("Public browser-based app using PKCE (no client secret) (e.g. Angular SPA with PKCE)"),
	CONFIDENTIAL_INTERNAL("Internal web apps or UIs with secrets (authorization_code flow)"),
	SERVICE("Backend service/machine-to-machine clients using client_credentials"),
	TEST_TOOL("Used for testing, development, or temporary clients");

	private final String description;

	ClientType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
