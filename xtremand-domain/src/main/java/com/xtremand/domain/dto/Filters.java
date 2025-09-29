package com.xtremand.domain.dto;

import java.util.List;

import com.xtremand.domain.enums.AnalyticsCampaignType;
import com.xtremand.domain.enums.Country;
import com.xtremand.domain.enums.DeviceType;
import com.xtremand.domain.enums.TimeOfDay;
import com.xtremand.domain.enums.TimeRange;

import lombok.Data;

@Data
public class Filters {
	private List<TimeRange> timeRanges;
	private List<AnalyticsCampaignType> campaigns;
	private List<Country> countries;
	private List<DeviceType> devices;
	private List<TimeOfDay> timeOfDay;
}