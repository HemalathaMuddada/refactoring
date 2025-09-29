package com.xtremand.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.config.AiConfigService;
import com.xtremand.domain.dto.EmailRequest;

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
                String responseBody = response.body();
                String subject = null;
                StringBuilder bodyBuilder = new StringBuilder();
                String[] lines = responseBody.split("\\r?\\n");
                for (String line : lines) {
                    if (line.toLowerCase().startsWith("subject:")) {
                        subject = line.substring("subject:".length()).trim();
                    } else {
                        bodyBuilder.append(line).append("\n");
                    }
                }
                if (subject == null) {
                    subject = "No Subject";
                }
                String body = bodyBuilder.toString().trim();

                Map<String, String> result = new LinkedHashMap<>();
                result.put("subject", subject);
                result.put("body", body);

                return objectMapper.writeValueAsString(result);
            } else {
                return "{\"error\": \"Pollinations API returned error: " + response.statusCode() + "\"}";
            }
        } catch (IOException | InterruptedException e) {
            return "{\"error\": \"Exception: " + e.getMessage() + "\"}";
        }
    }
}
