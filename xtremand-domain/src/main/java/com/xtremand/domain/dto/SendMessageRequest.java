package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class SendMessageRequest {

    private MessageRequest messageRequest;
    private RunRequest runRequest;

}
