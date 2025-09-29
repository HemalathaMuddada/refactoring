/*
 * package com.xtremand.auth.login.service;
 * 
 * import java.time.Duration;
 * 
 * import java.time.Instant; import java.util.concurrent.ConcurrentHashMap;
 * import java.util.concurrent.ConcurrentMap; import
 * java.util.concurrent.TimeUnit;
 * 
 * import org.springframework.data.redis.core.StringRedisTemplate;
 * 
 * import com.fasterxml.jackson.databind.ObjectMapper;
 * 
 * import io.micrometer.core.instrument.MeterRegistry;
 * 
 * class UserRateLimiter {
 * 
 * private static final String FAILURE_USER_PREFIX = "auth:failures:user:";
 * private static final String LOCKED_USER_PREFIX = "auth:locked:user:";
 * 
 * private final StringRedisTemplate redisTemplate; private final Duration
 * failureTtl; private final Duration lockTtl; private final int
 * failureThreshold;
 * 
 * private final ConcurrentMap<String, Instant> lockedUsers = new
 * ConcurrentHashMap<>();
 * 
 * UserRateLimiter(StringRedisTemplate redisTemplate, LoginRateLimitProperties
 * props, MeterRegistry meterRegistry, ObjectMapper objectMapper) {
 * this.redisTemplate = redisTemplate; this.failureTtl =
 * props.getUserFailureTtl(); this.lockTtl = props.getUserLockTtl();
 * this.failureThreshold = props.getUserFailureThreshold(); }
 * 
 * boolean isUserLocked(String username) { String lockKey = LOCKED_USER_PREFIX +
 * username; boolean locked =
 * Boolean.TRUE.equals(redisTemplate.hasKey(lockKey)); if (!locked) {
 * lockedUsers.remove(username); } return locked; }
 * 
 * boolean recordUserFailure(String username, String ip, String companyId) {
 * String failureKey = getUserFailureKey(username); Long count =
 * redisTemplate.opsForValue().increment(failureKey); if (count != null && count
 * == 1L) { redisTemplate.expire(failureKey, failureTtl); } if (count != null &&
 * count >= failureThreshold) {
 * redisTemplate.opsForValue().set(LOCKED_USER_PREFIX + username, "1", lockTtl);
 * lockedUsers.put(username, Instant.now()); return true; } return false; }
 * 
 * void resetUserAttempts(String username) {
 * redisTemplate.delete(getUserFailureKey(username));
 * redisTemplate.delete(LOCKED_USER_PREFIX + username);
 * lockedUsers.remove(username); }
 * 
 * int lockedCount() { return lockedUsers.size(); }
 * 
 * // ✅ NEW: returns how many failures are currently stored for the user public
 * int getFailureCount(String username) { String key =
 * getUserFailureKey(username); String value =
 * redisTemplate.opsForValue().get(key); return value != null ?
 * Integer.parseInt(value) : 0; }
 * 
 * // ✅ NEW: returns when the lock TTL will expire (i.e., when user can retry)
 * public Instant getUnlockAt(String username) { String key = LOCKED_USER_PREFIX
 * + username; Long expireSeconds = redisTemplate.getExpire(key,
 * TimeUnit.SECONDS); if (expireSeconds == null || expireSeconds <= 0) return
 * null; return Instant.now().plusSeconds(expireSeconds); }
 * 
 * // ✅ NEW: consistent key generator private String getUserFailureKey(String
 * username) { return FAILURE_USER_PREFIX + username.toLowerCase(); } }
 */