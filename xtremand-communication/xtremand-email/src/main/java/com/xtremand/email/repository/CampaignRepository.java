package com.xtremand.email.repository;

import com.xtremand.domain.entity.Campaign;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
	
	List<Campaign> findByScheduledAtBeforeAndSentFalse(LocalDateTime dateTime);

	long count();
	
	long countBySentFalse();
}
