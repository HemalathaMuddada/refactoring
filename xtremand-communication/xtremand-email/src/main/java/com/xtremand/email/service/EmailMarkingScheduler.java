package com.xtremand.email.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.xtremand.config.repository.MailConfigRepository;
import com.xtremand.domain.entity.MailConfig;
import com.xtremand.domain.util.XtremandUtil;

@Component
public class EmailMarkingScheduler {

	@Autowired
	private EmailService emailService;

	@Autowired
	private MailConfigRepository mailConfigRepository;

	@Autowired
	private XtremandUtil xtremandUtil;

	@Scheduled(fixedDelayString = "600000")
	public void runMarkImportantScheduler() {
		if (xtremandUtil.isProfileActive()) {
			List<MailConfig> configs = mailConfigRepository.findAll();
			for (MailConfig config : configs) {
				if (config.getImportantEmail() != null) {
					List<String> importantEmails = config.getImportantEmail();
					try {
						emailService.markImportantByEmailAndBody(importantEmails, config);
					} catch (Exception e) {
						System.err.println("Error marking important emails for config id " + config.getId());
					}
				}
			}
		}
	}
}
