package com.xtremand.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.EmailTemplate;

@Repository
public interface EmailTemplateRepository
		extends JpaRepository<EmailTemplate, Long>, JpaSpecificationExecutor<EmailTemplate> {
}
