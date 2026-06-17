package com.goalkeeperdash.game.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Submission audit + moderation, used by the back-office. Invalidating/restoring a
 * submission recomputes the affected user's season aggregate and Redis index.
 */
public interface ScoreModerationService {

    Page<SubmissionView> listFlagged(Pageable pageable);

    List<SubmissionView> listForUser(UUID userId);

    SubmissionView get(UUID submissionId);

    /** Marks a submission invalid (excluded from boards) and recomputes aggregates. */
    void invalidate(UUID submissionId);

    /** Restores a previously invalidated submission and recomputes aggregates. */
    void restore(UUID submissionId);
}
