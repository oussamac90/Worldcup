package com.goalkeeperdash.leaderboard.season;

import com.goalkeeperdash.common.domain.SeasonStatus;
import com.goalkeeperdash.common.error.ApiException;
import com.goalkeeperdash.common.error.ErrorCode;
import com.goalkeeperdash.leaderboard.domain.NationSeasonStat;
import com.goalkeeperdash.leaderboard.domain.Season;
import com.goalkeeperdash.leaderboard.repo.NationSeasonStatRepository;
import com.goalkeeperdash.leaderboard.repo.SeasonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Season lifecycle operations (create/activate/close) used by the back-office and
 * the seeder. Enforces the single-ACTIVE-season invariant (§3.1).
 */
@Service
public class SeasonAdminService {

    private static final Logger log = LoggerFactory.getLogger(SeasonAdminService.class);

    private final SeasonRepository seasons;
    private final NationSeasonStatRepository nationStats;

    public SeasonAdminService(SeasonRepository seasons, NationSeasonStatRepository nationStats) {
        this.seasons = seasons;
        this.nationStats = nationStats;
    }

    @Transactional
    public Season create(String name, Instant startsAt, Instant endsAt) {
        Season season = new Season();
        season.setName(name);
        season.setStartsAt(startsAt);
        season.setEndsAt(endsAt);
        season.setStatus(SeasonStatus.SCHEDULED);
        return seasons.save(season);
    }

    /** Activates a SCHEDULED season; fails if another season is already ACTIVE. */
    @Transactional
    public Season activate(UUID seasonId) {
        if (seasons.countByStatus(SeasonStatus.ACTIVE) > 0) {
            throw new ApiException(ErrorCode.CONFLICT, "Another season is already active");
        }
        Season season = require(seasonId);
        if (season.getStatus() == SeasonStatus.CLOSED) {
            throw new ApiException(ErrorCode.CONFLICT, "Cannot activate a closed season");
        }
        season.setStatus(SeasonStatus.ACTIVE);
        return season;
    }

    /**
     * Closes the active season: computes the final national board, sets
     * {@code winningNationId} and flips status to CLOSED. The aggregate rows are
     * the final snapshot (immutable once closed).
     */
    @Transactional
    public Season close(UUID seasonId) {
        Season season = require(seasonId);
        if (season.getStatus() != SeasonStatus.ACTIVE) {
            throw new ApiException(ErrorCode.CONFLICT, "Only an active season can be closed");
        }
        List<NationSeasonStat> board = nationStats.findBySeasonIdOrderByTotalScoreDesc(seasonId);
        board.stream()
                .max(Comparator.comparingLong(NationSeasonStat::getTotalScore))
                .ifPresent(top -> season.setWinningNationId(top.getNationId()));
        season.setStatus(SeasonStatus.CLOSED);
        log.info("Closed season {} winningNation={}", seasonId, season.getWinningNationId());
        return season;
    }

    @Transactional(readOnly = true)
    public List<Season> listAll() {
        return seasons.findAllByOrderByStartsAtDesc();
    }

    private Season require(UUID seasonId) {
        return seasons.findById(seasonId).orElseThrow(() -> ApiException.notFound("Season not found"));
    }
}
