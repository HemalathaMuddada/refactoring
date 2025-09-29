/*
 * package com.xtremand.auth.login.service;
 * 
 * import java.net.URI;
 * 
 * import java.time.Duration;
 * 
 * import org.springframework.boot.context.properties.ConfigurationProperties;
 * import org.springframework.validation.annotation.Validated;
 * 
 * import com.xamplify.common.validation.PositiveDuration;
 * 
 * import jakarta.annotation.Nullable; import
 * jakarta.validation.constraints.Min; import
 * jakarta.validation.constraints.NotNull; import lombok.Getter;
 * 
 *//**
	 * Configuration properties for login failure tracking and account lockout
	 * thresholds.
	 */
/*
 * @Getter
 * 
 * @Validated
 * 
 * @ConfigurationProperties(prefix = "xamplify.security.lockout") public class
 * LoginRateLimitProperties {
 * 
 *//** Time window to count failed login attempts per user. */
/*
 * @NotNull
 * 
 * @PositiveDuration private final Duration userFailureTtl;
 * 
 *//** Lock duration for a user after exceeding failure threshold. */
/*
 * @NotNull
 * 
 * @PositiveDuration private final Duration userLockTtl;
 * 
 *//** Maximum allowed user failures within {@link #userFailureTtl}. */
/*
 * @Min(1) private final int userFailureThreshold;
 * 
 *//** Time window to count failed login attempts per IP address. */
/*
 * @NotNull
 * 
 * @PositiveDuration private final Duration ipFailureTtl;
 * 
 *//** Lock duration for an IP address after exceeding failure threshold. */
/*
 * @NotNull
 * 
 * @PositiveDuration private final Duration ipLockTtl;
 * 
 *//** Maximum allowed IP failures within {@link #ipFailureTtl}. */
/*
 * @Min(1) private final int ipFailureThreshold;
 * 
 *//** Optional webhook URL invoked when a lockout occurs. *//*
																 * @Nullable private final URI lockoutWebhookUrl;
																 * 
																 * public LoginRateLimitProperties(Duration
																 * userFailureTtl, Duration userLockTtl, int
																 * userFailureThreshold, Duration ipFailureTtl, Duration
																 * ipLockTtl, int ipFailureThreshold,
																 * 
																 * @Nullable URI lockoutWebhookUrl) {
																 * this.userFailureTtl = userFailureTtl;
																 * this.userLockTtl = userLockTtl;
																 * this.userFailureThreshold = userFailureThreshold;
																 * this.ipFailureTtl = ipFailureTtl; this.ipLockTtl =
																 * ipLockTtl; this.ipFailureThreshold =
																 * ipFailureThreshold; this.lockoutWebhookUrl =
																 * lockoutWebhookUrl; } }
																 */