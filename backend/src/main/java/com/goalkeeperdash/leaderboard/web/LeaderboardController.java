package com.goalkeeperdash.leaderboard.web;

import com.goalkeeperdash.common.security.AppUserPrincipal;
import com.goalkeeperdash.leaderboard.service.LeaderboardService;
import com.goalkeeperdash.leaderboard.web.dto.LeaderboardDtos.DualLeaderboard;
import com.goalkeeperdash.leaderboard.web.dto.LeaderboardDtos.NationBoard;
import com.goalkeeperdash.leaderboard.web.dto.LeaderboardDtos.UserBoard;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Leaderboard reads (§5.4). Board reads default to the active season and accept a
 * {@code seasonId} to read a closed season's final board. {@code /me} is the
 * headline dual-board read and requires authentication.
 */
@RestController
@RequestMapping("/api/v1/leaderboards")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/me")
    public DualLeaderboard me(@AuthenticationPrincipal AppUserPrincipal principal) {
        return leaderboardService.dualForUser(principal.userId());
    }

    @GetMapping("/nations")
    public NationBoard nations(@RequestParam(required = false) UUID seasonId,
                               @RequestParam(defaultValue = "50") int limit,
                               @RequestParam(defaultValue = "0") int offset) {
        return leaderboardService.nationsBoard(seasonId, clampLimit(limit), Math.max(0, offset));
    }

    @GetMapping("/nations/{code}/users")
    public UserBoard nationUsers(@PathVariable String code,
                                 @RequestParam(required = false) UUID seasonId,
                                 @RequestParam(defaultValue = "50") int limit,
                                 @RequestParam(defaultValue = "0") int offset) {
        return leaderboardService.nationUsersBoard(code.toUpperCase(), seasonId, clampLimit(limit), Math.max(0, offset));
    }

    @GetMapping("/users")
    public UserBoard users(@RequestParam(required = false) UUID seasonId,
                           @RequestParam(defaultValue = "50") int limit,
                           @RequestParam(defaultValue = "0") int offset) {
        return leaderboardService.usersBoard(seasonId, clampLimit(limit), Math.max(0, offset));
    }

    private int clampLimit(int limit) {
        return Math.min(Math.max(limit, 1), 200);
    }
}
