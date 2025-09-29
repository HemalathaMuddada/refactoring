package com.xtremand.common.exception;

/**
 * Implement this on any exception class that should suppress `debug.stackTrace` in production-grade API responses.
 * These are usually known input validation or business errors.
 */
public interface SuppressDebugTrace {
    // Marker interface — indicates exceptions that should NOT include debug.stackTrace in response
}

