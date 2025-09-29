package com.xtremand.email.repository;

import com.xtremand.domain.dto.CountryPerformance;
import com.xtremand.domain.dto.HourlyData;
import com.xtremand.domain.dto.MonthlyPerformance;
import com.xtremand.domain.dto.PerformanceMetrics;
import com.xtremand.domain.entity.EmailAnalytics;
import com.xtremand.domain.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailAnalyticsRepository extends JpaRepository<EmailAnalytics, Long>, JpaSpecificationExecutor<EmailAnalytics>  {

	Optional<EmailAnalytics> findById(Long id);

	Optional<EmailAnalytics> findByTrackingId(UUID trackingId);

	int countByCampaignIdAndSentAtIsNotNull(Long campaignId);

	int countByCampaignIdAndOpenedAtIsNotNull(Long campaignId);

	int countByCampaignIdAndClickedAtIsNotNull(Long campaignId);

	int countByCampaignIdAndRepliedAtIsNotNull(Long campaignId);

	int countByCampaignIdAndBouncedAtIsNotNull(Long campaignId);

	long countBySentAtIsNotNull();

	long countByOpenedAtIsNotNull();

	long countByRepliedAtIsNotNull();

	long countByBouncedAtIsNotNull();

	long countByClickedAtIsNotNull();

	// Monthly performance counts grouped by month extracted from sent_at
	@Query("SELECT EXTRACT(MONTH FROM e.sentAt) AS month, " + "COUNT(e) AS sent, "
			+ "SUM(CASE WHEN e.bouncedAt IS NULL THEN 1 ELSE 0 END) AS delivered, "
			+ "SUM(CASE WHEN e.openedAt IS NOT NULL THEN 1 ELSE 0 END) AS opened, "
			+ "SUM(CASE WHEN e.clickedAt IS NOT NULL THEN 1 ELSE 0 END) AS clicked, "
			+ "SUM(CASE WHEN e.repliedAt IS NOT NULL THEN 1 ELSE 0 END) AS replied " + "FROM EmailAnalytics e "
			+ "GROUP BY month " + "ORDER BY month")
	List<Object[]> findMonthlyPerformanceData();

	// Hourly data grouped by EXTRACT(HOUR FROM sentAt)
	@Query("SELECT EXTRACT(HOUR FROM e.sentAt) AS hour, " + "COUNT(e) AS sent, "
			+ "SUM(CASE WHEN e.openedAt IS NOT NULL THEN 1 ELSE 0 END) AS opened, "
			+ "SUM(CASE WHEN e.clickedAt IS NOT NULL THEN 1 ELSE 0 END) AS clicked, "
			+ "SUM(CASE WHEN e.repliedAt IS NOT NULL THEN 1 ELSE 0 END) AS replied " + "FROM EmailAnalytics e "
			+ "GROUP BY hour " + "ORDER BY hour")
	List<Object[]> findHourlyData();

	// Country performance grouped by country field
	@Query("SELECT e.country AS country, " + "COUNT(e) AS sent, "
			+ "SUM(CASE WHEN e.bouncedAt IS NULL THEN 1 ELSE 0 END) AS delivered, "
			+ "SUM(CASE WHEN e.openedAt IS NOT NULL THEN 1 ELSE 0 END) AS opened, "
			+ "SUM(CASE WHEN e.clickedAt IS NOT NULL THEN 1 ELSE 0 END) AS clicked, "
			+ "SUM(CASE WHEN e.repliedAt IS NOT NULL THEN 1 ELSE 0 END) AS replied " + "FROM EmailAnalytics e "
			+ "GROUP BY country " + "ORDER BY sent DESC")
	List<Object[]> findCountryPerformance();

	// Device performance utility method using a single device param
	@Query("SELECT " + "SUM(CASE WHEN e.device = :device THEN 1 ELSE 0 END) AS sent, "
			+ "SUM(CASE WHEN e.device = :device AND e.bouncedAt IS NULL THEN 1 ELSE 0 END) AS delivered, "
			+ "SUM(CASE WHEN e.device = :device AND e.openedAt IS NOT NULL THEN 1 ELSE 0 END) AS opened, "
			+ "SUM(CASE WHEN e.device = :device AND e.clickedAt IS NOT NULL THEN 1 ELSE 0 END) AS clicked, "
			+ "SUM(CASE WHEN e.device = :device AND e.repliedAt IS NOT NULL THEN 1 ELSE 0 END) AS replied "
			+ "FROM EmailAnalytics e")
	PerformanceMetrics findDevicePerformance(@Param("device") String device);

	List<EmailAnalytics> findByIsIncomingFalse();
	
    List<EmailAnalytics> findByInReplyTo(String messageId);
    
    Optional<EmailAnalytics> findByMessageId(String messageId);
    
    Optional<EmailAnalytics> findByMessageIdAndIsIncomingFalse(String messageId);

	List<EmailAnalytics> findByIsIncomingFalseAndCreatedBy(User user);

	List<EmailAnalytics> findAllByCreatedBy(User user);

	Optional<EmailAnalytics> findByIdAndCreatedBy(Long id, User user);
	


}
