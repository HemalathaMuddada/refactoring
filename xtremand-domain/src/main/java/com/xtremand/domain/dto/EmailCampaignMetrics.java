package com.xtremand.domain.dto;

import java.util.List;

import lombok.Data;

@Data
public class EmailCampaignMetrics {
    private Overview overview;
    private List<MonthlyPerformance> monthlyPerformance;
    private List<CampaignComparison> campaignComparison;
    private List<DeliveryStatus> deliveryStatus;
    private List<HourlyData> hourlyData;
    private DevicePerformance devicePerformance;
    private List<CountryPerformance> countryPerformance;
    private Filters filters;
    private Metadata metadata;
    private OverviewTrend overviewTrend;
}
