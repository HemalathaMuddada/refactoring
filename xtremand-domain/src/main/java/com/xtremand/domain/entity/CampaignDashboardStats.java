package com.xtremand.domain.entity;

import lombok.Data;

@Data
public class CampaignDashboardStats {
    private long totalCampaigns;
    private long activeCampaigns;
    private long totalSent;
    private double responseRate; 
}
