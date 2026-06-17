package com.goalkeeperdash.user.service;

import com.goalkeeperdash.common.error.ApiException;
import com.goalkeeperdash.common.error.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * Handle rules (§4.5): 3–20 chars, alphanumeric + spaces + a small punctuation
 * set, with a basic profanity deny-list. Not strictly unique; a discriminator can
 * be appended for display when needed.
 */
@Component
public class DisplayNamePolicy {

    private static final Pattern ALLOWED = Pattern.compile("^[A-Za-z0-9 ._-]{3,20}$");

    // Basic deny-list for v1; replace with a fuller filter later.
    private static final Set<String> DENY = Set.of("admin", "fuck", "shit", "bitch", "nazi", "slur");

    public void validate(String displayName) {
        if (displayName == null || !ALLOWED.matcher(displayName).matches()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
                    "Display name must be 3-20 chars: letters, numbers, spaces, . _ -");
        }
        String lower = displayName.toLowerCase();
        for (String bad : DENY) {
            if (lower.contains(bad)) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "Display name not allowed");
            }
        }
    }

    /** Generates a default handle for a newly created account. */
    public String generateDefault() {
        return "Keeper" + (1000 + ThreadLocalRandom.current().nextInt(9000));
    }
}
