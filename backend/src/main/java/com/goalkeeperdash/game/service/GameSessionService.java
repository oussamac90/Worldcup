package com.goalkeeperdash.game.service;

import com.goalkeeperdash.common.config.AppProperties;
import com.goalkeeperdash.common.domain.GameMode;
import com.goalkeeperdash.common.util.HashUtil;
import com.goalkeeperdash.game.domain.GameSession;
import com.goalkeeperdash.game.domain.GameSessionStatus;
import com.goalkeeperdash.game.repo.GameSessionRepository;
import com.goalkeeperdash.game.web.dto.GameDtos.OpenSessionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Issues server-side, single-use game sessions with a nonce and a deterministic seed (§6.1). */
@Service
public class GameSessionService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final GameSessionRepository sessions;
    private final int ttlMin;

    public GameSessionService(GameSessionRepository sessions, AppProperties props) {
        this.sessions = sessions;
        this.ttlMin = props.game().sessionTtlMin();
    }

    @Transactional
    public OpenSessionResponse open(UUID userId, GameMode mode) {
        Instant now = Instant.now();
        GameSession session = new GameSession();
        session.setUserId(userId);
        session.setMode(mode);
        session.setNonce(HashUtil.randomToken());
        session.setSeed(RANDOM.nextLong());
        session.setOpenedAt(now);
        session.setExpiresAt(now.plus(ttlMin, ChronoUnit.MINUTES));
        session.setStatus(GameSessionStatus.OPEN);
        sessions.save(session);
        return new OpenSessionResponse(session.getId(), session.getNonce(), now, session.getSeed());
    }
}
