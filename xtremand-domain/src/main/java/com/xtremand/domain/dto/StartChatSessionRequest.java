package com.xtremand.domain.dto;

import lombok.Data;

@Data 
public class StartChatSessionRequest {
private String token;
private String vectorStoreId;
}
