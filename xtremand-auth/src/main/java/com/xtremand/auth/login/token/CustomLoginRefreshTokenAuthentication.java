package com.xtremand.auth.login.token;

import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

public class CustomLoginRefreshTokenAuthentication extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 7369231129943867116L;
    private final String clientId;
    private final String refreshToken;
    private final ClientAuthenticationMethod clientAuthenticationMethod;

    private final RegisteredClient registeredClient; // set after authentication

    public CustomLoginRefreshTokenAuthentication(String clientId, String refreshToken) {
        super(Collections.emptyList());
        this.clientId = clientId;
        this.refreshToken = refreshToken;
        this.clientAuthenticationMethod = ClientAuthenticationMethod.NONE;
        this.registeredClient = null;
        setAuthenticated(false);
    }

    // Constructor used after successful authentication
    public CustomLoginRefreshTokenAuthentication(RegisteredClient registeredClient) {
        super(Collections.emptyList());
        this.clientId = registeredClient.getClientId();
        this.refreshToken = null;
        this.clientAuthenticationMethod = ClientAuthenticationMethod.NONE;
        this.registeredClient = registeredClient;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return refreshToken;
    }

    @Override
    public Object getPrincipal() {
        return clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public ClientAuthenticationMethod getClientAuthenticationMethod() {
        return clientAuthenticationMethod;
    }

    public RegisteredClient getRegisteredClient() {
        return registeredClient;
    }

    public AuthorizationGrantType getGrantType() {
        return AuthorizationGrantType.REFRESH_TOKEN;
    }

}