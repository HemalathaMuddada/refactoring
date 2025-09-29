package com.xtremand.common.exception;

/**
 * Thrown when a unique constraint is violated for a given resource. E.g., duplicate email, username, campaign name,
 * etc.
 */
public class DuplicateResourceException extends BusinessException {

    private static final long serialVersionUID = -2615996796315184183L;
    private final String resourceName;
    private final String value;

    public DuplicateResourceException(String resourceName, String value) {
        super(String.format("%s already exists with value: %s", resourceName, value));
        this.resourceName = resourceName;
        this.value = value;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getValue() {
        return value;
    }
}
