package com.goalkeeperdash.game.service;

import com.goalkeeperdash.game.api.GameStatsService;
import com.goalkeeperdash.game.repo.ScoreSubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class GameStatsServiceImpl implements GameStatsService {

    private final ScoreSubmissionRepository submissions;

    public GameStatsServiceImpl(ScoreSubmissionRepository submissions) {
        this.submissions = submissions;
    }

    @Override
    public long totalSubmissions() {
        return submissions.count();
    }

    @Override
    public long submissionsSince(Instant since) {
        return submissions.countByCreatedAtAfter(since);
    }

    @Override
    public long flaggedCount() {
        return submissions.countByFlaggedTrue();
    }

    @Override
    public long activeUsersSince(Instant since) {
        return submissions.countDistinctUsersByCreatedAtAfter(since);
    }
}
