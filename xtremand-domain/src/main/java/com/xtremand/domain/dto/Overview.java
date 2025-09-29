package com.xtremand.domain.dto;

import lombok.Data;

@Data
public  class Overview {
    private long totalSent;
    private long totalDelivered;
    private long totalOpened;
    private long totalClicked;
    private long totalReplied;
    private long totalBounced;
    private double openRate;
    private double clickRate;
    private double replyRate;
    private double deliveryRate;
    private double bounceRate;
}

