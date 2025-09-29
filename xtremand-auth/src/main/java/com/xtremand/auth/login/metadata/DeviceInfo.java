package com.xtremand.auth.login.metadata;

import java.time.LocalDateTime;

public record DeviceInfo(String authorizationId, String deviceId, String browser, String operatingSystem,
		LocalDateTime loginTime, boolean currentSession, String accessToken, String refreshToken) {

}
