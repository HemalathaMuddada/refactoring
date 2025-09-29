package com.xtremand.auth.storage;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
public class TemporaryAuthStorage {

    private static final long TTL_SECONDS = 300; // 5 minutes

    private final Map<String, StoredAuthData> storage = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TemporaryAuthStorage() {
        scheduler.scheduleAtFixedRate(this::cleanupExpired, 1, 1, TimeUnit.MINUTES);
    }

    public void save(String key, Map<String, Object> data) {
        storage.put(key, new StoredAuthData(data, Instant.now().plusSeconds(TTL_SECONDS)));
    }

    public Map<String, Object> getAndRemove(String key) {
        Map<String, Object> data = get(key);
        if (data != null) {
            storage.remove(key);
        }
        return data;
    }

    public void remove(String key) {
        storage.remove(key); // 👈 New method you asked for
    }

    public Map<String, Object> get(String key) {
        System.err.println("Key:" + key);
        StoredAuthData stored = storage.get(key);
        if (stored == null || Instant.now().isAfter(stored.expiry)) {
            storage.remove(key);
            return null;
        }
        return stored.data;
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        storage.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiry));
    }

    private static class StoredAuthData {
        final Map<String, Object> data;
        final Instant expiry;

        StoredAuthData(Map<String, Object> data, Instant expiry) {
            this.data = data;
            this.expiry = expiry;
        }
    }
}
