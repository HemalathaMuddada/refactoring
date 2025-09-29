package com.xtremand.auth.login.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class CustomLoginAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = -4097240398569737402L;

    private final String username;
    private final String password;

    public CustomLoginAuthenticationToken(String username, String password) {
        super(null);
        this.username = username;
        this.password = password;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
