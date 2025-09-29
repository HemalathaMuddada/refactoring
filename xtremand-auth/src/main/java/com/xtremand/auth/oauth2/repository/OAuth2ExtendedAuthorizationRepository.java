package com.xtremand.auth.oauth2.repository;

import java.time.LocalDateTime;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.xtremand.auth.login.metadata.DeviceInfo;
import com.xtremand.auth.login.metadata.LoginMetaData;
import com.xtremand.auth.oauth2.constants.OAuth2CustomProperties;
import com.xtremand.auth.oauth2.token.OAuth2TokenGrantAuthenticationToken;

@Repository
public class OAuth2ExtendedAuthorizationRepository {

	private static final String DEVICE_ID = "deviceId";

	private static final String DEVICE_ID_PARAMETER = ":" + DEVICE_ID;

	private static final String DEVICE_TYPE = "deviceType";

	private static final String DEVICE_TYPE_PARAMETER = ":" + DEVICE_TYPE;

	private static final String IP_ADDRESS = "ipAddress";

	private static final String IP_ADDRESS_PARAMETER = ":" + IP_ADDRESS;

	private static final String INTERNET_SERVICE_PROVIDER = "internetServiceProvider";

	private static final String INTERNET_SERVICE_PROVIDER_PARAMETER = ":" + INTERNET_SERVICE_PROVIDER;

	private static final String NETWORK_ORGANIZATION = "networkOrganization";

	private static final String NETWORK_ORGANIZATION_PARAMETER = ":" + NETWORK_ORGANIZATION;

	private static final String CITY = "city";

	private static final String CITY_PARAMTER = ":" + CITY;

	private static final String REGION_NAME = "regionName";

	private static final String REGION_NAME_PARAMETER = ":" + REGION_NAME;

	private static final String COUNTRY = "country";

	private static final String COUNTRY_PARAMETER = ":" + COUNTRY;

	private static final String TIME_ZONE = "timezone";

	private static final String TIME_ZONE_PARAMETER = ":" + TIME_ZONE;

	private static final String ZIP = "zip";

	private static final String ZIP_PARAMETER = ":" + ZIP;

	private static final String USER_AGENT = "userAgent";

	private static final String USER_AGENT_PARAMETER = ":" + USER_AGENT;

	private static final String BROWSER = "browser";

	private static final String BROWSER_PARAMETER = ":" + BROWSER;

	private static final String OPERATING_SYSTEM = "operatingSystem";

	private static final String OPERATING_SYSTEM_PARAMETER = ":" + OPERATING_SYSTEM;

	private static final String LOGIN_TIME = "loginTime";

	private static final String LOGIN_TIME_PARAMETER = ":" + LOGIN_TIME;

	private static final String ASN = "asn";

	private static final String ASN_PARAMETER = ":" + ASN;

	private static final String LATITUDE = "latitude";

	private static final String LATITUDE_SYSTEM_PARAMETER = ":" + LATITUDE;

	private static final String LONGITUDE = "longitude";

	private static final String LONGITUDE_SYSTEM_PARAMETER = ":" + LONGITUDE;

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public OAuth2ExtendedAuthorizationRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public void updateDeviceMetadata(String authorizationId,
			OAuth2TokenGrantAuthenticationToken oAuth2TokenGrantAuthenticationToken) {

		String sql = updateAuthorizationMetadataSql();
		MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue(DEVICE_ID, oAuth2TokenGrantAuthenticationToken.getDeviceId())
				.addValue(DEVICE_TYPE, oAuth2TokenGrantAuthenticationToken.getDeviceType())
				.addValue(DEVICE_TYPE, oAuth2TokenGrantAuthenticationToken.getDeviceType())
				.addValue(IP_ADDRESS, oAuth2TokenGrantAuthenticationToken.getIpAddress())
				.addValue(INTERNET_SERVICE_PROVIDER, oAuth2TokenGrantAuthenticationToken.getInternetServiceProvider())
				.addValue(NETWORK_ORGANIZATION, oAuth2TokenGrantAuthenticationToken.getNetworkOrganization())
				.addValue(CITY, oAuth2TokenGrantAuthenticationToken.getCity())
				.addValue(REGION_NAME, oAuth2TokenGrantAuthenticationToken.getRegionName())
				.addValue(COUNTRY, oAuth2TokenGrantAuthenticationToken.getCountry())
				.addValue(TIME_ZONE, oAuth2TokenGrantAuthenticationToken.getTimezone())
				.addValue(ZIP, oAuth2TokenGrantAuthenticationToken.getZip())
				.addValue(USER_AGENT, oAuth2TokenGrantAuthenticationToken.getUserAgent())
				.addValue(BROWSER, oAuth2TokenGrantAuthenticationToken.getBrowser())
				.addValue(LATITUDE, oAuth2TokenGrantAuthenticationToken.getOperatingSystem())
				.addValue(LOGIN_TIME, oAuth2TokenGrantAuthenticationToken.getLoginTime())
				.addValue(ASN, oAuth2TokenGrantAuthenticationToken.getAutonomousSystemNumber())
				.addValue(LATITUDE, oAuth2TokenGrantAuthenticationToken.getLatitude())
				.addValue(LONGITUDE, oAuth2TokenGrantAuthenticationToken.getLongitude())
				.addValue("authorizationId", authorizationId);

		namedParameterJdbcTemplate.update(sql, params);

	}

	public void updateDeviceMetadataForCustomLogin(String authorizationId, LoginMetaData loginMetaData) {

		String sql = updateAuthorizationMetadataSql();

		MapSqlParameterSource params = new MapSqlParameterSource().addValue(DEVICE_ID, loginMetaData.getDeviceId())
				.addValue(DEVICE_TYPE, loginMetaData.getDeviceType())
				.addValue(DEVICE_TYPE, loginMetaData.getDeviceType()).addValue(IP_ADDRESS, loginMetaData.getIpAddress())
				.addValue(INTERNET_SERVICE_PROVIDER, loginMetaData.getInternetServiceProvider())
				.addValue(NETWORK_ORGANIZATION, loginMetaData.getNetworkOrganization())
				.addValue(CITY, loginMetaData.getCity()).addValue(REGION_NAME, loginMetaData.getRegionName())
				.addValue(COUNTRY, loginMetaData.getCountry()).addValue(TIME_ZONE, loginMetaData.getTimezone())
				.addValue(ZIP, loginMetaData.getZip()).addValue(USER_AGENT, loginMetaData.getUserAgent())
				.addValue(BROWSER, loginMetaData.getBrowser())
				.addValue(OPERATING_SYSTEM, loginMetaData.getOperatingSystem())
				.addValue(LOGIN_TIME, LocalDateTime.now()).addValue(ASN, loginMetaData.getAutonomousSystemNumber())
				.addValue(LATITUDE, loginMetaData.getLatitude()).addValue(LONGITUDE, loginMetaData.getLongitude())
				.addValue("authorizationId", authorizationId);

		namedParameterJdbcTemplate.update(sql, params);

	}

	private String updateAuthorizationMetadataSql() {
		return "UPDATE oauth2_authorization SET " + OAuth2CustomProperties.DEVICE_ID + " = " + DEVICE_ID_PARAMETER
				+ ", " + OAuth2CustomProperties.DEVICE_TYPE + " =" + DEVICE_TYPE_PARAMETER + ","
				+ OAuth2CustomProperties.IP_ADDRESS + " =" + IP_ADDRESS_PARAMETER + " , "
				+ OAuth2CustomProperties.INTERNET_SERVICE_PROVIDER + " =" + INTERNET_SERVICE_PROVIDER_PARAMETER + ","
				+ OAuth2CustomProperties.NETWORK_ORGANIZATION + " =" + NETWORK_ORGANIZATION_PARAMETER + ","
				+ OAuth2CustomProperties.CITY + " =" + CITY_PARAMTER + "," + OAuth2CustomProperties.REGION_NAME + " ="
				+ REGION_NAME_PARAMETER + "," + OAuth2CustomProperties.COUNTRY + " =" + COUNTRY_PARAMETER + ","
				+ OAuth2CustomProperties.TIME_ZONE + " =" + TIME_ZONE_PARAMETER + "," + OAuth2CustomProperties.ZIP
				+ " =" + ZIP_PARAMETER + "," + OAuth2CustomProperties.USER_AGENT + " =" + USER_AGENT_PARAMETER + ","
				+ OAuth2CustomProperties.BROWSER + " =" + BROWSER_PARAMETER + ","
				+ OAuth2CustomProperties.OPERATING_SYSTEM + " =" + OPERATING_SYSTEM_PARAMETER + ","
				+ OAuth2CustomProperties.LOGIN_TIME + " =" + LOGIN_TIME_PARAMETER + ", " + OAuth2CustomProperties.ASN
				+ " =" + ASN_PARAMETER + ", " + OAuth2CustomProperties.LATITUDE + " =" + LATITUDE_SYSTEM_PARAMETER
				+ ", " + OAuth2CustomProperties.LONGITUDE + " =" + LONGITUDE_SYSTEM_PARAMETER
				+ " WHERE id = :authorizationId";
	}

	public List<DeviceInfo> findDevicesByUsername(String username, String currentAuthorizationId) {
		String sql = """
				    SELECT id, device_id, browser, operating_system, login_time,
				           access_token_value, refresh_token_value
				    FROM oauth2_authorization
				    WHERE principal_name = :username
				""";

		MapSqlParameterSource params = new MapSqlParameterSource().addValue("username", username);

		return namedParameterJdbcTemplate.query(sql, params,
				(rs, rowNum) -> new DeviceInfo(rs.getString("id"), rs.getString("device_id"), rs.getString(BROWSER),
						rs.getString("operating_system"), LocalDateTime.now(),
						rs.getString("id").equals(currentAuthorizationId), rs.getString("access_token_value"),
						rs.getString("refresh_token_value")));
	}

	public void revokeSessionById(String authorizationId) {
		String sql = "DELETE FROM oauth2_authorization WHERE id = :authId";

		MapSqlParameterSource params = new MapSqlParameterSource().addValue("authId", authorizationId);

		namedParameterJdbcTemplate.update(sql, params);
	}

}
