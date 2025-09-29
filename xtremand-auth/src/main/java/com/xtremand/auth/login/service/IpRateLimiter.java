/*
 * package com.xtremand.auth.login.service;
 * 
 * import java.time.Duration; import java.time.Instant; import
 * java.util.concurrent.ConcurrentHashMap; import
 * java.util.concurrent.ConcurrentMap; import java.util.concurrent.TimeUnit;
 * 
 * import org.springframework.data.redis.core.StringRedisTemplate;
 * 
 * import com.fasterxml.jackson.databind.ObjectMapper;
 * 
 * import io.micrometer.core.instrument.MeterRegistry;
 * 
 * class IpRateLimiter {
 * 
 * private static final String FAILURE_IP_PREFIX = "auth:failures:ip:"; private
 * static final String LOCKED_IP_PREFIX = "auth:locked:ip:";
 * 
 * private final StringRedisTemplate redisTemplate; private final Duration
 * failureTtl; private final Duration lockTtl; private final int
 * failureThreshold;
 * 
 * private final ConcurrentMap<String, Instant> lockedIps = new
 * ConcurrentHashMap<>();
 * 
 * IpRateLimiter(StringRedisTemplate redisTemplate, LoginRateLimitProperties
 * props, MeterRegistry meterRegistry, ObjectMapper objectMapper) {
 * this.redisTemplate = redisTemplate; this.failureTtl =
 * props.getIpFailureTtl(); this.lockTtl = props.getIpLockTtl();
 * this.failureThreshold = props.getIpFailureThreshold(); }
 * 
 * boolean isIpLocked(String ip) { String lockKey = LOCKED_IP_PREFIX + ip;
 * boolean locked = Boolean.TRUE.equals(redisTemplate.hasKey(lockKey)); if
 * (!locked) { lockedIps.remove(ip); } return locked; }
 * 
 * boolean recordIpFailure(String ip, String username, String companyId) {
 * String failureKey = getIpFailureKey(ip); Long count =
 * redisTemplate.opsForValue().increment(failureKey); if (count != null && count
 * == 1L) { redisTemplate.expire(failureKey, failureTtl); } if (count != null &&
 * count >= failureThreshold) { redisTemplate.opsForValue().set(LOCKED_IP_PREFIX
 * + ip, "1", lockTtl); lockedIps.put(ip, Instant.now()); return true; } return
 * false; }
 * 
 * void resetIpAttempts(String ip) { redisTemplate.delete(getIpFailureKey(ip));
 * redisTemplate.delete(LOCKED_IP_PREFIX + ip); lockedIps.remove(ip); }
 * 
 * int lockedCount() { return lockedIps.size(); }
 * 
 * // ✅ NEW: get number of recorded failures for IP public int
 * getFailureCount(String ip) { String key = getIpFailureKey(ip); String value =
 * redisTemplate.opsForValue().get(key); return value != null ?
 * Integer.parseInt(value) : 0; }
 * 
 * // ✅ NEW: get when IP lock will expire public Instant getUnlockAt(String ip)
 * { String key = LOCKED_IP_PREFIX + ip; Long expireSeconds =
 * redisTemplate.getExpire(key, TimeUnit.SECONDS); if (expireSeconds == null ||
 * expireSeconds <= 0) return null; return
 * Instant.now().plusSeconds(expireSeconds); }
 * 
 * // ✅ NEW: consistent key generation private String getIpFailureKey(String ip)
 * { return FAILURE_IP_PREFIX + ip; } }
 */