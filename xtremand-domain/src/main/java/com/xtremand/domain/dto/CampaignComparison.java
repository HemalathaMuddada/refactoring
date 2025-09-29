package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class CampaignComparison {
    private String name;
    private long sent;
    private long opened;
    private long clicked;
    private long replied;
    private double openRate;
    private double clickRate;
    private double replyRate;
}