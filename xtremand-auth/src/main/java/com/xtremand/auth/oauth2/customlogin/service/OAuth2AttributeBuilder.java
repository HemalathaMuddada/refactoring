package com.xtremand.auth.oauth2.customlogin.service;

import java.util.HashMap;


import java.util.Map;

import org.springframework.stereotype.Service;

import com.xtremand.auth.login.dto.LoginRequest;
import com.xtremand.auth.oauth2.constants.OAuth2CustomProperties;

@Service
public class OAuth2AttributeBuilder {

    public Map<String, Object> build(LoginRequest request) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(OAuth2CustomProperties.DEVICE_ID, request.getDeviceId());
        attrs.put(OAuth2CustomProperties.DEVICE_TYPE, request.getDeviceType());
        attrs.put(OAuth2CustomProperties.IP_ADDRESS, request.getIpAddress());
        attrs.put(OAuth2CustomProperties.INTERNET_SERVICE_PROVIDER, request.getInternetServiceProvider());
        attrs.put(OAuth2CustomProperties.NETWORK_ORGANIZATION, request.getNetworkOrganization());
        attrs.put(OAuth2CustomProperties.CITY, request.getCity());
        attrs.put(OAuth2CustomProperties.REGION_NAME, request.getRegionName());
        attrs.put(OAuth2CustomProperties.COUNTRY, request.getCountry());
        attrs.put(OAuth2CustomProperties.TIME_ZONE, request.getTimezone());
        attrs.put(OAuth2CustomProperties.ZIP, request.getZip());
        attrs.put(OAuth2CustomProperties.USER_AGENT, request.getUserAgent());
        attrs.put(OAuth2CustomProperties.BROWSER, request.getBrowser());
        attrs.put(OAuth2CustomProperties.OPERATING_SYSTEM, request.getOperatingSystem());
        return attrs;
    }
}
