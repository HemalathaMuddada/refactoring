
package com.xtremand.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.config.IntegratedAppKeyService;
import com.xtremand.config.repository.AiConfigRepository;
import com.xtremand.domain.dto.*;
import com.xtremand.domain.entity.AiConfig;
import com.xtremand.domain.entity.RagSession;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.enums.AiConfigType;
import com.xtremand.user.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RAGService {

	@Autowired
	private AiConfigRepository aiConfigRepository;

	@Autowired
	private RagSessionRepository ragSessionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private IntegratedAppKeyService integratedAppKeyService;

	private final HttpClient client = HttpClient.newHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public String registerUser(RAGRegisterRequest req, Authentication authentication) throws Exception {
		User user = userRepository.fetchByUsername(authentication.getName());
		String requestBody = objectMapper.writeValueAsString(req);
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(URI.create(integratedAppKeyService.getUrl("RAG_REGISTER_URL")))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 201 || response.statusCode() == 200) {
			RAGLoginRequest loginReq = new RAGLoginRequest();
			loginReq.setUsername(req.getUsername());
			loginReq.setPassword(req.getPassword());
			return loginUser(loginReq, user);
		} else {
			return response.body();
		}
	}

	public String loginUser(RAGLoginRequest req, User user) throws Exception {
		String requestBody = objectMapper.writeValueAsString(req);
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(URI.create(integratedAppKeyService.getUrl("RAG_LOGIN_URL")))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 201 || response.statusCode() == 200) {
			String email = req.getUsername();
			JsonNode respJson = objectMapper.readTree(response.body());
			String token = respJson.get("token").asText();
			createAiConfig(email, req.getPassword(), token, user);
			return "registered successfully";
		}
		return response.body();
	}

	private void createAiConfig(String email, String password, String token, User user) {
		AiConfig config = new AiConfig();
		config.setEmail(email);
		config.setConfigType(AiConfigType.RAGITIFY);
		config.setApiKey(email);
		config.setApiSecret(password);
		config.setCreatedAt(LocalDateTime.now());
		config.setUpdatedAt(LocalDateTime.now());
		config.setCreatedBy(user);
		config.setUpdatedBy(user);
		config.setToken(token);
		aiConfigRepository.save(config);
	}

	private String getToken(Authentication authentication) {
		User user = userRepository.fetchByUsername(authentication.getName());
		AiConfig config = aiConfigRepository.findByCreatedByAndConfigType(user, AiConfigType.RAGITIFY);
		return config.getToken();
	}

	public List<ThreadInfo> getThreadList(Authentication authentication) throws Exception {
		String token = getToken(authentication);
		String url = integratedAppKeyService.getUrl("RAG_THREAD_LIST_URL").replace("{token}", token);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url)).header("Accept", "application/json")
				.GET().build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		return objectMapper.readValue(response.body(), new TypeReference<List<ThreadInfo>>() {
		});
	}

	public JsonNode getThreadMessages(String threadId, Authentication authentication) throws Exception {
		String token = getToken(authentication);
		String url = integratedAppKeyService.getUrl("RAG_THREAD_MESSAGES_URL").replace("{token}", token)
				.replace("{threadId}", threadId);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url)).header("Accept", "application/json")
				.GET().build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		return objectMapper.readTree(response.body());
	}

	public ThreadRequest createVectorStore(Authentication authentication) throws Exception {
		String token = getToken(authentication);
		String name = "My_Vector_Store_" + LocalDate.now();
		String uri = integratedAppKeyService.getUrl("RAG_VECTOR_STORE_URL").replace("{token}", token);
		ObjectNode requestBody = objectMapper.createObjectNode();
		requestBody.put("name", name);
		String body = objectMapper.writeValueAsString(requestBody);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(uri))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 201 || response.statusCode() == 200) {
			JsonNode jsonNode = objectMapper.readTree(response.body());
			String vectorId = null;
			if (jsonNode.has("id")) {
				vectorId = jsonNode.get("id").asText();
				return createThread(vectorId, token);
			} else {
				throw new Exception("vector id not found in response");
			}

		}
		return null;
	}

	public ThreadRequest createThread(String vectorStoreId, String token) throws Exception {
		String url = integratedAppKeyService.getUrl("RAG_THREAD_URL").replace("{token}", token);
		ObjectNode requestBody = objectMapper.createObjectNode();
		requestBody.put("vector_store_id", vectorStoreId);
		String body = objectMapper.writeValueAsString(requestBody);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 201 || response.statusCode() == 200) {
			JsonNode jsonNode = objectMapper.readTree(response.body());
			String threadId = null;
			if (jsonNode.has("id")) {
				threadId = jsonNode.get("id").asText();
				AssistantRequest assisstant = new AssistantRequest();
				assisstant.setName("Assistant");
				assisstant.setInstructions("");
				assisstant.setVector_store_id(vectorStoreId);
				return createAssistant(token, assisstant, threadId);
			} else {
				throw new Exception("threadId not found in response");
			}
		}
		return null;
	}

	private ThreadRequest createAssistant(String token, AssistantRequest request, String threadId) throws Exception {
		String url = integratedAppKeyService.getUrl("RAG_ASSISTANT_URL").replace("{token}", token);
		String body = objectMapper.writeValueAsString(request);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 201 || response.statusCode() == 200) {
			JsonNode jsonNode = objectMapper.readTree(response.body());
			String assisstantId = null;
			if (jsonNode.has("id")) {
				assisstantId = jsonNode.get("id").asText();
				RagSession session = new RagSession();
				session.setVectorStoreId(request.getVector_store_id());
				session.setThreadId(threadId);
				session.setAssistantId(assisstantId);
				session.setCreatedAt(LocalDateTime.now());
				session.setUpdatedAt(LocalDateTime.now());
				ragSessionRepository.save(session);
				ThreadRequest req = new ThreadRequest();
				req.setThreadId(threadId);
				req.setVectorStoreId(request.getVector_store_id());
				req.setAssisstantId(assisstantId);
				return req;
			} else {
				throw new Exception("assistant id not found in response");
			}
		}
		return null;
	}

	public String sentRequest(Authentication authentication, InputRequest input, MultipartFile file,
			String vector_store_id) throws Exception {
		String token = getToken(authentication);
		String documentId = null;
		if (file != null && !file.isEmpty()) {
			try {
				documentId = ingestDocument(file, vector_store_id, token);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		sendMessage(input, token, documentId);
		return token;

	}

	public String ingestDocument(MultipartFile file, String vector_store_id, String token) throws Exception {
		String url = integratedAppKeyService.getUrl("RAG_DOCUMENT_INGEST_URL").replace("{token}", token);
		RestTemplate restTemplate = new RestTemplate();

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", file);
		body.add("vector_store_id", vector_store_id);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

		if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
			JsonNode jsonNode = objectMapper.readTree(response.getBody());
			if (jsonNode.has("document_ingest_id")) {
				return jsonNode.get("document_ingest_id").asText();
			} else {
				throw new Exception("document_ingest_id not found in response: " + response.getBody());
			}
		}

		throw new Exception("Failed to ingest document. Status: " + response.getStatusCode());
	}

	public String sendMessage(InputRequest request, String token, String documentId) throws Exception {
		String url = integratedAppKeyService.getUrl("RAG_MESSAGE_URL").replace("{token}", token);
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode requestBody = objectMapper.createObjectNode();
		requestBody.put("thread_id", request.getMessageRequest().getThreadId());
		requestBody.put("content", request.getMessageRequest().getContent());
		String body = objectMapper.writeValueAsString(requestBody);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 201 || response.statusCode() == 200) {
			RunRequest run = new RunRequest();
			if (request.getMessageRequest() != null && request.getMessageRequest().getThreadId() != null) {
				run.setThreadId(request.getMessageRequest().getThreadId());
				run.setAssistantId(request.getAssisstantId());
				run.setMode(request.getMessageRequest().getMode());
				return runAssistant(token, run, documentId);

			}
		}
		return response.body();
	}

	public String runAssistant(String token, RunRequest request, String documentId) throws Exception {
		String url = integratedAppKeyService.getUrl("RAG_RUN_URL").replace("{token}", token);
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode requestBody = objectMapper.createObjectNode();
		requestBody.put("thread_id", request.getThreadId());
		requestBody.put("assistant_id", request.getAssistantId());
		requestBody.put("mode", request.getMode());
		String body = objectMapper.writeValueAsString(requestBody);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	public String deleteThread(String threadId, Authentication authentication) throws Exception {
		String token = getToken(authentication);
		String url = integratedAppKeyService.getUrl("RAG_THREAD_DELETE_URL").replace("{token}", token)
				.replace("{threadId}", threadId);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url)).DELETE()
				.header("Accept", "application/json").build();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

//	https://rag.xamplify.co/rag/thread/{token}/{id}/

	public String updateThread(String threadId, Authentication authentication, ThreadInfo threadInfo)
			throws Exception {
		String token = getToken(authentication);
		String url = integratedAppKeyService.getUrl("RAG_THREAD_UPDATE_URL").replace("{token}", token)
				.replace("{threadId}", threadId);
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode requestBody = objectMapper.createObjectNode();
		requestBody.put("title", threadInfo.getTitle());
		requestBody.put("vector_store_id", threadInfo.getVector_store_id_read());
		String body = objectMapper.writeValueAsString(requestBody);
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url))
				.header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

}