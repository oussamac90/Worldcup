package com.goalkeeperdash.game.repo;

import com.goalkeeperdash.game.domain.ScoreSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ScoreSubmissionRepository extends JpaRepository<ScoreSubmission, UUID> {

    Page<ScoreSubmission> findByFlaggedTrueOrderByCreatedAtDesc(Pageable pageable);

    List<ScoreSubmission> findByUserIdAndSeasonIdOrderByCreatedAtDesc(UUID userId, UUID seasonId);

    List<ScoreSubmission> findByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByCreatedAtAfter(Instant after);

    long countByFlaggedTrue();

    @Query("select count(distinct s.userId) from ScoreSubmission s where s.createdAt > :after")
    long countDistinctUsersByCreatedAtAfter(Instant after);
}
