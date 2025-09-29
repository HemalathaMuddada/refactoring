package com.xtremand.analytics.service;

import com.xtremand.domain.dto.*;
import com.xtremand.domain.enums.*;
import com.xtremand.email.repository.CampaignRepository;
import com.xtremand.email.repository.EmailAnalyticsRepository;
import com.xtremand.domain.entity.Campaign;
import com.xtremand.domain.entity.EmailAnalytics;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CampaignAnalyticsService {

    private final EmailAnalyticsRepository emailAnalyticsRepository;
    private final CampaignRepository campaignRepository;

    public CampaignAnalyticsService(EmailAnalyticsRepository emailAnalyticsRepository,
                                    CampaignRepository campaignRepository) {
        this.emailAnalyticsRepository = emailAnalyticsRepository;
        this.campaignRepository = campaignRepository;
    }

    public CampaignAnalyticsDto getCampaignAnalytics(
            TimeRange timeRange,
            AnalyticsCampaignType campaign,
            Country country,
            DeviceType device,
            TimeOfDay timeOfDay
    ) {
        TimeRangeInterval currentInterval = timeRange.getCurrentInterval();
        TimeRangeInterval previousInterval = timeRange.getPreviousInterval();
        Specification<EmailAnalytics> currentSpec = EmailAnalyticsSpecification.build(
                currentInterval.getStart(), currentInterval.getEnd(),
                campaign, country, device, timeOfDay
        );
        Specification<EmailAnalytics> prevSpec = EmailAnalyticsSpecification.build(
                previousInterval.getStart(), previousInterval.getEnd(),
                campaign, country, device, timeOfDay
        );

        List<EmailAnalytics> currentEmails = emailAnalyticsRepository.findAll(currentSpec);
        List<EmailAnalytics> prevEmails = emailAnalyticsRepository.findAll(prevSpec);
        OverviewWithTrends overviewWithTrends = buildOverviewWithTrends(currentEmails, prevEmails);
        EmailCampaignMetrics metrics = new EmailCampaignMetrics();
        metrics.setOverview(overviewWithTrends.getOverview());
        metrics.setOverviewTrend(overviewWithTrends.getTrend());
        metrics.setMonthlyPerformance(buildMonthlyPerformance(currentEmails));
        metrics.setCampaignComparison(buildCampaignComparison(currentEmails));
        metrics.setDeliveryStatus(buildDeliveryStatus(currentEmails));
        metrics.setHourlyData(buildHourlyData(currentEmails));
        metrics.setDevicePerformance(buildDevicePerformance(currentEmails));
        metrics.setCountryPerformance(buildCountryPerformance(currentEmails));
        metrics.setFilters(buildFilters());
        metrics.setMetadata(buildMetadata(currentEmails));

        CampaignAnalyticsDto dto = new CampaignAnalyticsDto();
        dto.setEmailCampaignMetrics(metrics);
        return dto;
    }


    // --- Overviews ---
    private Overview buildOverview(List<EmailAnalytics> emails) {
        long totalSent = emails.size();
        long totalBounced = emails.stream().filter(e -> e.getBouncedAt() != null).count();
        long totalDelivered = totalSent - totalBounced;
        long totalOpened = emails.stream().filter(e -> e.getOpenedAt() != null).count();
        long totalClicked = emails.stream().filter(e -> e.getClickedAt() != null).count();
        long totalReplied = emails.stream().filter(e -> e.getRepliedAt() != null).count();

        Overview o = new Overview();
        o.setTotalSent(totalSent);
        o.setTotalBounced(totalBounced);
        o.setTotalDelivered(totalDelivered);
        o.setTotalOpened(totalOpened);
        o.setTotalClicked(totalClicked);
        o.setTotalReplied(totalReplied);
        o.setOpenRate(calculateRate(totalOpened, totalDelivered));
        o.setClickRate(calculateRate(totalClicked, totalDelivered));
        o.setReplyRate(calculateRate(totalReplied, totalDelivered));
        o.setDeliveryRate(calculateRate(totalDelivered, totalSent));
        o.setBounceRate(calculateRate(totalBounced, totalSent));
        return o;
    }

    // --- Monthly Performance ---
    private List<MonthlyPerformance> buildMonthlyPerformance(List<EmailAnalytics> emails) {
        Map<Integer, List<EmailAnalytics>> byMonth = emails.stream()
                .filter(e -> e.getSentAt() != null)
                .collect(Collectors.groupingBy(e -> e.getSentAt().getMonthValue()));

        List<MonthlyPerformance> result = new ArrayList<>();
        for (Map.Entry<Integer, List<EmailAnalytics>> entry : byMonth.entrySet()) {
            int month = entry.getKey();
            List<EmailAnalytics> monthEmails = entry.getValue();

            long sent = monthEmails.size();
            long delivered = monthEmails.stream().filter(e -> e.getBouncedAt() == null).count();
            long opened = monthEmails.stream().filter(e -> e.getOpenedAt() != null).count();
            long clicked = monthEmails.stream().filter(e -> e.getClickedAt() != null).count();
            long replied = monthEmails.stream().filter(e -> e.getRepliedAt() != null).count();

            MonthlyPerformance mp = new MonthlyPerformance();
            mp.setMonthIndex(month);
            mp.setMonth(java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH));
            mp.setSent(sent);
            mp.setDelivered(delivered);
            mp.setOpened(opened);
            mp.setClicked(clicked);
            mp.setReplied(replied);
            mp.setOpenRate(calculateRate(opened, delivered));
            mp.setClickRate(calculateRate(clicked, delivered));
            mp.setReplyRate(calculateRate(replied, delivered));
            result.add(mp);
        }
        result.sort(Comparator.comparingInt(MonthlyPerformance::getMonthIndex));
        return result;
    }

    // --- Campaign Comparison ---
    private List<CampaignComparison> buildCampaignComparison(List<EmailAnalytics> emails) {
        Map<Long, Campaign> campaignMap = campaignRepository.findAll().stream()
                .collect(Collectors.toMap(Campaign::getId, Function.identity()));

        // Change: group by campaign ID, properly check for null campaign
        Map<Long, List<EmailAnalytics>> byCampaign = emails.stream()
                .filter(e -> e.getCampaign() != null && e.getCampaign().getId() != null)
                .collect(Collectors.groupingBy(e -> e.getCampaign().getId()));

        List<CampaignComparison> result = new ArrayList<>();
        for (Map.Entry<Long, List<EmailAnalytics>> entry : byCampaign.entrySet()) {
            Long campaignId = entry.getKey();
            Campaign c = campaignMap.get(campaignId);
            if (c == null) continue;

            List<EmailAnalytics> cEmails = entry.getValue();
            long sent = cEmails.size();
            long bounced = cEmails.stream().filter(e -> e.getBouncedAt() != null).count();
            long delivered = sent - bounced;
            long opened = cEmails.stream().filter(e -> e.getOpenedAt() != null).count();
            long clicked = cEmails.stream().filter(e -> e.getClickedAt() != null).count();
            long replied = cEmails.stream().filter(e -> e.getRepliedAt() != null).count();

            CampaignComparison cc = new CampaignComparison();
            cc.setName(c.getName());
            cc.setSent(sent);
            cc.setOpened(opened);
            cc.setClicked(clicked);
            cc.setReplied(replied);
            cc.setOpenRate(calculateRate(opened, delivered));
            cc.setClickRate(calculateRate(clicked, delivered));
            cc.setReplyRate(calculateRate(replied, delivered));
            result.add(cc);
        }
        return result;
    }


    // --- Delivery Status ---
    private List<DeliveryStatus> buildDeliveryStatus(List<EmailAnalytics> emails) {
        long sent = emails.size();
        long bounced = emails.stream().filter(e -> e.getBouncedAt() != null).count();
        long delivered = sent - bounced;

        DeliveryStatus deliveredStatus = new DeliveryStatus();
        deliveredStatus.setType("Delivered");
        deliveredStatus.setValue(calculateRate(delivered, sent));
        deliveredStatus.setColor("#0ea5e9");

        DeliveryStatus bouncedStatus = new DeliveryStatus();
        bouncedStatus.setType("Bounced");
        bouncedStatus.setValue(calculateRate(bounced, sent));
        bouncedStatus.setColor("#ef4444");

        return Arrays.asList(deliveredStatus, bouncedStatus);
    }

    // --- Hourly Data ---
    private List<HourlyData> buildHourlyData(List<EmailAnalytics> emails) {
        Map<Integer, List<EmailAnalytics>> byHour = emails.stream()
                .filter(e -> e.getSentAt() != null)
                .collect(Collectors.groupingBy(e -> e.getSentAt().getHour()));

        List<HourlyData> result = new ArrayList<>();
        for (Map.Entry<Integer, List<EmailAnalytics>> entry : byHour.entrySet()) {
            int hour = entry.getKey();
            List<EmailAnalytics> hourEmails = entry.getValue();

            long sent = hourEmails.size();
            long opened = hourEmails.stream().filter(e -> e.getOpenedAt() != null).count();
            long clicked = hourEmails.stream().filter(e -> e.getClickedAt() != null).count();
            long replied = hourEmails.stream().filter(e -> e.getRepliedAt() != null).count();

            HourlyData hd = new HourlyData();
            hd.setHourIndex(hour);
            hd.setHour(convertHourToLabel(hour));
            hd.setSent(sent);
            hd.setOpened(opened);
            hd.setClicked(clicked);
            hd.setReplied(replied);
            result.add(hd);
        }
        result.sort(Comparator.comparingInt(HourlyData::getHourIndex));
        return result;
    }

    // --- Device Performance ---
    private DevicePerformance buildDevicePerformance(List<EmailAnalytics> emails) {
        PerformanceMetrics mobile = buildPerformanceByDevice(emails, "mobile");
        PerformanceMetrics desktop = buildPerformanceByDevice(emails, "desktop");
        long totalSent = mobile.getSent() + desktop.getSent();
        setMetricsRates(mobile, totalSent);
        setMetricsRates(desktop, totalSent);

        DevicePerformance dp = new DevicePerformance();
        dp.setMobile(mobile);
        dp.setDesktop(desktop);
        return dp;
    }

    private PerformanceMetrics buildPerformanceByDevice(List<EmailAnalytics> emails, String deviceType) {
        List<EmailAnalytics> filtered = emails.stream()
                .filter(e -> deviceType.equals(e.getDevice()))
                .collect(Collectors.toList());

        long sent = filtered.size();
        long delivered = filtered.stream().filter(e -> e.getBouncedAt() == null).count();
        long opened = filtered.stream().filter(e -> e.getOpenedAt() != null).count();
        long clicked = filtered.stream().filter(e -> e.getClickedAt() != null).count();
        long replied = filtered.stream().filter(e -> e.getRepliedAt() != null).count();

        PerformanceMetrics pm = new PerformanceMetrics();
        pm.setSent(sent);
        pm.setDelivered(delivered);
        pm.setOpened(opened);
        pm.setClicked(clicked);
        pm.setReplied(replied);
        return pm;
    }

    // --- Country Performance ---
    private List<CountryPerformance> buildCountryPerformance(List<EmailAnalytics> emails) {
    	Map<String, List<EmailAnalytics>> byCountry = emails.stream()
    		    .filter(e -> e.getCountry() != null)
    		    .collect(Collectors.groupingBy(e -> e.getCountry().name()));


        List<CountryPerformance> result = new ArrayList<>();
        for (Map.Entry<String, List<EmailAnalytics>> entry : byCountry.entrySet()) {
            String country = entry.getKey();
            List<EmailAnalytics> cEmails = entry.getValue();
            long sent = cEmails.size();
            long delivered = cEmails.stream().filter(e -> e.getBouncedAt() == null).count();
            long opened = cEmails.stream().filter(e -> e.getOpenedAt() != null).count();
            long clicked = cEmails.stream().filter(e -> e.getClickedAt() != null).count();
            long replied = cEmails.stream().filter(e -> e.getRepliedAt() != null).count();

            CountryPerformance cp = new CountryPerformance();
            cp.setCountry(country);
            cp.setSent(sent);
            cp.setDelivered(delivered);
            cp.setOpened(opened);
            cp.setClicked(clicked);
            cp.setReplied(replied);
            cp.setOpenRate(calculateRate(opened, delivered));
            cp.setClickRate(calculateRate(clicked, delivered));
            cp.setReplyRate(calculateRate(replied, delivered));
            result.add(cp);
        }
        return result;
    }

    // --- Filters section (just enum values for UI) ---
    private Filters buildFilters() {
        Filters f = new Filters();
        f.setTimeRanges(Arrays.asList(TimeRange.values()));
        f.setCampaigns(Arrays.asList(AnalyticsCampaignType.values()));
        f.setCountries(Arrays.asList(Country.values()));
        f.setDevices(Arrays.asList(DeviceType.values()));
        f.setTimeOfDay(Arrays.asList(TimeOfDay.values()));
        return f;
    }

    // --- Metadata ---
    private Metadata buildMetadata(List<EmailAnalytics> emails) {
        Metadata metadata = new Metadata();
        metadata.setTotalRecords(emails.size());
        metadata.setLastUpdated(OffsetDateTime.now().toString());
        return metadata;
    }

    private double calculateRate(long numerator, long denominator) {
        if (denominator == 0)
            return 0.0;
        double rate = ((double) numerator * 100) / denominator;
        return Math.round(rate * 100.0) / 100.0; // round to two decimals
    }


    private void setMetricsRates(PerformanceMetrics pm, long totalSent) {
        pm.setOpenRate(calculateRate(pm.getOpened(), pm.getDelivered()));
        pm.setClickRate(calculateRate(pm.getClicked(), pm.getDelivered()));
        pm.setReplyRate(calculateRate(pm.getReplied(), pm.getDelivered()));
        pm.setPercentage(calculateRate(pm.getSent(), totalSent));
    }

    private String convertHourToLabel(int hour) {
        if (hour == 0) return "12AM";
        if (hour < 12) return hour + "AM";
        if (hour == 12) return "12PM";
        return (hour - 12) + "PM";
    }
    
    private OverviewWithTrends buildOverviewWithTrends(List<EmailAnalytics> current, List<EmailAnalytics> previous) {
        Overview currentOverview = buildOverview(current);
        Overview previousOverview = buildOverview(previous);

        OverviewTrend trend = new OverviewTrend();
        trend.setOpenRateDelta(calcDelta(currentOverview.getOpenRate(), previousOverview.getOpenRate()));
        trend.setClickRateDelta(calcDelta(currentOverview.getClickRate(), previousOverview.getClickRate()));
        trend.setReplyRateDelta(calcDelta(currentOverview.getReplyRate(), previousOverview.getReplyRate()));
        trend.setTotalSentDelta(calcDelta(currentOverview.getTotalSent(), previousOverview.getTotalSent()));

        OverviewWithTrends result = new OverviewWithTrends();
        result.setOverview(currentOverview);
        result.setTrend(trend);
        return result;
    }

    private double calcDelta(double current, double previous) {
        if (previous == 0) return current == 0 ? 0.0 : 100.0;
        double delta = ((current - previous) / Math.abs(previous)) * 100.0;
        return Math.round(delta * 100.0) / 100.0;
    }

}
