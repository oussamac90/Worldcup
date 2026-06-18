package com.goalkeeperdash.user.api;

import com.goalkeeperdash.user.domain.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** User administration + stats exposed to the back-office (§7.2). */
public interface UserAdminService {

    List<UserSummary> search(String query);

    UserDetail getDetail(UUID userId);

    void setStatus(UUID userId, UserStatus status);

    void resetDisplayName(UUID userId);

    long countRealUsers();

    long countUsersCreatedSince(Instant since);

    /** Full admin view of a user including linked identities. */
    record UserDetail(UserSummary summary, Instant createdAt, boolean nationLocked, List<Identity> identities) {}

    record Identity(String provider, String subject, String email, Instant createdAt) {}
}
