package com.xtremand.common.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiErrorResponse {
    private String timestamp;
    private int status;
    private String errorCode;
    private int codeId;
    private String message;
    private String severity;
    private String category;
    private boolean recoverable;
    @Builder.Default
    private List<?> details = List.of();   // Always present for consistent OpenAPI
    private String path;
    private String requestId;
    private Map<String, Object> debug;
}
