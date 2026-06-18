package com.goalkeeperdash.leaderboard.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Response payloads for the leaderboard reads (§5.3, §5.4). */
public final class LeaderboardDtos {

    private LeaderboardDtos() {}

    public record SeasonDto(UUID id, String name, Instant endsAt) {}

    public record NationDto(String code, String name, String flagColors) {}

    public record UserNeighbor(long rank, String displayName, long score) {}

    public record NationNeighbor(long rank, String nationCode, long score) {}

    public record PersonalWithinNation(long rank, long total, int bestScore, List<UserNeighbor> neighbors) {}

    public record NationInWorld(long rank, long totalNations, long nationTotalScore, List<NationNeighbor> neighbors) {}

    /** GET /api/v1/leaderboards/me */
    public record DualLeaderboard(
            SeasonDto season,
            NationDto nation,
            PersonalWithinNation personalWithinNation,
            NationInWorld nationInWorld) {}

    /** GET /api/v1/leaderboards/nations */
    public record NationBoard(SeasonDto season, long totalNations, List<NationNeighbor> entries) {}

    /** GET /api/v1/leaderboards/users and /nations/{code}/users */
    public record UserBoard(SeasonDto season, long total, List<UserNeighbor> entries) {}
}
