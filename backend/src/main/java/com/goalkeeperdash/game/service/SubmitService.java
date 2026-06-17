package com.goalkeeperdash.game.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.goalkeeperdash.common.domain.GameMode;
import com.goalkeeperdash.common.domain.SeasonService;
import com.goalkeeperdash.common.domain.SeasonView;
import com.goalkeeperdash.common.error.ApiException;
import com.goalkeeperdash.common.error.ErrorCode;
import com.goalkeeperdash.game.domain.GameSession;
import com.goalkeeperdash.game.domain.GameSessionStatus;
import com.goalkeeperdash.game.domain.ScoreSubmission;
import com.goalkeeperdash.game.repo.GameSessionRepository;
import com.goalkeeperdash.game.repo.ScoreSubmissionRepository;
import com.goalkeeperdash.game.validation.ScoreContext;
import com.goalkeeperdash.game.validation.ScoreValidator;
import com.goalkeeperdash.game.validation.ValidationResult;
import com.goalkeeperdash.game.web.dto.GameDtos.SubmitRequest;
import com.goalkeeperdash.game.web.dto.GameDtos.SubmitResponse;
import com.goalkeeperdash.leaderboard.api.ScoreIngestionService;
import com.goalkeeperdash.user.api.UserService;
import com.goalkeeperdash.user.api.UserSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Submit handler (§6.3): one DB transaction that validates the session/nonce,
 * runs sanity validation, persists the immutable {@code ScoreSubmission}, and —
 * only if the score counts — folds it into the season aggregates (which schedule
 * the Redis write for after commit) and locks the user's nation for the season.
 */
@Service
public class SubmitService {

    private static final Logger log = LoggerFactory.getLogger(SubmitService.class);

    private final GameSessionRepository sessions;
    private final ScoreSubmissionRepository submissions;
    private final ScoreValidator validator;
    private final ScoreIngestionService ingestion;
    private final SeasonService seasonService;
    private final UserService userService;

    public SubmitService(GameSessionRepository sessions, ScoreSubmissionRepository submissions,
                         ScoreValidator validator, ScoreIngestionService ingestion,
                         SeasonService seasonService, UserService userService) {
        this.sessions = sessions;
        this.submissions = submissions;
        this.validator = validator;
        this.ingestion = ingestion;
        this.seasonService = seasonService;
        this.userService = userService;
    }

    @Transactional
    public SubmitResponse submit(UUID userId, UUID sessionId, SubmitRequest req) {
        GameSession session = sessions.findById(sessionId)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_SESSION, "Unknown session"));

        // Hard checks (§6.4): bad nonce / wrong owner / consumed / expired.
        if (!session.getUserId().equals(userId) || !session.getNonce().equals(req.nonce())) {
            throw new ApiException(ErrorCode.INVALID_SESSION, "Session/nonce mismatch");
        }
        if (session.getStatus() == GameSessionStatus.CONSUMED) {
            throw new ApiException(ErrorCode.SESSION_CONSUMED, "Session already submitted");
        }
        if (session.getStatus() == GameSessionStatus.EXPIRED || session.getExpiresAt().isBefore(Instant.now())) {
            session.setStatus(GameSessionStatus.EXPIRED);
            throw new ApiException(ErrorCode.SESSION_EXPIRED, "Session expired");
        }

        SeasonView season = seasonService.findActiveSeason()
                .orElseThrow(() -> new ApiException(ErrorCode.NO_ACTIVE_SEASON, "No active season"));

        UserSummary user = userService.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        if (user.nationId() == null || user.nationCode() == null) {
            throw new ApiException(ErrorCode.NATION_NOT_CHOSEN, "Pick a nation before submitting scores");
        }

        GameMode mode = session.getMode();
        ValidationResult result = validator.validate(new ScoreContext(
                mode, req.score(), req.durationMs(), parseSummary(req.eventSummary())));

        // Persist the immutable audit record regardless of outcome.
        ScoreSubmission submission = new ScoreSubmission();
        submission.setUserId(userId);
        submission.setNationId(user.nationId());
        submission.setSeasonId(season.id());
        submission.setSessionId(sessionId);
        submission.setMode(mode);
        submission.setScore(req.score());
        submission.setValidated(result.validated());
        submission.setFlagged(result.flagged());
        submission.setClientReportedDurationMs(req.durationMs());
        submission.setEventSummary(req.eventSummary());
        submissions.save(submission);

        // Consume the session (single-use).
        session.setStatus(GameSessionStatus.CONSUMED);
        session.setConsumedAt(Instant.now());

        if (submission.countsTowardBoards()) {
            ingestion.applyValidatedScore(userId, user.nationId(), user.nationCode(), season.id(), req.score());
            // First validated score locks the nation for the season (§4.4).
            userService.lockNationForSeasonIfUnlocked(userId, season.id());
        } else {
            log.info("Flagged submission user={} score={} reasons={}", userId, req.score(), result.reasons());
        }

        return new SubmitResponse(submission.getId(), result.validated(), result.flagged(),
                req.score(), result.reasons());
    }

    private ScoreContext.EventSummary parseSummary(JsonNode node) {
        if (node == null || node.isNull()) {
            return new ScoreContext.EventSummary(0, 0, 0, 0, 0, 1);
        }
        return new ScoreContext.EventSummary(
                node.path("shotsFaced").asInt(0),
                node.path("saves").asInt(0),
                node.path("goalsConceded").asInt(0),
                node.path("maxCombo").asInt(0),
                node.path("perfectSaves").asInt(0),
                node.path("schemaVersion").asInt(1));
    }
}
