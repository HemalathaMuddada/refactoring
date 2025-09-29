package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class RunRequest {
    private String threadId;
    private String status;
    private String assistantId;
    private String mode;
}
