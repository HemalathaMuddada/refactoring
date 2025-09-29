package com.xtremand.domain.dto;

import java.util.List;

import com.xtremand.domain.entity.CampaignDashboardStats;

import lombok.Data;

@Data
public class CampaignsAnalyticsResponse {
	private List<CampaignResponse> campaigns;
	private CampaignDashboardStats dashboardStats;

}
