package com.goalkeeperdash.leaderboard.service;

import com.goalkeeperdash.leaderboard.api.SeasonStatsService;
import com.goalkeeperdash.leaderboard.domain.NationSeasonStat;
import com.goalkeeperdash.leaderboard.repo.NationSeasonStatRepository;
import com.goalkeeperdash.user.api.NationService;
import com.goalkeeperdash.user.api.NationView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SeasonStatsServiceImpl implements SeasonStatsService {

    private final NationSeasonStatRepository nationStats;
    private final NationService nationService;

    public SeasonStatsServiceImpl(NationSeasonStatRepository nationStats, NationService nationService) {
        this.nationStats = nationStats;
        this.nationService = nationService;
    }

    @Override
    public List<NationContribution> contributorCounts(UUID seasonId) {
        return nationStats.findBySeasonIdOrderByTotalScoreDesc(seasonId).stream()
                .map(this::toContribution)
                .toList();
    }

    private NationContribution toContribution(NationSeasonStat n) {
        NationView nation = nationService.findById(n.getNationId()).orElse(null);
        String code = nation == null ? "?" : nation.code();
        String name = nation == null ? "Unknown" : nation.name();
        return new NationContribution(code, name, n.getContributorCount(), n.getTotalScore());
    }
}
