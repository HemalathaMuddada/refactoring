
package com.xtremand.analytics.controller;

import com.xtremand.analytics.service.CampaignAnalyticsService;
import com.xtremand.domain.dto.CampaignAnalyticsDto;
import com.xtremand.domain.enums.AnalyticsCampaignType;
import com.xtremand.domain.enums.Country;
import com.xtremand.domain.enums.DeviceType;
import com.xtremand.domain.enums.TimeOfDay;
import com.xtremand.domain.enums.TimeRange;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics/campaigns")
@Tag(name = "Campaign Analytics", description = "APIs for retrieving campaign performance analytics")
public class CampaignAnalyticsController {

	private final CampaignAnalyticsService campaignAnalyticsService;

	public CampaignAnalyticsController(CampaignAnalyticsService campaignAnalyticsService) {
		this.campaignAnalyticsService = campaignAnalyticsService;
	}

	 @GetMapping
	    public CampaignAnalyticsDto getCampaignAnalytics(
	        @RequestParam(required = false) TimeRange timeRange,
	        @RequestParam(required = false) AnalyticsCampaignType campaign,
	        @RequestParam(required = false) Country country,
	        @RequestParam(required = false) DeviceType device,
	        @RequestParam(required = false) TimeOfDay timeOfDay
	    ) {
	        return campaignAnalyticsService.getCampaignAnalytics(timeRange, campaign, country, device, timeOfDay);
	    }
}