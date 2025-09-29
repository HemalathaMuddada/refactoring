package com.xtremand.common.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * RFC 7807 compliant problem details response.
 */
@Data
@Builder
public class Rfc7807ErrorResponse {
    private String type;
    private String title;
    private int status;
    private String code;
    private int codeId;
    private String detail;
    private String instance;
    private String requestId;
    private String traceId;
    private String severity;
    private String category;
    private boolean recoverable;

    @Default
    private List<FieldError> errors = List.of();
    private Map<String, Object> debug;
    private Map<String, Object> lockoutContext;

    @Data
    @Builder
    public static class FieldError {
        private String field;
        private String message;
        private String errorCode;
    }
}
