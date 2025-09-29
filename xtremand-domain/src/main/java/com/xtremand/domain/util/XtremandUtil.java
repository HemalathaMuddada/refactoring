package com.xtremand.domain.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class XtremandUtil {
	
	@Autowired
	private Environment environment;
	
	public String getActiveProfile() {
		return environment.getProperty("spring.profiles.active");
	}

	public boolean isProfileActive() {
		boolean isActive = false;
		String activeProfile = getActiveProfile();
		if (StringUtils.hasText(activeProfile) && !"local".equalsIgnoreCase(activeProfile)) {
			isActive = true;
		}
		return isActive;
	}

}
