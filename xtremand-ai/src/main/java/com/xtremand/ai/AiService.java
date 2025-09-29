package com.xtremand.ai;

import com.xtremand.domain.dto.EmailRequest;

public interface AiService {
    String generateEmailResponse(EmailRequest emailRequest);
}
