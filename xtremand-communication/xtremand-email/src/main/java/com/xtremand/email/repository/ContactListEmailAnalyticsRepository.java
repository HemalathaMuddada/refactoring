package com.xtremand.email.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.ContactListEmailAnalytics;
import com.xtremand.domain.entity.EmailAnalytics;

@Repository
public interface ContactListEmailAnalyticsRepository extends JpaRepository<ContactListEmailAnalytics, Long> {

	List<ContactListEmailAnalytics> findByEmailAnalytics(EmailAnalytics email);
}

