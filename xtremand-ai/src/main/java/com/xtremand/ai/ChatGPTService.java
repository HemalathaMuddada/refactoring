package com.xtremand.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.ai.util.AiServiceUtil;
import com.xtremand.config.AiConfigService;
import com.xtremand.domain.dto.EmailRequest;
import com.xtremand.domain.entity.AiConfig;
import com.xtremand.domain.enums.AiConfigType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ChatGPTService implements AiService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiConfigService aiConfigService;

    public ChatGPTService(AiConfigService aiConfigService) {
        this.aiConfigService = aiConfigService;
    }

    @Override
    public String generateEmailResponse(EmailRequest emailRequest) {
        Optional<AiConfig> configOpt = aiConfigService.getAiConfig(emailRequest.getSenderEmail(), AiConfigType.CHATGPT);
        if (configOpt.isEmpty()) {
            return "{\"error\": \"ChatGPT API config not found for user.\"}";
        }
        AiConfig config = configOpt.get();
        String apiKey = AiServiceUtil.decrypt(config.getApiKey());

        String prompt = AiPromptBuilder.buildGenericPrompt(emailRequest);
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("model", "gpt-4");
            requestMap.put("messages", new Map[]{message});

            String requestBody = objectMapper.writeValueAsString(requestMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            if (root.has("choices") && root.get("choices").isArray()) {
                String content = root.get("choices").get(0).get("message").get("content").asText();
                return AiServiceUtil.formatResponse(content);
            }
            return "{\"error\": \"Invalid response from ChatGPT API\"}";

        } catch (IOException | InterruptedException e) {
            return "{\"error\": \"ChatGPTService failed: " + e.getMessage() + "\"}";
        }
    }
}
