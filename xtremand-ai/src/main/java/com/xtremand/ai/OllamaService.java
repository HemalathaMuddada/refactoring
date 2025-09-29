package com.xtremand.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.domain.dto.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;

@Service
public class OllamaService implements AiService{

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();

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
				HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:11434/api/generate"))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

				HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
				String rawJson = response.body();

				JsonNode root = objectMapper.readTree(rawJson);
				if (root.has("response")) {
					String responseText = root.get("response").asText();
					String[] parts = responseText.split("\\n\\n", 2);
					String subject = "No Subject";
					String body = responseText;
					if (parts.length == 2 && parts[0].toLowerCase().startsWith("subject:")) {
						subject = parts[0].substring("subject:".length()).trim();
						body = parts[1];
					}
					Map<String, String> result = new LinkedHashMap<>();
					result.put("subject", subject);
					result.put("body", body);
					return objectMapper.writeValueAsString(result);
				}
			} catch (IOException | InterruptedException e) {
				System.err.println("Model " + modelName + " failed: " + e.getMessage());
			}
		}
		return "{\"error\": \"All models failed or returned empty response.\"}";
	}

}
