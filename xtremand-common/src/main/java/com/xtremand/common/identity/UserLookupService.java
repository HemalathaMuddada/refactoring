package com.xtremand.common.identity;

import java.util.Optional;

/**
 * Service interface for looking up users by email.
 */
public interface UserLookupService {
    /**
     * Find an authentication user by email.
     *
     * @param email user email
     * @return optional user DTO if found
     */
    Optional<AuthUserDto> findByEmail(String email);
    
    boolean existsByEmail(String email);
}
