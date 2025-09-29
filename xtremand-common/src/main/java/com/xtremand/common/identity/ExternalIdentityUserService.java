package com.xtremand.common.identity;

/**
 * Service for managing users authenticated through an external identity provider.
 */
public interface ExternalIdentityUserService {
    /**
     * Finds an existing user by external login or creates a new one if none exists.
     *
     * @param username the username from the external provider
     * @param email the email from the external provider
     * @return the persisted or newly created user DTO
     */
    ExternalUserDto findOrCreateUserByExternalLogin(String username, String email);
}
