package com.xtremand.ai;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.domain.entity.UserThread;


@RestController
@RequestMapping("/ai-chat")
public class AiChatController {

    private final PerplexityService perplexityService;
    private final UserThreadRepository userThreadRepository;

    @Autowired
    public AiChatController(PerplexityService perplexityService, UserThreadRepository userThreadRepository) {
        this.perplexityService = perplexityService;
        this.userThreadRepository = userThreadRepository;
    }
    
    @PostMapping("/vector-store")
    public ResponseEntity<String> createVectorStore(@RequestHeader("x-api-key") String apiKey,
                                                    @RequestBody Map<String, String> payload) {
        try {
            String name = payload.get("name");
            if (name == null || name.isBlank()) {
                return ResponseEntity.badRequest().body("{\"error\":\"'name' is required\"}");
            }
            String response = perplexityService.createVectorStore(apiKey, name);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }


    // Create a new chat session/thread and save thread ID for user
    @PostMapping("/sessions")
    public ResponseEntity<String> createSession(@RequestHeader("x-api-key") String apiKey,
                                                @RequestHeader("user-email") String userEmail) {
        try {
            String threadId = perplexityService.createSession(apiKey, userEmail);
            return ResponseEntity.ok("{\"thread_id\":\"" + threadId + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // Get saved thread ID for user
    @GetMapping("/sessions/thread-id")
    public ResponseEntity<String> getThreadId(@RequestHeader("user-email") String userEmail) {
        return userThreadRepository.findByUserEmail(userEmail)
                .map(thread -> ResponseEntity.ok("{\"thread_id\":\"" + thread.getThreadId() + "\"}"))
                .orElse(ResponseEntity.status(404).body("{\"error\":\"Thread ID not found for user\"}"));
    }

    // Send chat message using saved thread ID
    @PostMapping("/sessions/message")
    public ResponseEntity<String> sendMessage(@RequestHeader("x-api-key") String apiKey,
                                              @RequestHeader("user-email") String userEmail,
                                              @RequestBody Map<String, String> payload) {
        try {
            String content = payload.get("content");
            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body("{\"error\":\"'content' is required\"}");
            }

            Optional<UserThread> userThreadOpt = userThreadRepository.findByUserEmail(userEmail);
            if (userThreadOpt.isEmpty()) {
                return ResponseEntity.status(404).body("{\"error\":\"No chat session found for user\"}");
            }
            String threadId = userThreadOpt.get().getThreadId();

            String response = perplexityService.sendMessage(apiKey, threadId, content);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // Get messages for the user's thread
    @GetMapping("/sessions/messages")
    public ResponseEntity<String> getMessages(@RequestHeader("x-api-key") String apiKey,
                                              @RequestHeader("user-email") String userEmail) {
        try {
            Optional<UserThread> userThreadOpt = userThreadRepository.findByUserEmail(userEmail);
            if (userThreadOpt.isEmpty()) {
                return ResponseEntity.status(404).body("{\"error\":\"No chat session found for user\"}");
            }
            String threadId = userThreadOpt.get().getThreadId();

            String messages = perplexityService.getMessages(apiKey, threadId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
