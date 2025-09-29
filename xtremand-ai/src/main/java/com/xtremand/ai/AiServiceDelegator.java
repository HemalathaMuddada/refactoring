package com.xtremand.ai;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.xtremand.domain.enums.AiConfigType;
import com.xtremand.domain.dto.EmailRequest;

@Service
public class AiServiceDelegator {

    private final Map<AiConfigType, AiService> aiServices;

    public AiServiceDelegator(OllamaService ollamaService,
                              PerplexityService perplexityService,
                              ChatGPTService chatGPTService,
                              ClaudeService claudeService,
//                              CodexService codexService,
                              PollinationsService pollinationsService) {
        aiServices = Map.of(
            AiConfigType.OLLAMA, ollamaService,
            AiConfigType.PERPLEXITY, perplexityService,
            AiConfigType.CHATGPT, chatGPTService,
            AiConfigType.CLAUDE, claudeService,
            AiConfigType.POLLINATIONS, pollinationsService
        );
    }

    public String generateEmail(AiConfigType type, EmailRequest emailRequest) {
        AiService service = aiServices.get(type);
        if (service == null) {
            throw new IllegalArgumentException("Unsupported AI config type: " + type);
        }
        return service.generateEmailResponse(emailRequest);
    }
}
