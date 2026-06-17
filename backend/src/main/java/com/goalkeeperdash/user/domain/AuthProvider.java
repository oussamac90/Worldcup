package com.goalkeeperdash.user.domain;

public enum AuthProvider {
    GOOGLE,
    FACEBOOK,
    APPLE,
    /** Reserved marker provider for synthetic seed users so they can be excluded/purged (§10). */
    SYNTHETIC,
    /** Local dev-login path used only when app.oidc.dev-login-enabled=true. */
    DEV
}
