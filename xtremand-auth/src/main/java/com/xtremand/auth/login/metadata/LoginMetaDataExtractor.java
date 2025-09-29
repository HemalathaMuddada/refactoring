package com.xtremand.auth.login.metadata;

import org.springframework.stereotype.Component;


import com.xtremand.common.web.IpResolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoginMetaDataExtractor {

	public LoginMetaData extractFrom(HttpServletRequest request) {
		LoginMetaData meta = new LoginMetaData();

		// IP
		String ip = IpResolver.getClientIp(request);
		meta.setIpAddress(ip);

		// User-Agent
		String userAgent = request.getHeader("User-Agent");
		meta.setUserAgent(userAgent);

		// Custom headers (optional, from Postman / frontend app)
		meta.setDeviceType(request.getHeader("X-Device-Type"));
		meta.setOperatingSystem(request.getHeader("X-OS-Name"));
		meta.setBrowser(request.getHeader("X-Browser"));
		meta.setOperatingSystemVersion(Double.parseDouble(request.getHeader("X-OS-Version")));
		meta.setDeviceId(request.getHeader("X-Device-Id"));

		// Optional: geolocation/network info (if injected from reverse proxy or
		// frontend)
		meta.setAutonomousSystemNumber(request.getHeader("X-ASN"));
		meta.setInternetServiceProvider(request.getHeader("X-ISP"));
		meta.setNetworkOrganization(request.getHeader("X-Network-Org"));
		meta.setCity(request.getHeader("X-City"));
		meta.setRegionName(request.getHeader("X-Region"));
		meta.setCountry(request.getHeader("X-Country"));
		meta.setTimezone(request.getHeader("X-Timezone"));
		meta.setZip(request.getHeader("X-Zip"));

		try {
			if (request.getHeader("X-Latitude") != null) {
				meta.setLatitude(Double.parseDouble(request.getHeader("X-Latitude")));
			}
			if (request.getHeader("X-Longitude") != null) {
				meta.setLongitude(Double.parseDouble(request.getHeader("X-Longitude")));
			}
		} catch (NumberFormatException ex) {
			log.warn("Invalid lat/lon header format", ex);
		}

		return meta;
	}
}
