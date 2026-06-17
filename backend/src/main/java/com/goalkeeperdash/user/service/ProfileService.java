package com.goalkeeperdash.user.service;

import com.goalkeeperdash.common.domain.SeasonService;
import com.goalkeeperdash.common.domain.SeasonView;
import com.goalkeeperdash.common.error.ApiException;
import com.goalkeeperdash.common.error.ErrorCode;
import com.goalkeeperdash.user.domain.Nation;
import com.goalkeeperdash.user.domain.User;
import com.goalkeeperdash.user.repo.NationRepository;
import com.goalkeeperdash.user.repo.UserRepository;
import com.goalkeeperdash.user.web.dto.MeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Profile reads/writes and the nation set/switch rule (§4.4, §4.5).
 */
@Service
public class ProfileService {

    private final UserRepository users;
    private final NationRepository nations;
    private final SeasonService seasonService;
    private final DisplayNamePolicy displayNamePolicy;

    public ProfileService(UserRepository users, NationRepository nations,
                          SeasonService seasonService, DisplayNamePolicy displayNamePolicy) {
        this.users = users;
        this.nations = nations;
        this.seasonService = seasonService;
        this.displayNamePolicy = displayNamePolicy;
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(UUID userId) {
        User user = require(userId);
        return toMe(user);
    }

    @Transactional
    public MeResponse updateDisplayName(UUID userId, String displayName) {
        displayNamePolicy.validate(displayName);
        User user = require(userId);
        user.setDisplayName(displayName);
        return toMe(user);
    }

    /**
     * Sets the user's nation. Free until the nation is locked for the active season
     * (which happens on the first validated score, §4.4). A locked switch → 409.
     */
    @Transactional
    public MeResponse setNation(UUID userId, String nationCode) {
        User user = require(userId);
        SeasonView active = seasonService.findActiveSeason().orElse(null);

        if (active != null
                && active.id().equals(user.getNationLockedForSeasonId())) {
            throw new ApiException(ErrorCode.NATION_LOCKED,
                    "Your nation is locked for the current season");
        }

        Nation nation = nations.findByCode(nationCode)
                .filter(Nation::isActive)
                .orElseThrow(() -> ApiException.notFound("Unknown or inactive nation: " + nationCode));

        user.setNationId(nation.getId());
        if (user.getNationChosenAt() == null) {
            user.setNationChosenAt(Instant.now());
        }
        return toMe(user);
    }

    private MeResponse toMe(User user) {
        Nation nation = user.getNationId() == null ? null
                : nations.findById(user.getNationId()).orElse(null);
        SeasonView active = seasonService.findActiveSeason().orElse(null);
        boolean locked = active != null && active.id().equals(user.getNationLockedForSeasonId());

        MeResponse.NationDto nationDto = nation == null ? null
                : new MeResponse.NationDto(nation.getCode(), nation.getName(), nation.getFlagColors());
        MeResponse.SeasonDto seasonDto = active == null ? null
                : new MeResponse.SeasonDto(active.id(), active.name(), active.endsAt());

        return new MeResponse(
                user.getId(),
                user.getDisplayName(),
                nationDto,
                user.getStatus().name(),
                locked,
                user.getNationChosenAt(),
                seasonDto);
    }

    private User require(UUID userId) {
        return users.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
