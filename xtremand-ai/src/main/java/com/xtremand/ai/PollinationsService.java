package com.xtremand.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.ai.util.AiServiceUtil;
import com.xtremand.config.AiConfigService;
import com.xtremand.domain.dto.EmailRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class PollinationsService implements AiService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AiConfigService aiConfigService;

    public PollinationsService(AiConfigService aiConfigService) {
        this.aiConfigService = aiConfigService;
    }

    @Override
    public String generateEmailResponse(EmailRequest emailRequest) {
        String prompt = AiPromptBuilder.buildGenericPrompt(emailRequest);
        try {
            String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://text.pollinations.ai/" + encodedPrompt))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return AiServiceUtil.formatResponse(response.body());
            } else {
                return "{\"error\": \"Pollinations API returned error: " + response.statusCode() + "\"}";
            }
        } catch (IOException | InterruptedException e) {
            return "{\"error\": \"Exception: " + e.getMessage() + "\"}";
        }
    }
}
