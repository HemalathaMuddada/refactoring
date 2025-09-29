package com.xtremand.auth.oauth2.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository.RegisteredClientRowMapper;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

@Component
public class JdbcClientRegistrationRepository implements ClientRegistrationRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RegisteredClientRepository registeredClientRepository;

    public JdbcClientRegistrationRepository(RegisteredClientRepository registeredClientRepository,
            JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {

        RegisteredClient rc = jdbcTemplate
                .query("SELECT * FROM oauth2_registered_client WHERE registration_id = ?",
                        new RegisteredClientRowMapper(), registrationId)
                .stream().findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "No RegisteredClient found for registrationId: " + registrationId));

        if (rc == null) {
            throw new IllegalArgumentException("No RegisteredClient found for: " + registrationId);
        }

        ProviderMetadata metadata = fetchProviderMetadata(registrationId);

        return ClientRegistration.withRegistrationId(registrationId).clientId(rc.getClientId())
                .clientSecret(rc.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(rc.getRedirectUris().iterator().next()).scope(rc.getScopes())
                .authorizationUri(metadata.authorizationUri).tokenUri(metadata.tokenUri)
                .userInfoUri(metadata.userInfoUri).userNameAttributeName(metadata.userNameAttribute)
                .clientName(rc.getClientName()).build();

    }

    private ProviderMetadata fetchProviderMetadata(String registrationId) {
        ProviderMetadata metadata = jdbcTemplate
                .query("SELECT * FROM oauth_provider_metadata WHERE registration_id = ?",
                        (rs, rowNum) -> new ProviderMetadata(rs.getString("authorization_uri"),
                                rs.getString("token_uri"), rs.getString("user_info_uri"),
                                rs.getString("user_name_attribute")),
                        registrationId)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No provider metadata found for: " + registrationId));
        return metadata;
    }

    private record ProviderMetadata(String authorizationUri, String tokenUri, String userInfoUri,
            String userNameAttribute) {
    }

    public RegisteredClient findRegisteredClientByRegistrationId(String registrationId) {
        // Get the real clientId from registrationId
        String clientId = jdbcTemplate
                .query("SELECT client_id FROM oauth2_registered_client WHERE registration_id = ?",
                        (rs, rowNum) -> rs.getString("client_id"), registrationId)
                .stream().findFirst().orElseThrow(
                        () -> new IllegalArgumentException("No client_id found for registrationId: " + registrationId));

        // Use Spring's built-in repo to get the full RegisteredClient
        return registeredClientRepository.findByClientId(clientId);
    }

}
