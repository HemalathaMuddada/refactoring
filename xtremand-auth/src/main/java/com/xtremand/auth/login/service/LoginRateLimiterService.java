/*
 * package com.xtremand.auth.login.service;
 * 
 * import java.time.Instant;
 * 
 * import java.util.List;
 * 
 * import org.springframework.data.redis.core.StringRedisTemplate; import
 * org.springframework.stereotype.Service;
 * 
 * import com.fasterxml.jackson.databind.ObjectMapper; import
 * com.xamplify.common.environment.EnvironmentUtil; import
 * com.xamplify.common.identity.UserCompanyLookupService; import
 * com.xamplify.security.login.audit.LockoutEvent; import
 * com.xamplify.security.login.audit.LockoutEventType; import
 * com.xamplify.security.login.metadata.LoginMetaData; import
 * com.xamplify.security.login.notify.LockoutEmailNotificationService;
 * 
 * import io.micrometer.core.instrument.MeterRegistry; import
 * io.micrometer.core.instrument.MultiGauge; import
 * io.micrometer.core.instrument.Tags;
 * 
 * @Service public class LoginRateLimiterService {
 * 
 * private final IpRateLimiter ipRateLimiter; private final UserRateLimiter
 * userRateLimiter; private final LockoutNotifierService notifierService;
 * private final LockoutEmailNotificationService
 * lockoutEmailNotificationService; private final LockoutAuditService
 * auditService; private final MeterRegistry meterRegistry; private final
 * UserCompanyLookupService companyLookupService; private final String
 * environment; private final MultiGauge activeGauge;
 * 
 * public LoginRateLimiterService(StringRedisTemplate redisTemplate,
 * LoginRateLimitProperties props, EnvironmentUtil environmentUtil,
 * MeterRegistry meterRegistry, LockoutNotifierService notifierService,
 * LockoutEmailNotificationService lockoutEmailNotificationService, ObjectMapper
 * objectMapper, UserCompanyLookupService companyLookupService,
 * LockoutAuditService auditService) { this.meterRegistry = meterRegistry;
 * this.notifierService = notifierService; this.companyLookupService =
 * companyLookupService; this.lockoutEmailNotificationService =
 * lockoutEmailNotificationService; this.auditService = auditService;
 * this.environment = environmentUtil.getActiveProfile(); this.userRateLimiter =
 * new UserRateLimiter(redisTemplate, props, meterRegistry, objectMapper);
 * this.ipRateLimiter = new IpRateLimiter(redisTemplate, props, meterRegistry,
 * objectMapper); this.activeGauge =
 * MultiGauge.builder("login_lockout_active").register(meterRegistry);
 * updateGauge(); }
 * 
 * public boolean isUserLocked(String username) { boolean locked =
 * userRateLimiter.isUserLocked(username); if (!locked) { updateGauge(); }
 * return locked; }
 * 
 * public boolean isIpLocked(String ip) { boolean locked =
 * ipRateLimiter.isIpLocked(ip); if (!locked) { updateGauge(); } return locked;
 * }
 * 
 * public void recordFailure(String username, String ip, String companyId,
 * LoginMetaData loginMetaData, String clientType) { String resolvedCompanyId =
 * resolveCompanyId(username, companyId); auditService.recordEvent(username,
 * LockoutEventType.LOGIN_FAILED); incrementMetric("login_failures_total",
 * resolvedCompanyId);
 * 
 * boolean userLocked = userRateLimiter.recordUserFailure(username, ip,
 * resolvedCompanyId); if (userLocked) { notifierService.notifyLockout(username,
 * ip); incrementMetric("login_lockout_total", resolvedCompanyId);
 * auditService.recordEvent(username, LockoutEventType.LOCKED); }
 * 
 * boolean ipLocked = ipRateLimiter.recordIpFailure(ip, username,
 * resolvedCompanyId); if (ipLocked) { notifierService.notifyLockout(username,
 * ip); incrementMetric("login_lockout_total", resolvedCompanyId); }
 * 
 * if (userLocked || ipLocked) {
 * lockoutEmailNotificationService.sendLockoutEmail(username, ip, Instant.now(),
 * userLocked, ipLocked, loginMetaData, clientType, getTriggerCount(username,
 * ip), getUnlockAt(username, ip)); }
 * 
 * updateGauge(); }
 * 
 * public void resetAttempts(String username, String ip) {
 * userRateLimiter.resetUserAttempts(username);
 * ipRateLimiter.resetIpAttempts(ip); auditService.recordEvent(username,
 * LockoutEventType.UNLOCKED); updateGauge(); }
 * 
 * public List<LockoutEvent> getAuditTrail(String username) { return
 * auditService.getAuditTrail(username); }
 * 
 * private void incrementMetric(String name, String companyId) {
 * meterRegistry.counter(name, "companyId", companyId, "environment",
 * environment).increment(); }
 * 
 * private String resolveCompanyId(String username, String fallback) { if
 * (fallback != null && !"unknown".equals(fallback)) { return fallback; } return
 * companyLookupService.findCompanyUuidByUsername(username).map(java.util.UUID::
 * toString).orElse("unknown"); }
 * 
 * private void updateGauge() { int count = userRateLimiter.lockedCount() +
 * ipRateLimiter.lockedCount(); activeGauge.register(
 * List.of(MultiGauge.Row.of(Tags.of("companyId", "unknown", "environment",
 * environment), count)), true); }
 * 
 * public int getTriggerCount(String username, String ip) { int userCount =
 * userRateLimiter.getFailureCount(username); int ipCount =
 * ipRateLimiter.getFailureCount(ip); return Math.max(userCount, ipCount); }
 * 
 * public Instant getUnlockAt(String username, String ip) { Instant userUnlock =
 * userRateLimiter.getUnlockAt(username); Instant ipUnlock =
 * ipRateLimiter.getUnlockAt(ip);
 * 
 * if (userUnlock != null && ipUnlock != null) { return
 * userUnlock.isAfter(ipUnlock) ? userUnlock : ipUnlock; } if (userUnlock !=
 * null) return userUnlock; if (ipUnlock != null) return ipUnlock;
 * 
 * return Instant.now(); // fallback }
 * 
 * }
 */