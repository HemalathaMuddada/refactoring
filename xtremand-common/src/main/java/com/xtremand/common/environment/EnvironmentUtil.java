package com.xtremand.common.environment;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Utility to access the current Spring environment profile and detect
 * dev/qa/prod.
 */
@Component
public class EnvironmentUtil {

	private final Environment environment;

	public EnvironmentUtil(Environment environment) {
		this.environment = environment;
	}

	/**
	 * Returns the first active profile, or "default" if none are set.
	 */
	public String getActiveProfile() {
		String[] profiles = environment.getActiveProfiles();
		return profiles.length > 0 ? profiles[0] : "default";
	}

	/**
	 * Returns true if the active profile is 'local'.
	 */
	public boolean isLocal() {
		return "local".equalsIgnoreCase(getActiveProfile());
	}

	/**
	 * Returns true if the active profile is 'dev'.
	 */
	public boolean isDev() {
		return "dev".equalsIgnoreCase(getActiveProfile());
	}

	/**
	 * Returns true if the active profile is 'qa'.
	 */
	public boolean isQa() {
		return "qa".equalsIgnoreCase(getActiveProfile());
	}

	/**
	 * Returns true if the active profile is 'prod'.
	 */
	public boolean isProd() {
		return "prod".equalsIgnoreCase(getActiveProfile());
	}
}