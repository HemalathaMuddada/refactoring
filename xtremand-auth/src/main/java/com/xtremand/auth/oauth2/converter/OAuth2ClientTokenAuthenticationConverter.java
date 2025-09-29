package com.xtremand.auth.oauth2.converter;

import java.time.LocalDateTime;


import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import com.xtremand.auth.oauth2.constants.OAuth2CustomProperties;
import com.xtremand.auth.oauth2.token.OAuth2TokenGrantAuthenticationToken;

import jakarta.servlet.http.HttpServletRequest;

public class OAuth2ClientTokenAuthenticationConverter implements AuthenticationConverter {

	@Override
	public Authentication convert(HttpServletRequest request) {
		String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
		if (!StringUtils.hasText(grantType)) {
			return null;
		}

		String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
		String deviceId = request.getParameter(OAuth2CustomProperties.DEVICE_ID);
		String deciveType = request.getParameter(OAuth2CustomProperties.DEVICE_TYPE);
		String ipAddress = request.getParameter(OAuth2CustomProperties.IP_ADDRESS);
		String internetServiceProvider = request.getParameter(OAuth2CustomProperties.INTERNET_SERVICE_PROVIDER);
		String networkOrganization = request.getParameter(OAuth2CustomProperties.NETWORK_ORGANIZATION);

		String city = request.getParameter(OAuth2CustomProperties.CITY);
		String regionName = request.getParameter(OAuth2CustomProperties.REGION_NAME);
		String country = request.getParameter(OAuth2CustomProperties.COUNTRY);
		String timezone = request.getParameter(OAuth2CustomProperties.TIME_ZONE);
		String zip = request.getParameter(OAuth2CustomProperties.ZIP);

		String userAgent = request.getParameter(OAuth2CustomProperties.USER_AGENT);
		String browser = request.getParameter(OAuth2CustomProperties.BROWSER);
		String operatingSystem = request.getParameter(OAuth2CustomProperties.OPERATING_SYSTEM);
		String asn = request.getParameter(OAuth2CustomProperties.ASN);
		String latitude = request.getParameter(OAuth2CustomProperties.LATITUDE);
		String longitude = request.getParameter(OAuth2CustomProperties.LONGITUDE);

		LocalDateTime currentDateTime = LocalDateTime.now();

		if (!StringUtils.hasText(clientId)) {
			return null;
		}

		switch (grantType) {
		case "refresh_token":
			String refreshToken = request.getParameter(OAuth2ParameterNames.REFRESH_TOKEN);
			if (!StringUtils.hasText(refreshToken)) {
				return null;
			}
			return new OAuth2TokenGrantAuthenticationToken(clientId, refreshToken, grantType, deviceId, deciveType,
					ipAddress, internetServiceProvider, networkOrganization, city, regionName, country, timezone, zip,
					userAgent, browser, operatingSystem, currentDateTime, asn, Double.valueOf(latitude),
					Double.valueOf(longitude));

		case "authorization_code":
			String code = request.getParameter(OAuth2ParameterNames.CODE);
			if (!StringUtils.hasText(code)) {
				return null;
			}
			return new OAuth2TokenGrantAuthenticationToken(clientId, code, grantType, deviceId, deciveType, ipAddress,
					internetServiceProvider, networkOrganization, city, regionName, country, timezone, zip, userAgent,
					browser, operatingSystem, currentDateTime, asn, Double.valueOf(latitude),
					Double.valueOf(longitude));

		default:
			return null;
		}
	}

}