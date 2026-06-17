package com.goalkeeperdash.leaderboard.repo;

import com.goalkeeperdash.leaderboard.domain.NationSeasonStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NationSeasonStatRepository extends JpaRepository<NationSeasonStat, UUID> {

    Optional<NationSeasonStat> findByNationIdAndSeasonId(UUID nationId, UUID seasonId);

    List<NationSeasonStat> findBySeasonId(UUID seasonId);

    List<NationSeasonStat> findBySeasonIdOrderByTotalScoreDesc(UUID seasonId);
}
