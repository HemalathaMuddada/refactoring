package com.xtremand.ai;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.domain.dto.EmailRequest;
import com.xtremand.domain.enums.AiConfigType;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final AiServiceDelegator aiServiceDelegator;

    public AIController(AiServiceDelegator aiServiceDelegator) {
        this.aiServiceDelegator = aiServiceDelegator;
    }

    @PostMapping("/generateEmail")
    public String generate(@RequestBody EmailRequest emailRequest) {
        AiConfigType type = AiConfigType.valueOf(emailRequest.getAiConfigType().toString());
        return aiServiceDelegator.generateEmail(type, emailRequest);
    }
}
