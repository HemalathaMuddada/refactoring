package com.xtremand.auth.oauth2.customlogin.service;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.xtremand.auth.login.dto.LoginRequest;
import com.xtremand.common.util.AESUtil;

@Service
public class AuthenticationService {

	private final AuthenticationManager authenticationManager;

	public AuthenticationService(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public Authentication authenticate(LoginRequest loginRequest, String clientType) {
		String password = loginRequest.getPassword();
		if ("angular".equalsIgnoreCase(clientType) || "web".equalsIgnoreCase(clientType)) {
			password = AESUtil.decrypt(password);
		}
		return authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), password));
	}

}
