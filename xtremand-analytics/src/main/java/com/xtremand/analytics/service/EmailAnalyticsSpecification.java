package com.xtremand.analytics.service;

import org.springframework.data.jpa.domain.Specification;

import com.xtremand.domain.entity.EmailAnalytics;
import com.xtremand.domain.enums.AnalyticsCampaignType;
import com.xtremand.domain.enums.Country;
import com.xtremand.domain.enums.DeviceType;
import com.xtremand.domain.enums.TimeOfDay;

import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;

public class EmailAnalyticsSpecification {
	public static Specification<EmailAnalytics> build(LocalDateTime start, LocalDateTime end,
			AnalyticsCampaignType campaign, Country country, DeviceType device, TimeOfDay timeOfDay) {
		return (root, query, cb) -> {
			Predicate p = cb.conjunction();
			if (start != null) {
				p = cb.and(p, cb.greaterThanOrEqualTo(root.get("sentAt"), start));
			}
			if (end != null) {
				p = cb.and(p, cb.lessThanOrEqualTo(root.get("sentAt"), end));
			}

			if (campaign != null && campaign != AnalyticsCampaignType.ALL_CAMPAIGNS) {
				p = cb.and(p, cb.equal(root.get("campaign").get("type"), campaign.name()));
			}

			if (country != null && country != Country.ALL_COUNTRIES) {
				p = cb.and(p, cb.equal(root.get("country"), country));
			}

			if (device != null && device != DeviceType.ALL_DEVICES) {
				p = cb.and(p, cb.equal(root.get("device"), device));
			}
			if (timeOfDay != null && timeOfDay != TimeOfDay.ALL_DAY) {
				Expression<Integer> hour = cb.function("date_part", Integer.class, cb.literal("hour"), root.get("sentAt"));
				if (timeOfDay == TimeOfDay.AM)
				    p = cb.and(p, cb.lessThan(hour, 12));
				else if (timeOfDay == TimeOfDay.PM)
				    p = cb.and(p, cb.greaterThanOrEqualTo(hour, 12));

			}

			return p;
		};
	}
}
