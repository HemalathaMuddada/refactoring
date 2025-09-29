package com.xtremand.auth.login.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.auth.forgotpassword.dto.ForgotPasswordRequest;
import com.xtremand.auth.forgotpassword.dto.ResetPasswordRequest;
import com.xtremand.auth.forgotpassword.service.ForgotPasswordService;
import com.xtremand.auth.login.dto.LoginRequest;
import com.xtremand.auth.login.dto.RefreshTokenRequest;
import com.xtremand.auth.login.dto.TokenResponse;
import com.xtremand.auth.login.exception.InvalidRefreshTokenException;
import com.xtremand.auth.login.exception.TokenGenerationException;
import com.xtremand.auth.oauth2.customlogin.service.AuthenticationService;
import com.xtremand.auth.oauth2.customlogin.service.OAuth2LoginComponents;
import com.xtremand.auth.oauth2.service.CustomOAuth2AuthorizationService;
import com.xtremand.auth.user.service.UserService;
import com.xtremand.common.dto.UserProfile;
import com.xtremand.user.dto.SignupRequest;
import com.xtremand.user.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User signup and login")
public class AuthController {

	private final UserService userService;
	private final AuthenticationService authenticationService;
	private final OAuth2LoginComponents oauth2Components;
	private final UserRepository userRepository;
	private final ForgotPasswordService forgotPasswordService;

	public AuthController(UserService userService, AuthenticationService authenticationService,
			OAuth2LoginComponents oauth2Components, UserRepository userRepository,
			ForgotPasswordService forgotPasswordService) {
		this.userService = userService;
		this.authenticationService = authenticationService;
		this.oauth2Components = oauth2Components;
		this.userRepository = userRepository;
		this.forgotPasswordService = forgotPasswordService;
	}

	@PostMapping("/signup")
	@Operation(summary = "Register a new user", description = "Creates a new user and returns their profile information.")
	@ApiResponse(responseCode = "200", description = "User registered successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input")
	public UserProfile signup(@Validated @RequestBody SignupRequest request) {
		return userService.register(request);
	}

	@PostMapping("/login")
	@Operation(summary = "Authenticate a user", description = "Authenticates a user and returns an access token and refresh token.")
	@ApiResponse(responseCode = "200", description = "User authenticated successfully")
	@ApiResponse(responseCode = "401", description = "Invalid credentials")
	public ResponseEntity<TokenResponse> login(@Validated @RequestBody LoginRequest request) {
		Authentication authResult = authenticationService.authenticate(request, "web");

		RegisteredClient registeredClient = oauth2Components.getRegisteredClientService()
				.findByClientId("xtremand-Web-Client");

		if (oauth2Components
				.getAuthorizationService() instanceof CustomOAuth2AuthorizationService) {
			CustomOAuth2AuthorizationService customAuthService = (CustomOAuth2AuthorizationService) oauth2Components
					.getAuthorizationService();
			OAuth2Authorization existingAuthorization = customAuthService
					.findByClientIdAndPrincipal(registeredClient.getId(), authResult.getName());

			if (existingAuthorization != null) {
				OAuth2Authorization.Token<OAuth2AccessToken> accessTokenToken = existingAuthorization.getAccessToken();
				if (accessTokenToken != null
						&& accessTokenToken.getToken().getExpiresAt().isAfter(java.time.Instant.now())) {
					TokenResponse response = oauth2Components.getTokenResponseService()
							.build(accessTokenToken.getToken(), existingAuthorization.getRefreshToken().getToken());
					userRepository.findByEmail(authResult.getName())
							.ifPresent(user -> response.setUser(UserProfile.from(user)));
					return ResponseEntity.ok(response);
				}
			}
		}

		Map<String, Object> customAttributes = oauth2Components.getAttributeBuilder().build(request);

		OAuth2AccessToken accessToken = oauth2Components.getTokenService().generateAccessToken(authResult,
				registeredClient);
		OAuth2RefreshToken refreshToken = oauth2Components.getTokenService().generateRefreshToken(authResult,
				registeredClient);

		if (accessToken == null || refreshToken == null) {
			throw new TokenGenerationException();
		}

		OAuth2Authorization authorization = oauth2Components.getAuthorizationBuilder().build(registeredClient,
				authResult, accessToken, refreshToken, customAttributes);

		oauth2Components.getAuthorizationService().save(authorization);

		TokenResponse response = oauth2Components.getTokenResponseService().build(accessToken, refreshToken);

		userRepository.findByEmail(authResult.getName())
				.ifPresent(user -> response.setUser(UserProfile.from(user)));

		return ResponseEntity.ok(response);
	}

	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refreshToken(@Validated @RequestBody RefreshTokenRequest request) {
		final OAuth2Authorization authorization = oauth2Components.getAuthorizationService()
				.findByToken(request.getRefreshToken(), OAuth2TokenType.REFRESH_TOKEN);

		if (authorization == null) {
			throw new InvalidRefreshTokenException();
		}

		final RegisteredClient registeredClient = oauth2Components.getRegisteredClientService()
				.findById(authorization.getRegisteredClientId());

		final Authentication principal = new UsernamePasswordAuthenticationToken(authorization.getPrincipalName(), null,
				Collections.emptyList());

		final OAuth2AccessToken accessToken = oauth2Components.getTokenService()
				.generateAccessTokenFromRefresh(principal, registeredClient, authorization);
		final var refreshTokenHolder = authorization.getRefreshToken();
		if (refreshTokenHolder == null) {
			throw new InvalidRefreshTokenException();
		}
		final OAuth2RefreshToken refreshToken = refreshTokenHolder.getToken();

		final OAuth2Authorization updatedAuthorization = OAuth2Authorization.from(authorization)
				.accessToken(accessToken).refreshToken(refreshToken).build();

		oauth2Components.getAuthorizationService().save(updatedAuthorization);

		TokenResponse response = oauth2Components.getTokenResponseService().build(accessToken, refreshToken);

		userRepository.findByEmail(authorization.getPrincipalName())
				.ifPresent(user -> response.setUser(UserProfile.from(user)));

		return ResponseEntity.ok(response);
	}

	@PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Forgot password", description = "Sends a password reset link to the user's email.")
    @ApiResponse(responseCode = "200", description = "Password reset link sent successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public void forgotPassword(@Validated @RequestBody ForgotPasswordRequest request) {
        forgotPasswordService.forgotPassword(request);
    }

	@PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reset password", description = "Resets the user's password.")
    @ApiResponse(responseCode = "200", description = "Password reset successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public void resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        forgotPasswordService.resetPassword(request);
    }
}
