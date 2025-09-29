//package com.xtremand.ai;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.Optional;
//
//import org.springframework.stereotype.Service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.xtremand.config.AiConfigService;
//import com.xtremand.domain.dto.EmailRequest;
//import com.xtremand.domain.entity.AiConfig;
//import com.xtremand.domain.enums.AiConfigType;
//
//@Service
//public class CodexService implements AiService {
//
//    private final HttpClient httpClient = HttpClient.newHttpClient();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final AiConfigService aiConfigService;
//
//    public CodexService(AiConfigService aiConfigService) {
//        this.aiConfigService = aiConfigService;
//    }
//
//    @Override
//    public String generateEmailResponse(EmailRequest emailRequest) {
//        Optional<AiConfig> configOpt = aiConfigService.getAiConfig(emailRequest.getAiEmail(), AiConfigType.CODEX);
//        if (configOpt.isEmpty()) {
//            return "{\"error\": \"Codex API config not found for user.\"}";
//        }
//        AiConfig config = configOpt.get();
//        String apiKey = config.getApiKey();
//
//        String prompt = AiPromptBuilder.buildGenericPrompt(emailRequest);;
//        try {
//            Map<String, Object> requestMap = new HashMap<>();
//            requestMap.put("model", "code-davinci-002");
//            requestMap.put("prompt", prompt);
//            requestMap.put("max_tokens", 500);
//
//            String requestBody = objectMapper.writeValueAsString(requestMap);
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("https://api.openai.com/v1/completions"))
//                    .header("Content-Type", "application/json")
//                    .header("Authorization", "Bearer " + apiKey)
//                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//            JsonNode root = objectMapper.readTree(response.body());
//            if (root.has("choices") && root.get("choices").isArray()) {
//                String text = root.get("choices").get(0).get("text").asText();
//                return formatResponse(text);
//            }
//            return "{\"error\": \"Invalid response from Codex API\"}";
//
//        } catch (IOException | InterruptedException e) {
//            return "{\"error\": \"CodexService failed: " + e.getMessage() + "\"}";
//        }
//    }
//
//
//    private String formatResponse(String responseText) {
//        String[] parts = responseText.split("\n\n", 2);
//        String subject = "No Subject";
//        String body = responseText;
//        if (parts.length == 2 && parts[0].toLowerCase().startsWith("subject:")) {
//            subject = parts[0].substring("subject:".length()).trim();
//            body = parts[1];
//        }
//        Map<String, String> result = new LinkedHashMap<>();
//        result.put("subject", subject);
//        result.put("body", body);
//        try {
//            return objectMapper.writeValueAsString(result);
//        } catch (Exception e) {
//            return "{\"error\": \"Failed to format Codex response.\"}";
//        }
//    }
//}
