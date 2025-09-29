package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private String threadId;
    private String content;
    private String mode;
}
