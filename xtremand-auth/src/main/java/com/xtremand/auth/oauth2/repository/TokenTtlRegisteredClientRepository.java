package com.xtremand.auth.oauth2.repository;

import java.time.Duration;

import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

/**
 * Wraps {@link JdbcRegisteredClientRepository} to enforce token TTL settings
 * for each returned {@link RegisteredClient}.
 */
public class TokenTtlRegisteredClientRepository implements RegisteredClientRepository {

	private final JdbcRegisteredClientRepository delegate;

	public TokenTtlRegisteredClientRepository(JdbcRegisteredClientRepository delegate) {
		this.delegate = delegate;
	}

	@Override
	public void save(RegisteredClient registeredClient) {
		delegate.save(registeredClient);
	}

	@Override
	public RegisteredClient findById(String id) {
		RegisteredClient rc = delegate.findById(id);
		return applyTtl(rc);
	}

	@Override
	public RegisteredClient findByClientId(String clientId) {
		RegisteredClient rc = delegate.findByClientId(clientId);
		return applyTtl(rc);
	}

	private RegisteredClient applyTtl(RegisteredClient rc) {
		if (rc == null) {
			return null;
		}
		TokenSettings tokenSettings = TokenSettings.builder()
				.settings(settings -> settings.putAll(rc.getTokenSettings().getSettings()))
				.accessTokenTimeToLive(Duration.ofDays(3)).refreshTokenTimeToLive(Duration.ofDays(30)).build();
		return RegisteredClient.from(rc).tokenSettings(tokenSettings).build();
	}
}
