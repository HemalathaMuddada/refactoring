package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class RunSessionRequest {

    private DocumentIngestRequest documentIngestRequest; // Nullable if no document upload
    private AssistantRequest assistantRequest;
    private ThreadRequest threadRequest;
    private MessageRequest messageRequest;
    private RunRequest runRequest;
    private String messageContent;


}
