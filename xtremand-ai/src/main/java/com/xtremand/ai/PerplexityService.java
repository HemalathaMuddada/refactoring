package com.xtremand.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.config.AiConfigService;
import com.xtremand.domain.dto.EmailRequest;
import com.xtremand.domain.entity.AiConfig;
import com.xtremand.domain.entity.UserThread;
import com.xtremand.domain.enums.AiConfigType;

@Service
public class PerplexityService implements AiService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiConfigService aiConfigService;
    private final UserThreadRepository userThreadRepo;
    public PerplexityService(AiConfigService aiConfigService, UserThreadRepository userThreadRepo) {
        this.aiConfigService = aiConfigService;
        this.userThreadRepo = userThreadRepo;
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
    @Override
    public String generateEmailResponse(EmailRequest emailRequest) {
        Optional<AiConfig> configOpt = aiConfigService.getAiConfig(emailRequest.getAiEmail(), AiConfigType.PERPLEXITY);
        if (configOpt.isEmpty()) {
            return "{\"error\": \"Perplexity API config not found for user.\"}";
        }
        AiConfig config = configOpt.get();
//        String apiKey = config.getApiKey();
        String apiKey = decrypt(config.getApiKey());
        String prompt = AiPromptBuilder.buildGenericPrompt(emailRequest);

        try {
            Map<String, Object> systemMessage = Map.of("role", "system", "content", "You are a helpful assistant.");
            Map<String, Object> userMessage = Map.of("role", "user", "content", prompt);
            List<Map<String, Object>> messages = List.of(systemMessage, userMessage);

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("model", "sonar"); 
            requestMap.put("messages", messages);
            requestMap.put("max_tokens", 500);
            requestMap.put("temperature", 0.7);

            String requestBody = objectMapper.writeValueAsString(requestMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.perplexity.ai/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            String rawBody = response.body();

            if (statusCode != 200) {
                return "{\"error\": \"Perplexity API returned HTTP " + statusCode + "\"}";
            }

            System.out.println("Raw response: " + rawBody);
            JsonNode root = objectMapper.readTree(rawBody);
            if (root.has("choices") && root.get("choices").isArray()) {
                String content = root.get("choices").get(0).get("message").get("content").asText();
                return formatResponse(content);
            }
            return "{\"error\": \"Invalid response from Perplexity API\"}";

        } catch (IOException | InterruptedException e) {
            return "{\"error\": \"PerplexityService failed: " + e.getMessage() + "\"}";
        }
    }

    private String formatResponse(String responseText) {
        String subject = "No Subject";
        String body = responseText;
        int subjectIndex = responseText.toLowerCase().indexOf("subject:");
        if (subjectIndex != -1) {
            int endOfLine = responseText.indexOf('\n', subjectIndex);
            if (endOfLine == -1) endOfLine = responseText.length();
            subject = responseText.substring(subjectIndex + "subject:".length(), endOfLine).trim();
            body = responseText.substring(endOfLine).trim();
        }

        Map<String, String> result = new LinkedHashMap<>();
        result.put("subject", subject);
        result.put("body", body);

        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "{\"error\": \"Failed to format Perplexity response.\"}";
        }
    }
    
    
    public String listSessions(String apiKey) throws Exception {
        String url = "https://www.perplexity.ai/rest/thread/list_recent?version=2.18&source=default";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // Get messages for a specific thread
    public String getMessages(String apiKey, String threadId) throws Exception {
        String url = String.format("https://www.perplexity.ai/rest/thread/%s/messages?version=2.18&source=default", threadId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // Send a chat message and get response, posting message to Perplexity
    public String sendMessage(String apiKey, String threadId, String content) throws Exception {
        String url = String.format("https://www.perplexity.ai/rest/thread/%s/message?version=2.18&source=default", threadId);

        Map<String, String> messageBody = Map.of("content", content);
        String body = objectMapper.writeValueAsString(messageBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // Create a new chat session/thread
    public String createSession(String apiKey, String userEmail) throws Exception {
        String url = "https://www.perplexity.ai/rest/thread/create?version=2.18&source=default";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.noBody()) // no vector store
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            JsonNode root = objectMapper.readTree(response.body());
            String threadId = root.get("id").asText();
            UserThread userThread = userThreadRepo.findByUserEmail(userEmail)
                    .orElseGet(UserThread::new);
            userThread.setUserEmail(userEmail);
            userThread.setThreadId(threadId);
            userThread.setCreatedAt(LocalDateTime.now());
            userThread.setUpdatedAt(LocalDateTime.now());
            userThreadRepo.save(userThread);
            return threadId;
        } else {
            throw new RuntimeException("Failed to create Perplexity thread: " + response.body());
        }
    }

    
    public String createVectorStore(String apiKey, String name) throws Exception {
        String url = "https://api.perplexity.ai/vector-store/";
        Map<String, String> payload = Map.of("name", name);
        String body = objectMapper.writeValueAsString(payload);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new RuntimeException("Vector store creation failed: " + response.body());
        }

        return response.body(); 
    }

    
    
//    public String createSession(String apiKey, String vectorStoreId, String userEmail) throws Exception {
//        String url = "https://www.perplexity.ai/rest/thread/create?version=2.18&source=default";
//        Map<String, String> requestBody = Map.of("vector_store_id", vectorStoreId);
//        String body = objectMapper.writeValueAsString(requestBody);
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(url))
//                .header("Content-Type", "application/json")
//                .header("x-api-key", apiKey)
//                .POST(HttpRequest.BodyPublishers.ofString(body))
//                .build();
//        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//        if (response.statusCode() == 200 || response.statusCode() == 201) {
//            JsonNode root = objectMapper.readTree(response.body());
//            String threadId = root.get("id").asText();
//            UserThread userThread = userThreadRepo.findByUserEmail(userEmail)
//                    .orElseGet(() -> new UserThread());
//            userThread.setUserEmail(userEmail);
//            userThread.setThreadId(threadId);
//            userThread.setCreatedAt(LocalDateTime.now());
//            userThread.setUpdatedAt(LocalDateTime.now());
//            userThreadRepo.save(userThread);
//            return threadId;
//        } else {
//            throw new RuntimeException("Failed to create Perplexity thread: " + response.body());
//        }
//    }

    public Optional<String> getThreadIdByUserEmail(String userEmail) {
        return userThreadRepo.findByUserEmail(userEmail)
                             .map(UserThread::getThreadId);
    }

}
