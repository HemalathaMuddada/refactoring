package com.xtremand.domain.dto;

import java.util.List;
import lombok.Data;

@Data
public class CampaignAnalyticsDto {

	
	private EmailCampaignMetrics emailCampaignMetrics;
	
	private int totalCampaigns;
    private int activeCampaigns;
    private int totalSent;
    private double responseRate; // in percent
    private List<CampaignDto> campaigns;
    
    public CampaignAnalyticsDto() {
		super();
	}
    
    public CampaignAnalyticsDto(int totalCampaigns, int activeCampaigns, int totalSent, double responseRate,
			List<CampaignDto> campaigns) {
		super();
		this.totalCampaigns = totalCampaigns;
		this.activeCampaigns = activeCampaigns;
		this.totalSent = totalSent;
		this.responseRate = responseRate;
		this.campaigns = campaigns;
	}



	
}
