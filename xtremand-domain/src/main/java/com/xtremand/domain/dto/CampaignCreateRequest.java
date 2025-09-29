package com.xtremand.domain.dto;

import java.time.LocalDateTime;

import com.xtremand.domain.enums.CampaignType;

import lombok.Data;

@Data
public class CampaignCreateRequest {
	private String name;
	private CampaignType type;
	private Long contactListId;
	private String contentStrategy;
	private Long templateId;
	private LocalDateTime scheduledAt;
	private boolean aiPersonalization;
	private Long userId;
	private Long mailConfigId;

}
