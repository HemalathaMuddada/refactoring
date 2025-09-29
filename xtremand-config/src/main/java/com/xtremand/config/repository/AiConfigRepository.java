package com.xtremand.config.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.AiConfig;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.enums.AiConfigType;

@Repository
public interface AiConfigRepository extends JpaRepository<AiConfig, Long> {
	
    Optional<AiConfig> findByEmailAndConfigType(String email, AiConfigType configType);

	List<AiConfig> findByCreatedBy(User user);
	
	AiConfig findByCreatedByAndConfigType(User email, AiConfigType configType);
}

