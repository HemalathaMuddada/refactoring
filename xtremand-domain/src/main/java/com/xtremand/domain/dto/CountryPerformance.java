package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class CountryPerformance {
	private String country;

	private long sent;
	private long delivered;
	private long opened;
	private long clicked;
	private long replied;

	private double openRate;
	private double clickRate;
	private double replyRate;
}