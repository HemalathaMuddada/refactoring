package com.xtremand.auth.oauth2.custom.token;

import java.time.Instant;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2DeviceCodeAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import com.xtremand.auth.oauth2.constants.OAuth2CustomProperties;

@Configuration
public class CustomTokenGenerator {
    @Bean
    OAuth2TokenGenerator<?> tokenGenerator() {
        CustomOAuth2AccessTokenGenerator accessTokenGenerator = new CustomOAuth2AccessTokenGenerator();
        CustomOAuth2RefreshTokenGenerator refreshTokenGenerator = new CustomOAuth2RefreshTokenGenerator();

        return new DelegatingOAuth2TokenGenerator(accessTokenGenerator, refreshTokenGenerator);
    }

    @Bean
    OAuth2TokenCustomizer<OAuth2TokenClaimsContext> tokenCustomizer() {
        return context -> {
            if (context.getTokenType().getValue().equals(OAuth2TokenType.ACCESS_TOKEN.getValue())) {
                OAuth2Authorization authorization = context.getAuthorization();
                OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();

                Instant refreshExpiresAt = (Instant) refreshToken.getMetadata().get("expires_at");
                if (refreshExpiresAt != null) {
                    context.getClaims().claim("refresh_expires_at", refreshExpiresAt.getEpochSecond());
                }
            }

            Authentication grantAuth = context.getAuthorizationGrant();
            Map<String, Object> additionalParams = new HashMap<>();

            if (grantAuth instanceof OAuth2AuthorizationCodeAuthenticationToken codeAuthToken) {
                additionalParams = codeAuthToken.getAdditionalParameters();
            } else if (grantAuth instanceof OAuth2RefreshTokenAuthenticationToken refreshTokenAuth) {
                additionalParams = refreshTokenAuth.getAdditionalParameters();
            } else if (grantAuth instanceof OAuth2ClientCredentialsAuthenticationToken clientCredsAuth) {
                additionalParams = clientCredsAuth.getAdditionalParameters();
            } else if (grantAuth instanceof OAuth2DeviceCodeAuthenticationToken deviceCodeAuth) {
                additionalParams = deviceCodeAuth.getAdditionalParameters();
            }

            String deviceId = (String) additionalParams.get(OAuth2CustomProperties.DEVICE_ID);
            if (deviceId != null) {
                context.getClaims().claim(OAuth2CustomProperties.DEVICE_ID, deviceId);
            }
        };
    }

}
