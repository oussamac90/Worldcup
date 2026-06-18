package com.goalkeeperdash.game.web;

import com.goalkeeperdash.common.error.ApiException;
import com.goalkeeperdash.common.error.ErrorCode;
import com.goalkeeperdash.common.ratelimit.RateLimiterService;
import com.goalkeeperdash.common.security.AppUserPrincipal;
import com.goalkeeperdash.game.service.GameSessionService;
import com.goalkeeperdash.game.service.SubmitService;
import com.goalkeeperdash.game.web.dto.GameDtos.OpenSessionRequest;
import com.goalkeeperdash.game.web.dto.GameDtos.OpenSessionResponse;
import com.goalkeeperdash.game.web.dto.GameDtos.SubmitRequest;
import com.goalkeeperdash.game.web.dto.GameDtos.SubmitResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Game session lifecycle + score submission (§6.1). All require a valid access JWT. */
@RestController
@RequestMapping("/api/v1/sessions")
public class GameSessionController {

    private final GameSessionService sessionService;
    private final SubmitService submitService;
    private final RateLimiterService rateLimiter;

    public GameSessionController(GameSessionService sessionService, SubmitService submitService,
                                RateLimiterService rateLimiter) {
        this.sessionService = sessionService;
        this.submitService = submitService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping
    public OpenSessionResponse open(@AuthenticationPrincipal AppUserPrincipal principal,
                                    @Valid @RequestBody OpenSessionRequest body) {
        return sessionService.open(principal.userId(), body.mode());
    }

    @PostMapping("/{sessionId}/submit")
    public SubmitResponse submit(@AuthenticationPrincipal AppUserPrincipal principal,
                                 @PathVariable UUID sessionId,
                                 @Valid @RequestBody SubmitRequest body) {
        if (!rateLimiter.tryConsumeSubmit(principal.userId().toString())) {
            throw new ApiException(ErrorCode.RATE_LIMITED, "Too many submissions, slow down");
        }
        return submitService.submit(principal.userId(), sessionId, body);
    }
}
