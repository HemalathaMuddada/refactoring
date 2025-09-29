package com.xtremand.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailAnalyticsSummaryDto {

	private long totalEmails;
	private long sentCount;
	private long notSentCount;
	private long openedCount;
	private long clickedCount;
	private long bouncedCount;
	private long repliedCount;
	private double openRate;
	private double bounceRate;
	private double notSentRate;
	private double clickRate;
	private double replyRate;
}
