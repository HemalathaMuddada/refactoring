package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {
 private String token;
 private String threadId;
 private String content;
}


