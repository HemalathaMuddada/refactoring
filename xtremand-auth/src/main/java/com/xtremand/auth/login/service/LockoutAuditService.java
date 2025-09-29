/*
 * package com.xtremand.auth.login.service;
 * 
 * import java.time.Instant; import java.util.List;
 * 
 * import org.slf4j.Logger; import org.slf4j.LoggerFactory; import
 * org.springframework.data.redis.core.StringRedisTemplate; import
 * org.springframework.stereotype.Service;
 * 
 * import com.fasterxml.jackson.databind.ObjectMapper; import
 * com.xamplify.security.login.audit.LockoutEvent; import
 * com.xamplify.security.login.audit.LockoutEventType;
 * 
 * @Service public class LockoutAuditService {
 * 
 * private static final String AUDIT_PREFIX = "auth:audit:"; private static
 * final Logger LOGGER = LoggerFactory.getLogger(LockoutAuditService.class);
 * 
 * private final StringRedisTemplate redisTemplate; private final ObjectMapper
 * objectMapper;
 * 
 * public LockoutAuditService(StringRedisTemplate redisTemplate, ObjectMapper
 * objectMapper) { this.redisTemplate = redisTemplate; this.objectMapper =
 * objectMapper; }
 * 
 * public void recordEvent(String username, LockoutEventType type) {
 * LockoutEvent event = new LockoutEvent(type, Instant.now()); try { String json
 * = objectMapper.writeValueAsString(event);
 * redisTemplate.opsForList().leftPush(AUDIT_PREFIX + username, json);
 * redisTemplate.opsForList().trim(AUDIT_PREFIX + username, 0, 49); } catch
 * (Exception e) { LOGGER.warn("Failed to serialize lockout event", e); } }
 * 
 * public List<LockoutEvent> getAuditTrail(String username) { List<String>
 * events = redisTemplate.opsForList().range(AUDIT_PREFIX + username, 0, -1); if
 * (events == null) { return List.of(); } return events.stream()
 * .map(this::deserialize) .toList(); }
 * 
 * private LockoutEvent deserialize(String json) { try { return
 * objectMapper.readValue(json, LockoutEvent.class); } catch (Exception e) {
 * LOGGER.warn("Failed to deserialize lockout event: {}", json, e); return new
 * LockoutEvent(LockoutEventType.LOGIN_FAILED, Instant.EPOCH); } } }
 */