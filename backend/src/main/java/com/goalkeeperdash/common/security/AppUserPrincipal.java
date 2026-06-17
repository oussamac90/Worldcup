package com.goalkeeperdash.common.security;

import java.util.UUID;

/**
 * The authenticated player principal, derived from a verified access JWT and
 * stored as the Spring Security authentication principal for {@code /api/**}.
 */
public record AppUserPrincipal(UUID userId, String nation, UUID seasonId) {

    public static AppUserPrincipal current() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserPrincipal p) {
            return p;
        }
        return null;
    }
}
