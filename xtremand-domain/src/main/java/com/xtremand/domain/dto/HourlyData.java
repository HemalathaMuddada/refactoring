package com.xtremand.domain.dto;

import lombok.Data;

@Data
public class HourlyData {

	private int hourIndex;
	private String hour;
	private long sent;
	private long opened;
	private long clicked;
	private long replied;

}
