package com.xtremand.auth.handler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String errorCode;
    @Builder.Default
    private int codeId = 9999;
    private String message;
    @Builder.Default
    private Severity severity = Severity.ERROR;
    @Builder.Default
    private Category category = Category.UNKNOWN;
    @Builder.Default
    private boolean recoverable = false;
    private List<Detail> details;
    private String path;
    @Builder.Default
    private String requestId = "unknown";
    private DebugInfo debug;

    @Data
    @Builder
    public static class Detail {
        private String type;
        private String message;
    }

    @Data
    @Builder
    public static class DebugInfo {
        private String exceptionType;
    }

    public enum Severity {
        ERROR, WARN, INFO
    }

    public enum Category {
        AUTHENTICATION, AUTHORIZATION, VALIDATION, UNKNOWN
    }
}
