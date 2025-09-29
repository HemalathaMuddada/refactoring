package com.xtremand.user.role.service;

/**
 * Exception thrown when attempting to create a role that already exists.
 */
public class RoleAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RoleAlreadyExistsException(String message) {
        super(message);
    }
}