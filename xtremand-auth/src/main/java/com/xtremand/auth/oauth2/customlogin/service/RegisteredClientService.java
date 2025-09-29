package com.xtremand.auth.oauth2.customlogin.service;

import java.util.Optional;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

@Service
public class RegisteredClientService {

	private final RegisteredClientRepository registeredClientRepository;

	public RegisteredClientService(RegisteredClientRepository repository) {
		this.registeredClientRepository = repository;
	}

	public RegisteredClient findByClientId(String clientId) {
		return registeredClientRepository.findByClientId(clientId);
	}

	public RegisteredClient findById(String id) {
		return Optional.ofNullable(registeredClientRepository.findById(id))
				.orElseThrow(() -> new IllegalArgumentException("Registered client not found: " + id));
	}

}
