package com.xtremand.auth.login.metadata;

import lombok.Data;

@Data
public class LoginMetaData {

	private String autonomousSystemNumber;
	private String ipAddress;
	private String internetServiceProvider;
	private String networkOrganization;
	private String city;
	private String regionName;
	private String country;
	private String timezone;
	private String zip;
	private Double latitude;
	private Double longitude;

	private String deviceId;
	private String deviceType;

	private String userAgent;
	private String browser;
	private String operatingSystem;
	private Double operatingSystemVersion;

	private String refreshTokenOrAuthorizationCode;
	private String grantType;
	private String code;

}
