package com.goalkeeperdash.user.service;

import com.goalkeeperdash.user.api.UserService;
import com.goalkeeperdash.user.api.UserSummary;
import com.goalkeeperdash.user.domain.Nation;
import com.goalkeeperdash.user.domain.User;
import com.goalkeeperdash.user.repo.NationRepository;
import com.goalkeeperdash.user.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository users;
    private final NationRepository nations;

    public UserServiceImpl(UserRepository users, NationRepository nations) {
        this.users = users;
        this.nations = nations;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSummary> findById(UUID userId) {
        return users.findById(userId).map(this::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, String> displayNames(Collection<UUID> userIds) {
        Map<UUID, String> result = new HashMap<>();
        for (User u : users.findAllById(userIds)) {
            result.put(u.getId(), u.getDisplayName());
        }
        return result;
    }

    @Override
    @Transactional
    public void lockNationForSeasonIfUnlocked(UUID userId, UUID seasonId) {
        users.findById(userId).ifPresent(u -> {
            if (u.getNationLockedForSeasonId() == null) {
                u.setNationLockedForSeasonId(seasonId);
            }
        });
    }

    private UserSummary toSummary(User u) {
        String code = null;
        if (u.getNationId() != null) {
            code = nations.findById(u.getNationId()).map(Nation::getCode).orElse(null);
        }
        return new UserSummary(u.getId(), u.getDisplayName(), u.getNationId(), code, u.getStatus(), u.isSynthetic());
    }
}
