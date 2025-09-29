package com.xtremand.common.error;

/**
 * Definition of an error code loaded from YAML.
 */
public record ErrorDefinition(
        String code,
        int codeId,
        String title,
        String defaultMessage,
        String category,
        String severity,
        boolean recoverable) {
}
