package com.xtremand.auth.login.service;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Service
@Profile({"dev","prod","qa","local"})
public class LockoutNotifierService {

    private final RestTemplate restTemplate;
    private final String webhookUrl;

    public LockoutNotifierService(@Value("${xamplify.security.webhook.lockout-url:}") String webhookUrl) {
        this.restTemplate = new RestTemplate();
        this.webhookUrl = webhookUrl;
    }

    public void notifyLockout(String username, String ip) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        Map<String, Object> payload = Map.of(
                "event", "account_locked",
                "username", username,
                "ip", ip,
                "timestamp", Instant.now().toString());
        try {
            restTemplate.postForEntity(webhookUrl, payload, Void.class);
        } catch (RestClientException ex) {
            // ignore failures to prevent login flow disruption
        }
    }
}
