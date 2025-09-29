package com.xtremand.common.web;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility for resolving the originating client IP address when the application
 * is behind proxies or load balancers.
 */
public final class IpResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(IpResolver.class);

	private IpResolver() {
		// utility class
	}

	/**
	 * Determine the client IP from the given request using common proxy headers.
	 * <p>
	 * Order of precedence:
	 * <ol>
	 * <li>{@code X-Forwarded-For}</li>
	 * <li>{@code X-Real-IP}</li>
	 * <li>{@link HttpServletRequest#getRemoteAddr()}</li>
	 * </ol>
	 * </p>
	 *
	 * @param request the current HTTP request, may be {@code null}
	 * @return the resolved client IP or an empty string if unavailable
	 */
	public static String getClientIp(HttpServletRequest request) {
		if (request == null) {
			return "";
		}
		String xff = trim(request.getHeader("X-Forwarded-For"));
		if (xff != null && !xff.isEmpty()) {
			if (xff.contains(",")) {
				String first = xff.split(",")[0].trim();
				LOGGER.warn("X-Forwarded-For contains multiple IPs: {}, using {}", xff, first);
				return first;
			}
			return xff;
		}
		String realIp = trim(request.getHeader("X-Real-IP"));
		if (realIp != null && !realIp.isEmpty()) {
			return realIp;
		}
		String remoteAddr = trim(request.getRemoteAddr());
		return remoteAddr != null ? remoteAddr : "";
	}

	private static String trim(String value) {
		return value == null ? null : value.trim();
	}
}
