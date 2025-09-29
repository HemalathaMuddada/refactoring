package com.xtremand.auth.login.provider;

import org.springframework.security.authentication.AuthenticationProvider;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import com.xtremand.auth.login.token.CustomLoginRefreshTokenAuthentication;
import com.xtremand.auth.oauth2.token.OAuth2TokenGrantAuthenticationToken;

public class CustomLoginRefreshTokenGrantProvider implements AuthenticationProvider {

    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private final RegisteredClientRepository registeredClientRepository;

    public CustomLoginRefreshTokenGrantProvider(OAuth2AuthorizationService authorizationService,
            OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
            RegisteredClientRepository registeredClientRepository) {
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        CustomLoginRefreshTokenAuthentication auth = (CustomLoginRefreshTokenAuthentication) authentication;

        String refreshTokenValue = auth.getRefreshToken();
        OAuth2Authorization authorization = authorizationService.findByToken(refreshTokenValue,
                OAuth2TokenType.REFRESH_TOKEN);

        if (authorization == null) {
            throw new OAuth2AuthenticationException("Invalid refresh token");
        }

        String registeredClientId = authorization.getRegisteredClientId();
        RegisteredClient registeredClient = registeredClientRepository.findById(registeredClientId);

        // Build new access token
        OAuth2TokenContext context = DefaultOAuth2TokenContext.builder().authorization(authorization)
                .registeredClient(registeredClient)
                .principal(new UsernamePasswordAuthenticationToken(authorization.getPrincipalName(), null))
                .tokenType(OAuth2TokenType.ACCESS_TOKEN).authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .build();

        OAuth2AccessToken newAccessToken = (OAuth2AccessToken) tokenGenerator.generate(context);

        // (Optional) generate a new refresh token too
        // Save updated authorization
        OAuth2Authorization updatedAuth = OAuth2Authorization.from(authorization).accessToken(newAccessToken).build();

        authorizationService.save(updatedAuth);

        return new OAuth2AccessTokenAuthenticationToken(registeredClient,
                new UsernamePasswordAuthenticationToken(authorization.getPrincipalName(), null), newAccessToken);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2TokenGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
