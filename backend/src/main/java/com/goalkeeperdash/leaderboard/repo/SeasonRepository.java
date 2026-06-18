package com.goalkeeperdash.leaderboard.repo;

import com.goalkeeperdash.common.domain.SeasonStatus;
import com.goalkeeperdash.leaderboard.domain.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, java.util.UUID> {

    Optional<Season> findFirstByStatus(SeasonStatus status);

    List<Season> findAllByOrderByStartsAtDesc();

    long countByStatus(SeasonStatus status);
}
