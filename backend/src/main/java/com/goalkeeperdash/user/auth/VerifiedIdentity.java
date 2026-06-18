package com.goalkeeperdash.user.auth;

import com.goalkeeperdash.user.domain.AuthProvider;

/** Result of verifying an IdP ID token: the provider, its subject, and email (if present). */
public record VerifiedIdentity(AuthProvider provider, String subject, String email) {}
