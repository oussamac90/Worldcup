package com.goalkeeperdash.user.service;

import com.goalkeeperdash.common.error.ApiException;
import com.goalkeeperdash.user.api.UserAdminService;
import com.goalkeeperdash.user.api.UserSummary;
import com.goalkeeperdash.user.domain.Nation;
import com.goalkeeperdash.user.domain.User;
import com.goalkeeperdash.user.domain.UserStatus;
import com.goalkeeperdash.user.repo.NationRepository;
import com.goalkeeperdash.user.repo.UserIdentityRepository;
import com.goalkeeperdash.user.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository users;
    private final UserIdentityRepository identities;
    private final NationRepository nations;
    private final DisplayNamePolicy displayNamePolicy;

    public UserAdminServiceImpl(UserRepository users, UserIdentityRepository identities,
                                NationRepository nations, DisplayNamePolicy displayNamePolicy) {
        this.users = users;
        this.identities = identities;
        this.nations = nations;
        this.displayNamePolicy = displayNamePolicy;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummary> search(String query) {
        List<User> found = (query == null || query.isBlank())
                ? users.findAll().stream().limit(100).toList()
                : users.searchByDisplayName(query.trim());
        return found.stream().map(this::toSummary).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetail getDetail(UUID userId) {
        User user = require(userId);
        List<Identity> ids = identities.findByUserId(userId).stream()
                .map(i -> new Identity(i.getProvider().name(), i.getSubject(), i.getEmail(), i.getCreatedAt()))
                .toList();
        boolean locked = user.getNationLockedForSeasonId() != null;
        return new UserDetail(toSummary(user), user.getCreatedAt(), locked, ids);
    }

    @Override
    @Transactional
    public void setStatus(UUID userId, UserStatus status) {
        require(userId).setStatus(status);
    }

    @Override
    @Transactional
    public void resetDisplayName(UUID userId) {
        require(userId).setDisplayName(displayNamePolicy.generateDefault());
    }

    @Override
    @Transactional(readOnly = true)
    public long countRealUsers() {
        return users.countBySyntheticFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsersCreatedSince(Instant since) {
        return users.countByCreatedAtAfter(since);
    }

    private UserSummary toSummary(User u) {
        String code = u.getNationId() == null ? null
                : nations.findById(u.getNationId()).map(Nation::getCode).orElse(null);
        return new UserSummary(u.getId(), u.getDisplayName(), u.getNationId(), code, u.getStatus(), u.isSynthetic());
    }

    private User require(UUID userId) {
        return users.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
