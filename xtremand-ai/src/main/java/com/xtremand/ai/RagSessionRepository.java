package com.xtremand.ai;

import com.xtremand.domain.entity.RagSession;
import com.xtremand.domain.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RagSessionRepository extends JpaRepository<RagSession, Long> {
	
	RagSession findByCreatedBy(User user);
	
	RagSession findTopByCreatedByOrderByCreatedAtDesc(User user);

	
}
