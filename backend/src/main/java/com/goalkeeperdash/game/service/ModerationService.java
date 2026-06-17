package com.goalkeeperdash.game.service;

import com.goalkeeperdash.common.error.ApiException;
import com.goalkeeperdash.game.api.ScoreModerationService;
import com.goalkeeperdash.game.api.SubmissionView;
import com.goalkeeperdash.game.domain.ScoreSubmission;
import com.goalkeeperdash.game.repo.ScoreSubmissionRepository;
import com.goalkeeperdash.leaderboard.api.ScoreIngestionService;
import com.goalkeeperdash.user.api.NationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Implements submission moderation, recomputing aggregates after each change (§7.2). */
@Service
public class ModerationService implements ScoreModerationService {

    private final ScoreSubmissionRepository submissions;
    private final ScoreIngestionService ingestion;
    private final NationService nationService;

    public ModerationService(ScoreSubmissionRepository submissions, ScoreIngestionService ingestion,
                             NationService nationService) {
        this.submissions = submissions;
        this.ingestion = ingestion;
        this.nationService = nationService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubmissionView> listFlagged(Pageable pageable) {
        return submissions.findByFlaggedTrueOrderByCreatedAtDesc(pageable).map(ModerationService::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionView> listForUser(UUID userId) {
        return submissions.findByUserIdOrderByCreatedAtDesc(userId).stream().map(ModerationService::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionView get(UUID submissionId) {
        return toView(require(submissionId));
    }

    @Override
    @Transactional
    public void invalidate(UUID submissionId) {
        setInvalidated(submissionId, true);
    }

    @Override
    @Transactional
    public void restore(UUID submissionId) {
        setInvalidated(submissionId, false);
    }

    private void setInvalidated(UUID submissionId, boolean invalidated) {
        ScoreSubmission sub = require(submissionId);
        sub.setManuallyInvalidated(invalidated);
        recomputeAggregate(sub.getUserId(), sub.getNationId(), sub.getSeasonId());
    }

    private void recomputeAggregate(UUID userId, UUID nationId, UUID seasonId) {
        List<Integer> validScores = submissions.findByUserIdAndSeasonIdOrderByCreatedAtDesc(userId, seasonId).stream()
                .filter(ScoreSubmission::countsTowardBoards)
                .map(ScoreSubmission::getScore)
                .toList();
        String nationCode = nationService.findById(nationId)
                .map(n -> n.code())
                .orElseThrow(() -> ApiException.notFound("Nation for submission"));
        ingestion.recomputeUserFromScores(userId, nationId, nationCode, seasonId, validScores);
    }

    private ScoreSubmission require(UUID id) {
        return submissions.findById(id).orElseThrow(() -> ApiException.notFound("Submission not found"));
    }

    static SubmissionView toView(ScoreSubmission s) {
        return new SubmissionView(
                s.getId(), s.getUserId(), s.getNationId(), s.getSeasonId(), s.getSessionId(),
                s.getMode().name(), s.getScore(), s.isValidated(), s.isFlagged(), s.isManuallyInvalidated(),
                s.countsTowardBoards(), s.getClientReportedDurationMs(), s.getEventSummary(), s.getCreatedAt());
    }
}
