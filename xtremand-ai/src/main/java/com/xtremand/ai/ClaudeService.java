package com.xtremand.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.config.AiConfigService;
import com.xtremand.domain.dto.EmailRequest;
import com.xtremand.domain.entity.AiConfig;
import com.xtremand.domain.enums.AiConfigType;

@Service
public class ClaudeService implements AiService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiConfigService aiConfigService;

    public ClaudeService(AiConfigService aiConfigService) {
        this.aiConfigService = aiConfigService;
    }

    @Override
    public String generateEmailResponse(EmailRequest emailRequest) {
        Optional<AiConfig> configOpt = aiConfigService.getAiConfig(emailRequest.getAiEmail(), AiConfigType.CLAUDE);
        if (configOpt.isEmpty()) {
            return "{\"error\": \"Claude API config not found for user.\"}";
        }
        AiConfig config = configOpt.get();
        String apiKey = decrypt(config.getApiKey());

        String prompt = AiPromptBuilder.buildGenericPrompt(emailRequest);;
        try {
        	Map<String, Object> requestMap = new HashMap<>();
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt); 

            messages.add(message);
            requestMap.put("model", "claude-3-5-sonnet-20241022"); 
            requestMap.put("max_tokens", 1000); 
            requestMap.put("messages", messages); 

            String requestBody = objectMapper.writeValueAsString(requestMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.anthropic.com/v1/messages"))
                    .header("Content-Type", "application/json")
                    .header("anthropic-version", "2023-06-01")
                    .header("x-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = objectMapper.readTree(response.body());
            if (root.has("content")) {
                JsonNode content = root.get("content");
                if (content.isArray() && content.size() > 0) {
                    return formatResponse(content.get(0).get("text").asText());
                }
            }
            return "{\"error\": \"No completion from Claude API\"}";

        } catch (IOException | InterruptedException e) {
            return "{\"error\": \"ClaudeService failed: " + e.getMessage() + "\"}";
        }
    }

    private String formatResponse(String responseText) {
        String[] parts = responseText.split("\n\n", 2);
        String subject = "No Subject";
        String body = responseText;
        if (parts.length == 2 && parts[0].toLowerCase().startsWith("subject:")) {
            subject = parts[0].substring("subject:".length()).trim();
            body = parts[1];
        }
        Map<String, String> result = new LinkedHashMap<>();
        result.put("subject", subject);
        result.put("body", body);
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\": \"Failed to format Claude response.\"}";
        }
    }
    
    
    private String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) return null;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encrypted);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
