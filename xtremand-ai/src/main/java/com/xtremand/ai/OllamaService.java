package com.xtremand.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.ai.util.AiServiceUtil;
import com.xtremand.domain.dto.EmailRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import com.xtremand.config.IntegratedAppKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OllamaService implements AiService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private IntegratedAppKeyService integratedAppKeyService;

    public String generateEmailResponse(EmailRequest emailRequest) {
        String prompt = AiPromptBuilder.buildGenericPrompt(emailRequest);
        return getModelResponse(prompt);
    }


    public String getModelResponse(String prompt) {
        List<String> modelPriority = List.of("openhermes", "llama3", "mistral", "gemma", "dolphin-mistral",
                "neural-chat");
        for (String modelName : modelPriority) {
            try {
                Map<String, Object> requestMap = new HashMap<>();
                requestMap.put("model", modelName);
                requestMap.put("prompt", prompt);
                requestMap.put("stream", false);

                String requestBody = objectMapper.writeValueAsString(requestMap);
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(integratedAppKeyService.getUrl("OLLAMA_GENERATE_URL")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String rawJson = response.body();

                JsonNode root = objectMapper.readTree(rawJson);
                if (root.has("response")) {
                    String responseText = root.get("response").asText();
                    return AiServiceUtil.formatResponse(responseText);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Model " + modelName + " failed: " + e.getMessage());
            }
        }
        return "{\"error\": \"All models failed or returned empty response.\"}";
    }

}
