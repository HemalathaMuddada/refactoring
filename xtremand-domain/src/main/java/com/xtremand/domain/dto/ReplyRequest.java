package com.xtremand.domain.dto;

import com.xtremand.domain.enums.EmailConfigType;

import lombok.Data;

@Data
public class ReplyRequest {
	
	private Long userId;
    private String messageId;
    private String replyBody;
    private Long configType;

}
