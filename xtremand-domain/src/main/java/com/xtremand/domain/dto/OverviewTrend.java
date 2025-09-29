package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class OverviewTrend {

	private double openRateDelta;
    private double clickRateDelta;
    private double replyRateDelta;
    private double totalSentDelta;
	
}
