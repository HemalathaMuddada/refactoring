package com.xtremand.shared.services.audit;

import com.xtremand.shared.services.security.SecurityAuthenticatedUserContext;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return SecurityAuthenticatedUserContext.getCurrentUsername();
    }
}
