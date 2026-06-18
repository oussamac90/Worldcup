package com.goalkeeperdash.leaderboard.repo;

import com.goalkeeperdash.leaderboard.domain.UserSeasonStat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSeasonStatRepository extends JpaRepository<UserSeasonStat, UUID> {

    Optional<UserSeasonStat> findByUserIdAndSeasonId(UUID userId, UUID seasonId);

    List<UserSeasonStat> findBySeasonId(UUID seasonId);

    List<UserSeasonStat> findBySeasonIdAndNationId(UUID seasonId, UUID nationId);

    List<UserSeasonStat> findBySeasonIdOrderByBestScoreDesc(UUID seasonId, Pageable pageable);

    List<UserSeasonStat> findBySeasonIdAndNationIdOrderByBestScoreDesc(UUID seasonId, UUID nationId, Pageable pageable);
}
