package com.xtremand.common.util;

public final class AESKeyHolder {
    private static String key;

    private AESKeyHolder() {
    }

    public static void initialize(String rawKey) {
        if (rawKey == null || rawKey.length() != 16) {
            throw new IllegalArgumentException("AES key must be 16 characters.");
        }
        key = rawKey;
    }

    public static String getKey() {
        if (key == null) {
            throw new IllegalStateException("AES key is not initialized.");
        }
        return key;
    }
}
