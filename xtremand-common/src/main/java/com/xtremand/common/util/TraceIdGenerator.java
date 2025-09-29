package com.xtremand.common.util;

import java.util.UUID;

/**
 * Generates unique trace identifiers for logging correlation.
 */
public final class TraceIdGenerator {

    private TraceIdGenerator() {
        // utility class
    }

    /**
     * Returns a new trace identifier. Currently a random UUID string.
     */
    public static String get() {
        return UUID.randomUUID().toString();
    }
}
