package com.xtremand.auth.oauth2.token;

import java.time.LocalDateTime;

import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import lombok.Getter;

@Getter
public class OAuth2TokenGrantAuthenticationToken extends OAuth2ClientAuthenticationToken {
	private static final long serialVersionUID = 1L;

	private final String deviceId;
	private final String deviceType;
	private final String refreshTokenOrAuthorizationCode;
	private final String grantType;

	private final String ipAddress;
	private final String internetServiceProvider;
	private final String networkOrganization;

	private final String city;
	private final String regionName;
	private final String country;
	private final String timezone;
	private final String zip;

	private final String userAgent;
	private final String browser;
	private final String operatingSystem;

	private final LocalDateTime loginTime;
	private final String autonomousSystemNumber;
	private final Double latitude;
	private final Double longitude;

	public OAuth2TokenGrantAuthenticationToken(String clientId, String refreshTokenOrAuthorizationCode,
			String grantType, String deviceId, String deviceType, String ipAddress, String internetServiceProvider,
			String networkOrganization, String city, String regionName, String country, String timezone, String zip,
			String userAgent, String browser, String operatingSystem, LocalDateTime loginTime,
			String autonomousSystemNumber, Double latitude, Double longitude) {
		super(clientId, ClientAuthenticationMethod.NONE, null, null);
		this.refreshTokenOrAuthorizationCode = refreshTokenOrAuthorizationCode;
		this.grantType = grantType;
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.ipAddress = ipAddress;
		this.internetServiceProvider = internetServiceProvider;
		this.networkOrganization = networkOrganization;
		this.city = city;
		this.regionName = regionName;
		this.country = country;
		this.timezone = timezone;
		this.zip = zip;
		this.userAgent = userAgent;
		this.browser = browser;
		this.operatingSystem = operatingSystem;
		this.loginTime = loginTime;
		this.autonomousSystemNumber = autonomousSystemNumber;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public OAuth2TokenGrantAuthenticationToken(RegisteredClient registeredClient,
			String refreshTokenOrAuthorizationCode, String grantType, String deviceId, String deviceType,
			String ipAddress, String internetServiceProvider, String networkOrganization, String city,
			String regionName, String country, String timezone, String zip, String userAgent, String browser,
			String operatingSystem, LocalDateTime loginTime, String autonomousSystemNumber, Double latitude,
			Double longitude) {
		super(registeredClient, ClientAuthenticationMethod.NONE, null);
		this.refreshTokenOrAuthorizationCode = refreshTokenOrAuthorizationCode;
		this.grantType = grantType;
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.ipAddress = ipAddress;
		this.internetServiceProvider = internetServiceProvider;
		this.networkOrganization = networkOrganization;
		this.city = city;
		this.regionName = regionName;
		this.country = country;
		this.timezone = timezone;
		this.zip = zip;
		this.userAgent = userAgent;
		this.browser = browser;
		this.operatingSystem = operatingSystem;
		this.loginTime = loginTime;
		this.autonomousSystemNumber = autonomousSystemNumber;
		this.latitude = latitude;
		this.longitude = longitude;
	}

}
